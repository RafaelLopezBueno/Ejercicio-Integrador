-- Esquema de CandiTrack v2.0 (MySQL 8 / compatible con H2 en modo MySQL)

CREATE TABLE IF NOT EXISTS empresa (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(120) NOT NULL,
    sector          VARCHAR(120),
    web             VARCHAR(255),
    fecha_registro  DATE         NOT NULL,
    CONSTRAINT uq_empresa_nombre UNIQUE (nombre)
);

CREATE TABLE IF NOT EXISTS oferta (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    id_empresa         INT          NOT NULL,
    titulo             VARCHAR(160) NOT NULL,
    descripcion        TEXT,
    estado             VARCHAR(20)  NOT NULL,
    fecha_postulacion  DATE         NOT NULL,
    enlace             VARCHAR(500),
    salario            DECIMAL(10, 2),
    notas              TEXT,
    CONSTRAINT fk_oferta_empresa FOREIGN KEY (id_empresa) REFERENCES empresa (id),
    CONSTRAINT ck_oferta_estado  CHECK (estado IN ('pendiente', 'entrevista', 'rechazado', 'aceptado')),
    CONSTRAINT ck_oferta_salario CHECK (salario IS NULL OR salario >= 0)
);

CREATE TABLE IF NOT EXISTS entrevista (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    id_oferta  INT         NOT NULL,
    fecha      DATE        NOT NULL,
    tipo       VARCHAR(30) NOT NULL,
    notas      TEXT,
    resultado  VARCHAR(20) NOT NULL DEFAULT 'pendiente',
    CONSTRAINT fk_entrevista_oferta FOREIGN KEY (id_oferta) REFERENCES oferta (id),
    CONSTRAINT ck_entrevista_resultado CHECK (resultado IN ('pendiente', 'aprobada', 'rechazada'))
);
