create unique index if not exists credit_transaction_fee_overpayment_unique
    on credit_transaction (fee_id)
    where credit_movement = 'CREDIT' and type = 'FEE_OVERPAYMENT';
