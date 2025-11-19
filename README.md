📚 API Documentation
Items Management
Create Item
http
POST /api/items
Content-Type: application/json

{
  "name": "iPhone 15",
  "description": "Latest Apple smartphone",
  "basePrice": 999.99
}
Create Item with Variants
http
POST /api/items/with-variants
Content-Type: application/json

{
  "name": "iPhone 15",
  "description": "Latest Apple smartphone",
  "basePrice": 999.99,
  "variants": [
    {
      "sku": "IP15-128-BLK",
      "color": "Black",
      "price": 999.99,
      "stockQuantity": 50,
      "minStockLevel": 5
    }
  ]
}
Get All Items
http
GET /api/items
Get Item by ID
http
GET /api/items/1
Update Item
http
PUT /api/items/1
Content-Type: application/json

{
  "name": "iPhone 15 Pro",
  "description": "Updated description",
  "basePrice": 1199.99
}
Delete Item
http
DELETE /api/items/1
Variants Management
Create Variant
http
POST /api/variants
Content-Type: application/json

{
  "itemId": 1,
  "sku": "IP15-256-BLK",
  "color": "Black",
  "price": 1149.99,
  "stockQuantity": 30,
  "minStockLevel": 5
}
Get Variants by Item
http
GET /api/variants/item/1
Get Low Stock Variants
http
GET /api/variants/low-stock
Reserve Stock
http
POST /api/variants/1/reserve?quantity=3
Inventory Management
Add Stock
http
POST /api/inventory/add-stock
Content-Type: application/json

{
  "variantId": 1,
  "quantity": 10,
  "reason": "Restock from supplier",
  "reference": "PO-12345"
}
Remove Stock
http
POST /api/inventory/remove-stock
Content-Type: application/json

{
  "variantId": 1,
  "quantity": 2,
  "reason": "Customer sale",
  "reference": "SALE-67890"
}
Get Stock Movement History
http
GET /api/inventory/1/movements
🏗 Design Decisions
1. Layered Architecture
Controller Layer: Handles HTTP requests/responses and validation

Service Layer: Contains business logic and transaction management

Repository Layer: Data access and persistence

DTO Layer: Data transfer objects for API contracts

2. Database Design
Items Table: Core product information with base pricing

Variants Table: Specific product variations with individual pricing and stock

Stock Movements Table: Complete audit trail for inventory changes

3. API Design Choices
RESTful Principles: Consistent resource-based URLs

HTTP Status Codes: Proper status codes for different scenarios

JSON Request/Response: Standardized data format

Validation: Comprehensive input validation with meaningful error messages

4. Stock Management Strategy
Real-time Tracking: Stock levels updated immediately on movements

Movement History: Every change recorded for audit purposes

Reservation System: Prevent overselling with stock reservation

Low Stock Alerts: Proactive inventory management

5. Error Handling
Global Exception Handler: Consistent error response format

Specific Exceptions: ResourceNotFound, DuplicateResource, InsufficientStock

Validation Errors: Detailed field-level validation messages

🤔 Assumptions
Business Assumptions
Product Structure: Items can have multiple variants with different attributes

Pricing: Each variant can have its own price, independent of base item price

Stock Tracking: Inventory is managed at the variant level, not item level

Stock Movements: All inventory changes are recorded for audit purposes

Low Stock Definition: Stock is considered low when quantity ≤ minimum stock level but > 0

Technical Assumptions
Single Warehouse: System designed for a single warehouse inventory

No User Authentication: Basic system without user management

Synchronous Processing: All operations are synchronous

MySQL Database: Relational database with ACID properties

REST API: Stateless API without session management

🗃 Database Schema
sql
-- Items table
CREATE TABLE items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    base_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Variants table  
CREATE TABLE variants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,
    size VARCHAR(50),
    color VARCHAR(50),
    material VARCHAR(100),
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    min_stock_level INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- Stock movements table
CREATE TABLE stock_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    variant_id BIGINT NOT NULL,
    movement_type ENUM('IN', 'OUT', 'ADJUSTMENT') NOT NULL,
    quantity INT NOT NULL,
    reason VARCHAR(255),
    reference VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (variant_id) REFERENCES variants(id) ON DELETE CASCADE
);
