package com.reconstroi.backend.controller;

import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.reconstroi.backend.dto.ApiDtos.*;
import com.reconstroi.backend.model.Usuario;
import com.reconstroi.backend.service.DenunciaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/denuncias")
public class DenunciaController {
    private final DenunciaService service;
    public DenunciaController(DenunciaService service) { this.service = service; }
    @PostMapping
    public ResponseEntity<DenunciaResponse> criar(@Valid @RequestBody DenunciaRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request, usuario));
    }
}
