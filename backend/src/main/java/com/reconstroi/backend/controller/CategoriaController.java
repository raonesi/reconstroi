package com.reconstroi.backend.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.reconstroi.backend.dto.ApiDtos.CategoriaResponse;
import com.reconstroi.backend.repository.CategoriaRepository;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {
    private final CategoriaRepository repository;
    public CategoriaController(CategoriaRepository repository) { this.repository = repository; }
    @GetMapping public List<CategoriaResponse> listar() { return repository.findByAtivoTrueOrderByNome().stream().map(CategoriaResponse::from).toList(); }
}
