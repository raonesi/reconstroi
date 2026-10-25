package com.reconstroi.backend.controller;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.reconstroi.backend.dto.ApiDtos.DoacaoResponse;
import com.reconstroi.backend.model.Usuario;
import com.reconstroi.backend.service.DoacaoService;

@RestController
@RequestMapping("/doacoes")
public class DoacaoController {
    private final DoacaoService service;
    public DoacaoController(DoacaoService service) { this.service = service; }
    @GetMapping public List<DoacaoResponse> listar(@AuthenticationPrincipal Usuario usuario) { return service.listar(usuario); }
}
