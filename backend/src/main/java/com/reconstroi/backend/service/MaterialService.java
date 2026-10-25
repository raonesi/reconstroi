package com.reconstroi.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.reconstroi.backend.dto.ApiDtos.*;
import com.reconstroi.backend.exception.*;
import com.reconstroi.backend.model.*;
import com.reconstroi.backend.repository.*;

@Service
public class MaterialService {
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private final MaterialRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final FotoMaterialRepository fotoRepository;
    private final ReservaRepository reservaRepository;
    private final HistoricoService historicoService;
    private final AutorizacaoService autorizacaoService;
    private final Path uploadPath;
    private final long maxBytes;

    public MaterialService(MaterialRepository repository, CategoriaRepository categoriaRepository,
            FotoMaterialRepository fotoRepository, ReservaRepository reservaRepository, HistoricoService historicoService,
            AutorizacaoService autorizacaoService, @Value("${app.upload.directory:uploads}") String directory,
            @Value("${app.upload.max-bytes:5242880}") long maxBytes) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.fotoRepository = fotoRepository;
        this.reservaRepository = reservaRepository;
        this.historicoService = historicoService;
        this.autorizacaoService = autorizacaoService;
        this.uploadPath = Path.of(directory).toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
    }

    @Transactional(readOnly = true)
    public Page<MaterialResponse> pesquisar(String busca, Long categoriaId, String cidade, String estado,
            Integer quantidadeMinima, EstadoConservacao conservacao, StatusMaterial status, Pageable pageable, Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        Specification<Material> spec = (root, query, cb) -> cb.isFalse(root.get("excluido"));
        if (busca != null && !busca.isBlank()) {
            String termo = "%" + busca.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(cb.like(cb.lower(root.get("nome")), termo),
                cb.like(cb.lower(root.get("descricao")), termo)));
        }
        if (categoriaId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId));
        if (cidade != null && !cidade.isBlank()) spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("cidade")), cidade.trim().toLowerCase(Locale.ROOT)));
        if (estado != null && !estado.isBlank()) spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), estado.trim().toUpperCase(Locale.ROOT)));
        if (quantidadeMinima != null) {
            if (quantidadeMinima <= 0) throw new QuantidadeInvalidaException("A quantidade mínima deve ser maior que zero.");
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("quantidade"), quantidadeMinima));
        }
        if (conservacao != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("estadoConservacao"), conservacao));
        StatusMaterial statusPermitido = usuario.getTipo() == TipoUsuario.ADMIN && status != null ? status : StatusMaterial.DISPONIVEL;
        spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), statusPermitido));
        return repository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MaterialResponse buscar(Long id, Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        return toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> meus(Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        List<Material> materiais = repository.findByProprietarioIdOrderByDataCadastroDesc(usuario.getId());
        List<MaterialResponse> resultado = new ArrayList<>();
        for (Material material : materiais) {
            if (!material.isExcluido()) {
                resultado.add(toResponse(material));
            }
        }
        return resultado;
    }

    @Transactional
    public MaterialResponse criar(MaterialRequest request, List<MultipartFile> fotos, Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        if (!usuario.getTipo().isDoador() || usuario.getTipo() == TipoUsuario.ADMIN)
            throw new UsuarioSemPermissaoException("Apenas doadores podem cadastrar materiais.");
        validarDados(request);
        validarFotosObrigatorias(fotos);
        validarDuplicidade(request, usuario);
        Material material = new Material();
        preencherMaterial(material, request);
        material.setProprietario(usuario);
        repository.save(material);
        for (MultipartFile foto : fotos) {
            salvarFoto(material, foto);
        }
        historicoService.registrar(usuario, TipoAcaoHistorico.MATERIAL_CRIADO,
            "Material “" + material.getNome() + "” publicado.", "MATERIAL", material.getId());
        return toResponse(material);
    }

    @Transactional
    public MaterialResponse atualizar(Long id, MaterialRequest request, Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        Material material = find(id);
        validarResponsavel(material, usuario);
        if (material.getStatus() == StatusMaterial.FINALIZADO) throw new MaterialFinalizadoException();
        if (material.getStatus() != StatusMaterial.DISPONIVEL)
            throw new FluxoInvalidoException("Somente materiais disponíveis podem ser editados.");
        validarDados(request);
        preencherMaterial(material, request);
        historicoService.registrar(usuario, TipoAcaoHistorico.MATERIAL_EDITADO,
            "Material “" + material.getNome() + "” editado.", "MATERIAL", material.getId());
        return toResponse(material);
    }

    @Transactional
    public void excluir(Long id, Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        Material material = find(id);
        validarResponsavel(material, usuario);
        if (usuario.getTipo() != TipoUsuario.ADMIN) {
            if (material.getStatus() != StatusMaterial.DISPONIVEL)
                throw new FluxoInvalidoException("Somente materiais disponíveis podem ser removidos.");
            if (reservaRepository.existsByMaterialRelacionadoIdAndStatusReservaIn(id, StatusReserva.ativos()))
                throw new ReservaJaExistenteException();
        }
        remover(material, usuario, usuario.getTipo() == TipoUsuario.ADMIN
            ? "Anúncio removido pela administração." : "Anúncio removido pelo doador.");
    }

    @Transactional
    public void removerComoAdmin(Long id, Usuario admin, String motivo) {
        autorizacaoService.exigirAtivo(admin);
        if (admin.getTipo() != TipoUsuario.ADMIN) throw new UsuarioSemPermissaoException("Ação exclusiva de administradores.");
        Material material = repository.findById(id).orElseThrow(() -> BusinessException.notFound("Material não encontrado."));
        if (!material.isExcluido()) remover(material, admin,
            motivo == null || motivo.isBlank() ? "Anúncio irregular removido." : motivo.trim());
    }

    @Transactional
    public FotoResponse upload(Long id, MultipartFile file, Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        Material material = find(id);
        validarResponsavel(material, usuario);
        if (material.getStatus() == StatusMaterial.FINALIZADO) throw new MaterialFinalizadoException();
        if (material.getFotos().size() >= 5) throw BusinessException.conflict("Cada material pode possuir até 5 fotos.");
        FotoResponse response = FotoResponse.from(salvarFoto(material, file));
        historicoService.registrar(usuario, TipoAcaoHistorico.MATERIAL_EDITADO,
            "Foto adicionada ao material “" + material.getNome() + "”.", "MATERIAL", material.getId());
        return response;
    }

    private FotoMaterial salvarFoto(Material material, MultipartFile file) {
        validarFoto(file);
        try {
            Files.createDirectories(uploadPath);
            String extension = switch (file.getContentType()) {
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                default -> ".jpg";
            };
            String name = UUID.randomUUID() + extension;
            Path target = uploadPath.resolve(name).normalize();
            if (!target.startsWith(uploadPath)) throw BusinessException.badRequest("Nome de arquivo inválido.");
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            FotoMaterial foto = new FotoMaterial();
            foto.setMaterial(material);
            foto.setNomeArquivo(name);
            foto.setNomeOriginal(file.getOriginalFilename() == null ? name : file.getOriginalFilename());
            foto.setContentType(file.getContentType());
            foto.setUrl("/uploads/" + name);
            material.getFotos().add(foto);
            return fotoRepository.save(foto);
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível salvar a imagem.");
        }
    }

    Material find(Long id) {
        Material material = repository.findById(id).orElseThrow(() -> BusinessException.notFound("Material não encontrado."));
        if (material.isExcluido()) throw BusinessException.notFound("Material não encontrado.");
        return material;
    }

    Material findForUpdate(Long id) {
        Material material = repository.findByIdForUpdate(id).orElseThrow(() -> BusinessException.notFound("Material não encontrado."));
        if (material.isExcluido()) throw BusinessException.notFound("Material não encontrado.");
        return material;
    }

    void validarResponsavel(Material material, Usuario usuario) {
        if (usuario.getTipo() != TipoUsuario.ADMIN && !material.getProprietario().getId().equals(usuario.getId()))
            throw new UsuarioSemPermissaoException("Você não pode alterar este material.");
    }

    private void preencherMaterial(Material material, MaterialRequest request) {
        Categoria categoria = categoriaRepository.findById(request.categoriaId()).filter(Categoria::isAtivo)
            .orElseThrow(() -> BusinessException.badRequest("Categoria inválida."));
        material.setNome(request.nome().trim());
        material.setDescricao(request.descricao().trim());
        material.setQuantidade(request.quantidade());
        material.setUnidade(request.unidade().trim());
        material.setEstadoConservacao(request.estadoConservacao());
        material.setCategoria(categoria);
        material.setEnderecoRetirada(request.enderecoRetirada().trim());
        material.setCidade(request.cidade().trim());
        material.setEstado(request.estado().trim().toUpperCase(Locale.ROOT));
    }

    private void validarDados(MaterialRequest request) {
        if (request == null || blank(request.nome()) || blank(request.descricao()) || blank(request.unidade())
                || blank(request.enderecoRetirada()) || blank(request.cidade()) || blank(request.estado())
                || request.categoriaId() == null || request.estadoConservacao() == null) {
            throw BusinessException.badRequest("Preencha todos os campos obrigatórios do material.");
        }
        if (request.quantidade() == null || request.quantidade() <= 0)
            throw new QuantidadeInvalidaException("A quantidade deve ser maior que zero.");
        if (!request.estado().trim().matches("(?i)^[a-z]{2}$"))
            throw BusinessException.badRequest("Informe a UF com duas letras.");
    }

    private void validarFotosObrigatorias(List<MultipartFile> fotos) {
        if (fotos == null || fotos.isEmpty()) throw new FotoObrigatoriaException();
        if (fotos.size() > 5) throw BusinessException.badRequest("Cada material pode possuir até 5 fotos.");
        fotos.forEach(this::validarFoto);
    }

    private void validarFoto(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > maxBytes || !IMAGE_TYPES.contains(file.getContentType()))
            throw BusinessException.badRequest("Envie uma imagem JPG, PNG ou WebP de até 5 MB.");
    }

    private void validarDuplicidade(MaterialRequest request, Usuario usuario) {
        if (request.confirmarDuplicidade()) return;
        ZoneId zone = ZoneId.systemDefault();
        Instant inicio = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        Instant fim = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant();
        if (repository.existsDuplicadoNoPeriodo(usuario.getId(), request.nome().trim(), request.enderecoRetirada().trim(),
                request.quantidade(), inicio, fim)) throw new MaterialDuplicadoException();
    }

    private void remover(Material material, Usuario usuario, String motivo) {
        material.setExcluido(true);
        material.setExcluidoEm(Instant.now());
        material.setMotivoRemocao(motivo);
        historicoService.registrar(usuario, TipoAcaoHistorico.MATERIAL_REMOVIDO,
            motivo + " Material: “" + material.getNome() + "”.", "MATERIAL", material.getId());
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }

    public MaterialResponse toResponse(Material m) {
        CategoriaResponse categoria = m.getCategoria() == null
            ? new CategoriaResponse(0L, "Outros", "outros") : CategoriaResponse.from(m.getCategoria());
        return new MaterialResponse(m.getId(), m.getNome(), m.getDescricao(), m.getQuantidade(), m.getUnidade(),
            m.getEstadoConservacao(), m.getStatus(), categoria, m.getEnderecoRetirada(), m.getCidade(), m.getEstado(),
            m.getFotos().stream().map(FotoResponse::from).toList(), UsuarioResumo.from(m.getProprietario()),
            m.getDataCadastro(), m.getAtualizadoEm());
    }

    public MaterialResumo toResumo(Material m) {
        return new MaterialResumo(m.getId(), m.getNome(), m.getUnidade(), m.getCidade(), m.getEstado(),
            m.getStatus(), UsuarioResumo.from(m.getProprietario()));
    }
}
