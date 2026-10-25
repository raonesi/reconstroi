import controller.MaterialController;
import model.MaterialDAO;

public class CrudTeste {
    public static void main(String[] args) throws Exception {
        if (!"jdbc:postgresql://localhost:5432/crud_mvc_teste".equals(System.getenv("CRUD_DATABASE_URL"))) {
            throw new IllegalStateException("Execute somente no banco crud_mvc_teste.");
        }
        MaterialController controller = new MaterialController(new MaterialDAO());
        try {
            controller.cadastrar("Tijolo Ã§ = teste", "20", "DescriÃ§Ã£o com acento\ne outra linha");
            int id = controller.listar().get(0).getId();
            controller = new MaterialController(new MaterialDAO());
            conferir(controller.listar().get(0).getNome().equals("Tijolo Ã§ = teste"));
            conferir(controller.listar().get(0).getDescricao().contains("\n"));
            controller.editar(id, "Tijolo", "15", "Editado");
            conferir(controller.listar().get(0).getQuantidade() == 15);
            conferir(controller.listar().get(0).getId() == id);
            for (String quantidade : new String[] {"0", "-1", "abc"}) {
                try {
                    controller.cadastrar("Invalido", quantidade, "");
                    throw new AssertionError("Aceitou quantidade invalida");
                } catch (IllegalArgumentException esperado) { }
            }
            try {
                controller.cadastrar(" ", "1", "");
                throw new AssertionError("Aceitou nome vazio");
            } catch (IllegalArgumentException esperado) { }
            conferir(controller.listar().size() == 1);
            controller.excluir(id);
            conferir(new MaterialDAO().listar().isEmpty());
            System.out.println("PASS: cadastro, consulta, edicao, exclusao, persistencia e validacoes.");
        } finally {
            for (model.Material material : controller.listar()) {
                controller.excluir(material.getId());
            }
        }
    }
    private static void conferir(boolean resultado) {
        if (!resultado) throw new AssertionError("Resultado incorreto");
    }
}
