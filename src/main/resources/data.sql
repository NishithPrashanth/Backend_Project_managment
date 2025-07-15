-- Example only; use YOUR actual hash below
INSERT INTO users (id, name, email, password, role)
VALUES (1,
        'Administrator',
        'admin@example.com',
        '$2a$10$wKKxeFtxAj5LL4fwa6.0Ou2j12SFXYHlGHjrdfGaA6S6E6bVgUMhG',
        'ADMIN')
ON DUPLICATE KEY UPDATE
        email = email;
