CREATE TABLE IF NOT EXISTS users (
  rut VARCHAR(50) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255),
  phone VARCHAR(50),
  password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
  id VARCHAR(50) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS sales (
  id VARCHAR(100) PRIMARY KEY,
  product_id VARCHAR(50),
  rut VARCHAR(50),
  status VARCHAR(50),
  signature VARCHAR(2000),
  created_at DATETIME,
  FOREIGN KEY (product_id) REFERENCES products(id),
  FOREIGN KEY (rut) REFERENCES users(rut)
);
