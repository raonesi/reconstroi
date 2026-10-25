import Link from "next/link";
import {
  ArrowRight,
  Building2,
  CheckCircle2,
  HandHeart,
  Leaf,
  PackageCheck,
  Search,
  ShieldCheck,
  UsersRound,
} from "lucide-react";

export default function Home() {
  return (
    <div className="landing">
      <header className="landing-nav">
        <Link href="/" className="brand">
          <span>
            <Building2 />
          </span>
          <b>
            Re<span>Constrói</span>
          </b>
        </Link>
        <nav>
          <a href="#como-funciona">Como funciona</a>
          <a href="#impacto">Impacto</a>
          <Link href="/materiais">Materiais</Link>
        </nav>
        <div>
          <Link className="link-button" href="/login">
            Entrar
          </Link>
          <Link className="button primary small" href="/cadastro">
            Criar conta
          </Link>
        </div>
      </header>
      <main>
        <section className="hero">
          <div className="hero-copy">
            <span className="hero-tag">
              <Leaf /> Construção mais circular começa aqui
            </span>
            <h1>
              O que sobra em uma obra pode <em>construir um novo começo.</em>
            </h1>
            <p>
              Conectamos doadores, construtoras, ONGs e famílias para dar um
              novo destino a materiais de construção com segurança e
              transparência.
            </p>
            <div className="hero-actions">
              <Link className="button primary" href="/cadastro">
                Quero participar <ArrowRight />
              </Link>
              <Link className="button secondary" href="/materiais">
                <Search /> Explorar materiais
              </Link>
            </div>
            <div className="trust-row">
              <span>
                <CheckCircle2 /> Cadastro gratuito
              </span>
              <span>
                <CheckCircle2 /> Impacto mensurável
              </span>
              <span>
                <CheckCircle2 /> Rede colaborativa
              </span>
            </div>
          </div>
          <div className="hero-visual">
            <div className="hero-orbit orbit-one">
              <PackageCheck />
            </div>
            <div className="hero-orbit orbit-two">
              <HandHeart />
            </div>
            <div className="impact-board">
              <div className="board-head">
                <span className="live-dot" />
                Impacto em movimento
              </div>
              <strong>2.480 kg</strong>
              <p>de materiais ganharam um novo destino</p>
              <div className="impact-bars">
                <i />
                <i />
                <i />
                <i />
                <i />
                <i />
                <i />
              </div>
              <footer>
                <div>
                  <Leaf />
                  <span>
                    <b>1,8 t</b> CO₂ evitado
                  </span>
                </div>
                <div>
                  <UsersRound />
                  <span>
                    <b>146</b> famílias
                  </span>
                </div>
              </footer>
            </div>
          </div>
        </section>
        <section className="social-proof">
          <p>Uma rede construída por quem acredita em menos desperdício</p>
          <div>
            <span>Construtoras</span>
            <span>ONGs</span>
            <span>Famílias</span>
            <span>Doadores</span>
          </div>
        </section>
        <section id="como-funciona" className="section">
          <div className="section-title">
            <span>Simples e transparente</span>
            <h2>Três passos para transformar sobra em oportunidade</h2>
            <p>
              A plataforma acompanha todo o processo, do anúncio à retirada.
            </p>
          </div>
          <div className="steps">
            <article>
              <span>01</span>
              <div>
                <PackageCheck />
              </div>
              <h3>Cadastre ou encontre</h3>
              <p>
                Publique materiais disponíveis ou pesquise o que sua obra social
                precisa.
              </p>
            </article>
            <article>
              <span>02</span>
              <div>
                <UsersRound />
              </div>
              <h3>Combine a reserva</h3>
              <p>
                O beneficiário solicita e o doador aprova com total
                rastreabilidade.
              </p>
            </article>
            <article>
              <span>03</span>
              <div>
                <HandHeart />
              </div>
              <h3>Confirme o impacto</h3>
              <p>
                Após a retirada, a doação entra no histórico e nos indicadores
                ambientais.
              </p>
            </article>
          </div>
        </section>
        <section id="impacto" className="impact-section">
          <div>
            <span className="eyebrow light">Impacto que pode ser provado</span>
            <h2>
              Menos resíduos.
              <br />
              Mais obras concluídas.
            </h2>
            <p>
              Cada doação é acompanhada de dados que ajudam organizações e
              empresas a demonstrarem sua contribuição ambiental e social.
            </p>
            <Link href="/relatorios">
              Conhecer os indicadores <ArrowRight />
            </Link>
          </div>
          <div className="impact-numbers">
            <article>
              <strong>13</strong>
              <span>categorias de materiais</span>
            </article>
            <article>
              <strong>100%</strong>
              <span>do fluxo rastreável</span>
            </article>
            <article>
              <strong>4</strong>
              <span>perfis conectados</span>
            </article>
            <article>
              <strong>∞</strong>
              <span>possibilidades de reuso</span>
            </article>
          </div>
        </section>
        <section className="cta">
          <ShieldCheck />
          <h2>Pronto para construir essa mudança?</h2>
          <p>Cadastre-se gratuitamente e faça parte da rede ReConstrói.</p>
          <Link className="button white" href="/cadastro">
            Criar minha conta <ArrowRight />
          </Link>
        </section>
      </main>
      <footer className="landing-footer">
        <div className="brand">
          <span>
            <Building2 />
          </span>
          <b>
            Re<span>Constrói</span>
          </b>
        </div>
        <p>Materiais que constroem futuros.</p>
        <small>© 2026 ReConstrói</small>
      </footer>
    </div>
  );
}
