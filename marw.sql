-- Backup أذان الجزائر الرسمي using Cx Explorer
-- Extract assets\mindb.db
-- run these queries on it
-- rename it to marw-<hyear>.db
-- put it in assets\databases
-- update Constants.FILE_NAME_DB_MARW
-- update versionCode and versionName in build.gradle then build the new APK
-- commit, create tag, push, attach APK file to release

DROP TABLE itc_tab_ahdath;

DROP TABLE itc_tab_fawaid;

DROP TABLE itc_tab_fourouk_zamania;

DROP TABLE itc_tab_hikma_hadatha;

DROP TABLE itc_tab_imsakia;

CREATE VIEW V_DATES AS
    SELECT CAST (SUBSTR(HijriDate, 7, 4) AS INTEGER) AS HIJRI_YEAR,
           CAST (SUBSTR(HijriDate, 4, 2) AS INTEGER) AS HIJRI_MONTH,
           CAST (SUBSTR(HijriDate, 1, 2) AS INTEGER) AS HIJRI_DAY,
           CAST (SUBSTR(GeoDate, 1, 4) AS INTEGER) AS GREG_YEAR,
           CAST (SUBSTR(GeoDate, 6, 2) AS INTEGER) AS GREG_MONTH,
           CAST (SUBSTR(GeoDate, 9, 2) AS INTEGER) AS GREG_DAY
      FROM itc_tab_hijri_geo_date;

CREATE TABLE DATES (
    HIJRI_YEAR  INTEGER,
    HIJRI_MONTH INTEGER,
    HIJRI_DAY   INTEGER,
    GREG_YEAR   INTEGER,
    GREG_MONTH  INTEGER,
    GREG_DAY    INTEGER
);

DELETE FROM DATES;

INSERT INTO DATES SELECT *
                    FROM V_DATES;

DROP VIEW V_DATES;

DROP TABLE itc_tab_hijri_geo_date;

CREATE VIEW V_TIMES AS
    SELECT MADINA_ID AS CITY_ID,
           CAST (SUBSTR(GeoDate, 1, 4) AS INTEGER) AS GREG_YEAR,
           CAST (SUBSTR(GeoDate, 6, 2) AS INTEGER) AS GREG_MONTH,
           CAST (SUBSTR(GeoDate, 9, 2) AS INTEGER) AS GREG_DAY,
           CASE WHEN SUBSTR(Fajr, 2, 1) == ':' THEN '0' || SUBSTR(Fajr, 1, 4) ELSE SUBSTR(Fajr, 1, 5) END AS FAJR,
           CASE WHEN SUBSTR(Dhuhr, 2, 1) == ':' THEN '0' || SUBSTR(Dhuhr, 1, 4) ELSE SUBSTR(Dhuhr, 1, 5) END AS DHUHR,
           CASE WHEN SUBSTR(Asr, 2, 1) == ':' THEN '0' || SUBSTR(Asr, 1, 4) ELSE SUBSTR(Asr, 1, 5) END AS ASR,
           CASE WHEN SUBSTR(Maghrib, 2, 1) == ':' THEN '0' || SUBSTR(Maghrib, 1, 4) ELSE SUBSTR(Maghrib, 1, 5) END AS MAGHRIB,
           CASE WHEN SUBSTR(Isha, 2, 1) == ':' THEN '0' || SUBSTR(Isha, 1, 4) ELSE SUBSTR(Isha, 1, 5) END AS ISHA
      FROM itc_tab_mawakit_salat
     ORDER BY MADINA_ID,
              GeoDate;

CREATE TABLE TIMES (
    CITY_ID    INTEGER,
    GREG_YEAR  INTEGER,
    GREG_MONTH INTEGER,
    GREG_DAY   INTEGER,
    SUBH       TEXT,
    DHUHR      TEXT,
    ASR        TEXT,
    MAGHRIB    TEXT,
    ISHA       TEXT
);

DELETE FROM TIMES;

INSERT INTO TIMES SELECT *
                    FROM V_TIMES;

DROP VIEW V_TIMES;

DROP TABLE itc_tab_mawakit_salat;

CREATE VIEW V_CITIES AS
    SELECT _id AS CITY_ID,
           MADINA_NAME AS CITY_NAME
      FROM itc_tab_madina;

CREATE TABLE CITIES (
    CITY_ID   INTEGER,
    CITY_NAME TEXT
);

DELETE FROM CITIES;

INSERT INTO CITIES SELECT *
                     FROM V_CITIES;

DROP VIEW V_CITIES;

DROP TABLE itc_tab_madina;

CREATE VIEW V_DATES AS
    SELECT PRINTF('%04d', HIJRI_YEAR) || '-' || PRINTF('%02d', HIJRI_MONTH) || '-' || PRINTF('%02d', HIJRI_DAY) AS DATE_HIJRI,
           PRINTF('%04d', GREG_YEAR) || '-' || PRINTF('%02d', GREG_MONTH) || '-' || PRINTF('%02d', GREG_DAY) AS DATE_GREG
      FROM DATES;

CREATE VIEW V_TIMES AS
    SELECT c.CITY_NAME,
           PRINTF('%04d', GREG_YEAR) || '-' || PRINTF('%02d', GREG_MONTH) || '-' || PRINTF('%02d', GREG_DAY) AS DATE_GREG,
           SUBH,
           DHUHR,
           ASR,
           MAGHRIB,
           ISHA
      FROM TIMES t
           INNER JOIN
           CITIES c ON t.CITY_ID = c.CITY_ID;
		   
VACUUM;
