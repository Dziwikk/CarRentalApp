-- Tabela: roles
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL UNIQUE
);

-- Tabela: users
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE
);

-- Tabela: user_roles (relacja many-to-many: users <-> roles)
CREATE TABLE user_roles (
                            user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                            PRIMARY KEY (user_id, role_id)
);

-- Tabela: cars
CREATE TABLE cars (
                      id SERIAL PRIMARY KEY,
                      make VARCHAR(100),
                      model VARCHAR(100),
                      year INT,
                      available BOOLEAN
);

-- Tabela: reservations
CREATE TABLE reservations (
                              id SERIAL PRIMARY KEY,
                              start_date DATE NOT NULL,
                              end_date DATE NOT NULL,
                              user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              car_id INTEGER NOT NULL REFERENCES cars(id) ON DELETE CASCADE,
    -- Możesz dodać inne pola jeśli masz w encji
                              status VARCHAR(32)
);
