CREATE TABLE dbconstants
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NOT NULL,
    updated_at datetime NOT NULL,
    name       VARCHAR(255) NULL,
    value      VARCHAR(255) NULL,
    CONSTRAINT pk_dbconstants PRIMARY KEY (id)
);

ALTER TABLE dbconstants
    ADD CONSTRAINT uc_dbconstants_name UNIQUE (name);