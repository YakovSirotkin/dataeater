create table t_message(
    message_id serial primary key,
    device_id varchar(50),
    device_time timestamptz,
    db_time timestamptz default current_timestamp,
    message text);
