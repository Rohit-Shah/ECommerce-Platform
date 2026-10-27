create table inventory(
    sku varchar(64) primary key,
    available_stock int not null check (available_stock >= 0),
    reserved_stock int not null check (reserved_stock >= 0),
    version bigint not null default 0,
    updated_at timestamp default current_timestamp
);

insert into inventory (sku, available_stock, reserved_stock, version)
values ('IPHONE-15-PRO', 10, 0, 0);