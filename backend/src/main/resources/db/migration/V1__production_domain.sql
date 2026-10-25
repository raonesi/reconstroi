CREATE TABLE IF NOT EXISTS categoria (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(80) NOT NULL UNIQUE,
    slug VARCHAR(80) NOT NULL UNIQUE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO categoria (nome, slug) VALUES
 ('Tijolos','tijolos'),('Telhas','telhas'),('Madeira','madeira'),('Tinta','tinta'),
 ('Pisos','pisos'),('Portas','portas'),('Janelas','janelas'),('Tubos','tubos'),
 ('Ferro','ferro'),('Cimento','cimento'),('Areia','areia'),('Brita','brita'),('Outros','outros')
ON CONFLICT (nome) DO NOTHING;

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS tipo VARCHAR(40) NOT NULL DEFAULT 'DOADOR_PESSOA_FISICA';
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS cidade VARCHAR(120);
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS estado_uf VARCHAR(2);
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS documento VARCHAR(30);
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW();

ALTER TABLE material ADD COLUMN IF NOT EXISTS categoria_id BIGINT REFERENCES categoria(id);
ALTER TABLE material ADD COLUMN IF NOT EXISTS estado_conservacao VARCHAR(30) NOT NULL DEFAULT 'BOM';
ALTER TABLE material ADD COLUMN IF NOT EXISTS status_material VARCHAR(30) NOT NULL DEFAULT 'DISPONIVEL';
ALTER TABLE material ADD COLUMN IF NOT EXISTS estado_uf VARCHAR(2) NOT NULL DEFAULT 'SP';
ALTER TABLE material ADD COLUMN IF NOT EXISTS data_cadastro TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE material ADD COLUMN IF NOT EXISTS atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW();
UPDATE material SET categoria_id = COALESCE(
    (SELECT id FROM categoria c WHERE LOWER(c.nome) = LOWER(material.categoria) LIMIT 1),
    (SELECT id FROM categoria WHERE slug = 'outros'))
WHERE categoria_id IS NULL;

CREATE TABLE IF NOT EXISTS foto_material (
    id BIGSERIAL PRIMARY KEY,
    material_id BIGINT NOT NULL REFERENCES material(id) ON DELETE CASCADE,
    nome_arquivo VARCHAR(255) NOT NULL,
    nome_original VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    url VARCHAR(500) NOT NULL,
    criada_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE reserva ADD COLUMN IF NOT EXISTS material_id BIGINT REFERENCES material(id);
ALTER TABLE reserva ADD COLUMN IF NOT EXISTS solicitante_id BIGINT REFERENCES usuario(id);
ALTER TABLE reserva ADD COLUMN IF NOT EXISTS quantidade_solicitada INTEGER;
ALTER TABLE reserva ADD COLUMN IF NOT EXISTS status_reserva VARCHAR(30) NOT NULL DEFAULT 'SOLICITADA';
ALTER TABLE reserva ADD COLUMN IF NOT EXISTS mensagem VARCHAR(500);
ALTER TABLE reserva ADD COLUMN IF NOT EXISTS criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE reserva ADD COLUMN IF NOT EXISTS atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW();
UPDATE reserva SET quantidade_solicitada = quantidade WHERE quantidade_solicitada IS NULL;

ALTER TABLE doacao ADD COLUMN IF NOT EXISTS reserva_id BIGINT REFERENCES reserva(id);
ALTER TABLE doacao ADD COLUMN IF NOT EXISTS material_id BIGINT REFERENCES material(id);
ALTER TABLE doacao ADD COLUMN IF NOT EXISTS doador_id BIGINT REFERENCES usuario(id);
ALTER TABLE doacao ADD COLUMN IF NOT EXISTS beneficiario_id BIGINT REFERENCES usuario(id);
ALTER TABLE doacao ADD COLUMN IF NOT EXISTS quantidade_reaproveitada INTEGER;
ALTER TABLE doacao ADD COLUMN IF NOT EXISTS concluida_em TIMESTAMPTZ;
ALTER TABLE doacao ADD COLUMN IF NOT EXISTS co2_evitado_kg NUMERIC(12,2) NOT NULL DEFAULT 0;
UPDATE doacao SET quantidade_reaproveitada = quantidade WHERE quantidade_reaproveitada IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_doacao_reserva ON doacao(reserva_id) WHERE reserva_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS token_recuperacao_senha (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    expira_em TIMESTAMPTZ NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS denuncia (
    id BIGSERIAL PRIMARY KEY,
    denunciante_id BIGINT NOT NULL REFERENCES usuario(id),
    material_id BIGINT REFERENCES material(id) ON DELETE SET NULL,
    motivo VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ABERTA',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolvido_em TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_material_busca ON material(status_material, cidade, estado_uf, categoria_id);
CREATE INDEX IF NOT EXISTS idx_material_proprietario ON material(proprietario_id);
CREATE INDEX IF NOT EXISTS idx_reserva_solicitante ON reserva(solicitante_id, criado_em DESC);
CREATE INDEX IF NOT EXISTS idx_reserva_material ON reserva(material_id, criado_em DESC);
