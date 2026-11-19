package com.reconstroi.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reconstroi.backend.dto.ApiDtos.*;
import com.reconstroi.backend.exception.BusinessException;
import com.reconstroi.backend.model.*;
import com.reconstroi.backend.repository.TokenRecuperacaoSenhaRepository;
import com.reconstroi.backend.repository.UsuarioRepository;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    private final TokenRecuperacaoSenhaRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final boolean exposeResetToken;
    private final LimiteTentativas limite;
    private final SecureRandom random = new SecureRandom();
    /** Hash fictício: o login com e-mail inexistente gasta o mesmo tempo que um com e-mail real. */
    private final String hashFicticio;

    public UsuarioService(UsuarioRepository repository, TokenRecuperacaoSenhaRepository tokenRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService, EmailService emailService, LimiteTentativas limite,
            @Value("${app.auth.expose-reset-token:false}") boolean exposeResetToken,
            @Value("${server.address:}") String enderecoServidor) {
        this.repository = repository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.limite = limite;
        // Devolver o token na resposta permite trocar a senha de qualquer conta só com o e-mail.
        // Por isso só vale com a API presa a este computador (app desktop local, sem servidor de e-mail).
        this.exposeResetToken = exposeResetToken && somenteLocal(enderecoServidor);
        this.hashFicticio = passwordEncoder.encode("senha-ficticia-para-igualar-o-tempo");
    }

    private static boolean somenteLocal(String endereco) {
        if (endereco == null || endereco.isBlank()) return false;
        try { return java.net.InetAddress.getByName(endereco.trim()).isLoopbackAddress(); }
        catch (java.net.UnknownHostException e) { return false; }
    }

    @Transactional
    public AuthResponse cadastrar(CadastroUsuarioRequest request) {
        String email = request.email().trim().toLowerCase();
        if (repository.findByEmailIgnoreCase(email).isPresent()) throw BusinessException.conflict("Este e-mail já está cadastrado.");
        if (request.tipo() == TipoUsuario.ADMIN) throw BusinessException.forbidden("Administradores não podem ser criados pelo cadastro público.");
        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setTelefone(request.telefone().trim());
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setTipo(request.tipo());
        usuario.setCidade(normalize(request.cidade()));
        usuario.setEstado(uf(request.estado()));
        usuario.setDocumento(normalize(request.documento()));
        repository.save(usuario);
        return autenticar(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        String chave = "login:" + email;
        limite.verificar(chave);
        Usuario usuario = repository.findByEmailIgnoreCase(email).orElse(null);
        // Sempre compara uma senha (real ou fictícia) para não revelar pelo tempo se o e-mail existe.
        boolean senhaConfere = passwordEncoder.matches(request.senha(), usuario == null ? hashFicticio : usuario.getSenha());
        if (usuario == null || !usuario.isAtivo() || !senhaConfere) {
            limite.registrarFalha(chave);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        }
        limite.limpar(chave);
        return autenticar(usuario);
    }

    @Transactional
    public UsuarioResponse atualizar(Usuario usuario, AtualizarPerfilRequest request) {
        usuario.setNome(request.nome().trim());
        usuario.setTelefone(request.telefone().trim());
        usuario.setCidade(normalize(request.cidade()));
        usuario.setEstado(uf(request.estado()));
        usuario.setDocumento(normalize(request.documento()));
        return UsuarioResponse.from(repository.save(usuario));
    }

    @Transactional
    public RecuperarSenhaResponse solicitarRecuperacao(RecuperarSenhaRequest request) {
        String chave = "recuperar:" + request.email().trim().toLowerCase();
        limite.verificar(chave);
        limite.registrarFalha(chave); // cada pedido conta: evita encher a caixa de e-mail de alguém
        String rawToken = repository.findByEmailIgnoreCase(request.email().trim()).map(usuario -> {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            TokenRecuperacaoSenha entity = new TokenRecuperacaoSenha();
            entity.setUsuario(usuario);
            entity.setTokenHash(hash(token));
            entity.setExpiraEm(Instant.now().plus(Duration.ofMinutes(30)));
            tokenRepository.save(entity);
            emailService.enviarRecuperacao(usuario, token);
            return token;
        }).orElse(null);
        return new RecuperarSenhaResponse("Se o e-mail existir, as instruções de recuperação foram geradas.", exposeResetToken ? rawToken : null);
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaRequest request) {
        TokenRecuperacaoSenha token = tokenRepository
            .findByTokenHashAndUsadoFalseAndExpiraEmAfter(hash(request.token()), Instant.now())
            .orElseThrow(() -> BusinessException.badRequest("Token inválido ou expirado."));
        // Trocar a senha muda a "versão" gravada nos tokens JWT: sessões abertas antes deixam de valer.
        token.getUsuario().setSenha(passwordEncoder.encode(request.novaSenha()));
        token.setUsado(true);
        limite.limpar("login:" + token.getUsuario().getEmail().toLowerCase());
    }

    private AuthResponse autenticar(Usuario usuario) {
        var jwt = jwtService.gerar(usuario);
        return new AuthResponse(UsuarioResponse.from(usuario), jwt.token(), jwt.expiraEm());
    }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String uf(String value) { return value == null || value.isBlank() ? null : value.trim().toUpperCase(); }
    private String hash(String token) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
