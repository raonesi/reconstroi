"use client";
import { useState } from "react";
import {
  BadgeCheck,
  Building2,
  CalendarDays,
  Mail,
  MapPin,
  Phone,
  Save,
} from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { tipoLabels } from "@/lib/types";
import { useAuth } from "@/components/auth-provider";
import { useToast } from "@/components/toast-provider";
import { PageHeader } from "@/components/ui";
export default function PerfilPage() {
  const { user, refresh } = useAuth();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const toast = useToast();
  if (!user) return null;
  async function submit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setBusy(true);
    setError("");
    const f = Object.fromEntries(new FormData(e.currentTarget));
    try {
      await api.atualizarPerfil(f);
      await refresh();
      toast.success("Perfil atualizado.");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Erro ao salvar.");
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <PageHeader
        eyebrow="Sua conta"
        title="Meu perfil"
        description="Mantenha seus dados atualizados para facilitar as conexões."
      />
      <div className="profile-layout">
        <aside className="profile-card">
          <div className="profile-avatar">
            {user.nome
              .split(" ")
              .map((v) => v[0])
              .slice(0, 2)
              .join("")}
          </div>
          <h2>{user.nome}</h2>
          <span className="verified">
            <BadgeCheck /> Conta ativa
          </span>
          <dl>
            <div>
              <Mail />
              <span>{user.email}</span>
            </div>
            <div>
              <Phone />
              <span>{user.telefone}</span>
            </div>
            <div>
              <MapPin />
              <span>
                {user.cidade
                  ? `${user.cidade}/${user.estado}`
                  : "Localização não informada"}
              </span>
            </div>
            <div>
              <CalendarDays />
              <span>
                Membro desde{" "}
                {new Date(user.criadoEm).toLocaleDateString("pt-BR")}
              </span>
            </div>
          </dl>
          <div className="profile-type">
            <Building2 />
            <span>
              <small>Tipo de conta</small>
              <b>{tipoLabels[user.tipo]}</b>
            </span>
          </div>
        </aside>
        <form className="panel profile-form" onSubmit={submit}>
          <div className="panel-head">
            <div>
              <span className="eyebrow">Dados pessoais</span>
              <h2>Informações de contato</h2>
            </div>
          </div>
          <div className="form-grid">
            <label className="wide">
              Nome ou razão social
              <input name="nome" defaultValue={user.nome} required />
            </label>
            <label>
              E-mail
              <input value={user.email} disabled />
              <small>O e-mail de acesso não pode ser alterado.</small>
            </label>
            <label>
              Telefone
              <input name="telefone" defaultValue={user.telefone} required />
            </label>
            <label>
              Cidade
              <input name="cidade" defaultValue={user.cidade} />
            </label>
            <label>
              Estado
              <input
                name="estado"
                defaultValue={user.estado}
                maxLength={2}
                pattern="[A-Za-z]{2}"
              />
            </label>
            <label>
              CPF/CNPJ
              <input name="documento" defaultValue={user.documento} />
            </label>
          </div>
          {error && <p className="form-error">{error}</p>}
          <div className="form-actions">
            <button className="button primary" disabled={busy}>
              <Save />
              {busy ? "Salvando..." : "Salvar alterações"}
            </button>
          </div>
        </form>
      </div>
    </>
  );
}
