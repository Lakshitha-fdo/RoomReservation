CREATE TABLE IF NOT EXISTS users (
    username TEXT PRIMARY KEY,
    password TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS reservations (
    reservation_id TEXT PRIMARY KEY,
    guest_name TEXT NOT NULL,
    address TEXT NOT NULL,
    contact_number TEXT NOT NULL,
    room_type TEXT NOT NULL,
    checkin_date TEXT NOT NULL,
    checkout_date TEXT NOT NULL
);

INSERT OR IGNORE INTO users (username, password) VALUES ('admin', '1234');
