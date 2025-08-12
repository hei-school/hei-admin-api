do
$$
begin
  if not exists(select from pg_type where typname = 'yearly_result_transcript_status') then
  create type "yearly_result_transcript_status" as enum ('GENERATING', 'AVAILABLE');
end if;
end
$$;
