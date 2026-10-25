/* eslint-disable react-hooks/set-state-in-effect */
"use client";
import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import {
  ArrowRight,
  Flag,
  PackageSearch,
  ShieldCheck,
  UsersRound,
} from "lucide-react";
import { api, ApiError } from "@/lib/api";
import {
  tipoLabels,
  type Denuncia,
  type Doacao,
  type Material,
  type Reserva,
  type Usuario,
} from "@/lib/types";
import { useAuth } from "@/components/auth-provider";
import { useToast } from "@/components/toast-provider";
import { EmptyState, PageHeader, Spinner, StatusBadge } from "@/components/ui";
type Tab = "usuarios" | "materiais" | "reservas" | "doacoes" | "denuncias";
export default function AdminPage() {
  const { user } = useAuth();
  const [tab, setTab] = useState<Tab>("usuarios");
  const [users, setUsers] = useState<Usuario[]>([]);
  const [materials, setMaterials] = useState<Material[]>([]);
  const [reports, setReports] = useState<Denuncia[]>([]);
  const [reservations, setReservations] = useState<Reserva[]>([]);
  const [donations, setDonations] = useState<Doacao[]>([]);
  const [loading, setLoading] = useState(true);
  const toast = useToast();
  const load = useCallback(() => {
    setLoading(true);
    const task =
      tab === "usuarios"
        ? api.adminUsuarios().then((r) => setUsers(r.content))
        : tab === "materiais"
          ? api.adminMateriais().then((r) => setMaterials(r.content))
          : tab === "reservas"
            ? api.adminReservas().then(setReservations)
            : tab === "doacoes"
              ? api.adminDoacoes().then(setDonations)
              : api.adminDenuncias().then(setReports);
    task
      .catch((err) =>
        toast.error(
          err instanceof ApiError ? err.message : "Erro ao carregar.",
        ),
      )
      .finally(() => setLoading(false));
  }, [tab, toast]);
  useEffect(load, [load]);
  if (user?.tipo !== "ADMIN")
    return (
      <EmptyState
        title="Acesso restrito"
        description="Esta área é exclusiva para administradores."
      />
    );
  async function active(u: Usuario) {
    try {
      await api.adminAtivar(u.id, !u.ativo);
      toast.success("Status do usuário atualizado.");
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro.");
    }
  }
  async function remove(m: Material) {
    if (!confirm(`Remover o anúncio “${m.nome}”?`)) return;
    const motivo = prompt("Informe o motivo da remoção:", "Anúncio irregular.");
    if (motivo === null) return;
    try {
      await api.adminRemoverMaterial(m.id, motivo);
      toast.success("Anúncio removido.");
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro.");
    }
  }
  async function reportStatus(d: Denuncia, status: string) {
    try {
      await api.adminDenunciaStatus(d.id, status);
      toast.success("Denúncia atualizada.");
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro.");
    }
  }
  return (
    <>
      <PageHeader
        eyebrow="Governança"
        title="Administração"
        description="Gerencie a comunidade, os anúncios e a integridade da plataforma."
      />
      <div className="admin-highlights">
        <article>
          <UsersRound />
          <span>
            <b>{users.length}</b> usuários nesta página
          </span>
        </article>
        <article>
          <PackageSearch />
          <span>
            <b>{materials.length}</b> materiais nesta página
          </span>
        </article>
        <article>
          <Flag />
          <span>
            <b>{reports.filter((r) => r.status === "ABERTA").length}</b>{" "}
            denúncias abertas
          </span>
        </article>
        <article>
          <ShieldCheck />
          <span>
            <b>JWT</b> segurança ativa
          </span>
        </article>
      </div>
      <div className="tabs">
        <button
          className={tab === "usuarios" ? "active" : ""}
          onClick={() => setTab("usuarios")}
        >
          Usuários
        </button>
        <button
          className={tab === "materiais" ? "active" : ""}
          onClick={() => setTab("materiais")}
        >
          Materiais
        </button>
        <button
          className={tab === "denuncias" ? "active" : ""}
          onClick={() => setTab("denuncias")}
        >
          Denúncias
        </button>
        <button
          className={tab === "reservas" ? "active" : ""}
          onClick={() => setTab("reservas")}
        >
          Reservas
        </button>
        <button
          className={tab === "doacoes" ? "active" : ""}
          onClick={() => setTab("doacoes")}
        >
          Doações
        </button>
      </div>
      {loading ? (
        <Spinner />
      ) : tab === "usuarios" ? (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Usuário</th>
                <th>Perfil</th>
                <th>Localização</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>
                    <b>{u.nome}</b>
                    <small>{u.email}</small>
                  </td>
                  <td>{tipoLabels[u.tipo]}</td>
                  <td>{u.cidade ? `${u.cidade}/${u.estado}` : "—"}</td>
                  <td>
                    <span
                      className={
                        u.ativo
                          ? "status status-disponivel"
                          : "status status-cancelada"
                      }
                    >
                      {u.ativo ? "Ativo" : "Inativo"}
                    </span>
                  </td>
                  <td>
                    <button className="table-action" onClick={() => active(u)}>
                      {u.ativo ? "Desativar" : "Ativar"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : tab === "materiais" ? (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Material</th>
                <th>Responsável</th>
                <th>Local</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {materials.map((m) => (
                <tr key={m.id}>
                  <td>
                    <b>{m.nome}</b>
                    <small>{m.categoria.nome}</small>
                  </td>
                  <td>{m.proprietario.nome}</td>
                  <td>
                    {m.cidade}/{m.estado}
                  </td>
                  <td>
                    <StatusBadge status={m.status} />
                  </td>
                  <td className="actions-cell">
                    <Link href={`/materiais/${m.id}`}>
                      Ver <ArrowRight />
                    </Link>
                    <button onClick={() => remove(m)}>Remover</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : tab === "reservas" ? (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Material</th>
                <th>Beneficiário</th>
                <th>Quantidade</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {reservations.map((r) => (
                <tr key={r.id}>
                  <td>
                    <b>{r.material.nome}</b>
                    <small>{r.material.proprietario.nome}</small>
                  </td>
                  <td>{r.solicitante.nome}</td>
                  <td>
                    {r.quantidade} {r.material.unidade}
                  </td>
                  <td>
                    <StatusBadge status={r.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : tab === "doacoes" ? (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Material</th>
                <th>Doador</th>
                <th>Beneficiário</th>
                <th>Impacto</th>
              </tr>
            </thead>
            <tbody>
              {donations.map((d) => (
                <tr key={d.id}>
                  <td>
                    <b>{d.material.nome}</b>
                    <small>
                      {d.quantidade} {d.material.unidade}
                    </small>
                  </td>
                  <td>{d.doador.nome}</td>
                  <td>{d.beneficiario.nome}</td>
                  <td>{d.co2EvitadoKg} kg CO₂</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : reports.length ? (
        <div className="report-list">
          {reports.map((d) => (
            <article key={d.id}>
              <div>
                <Flag />
                <span>
                  <small>Denúncia #{d.id}</small>
                  <b>{d.materialNome || "Material removido"}</b>
                </span>
              </div>
              <p>{d.motivo}</p>
              <footer>
                <span>
                  Por {d.denunciante.nome} ·{" "}
                  {new Date(d.criadoEm).toLocaleDateString("pt-BR")}
                </span>
                <select
                  value={d.status}
                  onChange={(e) => reportStatus(d, e.target.value)}
                >
                  <option value="ABERTA">Aberta</option>
                  <option value="EM_ANALISE">Em análise</option>
                  <option value="RESOLVIDA">Resolvida</option>
                  <option value="ARQUIVADA">Arquivada</option>
                </select>
              </footer>
            </article>
          ))}
        </div>
      ) : (
        <EmptyState
          title="Nenhuma denúncia"
          description="A comunidade está sem ocorrências pendentes."
        />
      )}
    </>
  );
}
