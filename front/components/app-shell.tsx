"use client";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import {
  BarChart3,
  Bell,
  Building2,
  CircleUserRound,
  ClipboardList,
  FilePlus2,
  HandHeart,
  LayoutDashboard,
  LogOut,
  Menu,
  PackageSearch,
  ShieldCheck,
  X,
} from "lucide-react";
import { useAuth } from "./auth-provider";
import { Spinner } from "./ui";
import { api } from "@/lib/api";
import { pode, type Notificacao, type PermissaoSistema } from "@/lib/types";

type NavItem = {
  href: string;
  label: string;
  icon: typeof LayoutDashboard;
  permission?: PermissaoSistema;
};
const items: NavItem[] = [
  { href: "/dashboard", label: "Visão geral", icon: LayoutDashboard },
  { href: "/materiais", label: "Explorar materiais", icon: PackageSearch },
  {
    href: "/materiais/novo",
    label: "Cadastrar material",
    icon: FilePlus2,
    permission: "MATERIAL_CADASTRAR",
  },
  {
    href: "/minhas-doacoes",
    label: "Minhas doações",
    icon: HandHeart,
    permission: "MATERIAL_CADASTRAR",
  },
  { href: "/minhas-reservas", label: "Reservas", icon: ClipboardList },
  { href: "/relatorios", label: "Relatórios", icon: BarChart3 },
  { href: "/perfil", label: "Meu perfil", icon: CircleUserRound },
  {
    href: "/administracao",
    label: "Administração",
    icon: ShieldCheck,
    permission: "USUARIO_GERENCIAR",
  },
];
export function AppShell({ children }: { children: React.ReactNode }) {
  const { user, loading, logout } = useAuth();
  const router = useRouter();
  const path = usePathname();
  const [open, setOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [notifications, setNotifications] = useState<Notificacao[]>([]);
  useEffect(() => {
    if (!loading && !user)
      router.replace(`/login?redirect=${encodeURIComponent(path)}`);
  }, [loading, user, router, path]);
  useEffect(() => {
    if (!user) return;
    const loadNotifications = () =>
      api
        .notificacoes()
        .then(setNotifications)
        .catch(() => undefined);
    loadNotifications();
    const interval = window.setInterval(loadNotifications, 30000);
    return () => window.clearInterval(interval);
  }, [user]);
  if (loading || !user)
    return (
      <div className="screen-center">
        <Spinner label="Preparando seu espaço..." />
      </div>
    );
  async function readNotification(notification: Notificacao) {
    if (!notification.lida) {
      const updated = await api.lerNotificacao(notification.id);
      setNotifications((items) =>
        items.map((item) => (item.id === updated.id ? updated : item)),
      );
    }
  }
  return (
    <div className="app-shell">
      <aside className={open ? "sidebar open" : "sidebar"}>
        <div className="brand">
          <span>
            <Building2 />
          </span>
          <b>
            Re<span>Constrói</span>
          </b>
          <button className="mobile-close" onClick={() => setOpen(false)}>
            <X />
          </button>
        </div>
        <nav>
          {items
            .filter((i) => !i.permission || pode(user, i.permission))
            .map(({ href, label, icon: Icon }) => (
              <Link
                key={href}
                href={href}
                onClick={() => setOpen(false)}
                className={
                  path === href ||
                  (href !== "/dashboard" && path.startsWith(href + "/"))
                    ? "active"
                    : ""
                }
              >
                <Icon />
                <span>{label}</span>
              </Link>
            ))}
        </nav>
        <div className="sidebar-impact">
          <span>Impacto coletivo</span>
          <strong>Construção sem desperdício</strong>
          <p>Cada material reaproveitado transforma duas histórias.</p>
        </div>
        <button
          className="logout"
          onClick={() => {
            logout();
            router.replace("/login");
          }}
        >
          <LogOut /> Sair
        </button>
      </aside>
      {open && (
        <button
          className="backdrop"
          aria-label="Fechar menu"
          onClick={() => setOpen(false)}
        />
      )}
      <div className="app-main">
        <header className="topbar">
          <button className="menu-button" onClick={() => setOpen(true)}>
            <Menu />
          </button>
          <div className="top-context">
            <span>ReConstrói</span>
            <b>{items.find((i) => path === i.href)?.label ?? "Plataforma"}</b>
          </div>
          <div className="notification-center">
            <button
              className="notification-button"
              aria-label="Notificações"
              onClick={() => setNotificationsOpen((value) => !value)}
            >
              <Bell />
              {notifications.some((item) => !item.lida) && (
                <span>{notifications.filter((item) => !item.lida).length}</span>
              )}
            </button>
            {notificationsOpen && (
              <div className="notification-popover">
                <header>
                  <b>Notificações</b>
                  <small>
                    {notifications.filter((item) => !item.lida).length} nova(s)
                  </small>
                </header>
                {notifications.length ? (
                  notifications.slice(0, 8).map((notification) => (
                    <button
                      key={notification.id}
                      className={notification.lida ? "read" : ""}
                      onClick={() => readNotification(notification)}
                    >
                      <b>{notification.titulo}</b>
                      <span>{notification.mensagem}</span>
                      <small>
                        {new Date(notification.criadaEm).toLocaleString(
                          "pt-BR",
                        )}
                      </small>
                    </button>
                  ))
                ) : (
                  <p>Nenhuma notificação por enquanto.</p>
                )}
              </div>
            )}
          </div>
          <div className="user-pill">
            <span>
              {user.nome
                .split(" ")
                .map((v) => v[0])
                .slice(0, 2)
                .join("")}
            </span>
            <div>
              <b>{user.nome}</b>
              <small>{user.nivelAcesso.toLowerCase()}</small>
            </div>
          </div>
        </header>
        <main className="content">{children}</main>
      </div>
    </div>
  );
}
