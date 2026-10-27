create table outbox_events(
    id varchar(36) primary key,
    aggregate_type varchar(64) not null,
    aggregate_id varchar(64) not null,
    event_type varchar(64) not null,
    payload JSON not null,
    created_at timestamp not null default current_timestamp,
    processed boolean not null default false
);

create index idx_outbound_unprocessed on outbox_events(processed,created_at);