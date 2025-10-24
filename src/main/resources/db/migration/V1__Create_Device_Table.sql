-- Create devices table
CREATE TABLE devices (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(255) NOT NULL,
    state VARCHAR(20) NOT NULL,
    creation_time TIMESTAMP NOT NULL
);

-- Add comments for documentation
COMMENT ON TABLE devices IS 'Stores device information';
COMMENT ON COLUMN devices.id IS 'Unique identifier for the device';
COMMENT ON COLUMN devices.name IS 'Name of the device';
COMMENT ON COLUMN devices.brand IS 'Brand/manufacturer of the device';
COMMENT ON COLUMN devices.state IS 'Current state: AVAILABLE, IN_USE, or INACTIVE';
COMMENT ON COLUMN devices.creation_time IS 'Timestamp when the device was created (immutable)';

-- Create primary index (automatically created with PRIMARY KEY, but explicit for clarity)
CREATE INDEX idx_devices_id ON devices(id);
