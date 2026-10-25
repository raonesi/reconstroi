"use client";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { ArrowRight, Building2, Eye, EyeOff, Leaf } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { useAuth } from "@/components/auth-provider";
import { useToast } from "@/components/toast-provider";

/** Só aceita destinos do próprio site; qualquer outra origem volta ao painel. */
function safeRedirect(value: string | null) {
  if (!value) return "/dashboard";
  try {
    const url = new URL(value, window.location.origin);
    if (url.origin !== window.location.origin) return "/dashboard";
    return url.pathname + url.search + url.hash;
  } catch {
    return "/dashboard";
  }
}

export default function LoginPage() {
  const [show, setShow] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const router = useRouter();
  const { establish } = useAuth();
  const toast = useToast();
  async function submit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setBusy(true);
    setError("");
    const form = new FormData(e.currentTarget);
    try {
      const auth = await api.login({
        email: String(form.get("email")),
        senha: String(form.get("senha")),
      });
      establish(auth);
      toast.success("Que bom ter você de volta!");
      router.replace(
        safeRedirect(new URLSearchParams(window.location.search).get("redirect")),
      );
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Não foi possível entrar.",
      );
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="auth-page">
      <aside className="auth-story">
        <Link href="/" className="brand light-brand">
          <span>
            <Building2 />
          </span>
          <b>ReConstrói</b>
        </Link>
        <div>
          <span className="hero-tag dark">
            <Leaf /> Reaproveitar também é construir
          </span>
          <h1>Boas ideias merecem materiais para sair do papel.</h1>
          <p>
            Acesse sua conta e continue conectando recursos a quem transforma
            comunidades.
          </p>
        </div>
        <blockquote>
          “O material que não servia mais para nossa obra ajudou uma família a
          terminar sua casa.”<footer>— Rede de doadores ReConstrói</footer>
        </blockquote>
      </aside>
      <main className="auth-main">
        <div className="auth-card">
          <span className="eyebrow">Bem-vindo de volta</span>
          <h2>Acesse sua conta</h2>
          <p>Informe seus dados para continuar.</p>
          <form onSubmit={submit} className="form-stack">
            <label>
              E-mail
              <input
                name="email"
                type="email"
                autoComplete="email"
                placeholder="voce@exemplo.com"
                required
              />
            </label>
            <label>
              Senha
              <div className="password-field">
                <input
                  name="senha"
                  type={show ? "text" : "password"}
                  autoComplete="current-password"
                  placeholder="Sua senha"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShow(!show)}
                  aria-label="Mostrar senha"
                >
                  {show ? <EyeOff /> : <Eye />}
                </button>
              </div>
            </label>
            <div className="form-line">
              <label className="check">
                <input type="checkbox" /> Lembrar de mim
              </label>
            </div>
            {error && <p className="form-error">{error}</p>}
            <button className="button primary full" disabled={busy}>
              {busy ? (
                "Entrando..."
              ) : (
                <>
                  Entrar <ArrowRight />
                </>
              )}
            </button>
          </form>
          <div className="auth-switch">
            Ainda não possui conta?{" "}
            <Link href="/cadastro">Cadastre-se grátis</Link>
          </div>
        </div>
      </main>
    </div>
  );
}
