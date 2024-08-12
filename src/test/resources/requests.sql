-- clear_users
DELETE FROM users;

-- clear_auth_codes
DELETE FROM auth_codes;

-- clear_cards
DELETE FROM cards;

-- clear_card_transactions
DELETE FROM card_transactions;

-- select_user_by_login
SELECT * FROM users WHERE login = ?;

-- select_latest_auth_code_by_user_id
SELECT * FROM auth_codes WHERE user_id = ? ORDER BY created DESC LIMIT 1;