--
-- config.env
-- --------------------
-- This table is intended to store environment configuration.
--
-- find all configuration for region
-- > SELECT * FROM mercury.env WHERE env_region=?
--
-- find specific configuration for region
-- > SELECT * FROM mercury.env WHERE env_region=? and key=?
--
--
CREATE TABLE IF NOT EXISTS config.env (
    key text,
    value text,
    env_region text,
PRIMARY KEY(env_region, key)
) WITH CLUSTERING ORDER BY (key ASC)
AND compression = {
    'sstable_compression' : 'SnappyCompressor'
};
