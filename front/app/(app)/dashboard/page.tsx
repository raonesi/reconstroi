"use client";
import Link from "next/link";
import { useEffect, useState } from "react";
import {
  ArrowRight,
  Boxes,
  CalendarCheck2,
  CloudCog,
  HandHeart,
  PackageCheck,
  Plus,
  UsersRound,
} from "lucide-react";
import { api } from "@/lib/api";
import type { Dashboard, Material } from "@/lib/types";
import { isDoador } from "@/lib/types";
import { useAuth } from "@/components/auth-provider";
import { MetricCard, Spinner } from "@/components/ui";
import { MaterialCard } from "@/components/material-card";
import { MonthlyChart } from "@/components/charts";
export default function DashboardPage() {
  const { user } = useAuth();
  const [data, setData] = useState<Dashboard>();
  const [materials, setMaterials] = useState<Material[]>([]);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    Promise.all([api.dashboard(), api.materiais("size=3")])
      .then(([d, m]) => {
        setData(d);
        setMaterials(m.content);
      })
      .finally(() => setLoading(false));
  }, []);
  if (loading || !data) return <Spinner />;
  return (
    <>
      <PageWelcome name={user?.nome || ""} donor={isDoador(user?.tipo)} />
      <section className="metrics-grid">
        <MetricCard
          label="Materiais disponíveis"
          value={data.materiaisDisponiveis}
          detail="anúncios ativos"
          icon={Boxes}
        />
        <MetricCard
          label="Reservas"
          value={data.reservas}
          detail="solicitações na rede"
          icon={CalendarCheck2}
          tone="blue"
        />
        <MetricCard
          label="Doações concluídas"
          value={data.doacoesConcluidas}
          detail="novos destinos"
          icon={HandHeart}
          tone="orange"
        />
        <MetricCard
          label="CO₂ evitado"
          value={`${Number(data.co2EvitadoKg).toLocaleString("pt-BR")} kg`}
          detail="estimativa ambiental"
          icon={CloudCog}
          tone="purple"
        />
      </section>
      <section className="dashboard-grid">
        <article className="panel chart-panel">
          <div className="panel-head">
            <div>
              <span className="eyebrow">Evolução</span>
              <h2>Doações concluídas</h2>
            </div>
            <Link href="/relatorios">
              Ver relatório <ArrowRight />
            </Link>
          </div>
          <div className="chart-box">
            {Object.keys(data.doacoesPorMes).length ? (
              <MonthlyChart values={data.doacoesPorMes} />
            ) : (
              <div className="chart-empty">
                <PackageCheck />
                <p>As doações finalizadas aparecerão neste gráfico.</p>
              </div>
            )}
          </div>
        </article>
        <article className="panel network-panel">
          <span className="eyebrow">Nossa rede</span>
          <h2>Pessoas transformando</h2>
          <strong>{data.usuarios}</strong>
          <p>usuários ativos conectados</p>
          <div className="network-list">
            <span>
              <i /> {data.ongs} ONGs
            </span>
            <span>
              <i /> {data.construtoras} construtoras
            </span>
            <span>
              <i /> {data.familias} famílias
            </span>
          </div>
          <UsersRound className="network-art" />
        </article>
      </section>
      <section className="section-block">
        <div className="panel-head">
          <div>
            <span className="eyebrow">Disponíveis agora</span>
            <h2>Materiais que podem ganhar um novo destino</h2>
          </div>
          <Link href="/materiais">
            Explorar todos <ArrowRight />
          </Link>
        </div>
        <div className="material-grid">
          {materials.map((m) => (
            <MaterialCard key={m.id} material={m} />
          ))}
        </div>
      </section>
    </>
  );
}
function PageWelcome({ name, donor }: { name: string; donor: boolean }) {
  const first = name.split(" ")[0];
  return (
    <div className="welcome">
      <div>
        <span className="eyebrow">Visão geral da sua rede</span>
        <h1>
          Olá, {first}! <span>👋</span>
        </h1>
        <p>
          {donor
            ? "Seus materiais podem ser o começo de uma nova história."
            : "Encontre recursos para transformar seu projeto em realidade."}
        </p>
      </div>
      <Link
        className="button primary"
        href={donor ? "/materiais/novo" : "/materiais"}
      >
        {donor ? (
          <>
            <Plus /> Cadastrar material
          </>
        ) : (
          <>
            Explorar materiais <ArrowRight />
          </>
        )}
      </Link>
    </div>
  );
}
