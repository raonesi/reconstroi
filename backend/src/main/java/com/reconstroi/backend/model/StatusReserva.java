package com.reconstroi.backend.model;

import java.util.List;

public enum StatusReserva {
    SOLICITADA, APROVADA, RECUSADA, CANCELADA, RETIRADA, FINALIZADA;

    public static List<StatusReserva> ativos() {
        return List.of(SOLICITADA, APROVADA, RETIRADA);
    }
}
