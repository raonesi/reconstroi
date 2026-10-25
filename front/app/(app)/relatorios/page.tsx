"use client";
import { useEffect, useState } from "react";
import {
  Boxes,
  Building2,
  Download,
  HandHeart,
  Leaf,
  Recycle,
  UsersRound,
} from "lucide-react";
import { api } from "@/lib/api";
import type { Dashboard } from "@/lib/types";
import { CategoryChart, MonthlyChart } from "@/components/charts";
import { MetricCard, PageHeader, Spinner } from "@/components/ui";
export default function RelatoriosPage() {
  const [data, setData] = useState<Dashboard>();
  useEffect(() => {
    api.dashboard().then(setData);
  }, []);
  if (!data) return <Spinner />;
  const report = data;
  function exportCsv() {
    const rows = [
      ["Indicador", "Valor"],
      ["Materiais cadastrados", report.materiaisCadastrados],
      ["Quantidade reaproveitada", report.quantidadeReaproveitada],
      ["Doações concluídas", report.doacoesConcluidas],
      ["CO2 evitado kg", report.co2EvitadoKg],
    ];
    const blob = new Blob([rows.map((r) => r.join(";")).join("\n")], {
      type: "text/csv;charset=utf-8",
    });
    const a = document.createElement("a");
    a.href = URL.createObjectURL(blob);
    a.download = "relatorio-reconstroi.csv";
    a.click();
    URL.revokeObjectURL(a.href);
  }
  return (
    <>
      <PageHeader
        eyebrow="Dados de impacto"
        title="Relatórios"
        description="Indicadores sociais e ambientais consolidados da rede ReConstrói."
        action={
          <button className="button secondary" onClick={exportCsv}>
            <Download /> Exportar CSV
          </button>
        }
      />
      <section className="metrics-grid report-metrics">
        <MetricCard
          label="Materiais cadastrados"
          value={data.materiaisCadastrados}
          detail="total na plataforma"
          icon={Boxes}
        />
        <MetricCard
          label="Quantidade reaproveitada"
          value={data.quantidadeReaproveitada}
          detail="itens e unidades"
          icon={Recycle}
          tone="blue"
        />
        <MetricCard
          label="Doações concluídas"
          value={data.doacoesConcluidas}
          detail="ciclos finalizados"
          icon={HandHeart}
          tone="orange"
        />
        <MetricCard
          label="CO₂ evitado"
          value={`${Number(data.co2EvitadoKg).toLocaleString("pt-BR")} kg`}
          detail="estimativa acumulada"
          icon={Leaf}
          tone="purple"
        />
      </section>
      <div className="reports-grid">
        <article className="panel chart-panel">
          <div className="panel-head">
            <div>
              <span className="eyebrow">Linha do tempo</span>
              <h2>Doações por mês</h2>
            </div>
          </div>
          <div className="chart-box">
            {Object.keys(data.doacoesPorMes).length ? (
              <MonthlyChart values={data.doacoesPorMes} />
            ) : (
              <p className="chart-message">
                Finalize doações para gerar a série histórica.
              </p>
            )}
          </div>
        </article>
        <article className="panel chart-panel">
          <div className="panel-head">
            <div>
              <span className="eyebrow">Distribuição</span>
              <h2>Categorias mais utilizadas</h2>
            </div>
          </div>
          <div className="chart-box">
            {Object.keys(data.categoriasMaisUtilizadas).length ? (
              <CategoryChart values={data.categoriasMaisUtilizadas} />
            ) : (
              <p className="chart-message">
                Categorias aparecerão após os primeiros cadastros.
              </p>
            )}
          </div>
        </article>
      </div>
      <div className="reports-grid bottom">
        <article className="panel ranking">
          <span className="eyebrow">Ranking</span>
          <h2>Materiais mais doados</h2>
          {data.materiaisMaisDoados.length ? (
            data.materiaisMaisDoados.map((m, i) => (
              <div key={m.nome}>
                <span>{String(i + 1).padStart(2, "0")}</span>
                <b>{m.nome}</b>
                <strong>{m.quantidade}</strong>
              </div>
            ))
          ) : (
            <p className="chart-message">Sem doações finalizadas ainda.</p>
          )}
        </article>
        <article className="panel audience">
          <span className="eyebrow">Composição da rede</span>
          <h2>Quem está construindo junto</h2>
          <div>
            <span>
              <UsersRound />
            </span>
            <p>
              <strong>{data.familias}</strong> famílias
            </p>
          </div>
          <div>
            <span>
              <Building2 />
            </span>
            <p>
              <strong>{data.construtoras}</strong> construtoras
            </p>
          </div>
          <div>
            <span>
              <HandHeart />
            </span>
            <p>
              <strong>{data.ongs}</strong> ONGs
            </p>
          </div>
        </article>
      </div>
      <aside className="method-note">
        <Leaf />
        <div>
          <b>Como calculamos o impacto ambiental?</b>
          <p>
            A estimativa atual considera um fator médio de 2,5 kg de CO₂ evitado
            por unidade reaproveitada. Para relatórios regulatórios, configure
            fatores específicos por categoria.
          </p>
        </div>
      </aside>
    </>
  );
}
