-- Execute conectado ao banco crud_mvc.
CREATE TABLE IF NOT EXISTS material (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(120) NOT NULL CHECK (length(trim(nome)) > 0),
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    descricao TEXT NOT NULL DEFAULT ''
);
