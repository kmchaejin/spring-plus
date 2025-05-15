create table users (
    id bigint primary key AUTO_INCREMENT,
    created_at datetime(6),
    modified_at datetime(6),
    email varchar(255),
    nickname varchar(255),
    password varchar(255),
    user_role varchar(5)
);