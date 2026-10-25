package com.reconstroi.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.reconstroi.backend.model.TipoUsuario;
import com.reconstroi.backend.model.Usuario;
import com.reconstroi.backend.repository.UsuarioRepository;

@Component
public class AdminBootstrapConfig implements ApplicationRunner {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;

    public AdminBootstrapConfig(UsuarioRepository repository, PasswordEncoder encoder,
            @Value("${ADMIN_EMAIL:}") String email, @Value("${ADMIN_PASSWORD:}") String password) {
        this.repository = repository; this.encoder = encoder; this.email = email; this.password = password;
    }

    @Override public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank() || repository.findByEmailIgnoreCase(email).isPresent()) return;
        Usuario admin = new Usuario();
        admin.setNome("Administrador ReConstrói"); admin.setEmail(email.trim().toLowerCase()); admin.setTelefone("Não informado");
        admin.setSenha(encoder.encode(password)); admin.setTipo(TipoUsuario.ADMIN);
        repository.save(admin);
    }
}
