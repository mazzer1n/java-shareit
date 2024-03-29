DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS requests CASCADE;

CREATE TABLE IF NOT EXISTS users (
                                     user_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                     name VARCHAR(255) NOT NULL,
    email VARCHAR(50) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (user_id),
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
    );

CREATE TABLE IF NOT EXISTS items (
                                     item_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                     name VARCHAR(255),
    description VARCHAR(512),
    is_available BOOLEAN NOT NULL,
    owner_id BIGINT NOT NULL,
    request_id BIGINT,
    CONSTRAINT pk_item PRIMARY KEY (item_id)
    );

CREATE TABLE IF NOT EXISTS bookings (
                                        booking_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                        start_date TIMESTAMP WITHOUT TIME ZONE,
                                        end_date TIMESTAMP WITHOUT TIME ZONE,
                                        item_id BIGINT,
                                        booker_id BIGINT,
                                        status VARCHAR(8),
    CONSTRAINT pk_booking PRIMARY KEY (booking_id)
    );

CREATE TABLE IF NOT EXISTS requests (
                                        request_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                        description VARCHAR(512),
    requester_id BIGINT,
    created TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_request PRIMARY KEY (request_id)
    );

CREATE TABLE IF NOT EXISTS comments (
                                        comment_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                        text VARCHAR(1024) NOT NULL,
    item_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_commet PRIMARY KEY (comment_id)
    );

ALTER TABLE items ADD CONSTRAINT fk_items_requests FOREIGN KEY (request_id) REFERENCES requests (request_id) ON DELETE CASCADE;

ALTER TABLE items ADD CONSTRAINT fk_items_users FOREIGN KEY (owner_id) REFERENCES users (user_id) ON DELETE CASCADE;

ALTER TABLE bookings ADD CONSTRAINT fk_bookings_items FOREIGN KEY (item_id) REFERENCES items (item_id) ON DELETE CASCADE;

ALTER TABLE bookings ADD CONSTRAINT fk_bookings_users FOREIGN KEY (booker_id) REFERENCES users (user_id) ON DELETE CASCADE;

ALTER TABLE requests ADD CONSTRAINT fk_requests_users FOREIGN KEY (requester_id) REFERENCES users (user_id) ON DELETE CASCADE;

ALTER TABLE comments ADD CONSTRAINT fk_comments_items FOREIGN KEY (item_id) REFERENCES items (item_id) ON DELETE CASCADE;

ALTER TABLE comments ADD CONSTRAINT fk_comments_users FOREIGN KEY (author_id) REFERENCES users (user_id) ON DELETE CASCADE;