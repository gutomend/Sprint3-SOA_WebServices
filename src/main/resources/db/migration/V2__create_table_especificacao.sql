CREATE TABLE especificacao (
    id BIGSERIAL PRIMARY KEY,
    atributo VARCHAR(100) NOT NULL,
    valor VARCHAR(255) NOT NULL,
    veiculo_id BIGINT NOT NULL,

    CONSTRAINT fk_veiculo
        FOREIGN KEY (veiculo_id)
        REFERENCES veiculo(id)
);