"use client";
import Link from "next/link";
import { useEffect, useState } from "react";
import { ArrowRight, CalendarCheck, Leaf, PackagePlus } from "lucide-react";
import { api } from "@/lib/api";
import type { Doacao, Material } from "@/lib/types";
import { EmptyState, PageHeader, Spinner, StatusBadge } from "@/components/ui";
export default function DoacoesPage() {
  const [materials, setMaterials] = useState<Material[]>([]);
  const [donations, setDonations] = useState<Doacao[]>([]);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    Promise.all([api.meusMateriais(), api.doacoes()])
      .then(([m, d]) => {
        setMaterials(m);
        setDonations(d);
      })
      .finally(() => setLoading(false));
  }, []);
  if (loading) return <Spinner />;
  return (
    <>
      <PageHeader
        eyebrow="Seu impacto"
        title="Minhas doações"
        description="Gerencie anúncios e acompanhe materiais que já ganharam um novo destino."
        action={
          <Link className="button primary" href="/materiais/novo">
            <PackagePlus /> Novo material
          </Link>
        }
      />
      <section className="donation-summary">
        <article>
          <strong>{materials.length}</strong>
          <span>materiais cadastrados</span>
        </article>
        <article>
          <strong>{donations.length}</strong>
          <span>doações concluídas</span>
        </article>
        <article>
          <strong>{donations.reduce((s, d) => s + d.quantidade, 0)}</strong>
          <span>itens reaproveitados</span>
        </article>
        <article>
          <strong>
            {donations
              .reduce((s, d) => s + Number(d.co2EvitadoKg), 0)
              .toLocaleString("pt-BR")}{" "}
            kg
          </strong>
          <span>CO₂ evitado</span>
        </article>
      </section>
      <div className="tabs static">
        <button className="active">Anúncios ({materials.length})</button>
      </div>
      {materials.length ? (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Material</th>
                <th>Quantidade</th>
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
                  <td>
                    {m.quantidade} {m.unidade}
                  </td>
                  <td>
                    {m.cidade}/{m.estado}
                  </td>
                  <td>
                    <StatusBadge status={m.status} />
                  </td>
                  <td>
                    <Link href={`/materiais/${m.id}`}>
                      Gerenciar <ArrowRight />
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <EmptyState
          title="Você ainda não publicou materiais"
          action={
            <Link className="button primary small" href="/materiais/novo">
              Cadastrar primeiro material
            </Link>
          }
        />
      )}{" "}
      {donations.length > 0 && (
        <section className="section-block">
          <div className="panel-head">
            <div>
              <span className="eyebrow">Histórico</span>
              <h2>Doações concluídas</h2>
            </div>
          </div>
          <div className="history-grid">
            {donations.map((d) => (
              <article key={d.id}>
                <span>
                  <Leaf />
                </span>
                <div>
                  <b>{d.material.nome}</b>
                  <p>
                    {d.quantidade} {d.material.unidade} para{" "}
                    {d.beneficiario.nome}
                  </p>
                  <small>
                    <CalendarCheck />{" "}
                    {new Date(d.concluidaEm).toLocaleDateString("pt-BR")} ·{" "}
                    {d.co2EvitadoKg} kg CO₂ evitado
                  </small>
                </div>
              </article>
            ))}
          </div>
        </section>
      )}
    </>
  );
}
