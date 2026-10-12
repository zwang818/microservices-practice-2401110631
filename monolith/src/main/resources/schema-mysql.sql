CREATE TABLE IF NOT EXISTS equipment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    daily_rate DECIMAL(12, 2) NOT NULL,
    deposit DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    description VARCHAR(1000),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_equipment_code UNIQUE (code),
    CONSTRAINT ck_equipment_rate CHECK (daily_rate >= 0),
    CONSTRAINT ck_equipment_deposit CHECK (deposit >= 0),
    CONSTRAINT ck_equipment_status CHECK (status IN ('AVAILABLE', 'MAINTENANCE', 'OFF_SHELF'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    equipment_id BIGINT NOT NULL,
    renter_name VARCHAR(100) NOT NULL,
    start_time DATETIME(6) NOT NULL,
    end_time DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_reservation_equipment FOREIGN KEY (equipment_id) REFERENCES equipment (id),
    CONSTRAINT ck_reservation_time CHECK (end_time > start_time),
    CONSTRAINT ck_reservation_status CHECK (status IN ('CONFIRMED', 'CANCELLED', 'COMPLETED')),
    INDEX idx_reservation_equipment_time (equipment_id, status, start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;
