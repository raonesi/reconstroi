package com.reconstroi.backend.controller;

import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.reconstroi.backend.exception.BusinessException;

/**
 * Lista branca de campos aceitos em "?sort=".
 * Sem ela o Spring aceita qualquer caminho, inclusive "proprietario.senha": ordenar pelo hash da senha
 * de outras pessoas permite deduzi-lo aos poucos. Campo desconhecido vira 400 em vez de erro 500.
 */
final class OrdenacaoSegura {
    static final Set<String> MATERIAL = Set.of("id", "nome", "quantidade", "unidade", "cidade", "estado",
        "estadoConservacao", "status", "dataCadastro");
    static final Set<String> USUARIO = Set.of("id", "nome", "email", "tipo", "cidade", "estado", "ativo", "criadoEm");

    private OrdenacaoSegura() {}

    static Pageable validar(Pageable pageable, Set<String> permitidos) {
        for (Sort.Order ordem : pageable.getSort()) {
            if (!permitidos.contains(ordem.getProperty()))
                throw BusinessException.badRequest("Não é possível ordenar por \"" + ordem.getProperty() + "\".");
        }
        return pageable;
    }
}
