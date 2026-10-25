package com.reconstroi.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.reconstroi.backend.model.*;

import jakarta.validation.constraints.*;

public final class ApiDtos {
    private ApiDtos() {}

    public record CadastroUsuarioRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(max = 30) String telefone,
        @NotBlank @Size(min = 8, max = 72) String senha,
        @NotNull TipoUsuario tipo,
        @Size(max = 120) String cidade,
        @Pattern(regexp = "^$|^[A-Za-z]{2}$", message = "Informe a UF com duas letras") String estado,
        @Size(max = 30) String documento) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String senha) {}
    public record AtualizarPerfilRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 30) String telefone,
        @Size(max = 120) String cidade,
        @Pattern(regexp = "^$|^[A-Za-z]{2}$") String estado,
        @Size(max = 30) String documento) {}

    public record UsuarioResponse(Long id, String nome, String email, String telefone, TipoUsuario tipo,
        NivelAcesso nivelAcesso, java.util.Set<PermissaoSistema> permissoes,
        boolean ativo, String cidade, String estado, String documento, Instant criadoEm) {
        public static UsuarioResponse from(Usuario usuario) {
            return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTelefone(),
                usuario.getTipo(), usuario.getTipo().nivelAcesso(), usuario.getTipo().permissoes(), usuario.isAtivo(),
                usuario.getCidade(), usuario.getEstado(), usuario.getDocumento(), usuario.getCriadoEm());
        }
    }

    public record AuthResponse(UsuarioResponse usuario, String token, Instant expiraEm) {}
    public record CategoriaResponse(Long id, String nome, String slug) {
        public static CategoriaResponse from(Categoria categoria) {
            return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getSlug());
        }
    }
    public record FotoResponse(Long id, String url, String nomeOriginal, String contentType) {
        public static FotoResponse from(FotoMaterial foto) {
            return new FotoResponse(foto.getId(), foto.getUrl(), foto.getNomeOriginal(), foto.getContentType());
        }
    }
    public record MaterialRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 2000) String descricao,
        @NotNull @Positive Integer quantidade,
        @NotBlank @Size(max = 30) String unidade,
        @NotNull EstadoConservacao estadoConservacao,
        @NotNull Long categoriaId,
        @NotBlank @Size(max = 240) String enderecoRetirada,
        @NotBlank @Size(max = 120) String cidade,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") String estado,
        boolean confirmarDuplicidade) {}

    public record MaterialResponse(Long id, String nome, String descricao, Integer quantidade, String unidade,
        EstadoConservacao estadoConservacao, StatusMaterial status, CategoriaResponse categoria,
        String enderecoRetirada, String cidade, String estado, List<FotoResponse> fotos,
        UsuarioResumo proprietario, Instant dataCadastro, Instant atualizadoEm) {}

    public record UsuarioResumo(Long id, String nome, TipoUsuario tipo) {
        public static UsuarioResumo from(Usuario usuario) {
            if (usuario == null) return new UsuarioResumo(0L, "Usuário legado", TipoUsuario.DOADOR_PESSOA_FISICA);
            return new UsuarioResumo(usuario.getId(), usuario.getNome(), usuario.getTipo());
        }
    }

    public record ReservaRequest(@NotNull Long materialId, @NotNull @Positive Integer quantidade, @Size(max = 500) String mensagem) {}
    public record ReservaResponse(Long id, MaterialResumo material, UsuarioResumo solicitante, Integer quantidade,
        StatusReserva status, String mensagem, Instant criadoEm, Instant atualizadoEm) {}
    public record MaterialResumo(Long id, String nome, String unidade, String cidade, String estado,
        StatusMaterial status, UsuarioResumo proprietario) {}

    public record DoacaoResponse(Long id, MaterialResumo material, UsuarioResumo doador, UsuarioResumo beneficiario,
        Integer quantidade, Instant concluidaEm, BigDecimal pesoEstimadoKg,
        BigDecimal residuosEvitadosKg, BigDecimal co2EvitadoKg) {}

    public record HistoricoResponse(Long id, TipoAcaoHistorico tipo, String descricao, String ip,
        String recursoTipo, Long recursoId, UsuarioResumo usuario, Instant ocorridoEm) {}

    public record NotificacaoResponse(Long id, String titulo, String mensagem, boolean lida, Instant criadaEm) {}

    public record DenunciaRequest(@NotNull Long materialId, @NotBlank @Size(max = 500) String motivo) {}
    public record DenunciaResponse(Long id, Long materialId, String materialNome, UsuarioResumo denunciante,
        String motivo, StatusDenuncia status, Instant criadoEm, Instant resolvidoEm) {}

    public record DashboardResponse(long materiaisCadastrados, long materiaisDisponiveis, long materiaisReutilizados,
        long reservas, long doacoesConcluidas, long usuarios, long ongs, long construtoras, long familias,
        long quantidadeReaproveitada, BigDecimal residuosEvitadosKg, BigDecimal pesoEstimadoKg,
        BigDecimal co2EvitadoKg, Map<String, Long> categoriasMaisUtilizadas,
        Map<String, Long> doacoesPorMes, List<MaterialRanking> materiaisMaisDoados,
        List<MaterialRanking> materiaisMaisProcurados) {}
    public record MaterialRanking(String nome, long quantidade) {}
}
