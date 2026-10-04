CREATE DATABASE clarigence
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE USER 'clarigence_app'@'localhost'
IDENTIFIED BY 'Clarigence';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX
ON clarigence.* TO 'clarigence_app'@'localhost';

FLUSH PRIVILEGES;