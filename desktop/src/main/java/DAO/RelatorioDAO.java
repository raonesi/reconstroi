package DAO;

import DTO.DashboardDTO;

public class RelatorioDAO {
    private final ConexaoDAO conexao = new ConexaoDAO();
    public DashboardDTO consultarDashboard() { return conexao.get("/relatorios/dashboard", DashboardDTO.class); }
}
