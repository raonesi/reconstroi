package com.reconstroi.backend.model;

import java.util.Set;

public enum TipoUsuario {
    DOADOR_PESSOA_FISICA,
    DOADOR_CONSTRUTORA,
    BENEFICIARIO_ONG,
    BENEFICIARIO_FAMILIA,
    ADMIN;

    public boolean isDoador() {
        return this == DOADOR_PESSOA_FISICA || this == DOADOR_CONSTRUTORA || this == ADMIN;
    }

    public boolean isBeneficiario() {
        return this == BENEFICIARIO_ONG || this == BENEFICIARIO_FAMILIA || this == ADMIN;
    }

    public NivelAcesso nivelAcesso() {
        if (this == ADMIN) return NivelAcesso.ADMINISTRADOR;
        return isDoador() ? NivelAcesso.DOADOR : NivelAcesso.BENEFICIARIO;
    }

    public Set<PermissaoSistema> permissoes() {
        Set<PermissaoSistema> comuns = java.util.EnumSet.of(
            PermissaoSistema.MATERIAL_CONSULTAR,
            PermissaoSistema.DOACAO_VISUALIZAR_PROPRIAS,
            PermissaoSistema.RELATORIO_VISUALIZAR);
        if (nivelAcesso() == NivelAcesso.DOADOR) {
            comuns.addAll(Set.of(PermissaoSistema.MATERIAL_CADASTRAR, PermissaoSistema.MATERIAL_EDITAR_PROPRIO,
                PermissaoSistema.MATERIAL_REMOVER_PROPRIO, PermissaoSistema.RESERVA_GERENCIAR_RECEBIDA));
        } else if (nivelAcesso() == NivelAcesso.BENEFICIARIO) {
            comuns.addAll(Set.of(PermissaoSistema.RESERVA_SOLICITAR, PermissaoSistema.RESERVA_CANCELAR_PROPRIA));
        } else {
            comuns.addAll(java.util.EnumSet.allOf(PermissaoSistema.class));
            comuns.remove(PermissaoSistema.MATERIAL_CADASTRAR);
            comuns.remove(PermissaoSistema.RESERVA_SOLICITAR);
            comuns.remove(PermissaoSistema.RESERVA_CANCELAR_PROPRIA);
        }
        return Set.copyOf(comuns);
    }
}
