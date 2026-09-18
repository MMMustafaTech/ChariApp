-- Birth-certificate extraction and newborn registration are separate services.
-- Keep at most one open request per citizen and birth-request kind.
UPDATE service_requests
SET open_request_type = CONCAT(type, ':', request_kind)
WHERE type = 'BIRTH_CERTIFICATE'
  AND open_request_key IS NOT NULL;
