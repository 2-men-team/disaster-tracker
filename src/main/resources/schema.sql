create database if not exists disaster_tracker;

use disaster_tracker;

create table user (
    id int primary key auto_increment,
    api_key text not null ,
    access_token text,
    refresh_token text,
    expiration_time_in_millis long,
    notification_webhook_url varchar(150)
);

create table calendar_event (
    id int primary key auto_increment,
    user_id int not null ,
    google_id varchar(512) not null unique ,
    summary text,
    start datetime not null ,
    end datetime,
    location varchar(256) not null ,
    latitude double not null ,
    longitude double not null
);

create table disaster_event (
    id int primary key auto_increment,
    description text,
    external_id varchar(25) unique not null ,
    is_active boolean not null ,
    start datetime not null ,
    end datetime,
    latitude double not null ,
    longitude double not null
);

create table warning (
    id int primary key auto_increment,
    user_id int not null ,
    calendar_event_id int not null ,
    disaster_event_id int not null ,
    created_at datetime not null
);