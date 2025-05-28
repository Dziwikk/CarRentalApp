-- src/main/resources/db/migration/V1__init.sql

-- Tabela: roles
CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL UNIQUE
);

-- Tabela: users
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE
);

-- Tabela: user_roles (relacja many-to-many: users <-> roles)
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                            PRIMARY KEY (user_id, role_id)
);

-- Tabela: cars
CREATE TABLE cars (
                      id BIGSERIAL PRIMARY KEY,
                      make VARCHAR(100),
                      model VARCHAR(100),
                      year INT,
                      available BOOLEAN
);

-- Tabela: reservations
CREATE TABLE reservations (
                              id BIGSERIAL PRIMARY KEY,
                              start_date DATE NOT NULL,
                              end_date DATE NOT NULL,
                              user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              car_id BIGINT NOT NULL REFERENCES cars(id) ON DELETE CASCADE,
                              status VARCHAR(32)
);
