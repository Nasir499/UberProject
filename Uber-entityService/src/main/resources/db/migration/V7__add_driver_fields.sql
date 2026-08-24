ALTER TABLE driver
    ADD aadhar_card VARCHAR(255) NULL;

ALTER TABLE driver
    ADD active_city VARCHAR(255) NULL;

ALTER TABLE driver
    ADD driver_approval_status VARCHAR(255) NULL;

ALTER TABLE driver
    ADD driver_state VARCHAR(255) NULL;

ALTER TABLE driver
    ADD version BIGINT NULL;
