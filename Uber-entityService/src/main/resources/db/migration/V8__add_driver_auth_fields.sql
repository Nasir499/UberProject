SET @exist_driver_email := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'uber_db_local' AND TABLE_NAME = 'driver' AND COLUMN_NAME = 'email');
SET @sql_driver_email := IF(@exist_driver_email = 0, 'ALTER TABLE driver ADD email VARCHAR(255) NULL;', 'SELECT 1;');
PREPARE stmt FROM @sql_driver_email;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_driver_pass := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'uber_db_local' AND TABLE_NAME = 'driver' AND COLUMN_NAME = 'password');
SET @sql_driver_pass := IF(@exist_driver_pass = 0, 'ALTER TABLE driver ADD password VARCHAR(255) NULL;', 'SELECT 1;');
PREPARE stmt FROM @sql_driver_pass;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_car_driver := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'uber_db_local' AND TABLE_NAME = 'car' AND COLUMN_NAME = 'driver_id');
SET @sql_car_driver := IF(@exist_car_driver = 0, 'ALTER TABLE car ADD driver_id BIGINT NULL;', 'SELECT 1;');
PREPARE stmt FROM @sql_car_driver;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
