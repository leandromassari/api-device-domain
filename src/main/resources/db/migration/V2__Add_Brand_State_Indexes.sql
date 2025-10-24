-- Create index on brand column for faster brand-based queries
CREATE INDEX idx_devices_brand ON devices(brand);

-- Create index on state column for faster state-based queries
CREATE INDEX idx_devices_state ON devices(state);

-- Add comments for documentation
COMMENT ON INDEX idx_devices_brand IS 'Index for optimizing queries filtering by brand';
COMMENT ON INDEX idx_devices_state IS 'Index for optimizing queries filtering by state';
