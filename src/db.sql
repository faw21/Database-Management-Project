drop table if exists stations cascade;
create table if not exists stations
(
  sID serial not null
    constraint station_pk
      primary key,
  name varchar(5) not null,
  op_hour_start time not null,
  op_hour_end   time not null,
  stop_delay integer,
  street  varchar(30) not null,
  town  varchar(30) not null,
  postalCode varchar(15) not null
);

create unique index if not exists s_number_unique
  on stations(sID);

drop table if exists customers cascade;
create table if not exists customers
(
  cID serial not null
    constraint customers_pk
      primary key,
  FN varchar(15),
  LN varchar(15),
  street  varchar(30) not null,
  town  varchar(30) not null,
  postalCode varchar(15) not null
);

create unique index if not exists cID_unique
  on customers(cID);

drop table if exists trains cascade;
create table if not exists trains
(
  tID serial not null
    constraint trains_pk
      primary key,
  name varchar(5) not null,
  Description varchar(50),
  totalSeats integer not null,
  speed integer not null,
  cost_per_km double precision not null
);

create unique index if not exists tID_unique
  on trains(tID);

drop table if exists raillines cascade;
create table if not exists raillines
(
  railID serial not null
    constraint raillines_pk
      primary key,
  speedLimit integer not null
);

create unique index if not exists railID_unique
  on raillines(railID);

drop table if exists railline_info cascade;
create table if not exists railline_info
(
  railID serial not null
    constraint railline_info_railID_fk references raillines
    on update cascade on delete cascade,
  sID serial not null
    constraint railline_info_sID_fk references stations
    on update cascade on delete cascade,
  constraint railline_info_pk
    primary key (railID, sID)
);

drop table if exists distances cascade;
create table if not exists distances
(
  curr_station integer not null
    constraint distances_curr_fk references stations(sID)
      on update cascade on delete cascade,
  target_station integer not null
    constraint distances_curr_fk2 references stations(sID)
      on update cascade on delete cascade,
  distance double precision not null,
  speed_limit integer,
  constraint distances_pk
    primary key (curr_station, target_station)
);



drop table if exists routes cascade;
create table if not exists routes
(
  rID serial not null
    constraint routes_pk
      primary key
)


drop table if exists routeinfo cascade;
create table if not exists routeinfo
(
  rID serial not null
    constraint routeinfo_rID_fk references routes
    on update cascade on delete cascade,
  sID serial not null
    constraint routeinfo_sID_fk references stations
    on update cascade on delete cascade,
  stationOrder integer not null,
  isStop boolean not null,
  constraint routeinfo_pk
		primary key (rID, sID)
);


drop table if exists routescheds cascade;
create table if not exists routescheds
(
  rID serial not null
    constraint routescheds_rID_fk references routes
    on update cascade on delete cascade,
  day char(10) not null,
  op_time time not null,
  tID serial not null
    constraint routescheds_tID_fk references trains
    on update cascade on delete cascade,
  seatsAvailable integer,
  constraint routescheds_pk
		primary key (rID, tID, day, op_time)
);

drop table if exists bookings cascade;
create table if not exists bookings
(
  rID serial not null,
  tID serial not null,
  day char(10) not null,
  op_time time not null,
  cID serial not null constraint bookings_cID_fk references customers,
  constraint bookings_fk
  foreign key (rID, tID, day, op_time)
  references routescheds
    on update cascade on delete cascade

  
)




