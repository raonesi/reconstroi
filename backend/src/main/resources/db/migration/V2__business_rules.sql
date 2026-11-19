ALTER TABLE material ADD COLUMN IF NOT EXISTS excluido BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE material ADD COLUMN IF NOT EXISTS excluido_em TIMESTAMPTZ;
ALTER TABLE material ADD COLUMN IF NOT EXISTS motivo_remocao VARCHAR(500);
ALTER TABLE material ADD COLUMN IF NOT EXISTS versao BIGINT NOT NULL DEFAULT 0;

ALTER TABLE doacao ADD COLUMN IF NOT EXISTS peso_estimado_kg NUMERIC(14,2) NOT NULL DEFAULT 0;
ALTER TABLE doacao ADD COLUMN IF NOT EXISTS residuos_evitados_kg NUMERIC(14,2) NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS historico_acao (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id),
    tipo_acao VARCHAR(50) NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    ip VARCHAR(64),
    recurso_tipo VARCHAR(40) NOT NULL,
    recurso_id BIGINT NOT NULL,
    ocorrido_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS notificacao (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    titulo VARCHAR(140) NOT NULL,
    mensagem VARCHAR(500) NOT NULL,
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    criada_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Bancos antigos podiam conter mais de uma solicitação ativa para o mesmo anúncio.
-- Mantemos a mais antiga e recusamos as demais antes de criar a garantia definitiva.
WITH duplicadas AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY material_id ORDER BY criado_em, id) AS ordem
    FROM reserva
    WHERE material_id IS NOT NULL
      AND status_reserva IN ('SOLICITADA', 'APROVADA', 'RETIRADA')
)
UPDATE reserva
SET status_reserva = 'RECUSADA', atualizado_em = NOW()
WHERE id IN (SELECT id FROM duplicadas WHERE ordem > 1);

CREATE UNIQUE INDEX IF NOT EXISTS uk_reserva_ativa_material
    ON reserva(material_id)
    WHERE material_id IS NOT NULL AND status_reserva IN ('SOLICITADA', 'APROVADA', 'RETIRADA');
CREATE INDEX IF NOT EXISTS idx_historico_recurso ON historico_acao(recurso_tipo, recurso_id, ocorrido_em DESC);
CREATE INDEX IF NOT EXISTS idx_historico_usuario ON historico_acao(usuario_id, ocorrido_em DESC);
CREATE INDEX IF NOT EXISTS idx_notificacao_usuario ON notificacao(usuario_id, lida, criada_em DESC);
CREATE INDEX IF NOT EXISTS idx_material_publicado ON material(excluido, status_material, data_cadastro DESC);
