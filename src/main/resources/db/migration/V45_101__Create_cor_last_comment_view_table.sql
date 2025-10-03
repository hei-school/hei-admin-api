CREATE OR REPLACE VIEW cor_last_comment AS
SELECT cc.*
FROM cor_comment cc
         JOIN (SELECT cor_id, MAX(creation_datetime) AS max_datetime
               FROM cor_comment
               GROUP BY cor_id) latest
              ON cc.cor_id = latest.cor_id
                  AND cc.creation_datetime = latest.max_datetime;

-- SELECT Cor
-- FROM Cor
--          LEFT JOIN LATERAL (
--     SELECT cc.status last_comment_status
--     FROM CorComment cc
--     WHERE cc.cor.id = cor.id
--     ORDER BY cc.creationDatetime DESC
--     LIMIT 1
--     ) last_comment_status ON TRUE
