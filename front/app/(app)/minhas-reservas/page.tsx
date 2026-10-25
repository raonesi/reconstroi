/* eslint-disable react-hooks/set-state-in-effect */
"use client";
import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import {
  Check,
  CheckCheck,
  Clock3,
  MapPin,
  PackageSearch,
  X,
} from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { isDoador, type Reserva } from "@/lib/types";
import { useAuth } from "@/components/auth-provider";
import { useToast } from "@/components/toast-provider";
import { EmptyState, PageHeader, Spinner, StatusBadge } from "@/components/ui";
export default function ReservasPage() {
  const { user } = useAuth();
  const donor = isDoador(user?.tipo);
  const [tab, setTab] = useState<"minhas" | "recebidas">(
    donor ? "recebidas" : "minhas",
  );
  const [items, setItems] = useState<Reserva[]>([]);
  const [loading, setLoading] = useState(true);
  const toast = useToast();
  const load = useCallback(() => {
    setLoading(true);
    (tab === "recebidas" ? api.reservasRecebidas() : api.minhasReservas())
      .then(setItems)
      .finally(() => setLoading(false));
  }, [tab]);
  useEffect(load, [load]);
  async function action(id: number, acao: string) {
    try {
      await api.acaoReserva(id, acao);
      toast.success("Reserva atualizada.");
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Ação indisponível.");
    }
  }
  return (
    <>
      <PageHeader
        eyebrow="Acompanhamento"
        title="Reservas"
        description="Acompanhe cada etapa, da solicitação à conclusão da doação."
      />
      <div className="tabs">
        <button
          className={tab === "minhas" ? "active" : ""}
          onClick={() => setTab("minhas")}
        >
          Minhas solicitações
        </button>
        {donor && (
          <button
            className={tab === "recebidas" ? "active" : ""}
            onClick={() => setTab("recebidas")}
          >
            Solicitações recebidas
          </button>
        )}
      </div>
      {loading ? (
        <Spinner />
      ) : items.length ? (
        <div className="reservation-list">
          {items.map((r) => (
            <article className="reservation-card" key={r.id}>
              <div className="reservation-main">
                <div className="reservation-icon">
                  <PackageSearch />
                </div>
                <div>
                  <div className="reservation-title">
                    <Link href={`/materiais/${r.material.id}`}>
                      {r.material.nome}
                    </Link>
                    <StatusBadge status={r.status} />
                  </div>
                  <p>
                    <MapPin /> {r.material.cidade}/{r.material.estado} ·{" "}
                    {r.quantidade} {r.material.unidade}
                  </p>
                  <small>
                    <Clock3 /> Solicitada em{" "}
                    {new Date(r.criadoEm).toLocaleDateString("pt-BR")}{" "}
                    {tab === "recebidas" && (
                      <>
                        por <b>{r.solicitante.nome}</b>
                      </>
                    )}
                  </small>
                  {r.mensagem && <blockquote>“{r.mensagem}”</blockquote>}
                </div>
              </div>
              <div className="reservation-actions">
                {tab === "recebidas" && r.status === "SOLICITADA" && (
                  <>
                    <button
                      className="button primary small"
                      onClick={() => action(r.id, "aprovar")}
                    >
                      <Check /> Aprovar
                    </button>
                    <button
                      className="button ghost small"
                      onClick={() => action(r.id, "recusar")}
                    >
                      <X /> Recusar
                    </button>
                  </>
                )}
                {tab === "recebidas" && r.status === "APROVADA" && (
                  <>
                    <button
                      className="button primary small"
                      onClick={() => action(r.id, "confirmar-retirada")}
                    >
                      <CheckCheck /> Confirmar retirada
                    </button>
                    <button
                      className="button ghost small"
                      onClick={() => action(r.id, "cancelar")}
                    >
                      <X /> Cancelar reserva
                    </button>
                  </>
                )}
                {tab === "recebidas" && r.status === "RETIRADA" && (
                  <button
                    className="button primary small"
                    onClick={() => action(r.id, "finalizar")}
                  >
                    <CheckCheck /> Finalizar doação
                  </button>
                )}
                {tab === "minhas" && r.status === "SOLICITADA" && (
                  <button
                    className="button ghost small"
                    onClick={() => action(r.id, "cancelar")}
                  >
                    <X /> Cancelar
                  </button>
                )}
                <span className="status-help">{help[r.status]}</span>
              </div>
            </article>
          ))}
        </div>
      ) : (
        <EmptyState
          title={
            tab === "recebidas"
              ? "Nenhuma solicitação recebida"
              : "Você ainda não fez reservas"
          }
          description={
            tab === "recebidas"
              ? "Novos pedidos aparecerão aqui."
              : "Explore os materiais disponíveis e solicite uma reserva."
          }
          action={
            tab === "minhas" ? (
              <Link className="button primary small" href="/materiais">
                Explorar materiais
              </Link>
            ) : undefined
          }
        />
      )}
    </>
  );
}
const help: Record<string, string> = {
  SOLICITADA: "Aguardando decisão",
  APROVADA: "Retirada a combinar",
  RECUSADA: "Solicitação recusada",
  CANCELADA: "Solicitação cancelada",
  RETIRADA: "Material já retirado",
  FINALIZADA: "Doação concluída",
};
