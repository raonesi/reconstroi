import type {
  AuthResponse,
  Categoria,
  Dashboard,
  Denuncia,
  Doacao,
  Material,
  MaterialInput,
  Notificacao,
  PageResponse,
  Reserva,
  StatusMaterial,
  Usuario,
} from "./types";

const API = process.env.NEXT_PUBLIC_API_URL ?? "/api";
const TOKEN_KEY = "reconstroi_token";

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public fields?: Record<string, string>,
  ) {
    super(message);
  }
}
export const authStorage = {
  get: () =>
    typeof window === "undefined" ? null : localStorage.getItem(TOKEN_KEY),
  set: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = authStorage.get();
  const isForm = init.body instanceof FormData;
  const response = await fetch(`${API}${path}`, {
    ...init,
    headers: {
      ...(isForm ? {} : { "Content-Type": "application/json" }),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init.headers,
    },
  });
  if (!response.ok) {
    let detail = "Não foi possível concluir a operação.";
    let fields: Record<string, string> | undefined;
    try {
      const body = await response.json();
      detail = body.detail || body.message || detail;
      fields = body.campos;
    } catch {}
    if (response.status === 401 && token) {
      authStorage.clear();
      window.dispatchEvent(new Event("reconstroi:unauthorized"));
    }
    throw new ApiError(detail, response.status, fields);
  }
  if (response.status === 204) return undefined as T;
  return response.json();
}

export const api = {
  login: (body: { email: string; senha: string }) =>
    request<AuthResponse>("/usuarios/login", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  cadastro: (body: Record<string, unknown>) =>
    request<AuthResponse>("/usuarios", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  logout: () => request<void>("/usuarios/logout", { method: "POST" }),
  me: () => request<Usuario>("/usuarios/me"),
  atualizarPerfil: (body: Record<string, unknown>) =>
    request<Usuario>("/usuarios/me", {
      method: "PUT",
      body: JSON.stringify(body),
    }),
  categorias: () => request<Categoria[]>("/categorias"),
  materiais: (params: URLSearchParams | string = "") =>
    request<PageResponse<Material>>(`/materiais?${params}`),
  material: (id: number | string) => request<Material>(`/materiais/${id}`),
  meusMateriais: () => request<Material[]>("/materiais/meus"),
  criarMaterial: (body: MaterialInput, fotos: File[]) => {
    const form = new FormData();
    form.append(
      "dados",
      new Blob([JSON.stringify(body)], { type: "application/json" }),
    );
    fotos.forEach((foto) => form.append("fotos", foto));
    return request<Material>("/materiais", { method: "POST", body: form });
  },
  editarMaterial: (id: number, body: MaterialInput) =>
    request<Material>(`/materiais/${id}`, {
      method: "PUT",
      body: JSON.stringify(body),
    }),
  excluirMaterial: (id: number) =>
    request<void>(`/materiais/${id}`, { method: "DELETE" }),
  uploadFoto: (id: number, arquivo: File) => {
    const form = new FormData();
    form.append("arquivo", arquivo);
    return request(`/materiais/${id}/fotos`, { method: "POST", body: form });
  },
  minhasReservas: () => request<Reserva[]>("/reservas"),
  reservasRecebidas: () => request<Reserva[]>("/reservas/recebidas"),
  reservar: (materialId: number, quantidade: number, mensagem: string) =>
    request<Reserva>("/reservas", {
      method: "POST",
      body: JSON.stringify({ materialId, quantidade, mensagem }),
    }),
  acaoReserva: (id: number, acao: string) =>
    request<Reserva>(`/reservas/${id}/${acao}`, { method: "PATCH" }),
  notificacoes: () => request<Notificacao[]>("/notificacoes"),
  lerNotificacao: (id: number) =>
    request<Notificacao>(`/notificacoes/${id}/lida`, { method: "PATCH" }),
  doacoes: () => request<Doacao[]>("/doacoes"),
  dashboard: (publico = false) =>
    request<Dashboard>(`/relatorios/${publico ? "publico" : "dashboard"}`),
  denunciar: (materialId: number, motivo: string) =>
    request<Denuncia>("/denuncias", {
      method: "POST",
      body: JSON.stringify({ materialId, motivo }),
    }),
  adminUsuarios: (page = 0) =>
    request<PageResponse<Usuario>>(`/admin/usuarios?page=${page}&size=20`),
  adminAtivar: (id: number, ativo: boolean) =>
    request<Usuario>(`/admin/usuarios/${id}/ativo`, {
      method: "PATCH",
      body: JSON.stringify({ ativo }),
    }),
  adminDenuncias: () => request<Denuncia[]>("/admin/denuncias"),
  adminDenunciaStatus: (id: number, status: string) =>
    request<Denuncia>(`/admin/denuncias/${id}`, {
      method: "PATCH",
      body: JSON.stringify({ status }),
    }),
  adminMateriais: (page = 0) =>
    request<PageResponse<Material>>(`/admin/materiais?page=${page}&size=20`),
  adminReservas: () => request<Reserva[]>("/admin/reservas"),
  adminDoacoes: () => request<Doacao[]>("/admin/doacoes"),
  adminRemoverMaterial: (id: number, motivo: string) =>
    request<void>(`/admin/materiais/${id}`, {
      method: "DELETE",
      body: JSON.stringify({ motivo }),
    }),
};

export function assetUrl(path?: string) {
  if (!path) return "";
  return path.startsWith("/uploads/") ? `${API}${path}` : path;
}
export function materialQuery(
  filters: Record<string, string | number | undefined>,
) {
  const p = new URLSearchParams();
  Object.entries(filters).forEach(([k, v]) => {
    if (v !== undefined && v !== "") p.set(k, String(v));
  });
  return p;
}
export type { StatusMaterial };
