package com.reconstroi.backend;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import com.reconstroi.backend.exception.*;
import com.reconstroi.backend.model.*;
import com.reconstroi.backend.service.*;

class BusinessRulesUnitTests {
    private final AutorizacaoService autorizacao = new AutorizacaoService();
    private final ImpactoAmbientalService impacto = new ImpactoAmbientalService();

    @Test
    void mapeiaNiveisEPermissoesSemEscalarPrivilegios() {
        assertThat(TipoUsuario.DOADOR_PESSOA_FISICA.nivelAcesso()).isEqualTo(NivelAcesso.DOADOR);
        assertThat(TipoUsuario.DOADOR_CONSTRUTORA.permissoes()).contains(PermissaoSistema.MATERIAL_CADASTRAR)
            .doesNotContain(PermissaoSistema.RESERVA_SOLICITAR, PermissaoSistema.USUARIO_GERENCIAR);
        assertThat(TipoUsuario.BENEFICIARIO_ONG.nivelAcesso()).isEqualTo(NivelAcesso.BENEFICIARIO);
        assertThat(TipoUsuario.BENEFICIARIO_FAMILIA.permissoes()).contains(PermissaoSistema.RESERVA_SOLICITAR)
            .doesNotContain(PermissaoSistema.MATERIAL_CADASTRAR);
        assertThat(TipoUsuario.ADMIN.nivelAcesso()).isEqualTo(NivelAcesso.ADMINISTRADOR);
        assertThat(TipoUsuario.ADMIN.permissoes()).contains(PermissaoSistema.USUARIO_GERENCIAR,
            PermissaoSistema.MATERIAL_EDITAR_TODOS, PermissaoSistema.RESERVA_VISUALIZAR_TODAS)
            .doesNotContain(PermissaoSistema.MATERIAL_CADASTRAR, PermissaoSistema.RESERVA_SOLICITAR);
    }

    @Test
    void rejeitaUsuarioBloqueadoNaCamadaDeServico() {
        Usuario bloqueado = usuario(TipoUsuario.DOADOR_PESSOA_FISICA);
        bloqueado.setAtivo(false);
        assertThatThrownBy(() -> autorizacao.exigirAtivo(bloqueado))
            .isInstanceOf(UsuarioSemPermissaoException.class)
            .extracting(error -> ((BusinessException) error).getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void aceitaUsuarioAtivo() {
        assertThatCode(() -> autorizacao.exigirAtivo(usuario(TipoUsuario.BENEFICIARIO_ONG))).doesNotThrowAnyException();
    }

    @Test
    void calculaPesoResiduosECo2PelaUnidade() {
        Material material = material("kg", "ferro");
        var resultado = impacto.calcular(material, 100);
        assertThat(resultado.pesoEstimadoKg()).isEqualByComparingTo("100.00");
        assertThat(resultado.residuosEvitadosKg()).isEqualByComparingTo("100.00");
        assertThat(resultado.co2EvitadoKg()).isEqualByComparingTo("42.00");
    }

    @Test
    void estimaPesoPorCategoriaQuandoUnidadeNaoTemConversaoDireta() {
        Material material = material("unidades", "tijolos");
        assertThat(impacto.calcular(material, 10).pesoEstimadoKg()).isEqualByComparingTo("25.00");
    }

    @Test
    void defineEstadosQueImpedemSegundaReserva() {
        assertThat(StatusReserva.ativos()).containsExactly(
            StatusReserva.SOLICITADA, StatusReserva.APROVADA, StatusReserva.RETIRADA);
        assertThat(StatusReserva.ativos()).doesNotContain(
            StatusReserva.RECUSADA, StatusReserva.CANCELADA, StatusReserva.FINALIZADA);
    }

    @Test
    void excecoesDeDominioRetornamStatusHttpCorreto() {
        assertThat(new MaterialNaoDisponivelException().getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(new ReservaJaExistenteException().getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(new QuantidadeInvalidaException("erro").getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(new UsuarioSemPermissaoException("erro").getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(new MaterialFinalizadoException().getStatus()).isEqualTo(HttpStatus.CONFLICT);
    }

    private Usuario usuario(TipoUsuario tipo) {
        Usuario usuario = new Usuario();
        usuario.setTipo(tipo);
        usuario.setAtivo(true);
        return usuario;
    }

    private Material material(String unidade, String categoriaSlug) {
        Categoria categoria = new Categoria();
        categoria.setSlug(categoriaSlug);
        Material material = new Material();
        material.setUnidade(unidade);
        material.setCategoria(categoria);
        return material;
    }
}
