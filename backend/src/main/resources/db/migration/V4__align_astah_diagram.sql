ALTER TABLE material ADD COLUMN IF NOT EXISTS estado VARCHAR(30);
ALTER TABLE material ADD COLUMN IF NOT EXISTS usuario_id BIGINT REFERENCES usuario(id);
ALTER TABLE reserva ADD COLUMN IF NOT EXISTS material VARCHAR(120);
ALTER TABLE reserva ADD COLUMN IF NOT EXISTS solicitante VARCHAR(120);
ALTER TABLE reserva ADD COLUMN IF NOT EXISTS status VARCHAR(30);
ALTER TABLE doacao ADD COLUMN IF NOT EXISTS data VARCHAR(40);
ALTER TABLE doacao ADD COLUMN IF NOT EXISTS doador VARCHAR(120);
ALTER TABLE doacao ADD COLUMN IF NOT EXISTS material VARCHAR(120);

CREATE TABLE sessao (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    expira_em TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_sessao_usuario ON sessao(usuario_id);

CREATE FUNCTION sincronizar_material_diagrama() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    NEW.usuario_id := NEW.proprietario_id;
    NEW.estado := NEW.estado_conservacao;
    SELECT nome INTO NEW.categoria FROM categoria WHERE id = NEW.categoria_id;
    RETURN NEW;
END;
$$;
CREATE TRIGGER material_diagrama BEFORE INSERT OR UPDATE ON material
FOR EACH ROW EXECUTE FUNCTION sincronizar_material_diagrama();

CREATE FUNCTION sincronizar_reserva_diagrama() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    SELECT nome INTO NEW.material FROM material WHERE id = NEW.material_id;
    SELECT nome INTO NEW.solicitante FROM usuario WHERE id = NEW.solicitante_id;
    NEW.quantidade := NEW.quantidade_solicitada;
    NEW.status := NEW.status_reserva;
    RETURN NEW;
END;
$$;
CREATE TRIGGER reserva_diagrama BEFORE INSERT OR UPDATE ON reserva
FOR EACH ROW EXECUTE FUNCTION sincronizar_reserva_diagrama();

CREATE FUNCTION sincronizar_doacao_diagrama() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    SELECT nome INTO NEW.material FROM material WHERE id = NEW.material_id;
    SELECT nome INTO NEW.doador FROM usuario WHERE id = NEW.doador_id;
    NEW.quantidade := NEW.quantidade_reaproveitada;
    NEW.data := to_char(NEW.concluida_em AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS.MS"Z"');
    RETURN NEW;
END;
$$;
CREATE TRIGGER doacao_diagrama BEFORE INSERT OR UPDATE ON doacao
FOR EACH ROW EXECUTE FUNCTION sincronizar_doacao_diagrama();

UPDATE material SET proprietario_id = proprietario_id;
UPDATE reserva SET quantidade_solicitada = quantidade_solicitada WHERE material_id IS NOT NULL AND solicitante_id IS NOT NULL;
UPDATE doacao SET quantidade_reaproveitada = quantidade_reaproveitada WHERE material_id IS NOT NULL AND doador_id IS NOT NULL;

-- token_recuperacao_senha permanece ausente por decisao expressa do usuario.
-- Campos legados presentes no diagrama Astah. Os nomes usados pela API continuam
-- canonicos; os campos equivalentes sao mantidos pelo banco em cada gravacao.
