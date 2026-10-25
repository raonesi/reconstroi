-- O diagrama Astah marca doacao.reserva_id como UNIQUE. Até aqui a garantia era um índice
-- único parcial (WHERE reserva_id IS NOT NULL); trocamos por uma restrição UNIQUE de coluna
-- única. Várias doações sem reserva continuam permitidas, pois NULL não conflita em UNIQUE.
DROP INDEX IF EXISTS uk_doacao_reserva;
ALTER TABLE doacao ADD CONSTRAINT uk_doacao_reserva UNIQUE (reserva_id);
