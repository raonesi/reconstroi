package DAO;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConexaoDAO {
    private static final String API_URL = descobrirApiUrl();
    private static final ObjectMapper JSON = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .build();

    private static String descobrirApiUrl() {
        String valor = System.getProperty("reconstroi.api.url");
        if (valor == null || valor.isBlank()) valor = System.getenv("RECONSTROI_API_URL");
        if (valor == null || valor.isBlank()) valor = "http://127.0.0.1:8081";
        return valor.endsWith("/") ? valor.substring(0, valor.length() - 1) : valor;
    }

    public ObjectMapper json() { return JSON; }
    public String apiUrl() { return API_URL; }

    public <T> T get(String path, Class<T> type) { return request("GET", path, null, type); }
    public <T> T post(String path, Object body, Class<T> type) { return request("POST", path, body, type); }
    public <T> T put(String path, Object body, Class<T> type) { return request("PUT", path, body, type); }
    public <T> T patch(String path, Object body, Class<T> type) { return request("PATCH", path, body, type); }
    public void delete(String path, Object body) { request("DELETE", path, body, Void.class); }

    public <T> List<T> getList(String path, Class<T> itemType) {
        JsonNode node = request("GET", path, null, JsonNode.class);
        try {
            return JSON.readerForListOf(itemType).readValue(node);
        } catch (IOException error) {
            throw new ApiException(0, "Resposta inválida recebida da API.");
        }
    }

    public <T> List<T> getPageContent(String path, Class<T> itemType) {
        JsonNode node = request("GET", path, null, JsonNode.class).path("content");
        try {
            return JSON.readerForListOf(itemType).readValue(node);
        } catch (IOException error) {
            throw new ApiException(0, "Resposta paginada inválida recebida da API.");
        }
    }

    public <T> T multipart(String path, Object dados, List<File> arquivos, Class<T> type) {
        String boundary = "----ReConstroi" + UUID.randomUUID();
        try {
            List<byte[]> parts = new ArrayList<>();
            parts.add(("--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"dados\"\r\n"
                + "Content-Type: application/json; charset=UTF-8\r\n\r\n"
                + JSON.writeValueAsString(dados) + "\r\n").getBytes(StandardCharsets.UTF_8));
            for (File arquivo : arquivos) {
                ImagemArquivo.validar(arquivo);
                String contentType = ImagemArquivo.contentType(arquivo);
                parts.add(("--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"fotos\"; filename=\"" + seguro(arquivo.getName()) + "\"\r\n"
                    + "Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                parts.add(Files.readAllBytes(arquivo.toPath()));
                parts.add("\r\n".getBytes(StandardCharsets.UTF_8));
            }
            parts.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            HttpRequest.Builder builder = base(path)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArrays(parts));
            return send(builder.build(), type);
        } catch (IOException error) {
            throw new ApiException(0, "Não foi possível ler as fotos selecionadas.");
        }
    }

    private <T> T request(String method, String path, Object body, Class<T> type) {
        try {
            HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body), StandardCharsets.UTF_8);
            HttpRequest.Builder builder = base(path);
            if (body != null) builder.header("Content-Type", "application/json; charset=UTF-8");
            return send(builder.method(method, publisher).build(), type);
        } catch (IOException error) {
            throw new ApiException(0, "Não foi possível preparar a requisição.");
        }
    }

    private HttpRequest.Builder base(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(API_URL + path))
            .timeout(Duration.ofSeconds(30))
            .header("Accept", "application/json");
        if (SessaoDAO.getToken() != null) builder.header("Authorization", "Bearer " + SessaoDAO.getToken());
        return builder;
    }

    private <T> T send(HttpRequest request, Class<T> type) {
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException(response.statusCode(), mensagemErro(response.body(), response.statusCode()));
            }
            if (type == Void.class || response.statusCode() == 204 || response.body().isBlank()) return null;
            return JSON.readValue(response.body(), type);
        } catch (ApiException error) {
            throw error;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new ApiException(0, "A operação foi interrompida.");
        } catch (IOException error) {
            throw new ApiException(0, "Não foi possível conectar à API em " + API_URL + ". Verifique se o backend está iniciado.");
        }
    }

    private String mensagemErro(String body, int status) {
        try {
            JsonNode node = JSON.readTree(body);
            String detail = node.path("detail").asText();
            if (detail.isBlank()) detail = node.path("message").asText();
            if (!detail.isBlank()) return detail;
        } catch (Exception ignored) { }
        return "A API recusou a operação (HTTP " + status + ").";
    }

    private static String seguro(String nome) { return nome.replace("\"", "").replace("\r", "").replace("\n", ""); }
}
