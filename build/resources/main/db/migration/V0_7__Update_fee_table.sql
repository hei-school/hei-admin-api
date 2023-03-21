alter table "fee" add column if not exists remaining_amount integer;
alter table "fee" add column if not exists updated_at timestamp without time zone default now();

create or replace function update_updated_at_fee()
    returns trigger as
$$
begin
    new.updated_at = now();
    return new;
end;
$$ language 'plpgsql';

create trigger update_fee_updated_at
    before update
        of status
    on
        "fee"
    for each row
execute procedure update_updated_at_fee();