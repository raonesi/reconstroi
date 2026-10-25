package com.reconstroi.backend.service;

/** Evento publicado na transação da reserva; o e-mail só sai depois do commit. */
public record StatusReservaAtualizada(String email, String nome, String material, boolean aprovada) {}
