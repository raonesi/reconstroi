import type { LucideIcon } from "lucide-react";
import { Inbox, LoaderCircle } from "lucide-react";
import {
  statusMaterialLabels,
  statusReservaLabels,
  type StatusMaterial,
  type StatusReserva,
} from "@/lib/types";

export function Spinner({ label = "Carregando..." }: { label?: string }) {
  return (
    <div className="loading">
      <LoaderCircle className="spin" />
      <span>{label}</span>
    </div>
  );
}
export function EmptyState({
  title = "Nada por aqui",
  description = "Os registros aparecerão assim que forem criados.",
  action,
}: {
  title?: string;
  description?: string;
  action?: React.ReactNode;
}) {
  return (
    <div className="empty">
      <span>
        <Inbox />
      </span>
      <h3>{title}</h3>
      <p>{description}</p>
      {action}
    </div>
  );
}
export function PageHeader({
  eyebrow,
  title,
  description,
  action,
}: {
  eyebrow?: string;
  title: string;
  description?: string;
  action?: React.ReactNode;
}) {
  return (
    <header className="page-header">
      <div>
        {eyebrow && <span className="eyebrow">{eyebrow}</span>}
        <h1>{title}</h1>
        {description && <p>{description}</p>}
      </div>
      {action}
    </header>
  );
}
export function MetricCard({
  label,
  value,
  detail,
  icon: Icon,
  tone = "green",
}: {
  label: string;
  value: string | number;
  detail: string;
  icon: LucideIcon;
  tone?: "green" | "blue" | "orange" | "purple";
}) {
  return (
    <article className="metric-card">
      <div className={`metric-icon ${tone}`}>
        <Icon />
      </div>
      <span>{label}</span>
      <strong>{value}</strong>
      <small>{detail}</small>
    </article>
  );
}
export function StatusBadge({
  status,
}: {
  status: StatusMaterial | StatusReserva;
}) {
  const label =
    status in statusMaterialLabels
      ? statusMaterialLabels[status as StatusMaterial]
      : statusReservaLabels[status as StatusReserva];
  return (
    <span className={`status status-${status.toLowerCase()}`}>{label}</span>
  );
}
export function SkeletonCards({ count = 3 }: { count?: number }) {
  return (
    <div className="material-grid">
      {Array.from({ length: count }).map((_, i) => (
        <div className="skeleton-card" key={i}>
          <div />
          <span />
          <span />
          <span />
        </div>
      ))}
    </div>
  );
}
