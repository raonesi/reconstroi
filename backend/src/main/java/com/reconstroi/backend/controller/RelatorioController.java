package com.reconstroi.backend.controller;

import org.springframework.web.bind.annotation.*;
import com.reconstroi.backend.dto.ApiDtos.DashboardResponse;
import com.reconstroi.backend.service.RelatorioService;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {
    private final RelatorioService service;
    public RelatorioController(RelatorioService service) { this.service = service; }
    @GetMapping({"/dashboard", "/publico"}) public DashboardResponse dashboard() { return service.dashboard(); }
}
