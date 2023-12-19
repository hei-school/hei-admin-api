update "fee" set status = 'PAID' where status != 'PAID' and remaining_amount = 0;
