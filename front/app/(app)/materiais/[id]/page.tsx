/* As imagens são arquivos enviados em runtime e servidos pela API. */
/* eslint-disable @next/next/no-img-element */
"use client";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import {
  ArrowLeft,
  CalendarDays,
  Edit3,
  Flag,
  MapPin,
  PackageOpen,
  ShieldCheck,
  Trash2,
  UserRound,
  X,
} from "lucide-react";
import { api, ApiError, assetUrl } from "@/lib/api";
import { conservacaoLabels, isBeneficiario, type Material } from "@/lib/types";
import { useAuth } from "@/components/auth-provider";
import { useToast } from "@/components/toast-provider";
import { Spinner, StatusBadge } from "@/components/ui";
export default function MaterialDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [material, setMaterial] = useState<Material>();
  const [selected, setSelected] = useState(0);
  const [reserve, setReserve] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const { user } = useAuth();
  const toast = useToast();
  const router = useRouter();
  useEffect(() => {
    api
      .material(id)
      .then(setMaterial)
      .catch(() => router.replace("/materiais"));
  }, [id, router]);
  if (!material) return <Spinner />;
  const currentMaterial = material;
  const owner = user?.id === material.proprietario.id || user?.tipo === "ADMIN";
  async function reservar(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setBusy(true);
    setError("");
    const f = new FormData(e.currentTarget);
    try {
      await api.reservar(
        currentMaterial.id,
        Number(f.get("quantidade")),
        String(f.get("mensagem")),
      );
      toast.success("Solicitação enviada ao doador.");
      setReserve(false);
      router.push("/minhas-reservas");
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Não foi possível reservar.",
      );
    } finally {
      setBusy(false);
    }
  }
  async function remove() {
    if (!confirm("Excluir este anúncio? Esta ação não pode ser desfeita."))
      return;
    try {
      await api.excluirMaterial(currentMaterial.id);
      toast.success("Material excluído.");
      router.replace("/minhas-doacoes");
    } catch (err) {
      toast.error(
        err instanceof ApiError ? err.message : "Não foi possível excluir.",
      );
    }
  }
  async function report() {
    const motivo = prompt("Descreva o motivo da denúncia:");
    if (!motivo) return;
    try {
      await api.denunciar(currentMaterial.id, motivo);
      toast.success("Denúncia enviada para análise.");
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Erro ao denunciar.");
    }
  }
  return (
    <>
      <Link className="back-link" href="/materiais">
        <ArrowLeft /> Voltar aos materiais
      </Link>
      <div className="detail-layout">
        <section className="gallery">
          <div className="main-photo">
            {material.fotos.length ? (
              <img
                src={assetUrl(material.fotos[selected].url)}
                alt={material.nome}
              />
            ) : (
              <div className="image-placeholder large">
                <PackageOpen />
                <span>Sem fotos cadastradas</span>
              </div>
            )}
            <StatusBadge status={material.status} />
          </div>
          {material.fotos.length > 1 && (
            <div className="thumbnails">
              {material.fotos.map((f, i) => (
                <button
                  className={i === selected ? "active" : ""}
                  key={f.id}
                  onClick={() => setSelected(i)}
                >
                  <img src={assetUrl(f.url)} alt="" />
                </button>
              ))}
            </div>
          )}
        </section>
        <section className="detail-info">
          <span className="category-chip">{material.categoria.nome}</span>
          <h1>{material.nome}</h1>
          <p className="detail-description">{material.descricao}</p>
          <div className="detail-facts">
            <article>
              <PackageOpen />
              <span>
                Quantidade
                <strong>
                  {material.quantidade} {material.unidade}
                </strong>
              </span>
            </article>
            <article>
              <ShieldCheck />
              <span>
                Conservação
                <strong>{conservacaoLabels[material.estadoConservacao]}</strong>
              </span>
            </article>
            <article>
              <MapPin />
              <span>
                Localização
                <strong>
                  {material.cidade}/{material.estado}
                </strong>
              </span>
            </article>
            <article>
              <CalendarDays />
              <span>
                Publicado
                <strong>
                  {new Date(material.dataCadastro).toLocaleDateString("pt-BR")}
                </strong>
              </span>
            </article>
          </div>
          <div className="owner-card">
            <span>
              <UserRound />
            </span>
            <div>
              <small>Disponibilizado por</small>
              <b>{material.proprietario.nome}</b>
            </div>
          </div>
          <div className="pickup">
            <b>Endereço de retirada</b>
            <p>
              <MapPin /> {material.enderecoRetirada}
            </p>
            <small>Combine o horário após a aprovação da reserva.</small>
          </div>
          {owner ? (
            <div className="detail-actions">
              <Link
                className="button primary"
                href={`/materiais/${material.id}/editar`}
              >
                <Edit3 /> Editar anúncio
              </Link>
              <button className="button danger" onClick={remove}>
                <Trash2 /> Excluir
              </button>
            </div>
          ) : isBeneficiario(user?.tipo) && material.status === "DISPONIVEL" ? (
            <button
              className="button primary full"
              onClick={() => setReserve(true)}
            >
              Solicitar reserva
            </button>
          ) : null}
          {!owner && (
            <button className="report-link" onClick={report}>
              <Flag /> Denunciar anúncio
            </button>
          )}
        </section>
      </div>
      {reserve && (
        <div className="modal-backdrop">
          <div className="modal">
            <button className="modal-close" onClick={() => setReserve(false)}>
              <X />
            </button>
            <span className="eyebrow">Solicitação de reserva</span>
            <h2>{material.nome}</h2>
            <p>Informe quanto precisa e deixe uma mensagem para o doador.</p>
            <form className="form-stack" onSubmit={reservar}>
              <label>
                Quantidade (máximo {material.quantidade})
                <input
                  name="quantidade"
                  type="number"
                  min="1"
                  max={material.quantidade}
                  defaultValue={1}
                  required
                />
              </label>
              <label>
                Mensagem
                <textarea
                  name="mensagem"
                  maxLength={500}
                  rows={4}
                  placeholder="Conte brevemente sobre o uso do material."
                />
              </label>
              {error && <p className="form-error">{error}</p>}
              <button className="button primary full" disabled={busy}>
                {busy ? "Enviando..." : "Enviar solicitação"}
              </button>
            </form>
          </div>
        </div>
      )}
    </>
  );
}
