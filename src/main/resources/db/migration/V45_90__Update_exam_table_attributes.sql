alter table exam rename column coefficient to coefficient_numerator;
alter table exam add column coefficient_denominator int not null default 1;