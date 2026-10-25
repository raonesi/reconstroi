"use client";
import { useEffect, useState } from "react";
import { Filter, Search, X } from "lucide-react";
import { api, materialQuery } from "@/lib/api";
import type { Categoria, Material } from "@/lib/types";
import { MaterialCard } from "@/components/material-card";
import { EmptyState, PageHeader, SkeletonCards } from "@/components/ui";
export default function MateriaisPage() {
  const [items, setItems] = useState<Material[]>([]);
  const [categories, setCategories] = useState<Categoria[]>([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({
    busca: "",
    categoriaId: "",
    cidade: "",
    estado: "",
    conservacao: "",
  });
  const [page, setPage] = useState(0);
  const [pages, setPages] = useState(0);
  useEffect(() => {
    api.categorias().then(setCategories);
  }, []);
  useEffect(() => {
    const timer = setTimeout(
      () =>
        api
          .materiais(materialQuery({ ...filters, page, size: 12 }))
          .then((r) => {
            setItems(r.content);
            setPages(r.totalPages);
          })
          .finally(() => setLoading(false)),
      250,
    );
    return () => clearTimeout(timer);
  }, [filters, page]);
  const set = (key: string, value: string) => {
    setLoading(true);
    setPage(0);
    setFilters((f) => ({ ...f, [key]: value }));
  };
  const clear = () => {
    setLoading(true);
    setFilters({
      busca: "",
      categoriaId: "",
      cidade: "",
      estado: "",
      conservacao: "",
    });
  };
  return (
    <>
      <PageHeader
        eyebrow="Marketplace circular"
        title="Encontre materiais"
        description="Pesquise anúncios disponíveis e solicite o que seu projeto precisa."
      />
      <div className="search-panel">
        <label className="search-wide">
          <Search />
          <input
            value={filters.busca}
            onChange={(e) => set("busca", e.target.value)}
            placeholder="Busque por tijolos, madeira, tinta..."
          />
        </label>
        <label>
          <Filter />
          <select
            value={filters.categoriaId}
            onChange={(e) => set("categoriaId", e.target.value)}
          >
            <option value="">Todas as categorias</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.nome}
              </option>
            ))}
          </select>
        </label>
        <label>
          <input
            value={filters.cidade}
            onChange={(e) => set("cidade", e.target.value)}
            placeholder="Cidade"
          />
        </label>
        <label>
          <select
            value={filters.estado}
            onChange={(e) => set("estado", e.target.value)}
          >
            <option value="">Todo o Brasil</option>
            {[
              "AC",
              "AL",
              "AP",
              "AM",
              "BA",
              "CE",
              "DF",
              "ES",
              "GO",
              "MA",
              "MT",
              "MS",
              "MG",
              "PA",
              "PB",
              "PR",
              "PE",
              "PI",
              "RJ",
              "RN",
              "RS",
              "RO",
              "RR",
              "SC",
              "SP",
              "SE",
              "TO",
            ].map((uf) => (
              <option key={uf}>{uf}</option>
            ))}
          </select>
        </label>
        <label>
          <select
            value={filters.conservacao}
            onChange={(e) => set("conservacao", e.target.value)}
          >
            <option value="">Conservação</option>
            <option value="NOVO">Novo</option>
            <option value="EXCELENTE">Excelente</option>
            <option value="BOM">Bom</option>
            <option value="REGULAR">Regular</option>
          </select>
        </label>
        {Object.values(filters).some(Boolean) && (
          <button className="clear-filters" onClick={clear}>
            <X /> Limpar
          </button>
        )}
      </div>
      {loading ? (
        <SkeletonCards count={6} />
      ) : items.length ? (
        <>
          <div className="results-line">
            <b>{items.length} materiais nesta página</b>
            <span>Ordenados pelos mais recentes</span>
          </div>
          <div className="material-grid">
            {items.map((m) => (
              <MaterialCard key={m.id} material={m} />
            ))}
          </div>
          {pages > 1 && (
            <div className="pagination">
              <button
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
              >
                Anterior
              </button>
              <span>
                Página {page + 1} de {pages}
              </span>
              <button
                disabled={page + 1 >= pages}
                onClick={() => setPage((p) => p + 1)}
              >
                Próxima
              </button>
            </div>
          )}
        </>
      ) : (
        <EmptyState
          title="Nenhum material encontrado"
          description="Tente remover alguns filtros ou buscar por outro termo."
        />
      )}
    </>
  );
}
