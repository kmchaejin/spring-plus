create table users (
    id bigint auto_increment,
    created_at datetime(6),
    modified_at datetime(6),
    email varchar(255),
    nickname varchar(255),
    password varchar(255),
    user_role varchar(5)
)