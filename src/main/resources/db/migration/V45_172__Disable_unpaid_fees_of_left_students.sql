update "fee"
set is_deleted = true
where is_deleted = false
  and status in ('UNPAID', 'LATE')
  and user_id in (select id from "user" where role = 'STUDENT' and status = 'DISABLED');
