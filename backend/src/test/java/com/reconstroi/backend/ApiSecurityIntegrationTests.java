package com.reconstroi.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reconstroi.backend.model.*;
import com.reconstroi.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiSecurityIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired PasswordEncoder encoder;

    private long categoriaId;

    @BeforeEach
    void categoria() {
        categoriaId = categorias.findByAtivoTrueOrderByNome().get(0).getId();
    }

    @Test
    void exigeAutenticacaoEAplicaNiveisDeAcesso() throws Exception {
        mockMvc.perform(get("/materiais")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/categorias")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/usuarios/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"inexistente@teste.local\",\"senha\":\"Senha123!\"}"))
            .andExpect(status().isUnauthorized());

        String donor = register("Doador Nível", "doador-nivel@reconstroi.local", "DOADOR_PESSOA_FISICA",
            "DOADOR", "MATERIAL_CADASTRAR");
        String beneficiary = register("Família Nível", "familia-nivel@reconstroi.local", "BENEFICIARIO_FAMILIA",
            "BENEFICIARIO", "RESERVA_SOLICITAR");

        mockMvc.perform(createMaterialRequest("Material proibido", 5, false, beneficiary))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/materiais").header(auth(), bearer(donor))).andExpect(status().isOk());
    }

    @Test
    void validaCamposFotoQuantidadeEnumsEDuplicidadeComConfirmacao() throws Exception {
        String donor = register("Doador Validação", "doador-validacao@reconstroi.local", "DOADOR_CONSTRUTORA",
            "DOADOR", "MATERIAL_CADASTRAR");

        MockMultipartFile dadosSemFoto = jsonPart(materialJson("Sem foto", 2, false));
        mockMvc.perform(multipart("/materiais").file(dadosSemFoto).header(auth(), bearer(donor)))
            .andExpect(status().isBadRequest());

        mockMvc.perform(createMaterialRequest("Quantidade inválida", 0, false, donor))
            .andExpect(status().isBadRequest());

        String enumInvalido = materialJson("Enum inválido", 1, false).replace("\"BOM\"", "\"QUEBRADO\"");
        mockMvc.perform(multipart("/materiais").file(jsonPart(enumInvalido)).file(photo()).header(auth(), bearer(donor)))
            .andExpect(status().isBadRequest());

        mockMvc.perform(createMaterialRequest("Tijolo duplicado", 50, false, donor))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.fotos.length()").value(1));
        mockMvc.perform(createMaterialRequest("Tijolo duplicado", 50, false, donor))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("mesmo nome")));
        mockMvc.perform(createMaterialRequest("Tijolo duplicado", 50, true, donor))
            .andExpect(status().isCreated());
    }

    @Test
    void garanteUmaReservaAtivaPermissoesCancelamentoENotificacoes() throws Exception {
        String donor = register("Doador Reserva", "doador-reserva@reconstroi.local", "DOADOR_PESSOA_FISICA",
            "DOADOR", "MATERIAL_CADASTRAR");
        String first = register("ONG Um", "ong-um@reconstroi.local", "BENEFICIARIO_ONG",
            "BENEFICIARIO", "RESERVA_SOLICITAR");
        String second = register("ONG Dois", "ong-dois@reconstroi.local", "BENEFICIARIO_ONG",
            "BENEFICIARIO", "RESERVA_SOLICITAR");
        long materialId = createMaterial("Madeira de reserva", 30, donor);
        long reservaId = requestReservation(materialId, 10, first);

        mockMvc.perform(get("/notificacoes").header(auth(), bearer(donor))).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].titulo").value("Nova solicitação recebida"))
            .andExpect(jsonPath("$[0].mensagem").value(org.hamcrest.Matchers.containsString("ONG Um")))
            .andExpect(jsonPath("$[0].mensagem").value(org.hamcrest.Matchers.containsString("10 unidades")));

        mockMvc.perform(post("/reservas").header(auth(), bearer(second)).contentType(MediaType.APPLICATION_JSON)
            .content("{\"materialId\":" + materialId + ",\"quantidade\":5}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("reserva ativa")));

        action(reservaId, "aprovar", donor, "APROVADA");
        mockMvc.perform(get("/notificacoes").header(auth(), bearer(first))).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].titulo").value("Reserva aprovada")).andExpect(jsonPath("$[0].lida").value(false));

        mockMvc.perform(patch("/reservas/{id}/cancelar", reservaId).header(auth(), bearer(first)))
            .andExpect(status().isConflict());
        mockMvc.perform(patch("/reservas/{id}/finalizar", reservaId).header(auth(), bearer(donor)))
            .andExpect(status().isConflict());
        mockMvc.perform(put("/materiais/{id}", materialId).header(auth(), bearer(donor)).contentType(MediaType.APPLICATION_JSON)
            .content(materialJson("Tentativa edição", 30, false)))
            .andExpect(status().isConflict());

        action(reservaId, "cancelar", donor, "CANCELADA");
        mockMvc.perform(get("/materiais").header(auth(), bearer(second)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content[?(@.id == " + materialId + ")]").exists());
    }

    @Test
    void naoDeixaLoteFicarMenorQueSolicitacaoPendenteNemAprovarAcimaDoLote() throws Exception {
        String donor = register("Doador Quantidade", "doador-quantidade@reconstroi.local", "DOADOR_PESSOA_FISICA",
            "DOADOR", "MATERIAL_CADASTRAR");
        String ong = register("ONG Quantidade", "ong-quantidade@reconstroi.local", "BENEFICIARIO_ONG",
            "BENEFICIARIO", "RESERVA_SOLICITAR");
        long materialId = createMaterial("Telhas para quantidade", 50, donor);
        long reservaId = requestReservation(materialId, 40, ong);

        // Reduzir o lote para 5 com uma solicitação de 40 pendente é recusado.
        mockMvc.perform(put("/materiais/{id}", materialId).header(auth(), bearer(donor)).contentType(MediaType.APPLICATION_JSON)
            .content(materialJson("Telhas para quantidade", 5, true)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("solicitação pendente de 40")));
        // Igual ou acima da solicitação continua permitido.
        mockMvc.perform(put("/materiais/{id}", materialId).header(auth(), bearer(donor)).contentType(MediaType.APPLICATION_JSON)
            .content(materialJson("Telhas para quantidade", 40, true)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.quantidade").value(40));

        action(reservaId, "aprovar", donor, "APROVADA");
    }

    @Test
    void bloqueiaAtaquesComunsDeUploadOrdenacaoForcaBrutaESessaoAntiga() throws Exception {
        String donor = register("Doador Segurança", "doador-seguranca@reconstroi.local", "DOADOR_CONSTRUTORA",
            "DOADOR", "MATERIAL_CADASTRAR");

        // Arquivo que diz ser PNG mas é texto/script.
        MockMultipartFile falso = new MockMultipartFile("fotos", "x.png", MediaType.IMAGE_PNG_VALUE,
            "<script>alert(1)</script>".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/materiais").file(jsonPart(materialJson("Upload falso", 1, true))).file(falso)
            .header(auth(), bearer(donor))).andExpect(status().isBadRequest());

        // Ordenar pela senha de outro usuário (ou campo inexistente) é recusado.
        mockMvc.perform(get("/materiais").param("sort", "proprietario.senha").header(auth(), bearer(donor)))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get("/materiais").param("sort", "naoExiste").header(auth(), bearer(donor)))
            .andExpect(status().isBadRequest());

        // Força bruta: depois de 10 senhas erradas, a conta fica temporariamente bloqueada (429).
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"doador-seguranca@reconstroi.local\",\"senha\":\"errada" + i + "\"}"))
                .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/usuarios/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"doador-seguranca@reconstroi.local\",\"senha\":\"SenhaForte123!\"}"))
            .andExpect(status().isTooManyRequests());

        // Trocar a senha derruba as sessões abertas antes.
        Usuario usuario = usuarios.findByEmailIgnoreCase("doador-seguranca@reconstroi.local").orElseThrow();
        usuario.setSenha(encoder.encode("OutraSenha456!"));
        usuarios.saveAndFlush(usuario);
        mockMvc.perform(get("/usuarios/me").header(auth(), bearer(donor))).andExpect(status().isUnauthorized());
    }

    @Test
    void executaFluxoCompletoSemPularEtapasEPreservaDoacaoEHistorico() throws Exception {
        String donor = register("Doador Fluxo", "doador-fluxo-novo@reconstroi.local", "DOADOR_PESSOA_FISICA",
            "DOADOR", "MATERIAL_CADASTRAR");
        String beneficiary = register("Família Fluxo", "familia-fluxo@reconstroi.local", "BENEFICIARIO_FAMILIA",
            "BENEFICIARIO", "RESERVA_SOLICITAR");
        long materialId = createMaterial("Tijolos de demolição", 100, donor);
        long reservaId = requestReservation(materialId, 20, beneficiary);

        action(reservaId, "aprovar", donor, "APROVADA");
        action(reservaId, "confirmar-retirada", donor, "RETIRADA");
        action(reservaId, "finalizar", donor, "FINALIZADA");

        mockMvc.perform(get("/doacoes").header(auth(), bearer(donor))).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].material.nome").value("Tijolos de demolição"))
            .andExpect(jsonPath("$[0].quantidade").value(20))
            .andExpect(jsonPath("$[0].pesoEstimadoKg").value(org.hamcrest.Matchers.greaterThan(0.0)))
            .andExpect(jsonPath("$[0].co2EvitadoKg").value(org.hamcrest.Matchers.greaterThan(0.0)));

        // Doação parcial (20 de 100): a sobra volta a ficar disponível.
        mockMvc.perform(get("/materiais/{id}", materialId).header(auth(), bearer(donor))).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DISPONIVEL")).andExpect(jsonPath("$.quantidade").value(80));

        // Outro beneficiário leva o restante; aí o lote é encerrado.
        String second = register("ONG Restante", "ong-restante@reconstroi.local", "BENEFICIARIO_ONG",
            "BENEFICIARIO", "RESERVA_SOLICITAR");
        long restoId = requestReservation(materialId, 80, second);
        action(restoId, "aprovar", donor, "APROVADA");
        action(restoId, "confirmar-retirada", donor, "RETIRADA");
        action(restoId, "finalizar", donor, "FINALIZADA");
        mockMvc.perform(get("/materiais/{id}", materialId).header(auth(), bearer(donor))).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("FINALIZADO"));

        mockMvc.perform(put("/materiais/{id}", materialId).header(auth(), bearer(donor)).contentType(MediaType.APPLICATION_JSON)
            .content(materialJson("Não editar finalizado", 100, false))).andExpect(status().isConflict());
        mockMvc.perform(delete("/materiais/{id}", materialId).header(auth(), bearer(donor))).andExpect(status().isConflict());
        mockMvc.perform(post("/reservas").header(auth(), bearer(beneficiary)).contentType(MediaType.APPLICATION_JSON)
            .content("{\"materialId\":" + materialId + ",\"quantidade\":1}"))
            .andExpect(status().isConflict());

        mockMvc.perform(get("/historico/meu").header(auth(), bearer(donor))).andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.tipo == 'MATERIAL_CRIADO')]").exists())
            .andExpect(jsonPath("$[?(@.tipo == 'DOACAO_FINALIZADA')]").exists())
            .andExpect(jsonPath("$[0].ip").isNotEmpty());
    }

    @Test
    void administradorBloqueiaUsuarioEVisualizaDadosGlobais() throws Exception {
        String donor = register("Doador Bloqueado", "bloqueado@reconstroi.local", "DOADOR_CONSTRUTORA",
            "DOADOR", "MATERIAL_CADASTRAR");
        JsonNode me = json(mockMvc.perform(get("/usuarios/me").header(auth(), bearer(donor))).andReturn().getResponse().getContentAsString());
        long userId = me.get("id").asLong();
        String admin = adminToken();

        mockMvc.perform(patch("/admin/usuarios/{id}/ativo", userId).header(auth(), bearer(admin))
            .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":false}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        mockMvc.perform(post("/usuarios/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"bloqueado@reconstroi.local\",\"senha\":\"SenhaForte123!\"}"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/materiais").header(auth(), bearer(donor))).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/admin/reservas").header(auth(), bearer(admin))).andExpect(status().isOk());
        mockMvc.perform(get("/admin/doacoes").header(auth(), bearer(admin))).andExpect(status().isOk());
        mockMvc.perform(get("/admin/relatorios").header(auth(), bearer(admin))).andExpect(status().isOk())
            .andExpect(jsonPath("$.pesoEstimadoKg").exists())
            .andExpect(jsonPath("$.materiaisMaisProcurados").isArray());
    }

    private String register(String nome, String email, String tipo, String nivel, String permissao) throws Exception {
        String response = mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"" + nome + "\",\"email\":\"" + email
                + "\",\"telefone\":\"11999999999\",\"senha\":\"SenhaForte123!\",\"tipo\":\"" + tipo
                + "\",\"cidade\":\"São Paulo\",\"estado\":\"SP\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.usuario.nivelAcesso").value(nivel))
            .andExpect(jsonPath("$.usuario.permissoes", org.hamcrest.Matchers.hasItem(permissao)))
            .andReturn().getResponse().getContentAsString();
        return json(response).get("token").asText();
    }

    private String adminToken() throws Exception {
        Usuario admin = new Usuario();
        admin.setNome("Admin Teste");
        admin.setEmail("admin-regras@reconstroi.local");
        admin.setTelefone("11999999999");
        admin.setSenha(encoder.encode("SenhaAdmin123!"));
        admin.setTipo(TipoUsuario.ADMIN);
        usuarios.save(admin);
        String response = mockMvc.perform(post("/usuarios/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"admin-regras@reconstroi.local\",\"senha\":\"SenhaAdmin123!\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.usuario.nivelAcesso").value("ADMINISTRADOR"))
            .andReturn().getResponse().getContentAsString();
        return json(response).get("token").asText();
    }

    private long createMaterial(String nome, int quantidade, String token) throws Exception {
        String response = mockMvc.perform(createMaterialRequest(nome, quantidade, false, token))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("DISPONIVEL"))
            .andReturn().getResponse().getContentAsString();
        return json(response).get("id").asLong();
    }

    private long requestReservation(long materialId, int quantidade, String token) throws Exception {
        String response = mockMvc.perform(post("/reservas").header(auth(), bearer(token)).contentType(MediaType.APPLICATION_JSON)
            .content("{\"materialId\":" + materialId + ",\"quantidade\":" + quantidade + ",\"mensagem\":\"Uso social\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("SOLICITADA"))
            .andReturn().getResponse().getContentAsString();
        return json(response).get("id").asLong();
    }

    private MockHttpServletRequestBuilder createMaterialRequest(String nome, int quantidade, boolean confirmar, String token) {
        return multipart("/materiais").file(jsonPart(materialJson(nome, quantidade, confirmar))).file(photo()).header(auth(), bearer(token));
    }

    private String materialJson(String nome, int quantidade, boolean confirmar) {
        return "{\"nome\":\"" + nome + "\",\"descricao\":\"Lote limpo e pronto para retirada\",\"quantidade\":"
            + quantidade + ",\"unidade\":\"unidades\",\"estadoConservacao\":\"BOM\",\"categoriaId\":" + categoriaId
            + ",\"enderecoRetirada\":\"Rua do Teste, 100\",\"cidade\":\"São Paulo\",\"estado\":\"SP\",\"confirmarDuplicidade\":"
            + confirmar + "}";
    }

    private MockMultipartFile jsonPart(String json) {
        return new MockMultipartFile("dados", "dados.json", MediaType.APPLICATION_JSON_VALUE,
            json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private MockMultipartFile photo() {
        // PNG 1x1 de verdade: a API confere a assinatura do arquivo, não só o Content-Type.
        return new MockMultipartFile("fotos", "material.png", MediaType.IMAGE_PNG_VALUE, java.util.Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="));
    }

    private void action(long id, String action, String token, String expected) throws Exception {
        mockMvc.perform(patch("/reservas/{id}/{action}", id, action).header(auth(), bearer(token)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value(expected));
    }

    private String auth() { return "Authorization"; }
    private String bearer(String token) { return "Bearer " + token; }
    private JsonNode json(String value) throws Exception { return mapper.readTree(value); }
}
