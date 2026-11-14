-- ============================================================================
-- Carburants database (MySQL 8) - schema + sample data
--
-- Translation of the class diagram of the workshop:
--   Station (nom, ville, adresse)  0..*  <->  0..*  Carburant (nom, description)
--   HistoCarb (date, prix) is the association class.
--
-- A many-to-many association WITH attributes cannot be a plain join table:
-- HistoCarb becomes a table of its own (histo_carb) with two foreign keys and
-- the two attributes of the association class (date_prix, prix).
--
-- WARNING: this script DROPS and recreates the three tables. Development only.
-- Usage (PowerShell):  mysql -u root -p -e "source db/create-database.sql"
-- ============================================================================

CREATE DATABASE IF NOT EXISTS carburants
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE carburants;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS histo_carb;
DROP TABLE IF EXISTS carburant;
DROP TABLE IF EXISTS station;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE station (
  id      BIGINT       NOT NULL AUTO_INCREMENT,
  nom     VARCHAR(100) NOT NULL,
  ville   VARCHAR(100) NOT NULL,
  adresse VARCHAR(255) NOT NULL,
  CONSTRAINT pk_station PRIMARY KEY (id),
  CONSTRAINT uq_station_nom UNIQUE (nom),
  CONSTRAINT ck_station_nom CHECK (CHAR_LENGTH(TRIM(nom)) > 0),
  CONSTRAINT ck_station_ville CHECK (CHAR_LENGTH(TRIM(ville)) > 0),
  CONSTRAINT ck_station_adresse CHECK (CHAR_LENGTH(TRIM(adresse)) > 0)
) ENGINE = InnoDB;

CREATE TABLE carburant (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  nom         VARCHAR(100) NOT NULL,
  description VARCHAR(255) NULL,
  CONSTRAINT pk_carburant PRIMARY KEY (id),
  CONSTRAINT uq_carburant_nom UNIQUE (nom),
  CONSTRAINT ck_carburant_nom CHECK (CHAR_LENGTH(TRIM(nom)) > 0)
) ENGINE = InnoDB;

CREATE TABLE histo_carb (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  station_id   BIGINT       NOT NULL,
  carburant_id BIGINT       NOT NULL,
  date_prix    DATE         NOT NULL,
  prix         DECIMAL(8,2) NOT NULL,
  CONSTRAINT pk_histo_carb PRIMARY KEY (id),
  -- One price per station, fuel and day: the "journalier" rule of the subject.
  CONSTRAINT uq_histo_jour UNIQUE (station_id, carburant_id, date_prix),
  CONSTRAINT ck_histo_prix CHECK (prix >= 0),
  -- RESTRICT: a station or a fuel that has a price history cannot be deleted silently.
  CONSTRAINT fk_histo_station   FOREIGN KEY (station_id)   REFERENCES station (id)   ON DELETE RESTRICT,
  CONSTRAINT fk_histo_carburant FOREIGN KEY (carburant_id) REFERENCES carburant (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE INDEX ix_histo_station_date ON histo_carb (station_id, date_prix);

-- ---------------------------------------------------------------------------
-- Sample data (prices in MAD per litre). Two fuels, three stations in Tangier.
-- ---------------------------------------------------------------------------
INSERT INTO station (nom, ville, adresse) VALUES
  ('Station Malabata',     'Tanger', 'Avenue Mohammed VI, Malabata'),
  ('Station Boukhalef',    'Tanger', 'Route de Rabat, Boukhalef'),
  ('Station Centre-Ville', 'Tanger', 'Boulevard Pasteur, Centre-Ville');

INSERT INTO carburant (nom, description) VALUES
  ('Gasoil',         'Diesel fuel'),
  ('Sans plomb 95',  'Unleaded petrol, 95 octane');

-- Station Malabata: prices change on 2026-11-01 and 2026-11-08.
INSERT INTO histo_carb (station_id, carburant_id, date_prix, prix)
SELECT s.id, c.id, v.d, v.p
FROM (
  SELECT 'Station Malabata' AS sn, 'Gasoil' AS cn, DATE '2026-11-01' AS d, 10.45 AS p UNION ALL
  SELECT 'Station Malabata', 'Sans plomb 95', DATE '2026-11-01', 11.60 UNION ALL
  SELECT 'Station Malabata', 'Gasoil',        DATE '2026-11-08', 10.62 UNION ALL
  SELECT 'Station Malabata', 'Sans plomb 95', DATE '2026-11-08', 11.75 UNION ALL
  SELECT 'Station Boukhalef', 'Gasoil',        DATE '2026-11-01', 10.40 UNION ALL
  SELECT 'Station Boukhalef', 'Sans plomb 95', DATE '2026-11-01', 11.55 UNION ALL
  SELECT 'Station Centre-Ville', 'Gasoil',        DATE '2026-11-05', 10.50 UNION ALL
  SELECT 'Station Centre-Ville', 'Sans plomb 95', DATE '2026-11-05', 11.70
) v
JOIN station  s ON s.nom = v.sn
JOIN carburant c ON c.nom = v.cn;

SELECT 'station' AS table_name, COUNT(*) AS row_count FROM station
UNION ALL SELECT 'carburant', COUNT(*) FROM carburant
UNION ALL SELECT 'histo_carb', COUNT(*) FROM histo_carb;
