"use client";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import {
  ArrowLeft,
  ArrowRight,
  Building2,
  HandHeart,
  Home,
  Landmark,
  UserRound,
} from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { useAuth } from "@/components/auth-provider";
import { useToast } from "@/components/toast-provider";
import type { TipoUsuario } from "@/lib/types";

const roles = [
  {
    id: "DOADOR_PESSOA_FISICA",
    label: "Pessoa física",
    desc: "Quero doar sobras de obra",
    icon: UserRound,
  },
  {
    id: "DOADOR_CONSTRUTORA",
    label: "Construtora",
    desc: "Quero destinar materiais",
    icon: Building2,
  },
  {
    id: "BENEFICIARIO_ONG",
    label: "ONG",
    desc: "Busco materiais para projetos",
    icon: Landmark,
  },
  {
    id: "BENEFICIARIO_FAMILIA",
    label: "Família",
    desc: "Busco materiais para minha casa",
    icon: Home,
  },
] as const;
export default function CadastroPage() {
  const [role, setRole] = useState<TipoUsuario>("DOADOR_PESSOA_FISICA");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const router = useRouter();
  const { establish } = useAuth();
  const toast = useToast();
  async function submit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setBusy(true);
    setError("");
    const f = Object.fromEntries(new FormData(e.currentTarget));
    if (f.senha !== f.confirmar) {
      setError("As senhas não coincidem.");
      setBusy(false);
      return;
    }
    try {
      const auth = await api.cadastro({ ...f, tipo: role });
      establish(auth);
      toast.success("Conta criada com sucesso!");
      router.replace("/dashboard");
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : "Não foi possível criar a conta.",
      );
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="signup-page">
      <header>
        <Link href="/" className="brand">
          <span>
            <Building2 />
          </span>
          <b>
            Re<span>Constrói</span>
          </b>
        </Link>
        <Link href="/login">Já tenho uma conta</Link>
      </header>
      <main className="signup-card">
        <Link href="/" className="back-link">
          <ArrowLeft /> Voltar
        </Link>
        <div className="section-title compact">
          <span>Faça parte da rede</span>
          <h1>Como você quer transformar materiais?</h1>
          <p>
            Escolha seu perfil. As permissões serão configuradas
            automaticamente.
          </p>
        </div>
        <div className="role-grid">
          {roles.map(({ id, label, desc, icon: Icon }) => (
            <button
              type="button"
              key={id}
              onClick={() => setRole(id)}
              className={role === id ? "role-card selected" : "role-card"}
            >
              <Icon />
              <b>{label}</b>
              <span>{desc}</span>
            </button>
          ))}
        </div>
        <form onSubmit={submit} className="form-grid">
          <label className="wide">
            Nome ou razão social
            <input
              name="nome"
              required
              maxLength={120}
              placeholder="Como devemos chamar você?"
            />
          </label>
          <label>
            E-mail
            <input
              name="email"
              type="email"
              required
              placeholder="voce@exemplo.com"
            />
          </label>
          <label>
            Telefone
            <input
              name="telefone"
              required
              maxLength={30}
              placeholder="(00) 00000-0000"
            />
          </label>
          <label>
            Cidade
            <input name="cidade" maxLength={120} placeholder="Sua cidade" />
          </label>
          <label>
            Estado
            <input
              name="estado"
              maxLength={2}
              pattern="[A-Za-z]{2}"
              placeholder="UF"
            />
          </label>
          <label>
            CPF/CNPJ (opcional)
            <input
              name="documento"
              maxLength={30}
              placeholder="Somente para identificação"
            />
          </label>
          <span />
          <label>
            Senha
            <input
              name="senha"
              type="password"
              minLength={8}
              required
              placeholder="Mínimo de 8 caracteres"
            />
          </label>
          <label>
            Confirmar senha
            <input
              name="confirmar"
              type="password"
              minLength={8}
              required
              placeholder="Repita a senha"
            />
          </label>
          <label className="check wide">
            <input type="checkbox" required /> Concordo com os termos de uso e a
            política de privacidade.
          </label>
          {error && <p className="form-error wide">{error}</p>}
          <button className="button primary wide" disabled={busy}>
            {busy ? (
              "Criando conta..."
            ) : (
              <>
                Criar minha conta <ArrowRight />
              </>
            )}
          </button>
        </form>
        <div className="signup-note">
          <HandHeart /> Seu cadastro é gratuito e leva menos de dois minutos.
        </div>
      </main>
    </div>
  );
}
