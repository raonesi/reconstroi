"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { ImagePlus, Save } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import type { Categoria, Material, MaterialInput } from "@/lib/types";
import { useToast } from "./toast-provider";
export function MaterialForm({ material }: { material?: Material }) {
  const [categories, setCategories] = useState<Categoria[]>([]);
  const [files, setFiles] = useState<File[]>([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const router = useRouter();
  const toast = useToast();
  useEffect(() => {
    api.categorias().then(setCategories);
  }, []);
  async function submit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setBusy(true);
    setError("");
    const f = new FormData(e.currentTarget);
    const body: MaterialInput = {
      nome: String(f.get("nome")),
      descricao: String(f.get("descricao")),
      quantidade: Number(f.get("quantidade")),
      unidade: String(f.get("unidade")),
      estadoConservacao: String(
        f.get("estadoConservacao"),
      ) as MaterialInput["estadoConservacao"],
      categoriaId: Number(f.get("categoriaId")),
      enderecoRetirada: String(f.get("enderecoRetirada")),
      cidade: String(f.get("cidade")),
      estado: String(f.get("estado")),
    };
    try {
      if (!material && files.length === 0) {
        setError("Adicione pelo menos uma foto do material.");
        return;
      }
      let saved: Material;
      if (material) {
        saved = await api.editarMaterial(material.id, body);
        for (const file of files) await api.uploadFoto(saved.id, file);
      } else {
        try {
          saved = await api.criarMaterial(body, files);
        } catch (err) {
          if (
            err instanceof ApiError &&
            err.status === 409 &&
            err.message.includes("mesmo nome") &&
            confirm(`${err.message}\n\nDeseja publicar mesmo assim?`)
          ) {
            saved = await api.criarMaterial(
              { ...body, confirmarDuplicidade: true },
              files,
            );
          } else {
            throw err;
          }
        }
      }
      toast.success(
        material ? "Material atualizado." : "Material publicado com sucesso!",
      );
      router.push(`/materiais/${saved.id}`);
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : "Não foi possível salvar o material.",
      );
    } finally {
      setBusy(false);
    }
  }
  return (
    <form className="material-form" onSubmit={submit}>
      <section className="form-section">
        <div>
          <span>01</span>
          <h2>Sobre o material</h2>
          <p>Conte o que está disponível.</p>
        </div>
        <div className="form-grid">
          <label className="wide">
            Nome do material
            <input
              name="nome"
              defaultValue={material?.nome}
              required
              maxLength={120}
              placeholder="Ex.: Tijolos cerâmicos"
            />
          </label>
          <label>
            Categoria
            <select
              name="categoriaId"
              defaultValue={material?.categoria.id || ""}
              required
            >
              <option value="" disabled>
                Selecione
              </option>
              {categories.map((c) => (
                <option value={c.id} key={c.id}>
                  {c.nome}
                </option>
              ))}
            </select>
          </label>
          <label>
            Estado de conservação
            <select
              name="estadoConservacao"
              defaultValue={material?.estadoConservacao || "BOM"}
            >
              <option value="NOVO">Novo</option>
              <option value="EXCELENTE">Excelente</option>
              <option value="BOM">Bom</option>
              <option value="REGULAR">Regular</option>
            </select>
          </label>
          <label>
            Quantidade
            <input
              name="quantidade"
              type="number"
              min="1"
              defaultValue={material?.quantidade || 1}
              required
            />
          </label>
          <label>
            Unidade
            <select
              name="unidade"
              defaultValue={material?.unidade || "unidades"}
            >
              <option>unidades</option>
              <option>kg</option>
              <option>m²</option>
              <option>m³</option>
              <option>litros</option>
              <option>sacos</option>
              <option>caixas</option>
            </select>
          </label>
          <label className="wide">
            Descrição
            <textarea
              name="descricao"
              rows={5}
              maxLength={2000}
              defaultValue={material?.descricao}
              required
              placeholder="Informe dimensões, características e condições de uso."
            />
          </label>
        </div>
      </section>
      <section className="form-section">
        <div>
          <span>02</span>
          <h2>Local de retirada</h2>
          <p>O endereço é exibido para organizar a coleta.</p>
        </div>
        <div className="form-grid">
          <label className="wide">
            Endereço completo
            <input
              name="enderecoRetirada"
              defaultValue={material?.enderecoRetirada}
              required
              placeholder="Rua, número e complemento"
            />
          </label>
          <label>
            Cidade
            <input name="cidade" defaultValue={material?.cidade} required />
          </label>
          <label>
            Estado
            <input
              name="estado"
              defaultValue={material?.estado}
              pattern="[A-Za-z]{2}"
              maxLength={2}
              required
              placeholder="UF"
            />
          </label>
        </div>
      </section>
      <section className="form-section">
        <div>
          <span>03</span>
          <h2>Fotos</h2>
          <p>Até 5 imagens JPG, PNG ou WebP.</p>
        </div>
        <label className="upload-box">
          <ImagePlus />
          <b>Adicione fotos do material</b>
          <span>
            {files.length
              ? `${files.length} arquivo(s) selecionado(s)`
              : "Clique para selecionar — máximo 5 MB por foto"}
          </span>
          <input
            type="file"
            accept="image/jpeg,image/png,image/webp"
            multiple
            required={!material}
            onChange={(e) =>
              setFiles(Array.from(e.target.files || []).slice(0, 5))
            }
          />
        </label>
      </section>
      {error && <p className="form-error">{error}</p>}
      <div className="form-actions">
        <button
          type="button"
          className="button secondary"
          onClick={() => router.back()}
        >
          Cancelar
        </button>
        <button className="button primary" disabled={busy}>
          <Save />
          {busy
            ? "Salvando..."
            : material
              ? "Salvar alterações"
              : "Publicar material"}
        </button>
      </div>
    </form>
  );
}
