-- ============================================================
-- GHADS - Gaza Humanitarian Aid Distribution System
-- Complete MySQL Database Schema
-- ============================================================

DROP DATABASE IF EXISTS ghads_db;
CREATE DATABASE ghads_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ghads_db;

-- ============================================================
-- TABLE: organizations
-- ============================================================
CREATE TABLE organizations (
    org_id        INT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(150) NOT NULL,
    type          VARCHAR(100) NOT NULL COMMENT 'e.g. NGO, UN, Local',
    contact_info  VARCHAR(255) NOT NULL COMMENT 'Phone or email',
    CONSTRAINT pk_organizations PRIMARY KEY (org_id),
    CONSTRAINT uq_org_name     UNIQUE (name)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: users
-- ============================================================
CREATE TABLE users (
    user_id    INT          NOT NULL AUTO_INCREMENT,
    username   VARCHAR(100) NOT NULL,
    password   VARCHAR(255) NOT NULL COMMENT 'Store as plain text per spec',
    full_name  VARCHAR(200) NOT NULL,
    email      VARCHAR(200) NOT NULL,
    role       VARCHAR(20)  NOT NULL COMMENT 'ADMIN or COORDINATOR',
    org_id     INT          DEFAULT NULL COMMENT 'NULL for ADMIN',
    photo_path VARCHAR(500) DEFAULT NULL COMMENT 'Bonus: path to profile photo',
    CONSTRAINT pk_users       PRIMARY KEY (user_id),
    CONSTRAINT uq_username    UNIQUE (username),
    CONSTRAINT uq_email       UNIQUE (email),
    CONSTRAINT fk_user_org    FOREIGN KEY (org_id) REFERENCES organizations(org_id) ON DELETE SET NULL,
    CONSTRAINT chk_role       CHECK (role IN ('ADMIN', 'COORDINATOR'))
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: families
-- ============================================================
CREATE TABLE families (
    family_id         INT          NOT NULL AUTO_INCREMENT,
    household_name    VARCHAR(200) NOT NULL,
    phone             VARCHAR(30)  NOT NULL,
    location          VARCHAR(255) NOT NULL COMMENT 'Area or camp name',
    family_size       INT          NOT NULL,
    national_id       VARCHAR(50)  NOT NULL,
    vulnerability_level VARCHAR(10) NOT NULL COMMENT 'HIGH, MEDIUM, LOW',
    registration_date DATE         NOT NULL DEFAULT (CURRENT_DATE),
    last_aid_date     DATE         DEFAULT NULL COMMENT 'Updated after each distribution',
    CONSTRAINT pk_families         PRIMARY KEY (family_id),
    CONSTRAINT uq_national_id      UNIQUE (national_id),
    CONSTRAINT chk_vulnerability   CHECK (vulnerability_level IN ('HIGH', 'MEDIUM', 'LOW')),
    CONSTRAINT chk_family_size     CHECK (family_size > 0)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: aid_distributions
-- ============================================================
CREATE TABLE aid_distributions (
    distribution_id   INT          NOT NULL AUTO_INCREMENT,
    family_id         INT          NOT NULL,
    org_id            INT          NOT NULL,
    distributed_by    INT          NOT NULL COMMENT 'user_id of coordinator',
    distribution_date DATE         NOT NULL DEFAULT (CURRENT_DATE),
    aid_type          VARCHAR(100) NOT NULL DEFAULT 'General' COMMENT 'Bonus: Food, Medicine, Clothes, etc.',
    notes             VARCHAR(500) DEFAULT NULL,
    CONSTRAINT pk_distributions    PRIMARY KEY (distribution_id),
    CONSTRAINT fk_dist_family      FOREIGN KEY (family_id)      REFERENCES families(family_id)      ON DELETE CASCADE,
    CONSTRAINT fk_dist_org         FOREIGN KEY (org_id)         REFERENCES organizations(org_id)    ON DELETE CASCADE,
    CONSTRAINT fk_dist_user        FOREIGN KEY (distributed_by) REFERENCES users(user_id)           ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- INDEXES for performance
-- ============================================================
CREATE INDEX idx_dist_family_date  ON aid_distributions (family_id, distribution_date);
CREATE INDEX idx_dist_org          ON aid_distributions (org_id);
CREATE INDEX idx_dist_aid_type     ON aid_distributions (family_id, aid_type, distribution_date);
CREATE INDEX idx_family_vuln       ON families (vulnerability_level);

-- ============================================================
-- INITIAL DATA: Insert admin user
-- ============================================================
INSERT INTO users (username, password, full_name, email, role, org_id, photo_path)
VALUES ('admin', 'admin1234', 'System Administrator', 'admin@ghads.org', 'ADMIN', NULL, NULL);

-- ============================================================
-- SAMPLE DATA: Organizations
-- ============================================================
INSERT INTO organizations (name, type, contact_info) VALUES
('UNRWA Gaza',        'UN',    'unrwa@un.org'),
('Islamic Relief',    'NGO',   '+970-8-2840000'),
('World Food Program','UN',    'wfp.gaza@wfp.org'),
('Local Aid Committee','Local','localaid@gmail.com');

-- ============================================================
-- SAMPLE DATA: Coordinator users
-- ============================================================
INSERT INTO users (username, password, full_name, email, role, org_id) VALUES
('coord1', 'coord1234', 'Ahmed Al-Masri',   'ahmed@unrwa.org',    'COORDINATOR', 1),
('coord2', 'coord1234', 'Sara Al-Helo',     'sara@islamic.org',   'COORDINATOR', 2),
('coord3', 'coord1234', 'Omar Baraka',      'omar@wfp.org',       'COORDINATOR', 3);

-- ============================================================
-- SAMPLE DATA: Families
-- ============================================================
INSERT INTO families (household_name, phone, location, family_size, national_id, vulnerability_level, registration_date) VALUES
('Al-Masri Family',   '0599111001', 'Jabalia Camp',    6, 'NID-001', 'HIGH',   '2024-01-10'),
('Al-Helo Family',    '0599111002', 'Khan Younis',     4, 'NID-002', 'MEDIUM', '2024-01-15'),
('Baraka Family',     '0599111003', 'Rafah',           8, 'NID-003', 'HIGH',   '2024-01-20'),
('Al-Najjar Family',  '0599111004', 'Beit Lahia',      3, 'NID-004', 'LOW',    '2024-02-01'),
('Abu Eid Family',    '0599111005', 'Deir el-Balah',   5, 'NID-005', 'MEDIUM', '2024-02-10'),
('Al-Qassim Family',  '0599111006', 'Nuseirat Camp',   7, 'NID-006', 'HIGH',   '2024-02-15'),
('Al-Ghoul Family',   '0599111007', 'Gaza City',       2, 'NID-007', 'LOW',    '2024-03-01'),
('Khalil Family',     '0599111008', 'Jabalia Camp',    9, 'NID-008', 'HIGH',   '2024-03-05');

-- ============================================================
-- SAMPLE DATA: Aid Distributions
-- ============================================================
INSERT INTO aid_distributions (family_id, org_id, distributed_by, distribution_date, aid_type) VALUES
(1, 1, 2, DATE_SUB(CURDATE(), INTERVAL 40 DAY), 'Food'),
(2, 1, 2, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'Medicine'),
(3, 2, 3, DATE_SUB(CURDATE(), INTERVAL 5  DAY),  'Food'),
(4, 3, 4, DATE_SUB(CURDATE(), INTERVAL 60 DAY), 'Clothes'),
(5, 1, 2, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 'Food'),
(6, 2, 3, DATE_SUB(CURDATE(), INTERVAL 2  DAY),  'Food'),
(8, 4, 2, DATE_SUB(CURDATE(), INTERVAL 1  DAY),  'Medicine');

-- Update last_aid_date for served families
UPDATE families f
SET last_aid_date = (
    SELECT MAX(ad.distribution_date)
    FROM aid_distributions ad
    WHERE ad.family_id = f.family_id
)
WHERE EXISTS (
    SELECT 1 FROM aid_distributions ad WHERE ad.family_id = f.family_id
);

-- ============================================================
-- USEFUL VIEWS
-- ============================================================

-- View: families with service status
CREATE VIEW v_family_service_status AS
SELECT
    f.family_id,
    f.household_name,
    f.national_id,
    f.vulnerability_level,
    f.location,
    f.family_size,
    f.last_aid_date,
    CASE WHEN f.last_aid_date IS NULL THEN 'Unserved' ELSE 'Served' END AS service_status
FROM families f;

-- View: full distribution details
CREATE VIEW v_distribution_details AS
SELECT
    ad.distribution_id,
    f.household_name      AS family_name,
    f.national_id,
    f.vulnerability_level,
    o.name                AS organization_name,
    CONCAT(u.full_name)   AS coordinator_name,
    ad.distribution_date,
    ad.aid_type,
    ad.notes
FROM aid_distributions ad
JOIN families      f ON ad.family_id      = f.family_id
JOIN organizations o ON ad.org_id         = o.org_id
JOIN users         u ON ad.distributed_by = u.user_id;
