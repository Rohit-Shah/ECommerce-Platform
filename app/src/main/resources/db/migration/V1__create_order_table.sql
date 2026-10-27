create table orders (
    id varchar(36) primary key,
    customer_id varchar(36) not null,
    total_amount numeric(12,2) not null,
    status varchar(32) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table order_items (
    id varchar(36) primary key,
    order_id varchar(36) not null references orders(id) on delete cascade,
    product_sku varchar(64) not null,
    quantity int not null check (quantity > 0),
    unit_price numeric(12,2) not null
);

create index idx_order_customer_id on orders(customer_id);
create index idx_order_items_order_id on order_items(order_id);