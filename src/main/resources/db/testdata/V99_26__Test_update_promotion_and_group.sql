insert into "promotion"(id, name, ref, creation_datetime) values
('promotion1_id', 'Promotion 21-22', 'PROM21', '2021-11-08T08:30:24.00Z'),
    ('promotion2_id', 'Promotion 22-23', 'PROM22', '2021-11-08T08:30:24.00Z'),
('promotion3_id', 'Promotion 23-24', 'PROM23', '2021-11-08T08:30:24.00Z');

update "group" set promotion_id = 'promotion1_id' where id = 'group1_id' or id = 'group2_id';

update "promotion" set start_date = '2021-11-08' where id = 'promotion1_id';
update "promotion" set start_date = '2022-11-08' where id = 'promotion2_id';
update "promotion" set start_date = '2023-11-08' where id = 'promotion3_id';