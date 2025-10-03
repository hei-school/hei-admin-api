CREATE OR REPLACE VIEW cor_last_comment AS
SELECT cor_comment.*
FROM cor_comment
JOIN (
    SELECT cor_id, MAX(creation_datetime) creation_datetime
    FROM cor_comment
    GROUP BY cor_id
) latest
ON cor_comment.cor_id = latest.cor_id
       AND cor_comment.creation_datetime = latest.creation_datetime;
