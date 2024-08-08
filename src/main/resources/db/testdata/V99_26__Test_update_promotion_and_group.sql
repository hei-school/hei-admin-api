update "promotion" set start_date = '2021-11-08' where id = 'promotion1_id';
update "promotion" set start_date = '2022-11-08' where id = 'promotion2_id';
update "promotion" set start_date = '2023-11-08' where id = 'promotion3_id';
update "group" set promotion_id = 'promotion3_id' where id = 'group5_id';