package com.reconstroi.backend.controller;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.reconstroi.backend.dto.ApiDtos.*;
import com.reconstroi.backend.model.*;
import com.reconstroi.backend.service.MaterialService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/materiais")
public class MaterialController {
    private final MaterialService service;
    public MaterialController(MaterialService service) { this.service = service; }

    @GetMapping
    public Page<MaterialResponse> pesquisar(@RequestParam(required = false) String busca,
            @RequestParam(required = false) Long categoriaId, @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String estado, @RequestParam(required = false) Integer quantidadeMinima,
            @RequestParam(required = false) EstadoConservacao conservacao,
            @RequestParam(required = false) StatusMaterial status,
            @PageableDefault(size = 12, sort = "dataCadastro", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal Usuario usuario) {
        return service.pesquisar(busca, categoriaId, cidade, estado, quantidadeMinima, conservacao, status, pageable, usuario);
    }

    @GetMapping("/{id}")
    public MaterialResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return service.buscar(id, usuario);
    }

    @GetMapping("/meus")
    public List<MaterialResponse> meus(@AuthenticationPrincipal Usuario usuario) { return service.meus(usuario); }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('MATERIAL_CADASTRAR')")
    public ResponseEntity<MaterialResponse> criar(@Valid @RequestPart("dados") MaterialRequest request,
            @RequestPart("fotos") List<MultipartFile> fotos, @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request, fotos, usuario));
    }

    @PutMapping("/{id}")
    public MaterialResponse atualizar(@PathVariable Long id, @Valid @RequestBody MaterialRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        return service.atualizar(id, request, usuario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        service.excluir(id, usuario);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FotoResponse> upload(@PathVariable Long id, @RequestPart("arquivo") MultipartFile file,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.upload(id, file, usuario));
    }
}
