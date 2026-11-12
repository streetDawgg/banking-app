CREATE TABLE users (
    user_id               BIGSERIAL PRIMARY KEY,
    full_name             VARCHAR(100) NOT NULL,
    phone_number          VARCHAR(11)  NOT NULL UNIQUE,
    pin_hash              VARCHAR(100) NOT NULL,
    failed_login_attempts INT          NOT NULL DEFAULT 0,
    locked_until          TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_phone_11_digits CHECK (phone_number ~ '^[0-9]{11}$')
);

CREATE TABLE accounts (
    account_id     BIGSERIAL PRIMARY KEY,
    user_id        BIGINT        NOT NULL UNIQUE REFERENCES users(user_id),
    account_number VARCHAR(10)   NOT NULL UNIQUE,
    balance        NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    account_status VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT chk_account_number_10_digits CHECK (account_number ~ '^[0-9]{10}$')
);

CREATE TABLE transactions (
    transaction_id      BIGSERIAL PRIMARY KEY,
    sender_account_id   BIGINT REFERENCES accounts(account_id),
    receiver_account_id BIGINT REFERENCES accounts(account_id),
    transaction_type    VARCHAR(20)   NOT NULL,
    amount              NUMERIC(19,2) NOT NULL,
    description         VARCHAR(255),
    transaction_status  VARCHAR(20)   NOT NULL,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_has_account CHECK (sender_account_id IS NOT NULL OR receiver_account_id IS NOT NULL)
);

CREATE INDEX idx_tx_sender   ON transactions(sender_account_id, created_at DESC);
CREATE INDEX idx_tx_receiver ON transactions(receiver_account_id, created_at DESC);

-- Transaction records are an audit trail: block edits and deletes at the database level.
CREATE FUNCTION forbid_transaction_changes() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Transaction records cannot be modified or deleted';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_transactions_immutable
    BEFORE UPDATE OR DELETE ON transactions
    FOR EACH ROW EXECUTE FUNCTION forbid_transaction_changes();
