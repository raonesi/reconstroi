-- Tabela prevista no diagrama Astah. A recuperação de senha continua desativada (V3):
-- nenhuma entidade, serviço ou endpoint usa esta tabela; ela existe só para o esquema
-- corresponder ao diagrama.
CREATE TABLE IF NOT EXISTS token_recuperacao_senha (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    expira_em TIMESTAMPTZ NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE
);
