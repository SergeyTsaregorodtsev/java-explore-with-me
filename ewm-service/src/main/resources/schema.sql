CREATE TABLE IF NOT EXISTS users (
    id INT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS categories (
    id INT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS events (
    id INT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    category_id INT REFERENCES categories(id),
    title VARCHAR(120) NOT NULL,
    annotation VARCHAR(2000) NOT NULL,
    description VARCHAR(7000),
    initiator_id INT REFERENCES users(id),
    event_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_on TIMESTAMP WITHOUT TIME ZONE,
    published_on TIMESTAMP WITHOUT TIME ZONE,
    location_lat INT NOT NULL,
    location_lon INT NOT NULL,
    paid BOOLEAN NOT NULL,
    request_moderation BOOLEAN,
    participant_limit INT,
    status VARCHAR(255),
    confirmed_requests INT
);
CREATE TABLE IF NOT EXISTS requests (
    id INT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    event_id INT REFERENCES events(id),
    requester_id INT REFERENCES users(id),
    created TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS compilations (
    id INT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    title  VARCHAR(255) NOT NULL UNIQUE,
    pinned BOOLEAN NOT NULL
);
CREATE TABLE IF NOT EXISTS event_compilations (
    event_id INT REFERENCES events(id),
    compilation_id INT REFERENCES compilations(id),
    PRIMARY KEY (event_id, compilation_id)
)