export type TipoUsuario =
  | "DOADOR_PESSOA_FISICA"
  | "DOADOR_CONSTRUTORA"
  | "BENEFICIARIO_ONG"
  | "BENEFICIARIO_FAMILIA"
  | "ADMIN";
export type EstadoConservacao = "NOVO" | "EXCELENTE" | "BOM" | "REGULAR";
export type StatusMaterial =
  "DISPONIVEL" | "RESERVADO" | "RETIRADO" | "FINALIZADO";
export type StatusReserva =
  | "SOLICITADA"
  | "APROVADA"
  | "RECUSADA"
  | "CANCELADA"
  | "RETIRADA"
  | "FINALIZADA";
export type NivelAcesso = "DOADOR" | "BENEFICIARIO" | "ADMINISTRADOR";
export type PermissaoSistema =
  | "MATERIAL_CONSULTAR"
  | "MATERIAL_CADASTRAR"
  | "MATERIAL_EDITAR_PROPRIO"
  | "MATERIAL_EDITAR_TODOS"
  | "MATERIAL_REMOVER_PROPRIO"
  | "MATERIAL_REMOVER_TODOS"
  | "RESERVA_SOLICITAR"
  | "RESERVA_CANCELAR_PROPRIA"
  | "RESERVA_GERENCIAR_RECEBIDA"
  | "RESERVA_VISUALIZAR_TODAS"
  | "DOACAO_VISUALIZAR_PROPRIAS"
  | "DOACAO_VISUALIZAR_TODAS"
  | "RELATORIO_VISUALIZAR"
  | "USUARIO_GERENCIAR"
  | "DENUNCIA_GERENCIAR"
  | "HISTORICO_VISUALIZAR_TODOS";

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  telefone: string;
  tipo: TipoUsuario;
  nivelAcesso: NivelAcesso;
  permissoes: PermissaoSistema[];
  ativo: boolean;
  cidade?: string;
  estado?: string;
  documento?: string;
  criadoEm: string;
}
export interface AuthResponse {
  usuario: Usuario;
  token: string;
  expiraEm: string;
}
export interface Categoria {
  id: number;
  nome: string;
  slug: string;
}
export interface Foto {
  id: number;
  url: string;
  nomeOriginal: string;
  contentType: string;
}
export interface UsuarioResumo {
  id: number;
  nome: string;
  tipo: TipoUsuario;
}
export interface MaterialResumo {
  id: number;
  nome: string;
  unidade: string;
  cidade: string;
  estado: string;
  status: StatusMaterial;
  proprietario: UsuarioResumo;
}
export interface Material extends MaterialResumo {
  descricao: string;
  quantidade: number;
  estadoConservacao: EstadoConservacao;
  categoria: Categoria;
  enderecoRetirada: string;
  fotos: Foto[];
  dataCadastro: string;
  atualizadoEm: string;
}
export interface MaterialInput {
  nome: string;
  descricao: string;
  quantidade: number;
  unidade: string;
  estadoConservacao: EstadoConservacao;
  categoriaId: number;
  enderecoRetirada: string;
  cidade: string;
  estado: string;
  confirmarDuplicidade?: boolean;
}
export interface Reserva {
  id: number;
  material: MaterialResumo;
  solicitante: UsuarioResumo;
  quantidade: number;
  status: StatusReserva;
  mensagem?: string;
  criadoEm: string;
  atualizadoEm: string;
}
export interface Doacao {
  id: number;
  material: MaterialResumo;
  doador: UsuarioResumo;
  beneficiario: UsuarioResumo;
  quantidade: number;
  concluidaEm: string;
  pesoEstimadoKg: number;
  residuosEvitadosKg: number;
  co2EvitadoKg: number;
}
export interface Notificacao {
  id: number;
  titulo: string;
  mensagem: string;
  lida: boolean;
  criadaEm: string;
}
export interface Dashboard {
  materiaisCadastrados: number;
  materiaisDisponiveis: number;
  materiaisReutilizados: number;
  reservas: number;
  doacoesConcluidas: number;
  usuarios: number;
  ongs: number;
  construtoras: number;
  familias: number;
  quantidadeReaproveitada: number;
  residuosEvitadosKg: number;
  pesoEstimadoKg: number;
  co2EvitadoKg: number;
  categoriasMaisUtilizadas: Record<string, number>;
  doacoesPorMes: Record<string, number>;
  materiaisMaisDoados: { nome: string; quantidade: number }[];
  materiaisMaisProcurados: { nome: string; quantidade: number }[];
}
export interface Denuncia {
  id: number;
  materialId?: number;
  materialNome?: string;
  denunciante: UsuarioResumo;
  motivo: string;
  status: "ABERTA" | "EM_ANALISE" | "RESOLVIDA" | "ARQUIVADA";
  criadoEm: string;
  resolvidoEm?: string;
}
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export const tipoLabels: Record<TipoUsuario, string> = {
  DOADOR_PESSOA_FISICA: "Doador pessoa física",
  DOADOR_CONSTRUTORA: "Construtora",
  BENEFICIARIO_ONG: "ONG",
  BENEFICIARIO_FAMILIA: "Família",
  ADMIN: "Administrador",
};
export const statusMaterialLabels: Record<StatusMaterial, string> = {
  DISPONIVEL: "Disponível",
  RESERVADO: "Reservado",
  RETIRADO: "Retirado",
  FINALIZADO: "Finalizado",
};
export const statusReservaLabels: Record<StatusReserva, string> = {
  SOLICITADA: "Solicitada",
  APROVADA: "Aprovada",
  RECUSADA: "Recusada",
  CANCELADA: "Cancelada",
  RETIRADA: "Retirada",
  FINALIZADA: "Finalizada",
};
export const conservacaoLabels: Record<EstadoConservacao, string> = {
  NOVO: "Novo",
  EXCELENTE: "Excelente",
  BOM: "Bom",
  REGULAR: "Regular",
};
export const isDoador = (tipo?: TipoUsuario) =>
  tipo === "DOADOR_PESSOA_FISICA" || tipo === "DOADOR_CONSTRUTORA";
export const isBeneficiario = (tipo?: TipoUsuario) =>
  tipo === "BENEFICIARIO_ONG" || tipo === "BENEFICIARIO_FAMILIA";

export const pode = (
  usuario: Usuario | null | undefined,
  permissao: PermissaoSistema,
) => Boolean(usuario?.permissoes?.includes(permissao));
