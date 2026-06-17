CREATE TABLE IF NOT EXISTS person (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    age INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bootcamp_person (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bootcamp_id BIGINT NOT NULL,
    person_id BIGINT NOT NULL,
    UNIQUE (bootcamp_id, person_id),
    FOREIGN KEY (person_id) REFERENCES person(id)
);

INSERT IGNORE INTO person (name, email, age) VALUES
('Alice Smith', 'Alice@smith.com', 30),
('Bob Johnson', 'Bob@Johnson.com',25);

INSERT IGNORE INTO bootcamp_person (bootcamp_id, person_id) VALUES
(1, 1),
(1, 2);