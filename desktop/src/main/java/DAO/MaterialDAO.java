package DAO;

import DTO.MaterialDTO;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MaterialDAO {
    private final ConexaoDAO conexao = new ConexaoDAO();

    public List<MaterialDTO> pesquisar(String busca, String categoriaId, String cidade, String estado, String conservacao) {
        Map<String, String> filtros = new LinkedHashMap<>();
        filtros.put("busca", busca);
        filtros.put("categoriaId", categoriaId);
        filtros.put("cidade", cidade);
        filtros.put("estado", estado);
        filtros.put("conservacao", conservacao);

        // Filtros vazios nao precisam ser enviados ao backend.
        StringBuilder endereco = new StringBuilder("/materiais?size=100");
        for (Map.Entry<String, String> filtro : filtros.entrySet()) {
            String valor = filtro.getValue();
            if (valor != null && !valor.isBlank()) {
                endereco.append('&');
                endereco.append(filtro.getKey());
                endereco.append('=');
                endereco.append(URLEncoder.encode(valor, StandardCharsets.UTF_8));
            }
        }
        return conexao.getPageContent(endereco.toString(), MaterialDTO.class);
    }

    public List<MaterialDTO> listarMeus() {
        return conexao.getList("/materiais/meus", MaterialDTO.class);
    }

    public MaterialDTO consultar(long id) {
        return conexao.get("/materiais/" + id, MaterialDTO.class);
    }

    public MaterialDTO cadastrar(Map<String, Object> dados, List<File> fotos) {
        if (fotos == null || fotos.isEmpty()) {
            throw new ApiException(0, "Selecione pelo menos uma foto.");
        }
        return conexao.multipart("/materiais", dados, fotos, MaterialDTO.class);
    }

    public MaterialDTO alterar(long id, Map<String, Object> dados) {
        return conexao.put("/materiais/" + id, dados, MaterialDTO.class);
    }

    public void excluir(long id) {
        conexao.delete("/materiais/" + id, null);
    }

    public void denunciar(long id, String motivo) {
        conexao.post("/denuncias", Map.of("materialId", id, "motivo", motivo), Void.class);
    }
}
