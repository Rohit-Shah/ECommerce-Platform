alter table outbox_events
    add column status VARCHAR(32) not null default 'PENDING',
    add column retry_count int not null default 0,
    add column updated_at timestamp not null default current_timestamp on update current_timestamp;

update outbox_events
    set status = case when processed = true then 'PUBLISHED' else 'PENDING' end;

alter table outbox_events
    drop column processed;

create index idx_outbox_status_created on outbox_events (status,created_at);