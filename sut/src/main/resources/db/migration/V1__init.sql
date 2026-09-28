CREATE TABLE orders (
    id UUID PRIMARY KEY,
    customer_id TEXT NOT NULL,
    product_id TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    status TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE inventory (
    product_id TEXT PRIMARY KEY,
    quantity INTEGER NOT NULL
);

INSERT INTO inventory (product_id, quantity) VALUES
    ('prod-abc', 100),
    ('prod-def', 5),
    ('prod-ghi', 0);
