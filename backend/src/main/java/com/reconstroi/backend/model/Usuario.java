package com.reconstroi.backend.model;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Usuario implements UserDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(nullable = false, length = 30)
    private String telefone;

    @JsonIgnore
    @Column(nullable = false, length = 100)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoUsuario tipo = TipoUsuario.DOADOR_PESSOA_FISICA;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(length = 120)
    private String cidade;

    @Column(name = "estado_uf", length = 2)
    private String estado;

    @Column(length = 30)
    private String documento;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        java.util.ArrayList<GrantedAuthority> authorities = new java.util.ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + tipo.name()));
        authorities.add(new SimpleGrantedAuthority("NIVEL_" + tipo.nivelAcesso().name()));
        tipo.permissoes().stream().map(Enum::name).map(SimpleGrantedAuthority::new).forEach(authorities::add);
        return List.copyOf(authorities);
    }
    @Override public String getPassword() { return senha; }
    @Override public String getUsername() { return email; }
    @Override public boolean isEnabled() { return ativo; }
}
