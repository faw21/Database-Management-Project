--1.1.1
create or replace function Add_customer(fnam varchar(15),lnam varchar(15),stree varchar(30),
tow varchar(30), postal varchar(15))
  Returns INTEGER as
$$
Declare
  max_custID integer;
BEGIN
  select max(cid) into max_custID
  from customers;

  Insert into customers
  values (max_custID +1,fnam ,lnam, stree, tow, postal);
  return max_custID+1;
End
$$
  Language plpgsql;

--1.2.1
create or replace function drop_tables()
  returns void as $$
  BEGIN
    drop table if exists dt,rid_day,arrivals,destinations, combination_table,t1,t2,
      temp_tab, temp_tab2, ret1, ret2, ret3, ret4 ,join1, arrivalsRoutes, destinationsRoutes, arrivalstaorder, deststaorder,
      aaa, bbb, ccc;
  end;
  $$ language plpgsql;


create or replace function single_route_trip_search(arrival integer, destination integer, weekday varchar (10))
  Returns table (ret_rid integer) as $$
  BEGIN
    execute drop_tables();
    create temp table dt as select routescheds.rid, routescheds.op_time from routescheds where day=weekday and seatsavailable>0;
    create temp table rid_day as
      (select r.rid, r.sid, r.stationorder, r.isstop, dt.op_time from routeinfo as r inner join dt on r.rid=dt.rid);
    delete from rid_day where isstop = false;
    create temp table arrivals as (select rid_day.rid,rid_day.op_time, rid_day.stationorder as sorder1 from rid_day where sid = arrival);
    create temp table destinations as (select rid_day.rid,rid_day.op_time, rid_day.stationorder as sorder2 from rid_day where sid = destination);
    return query
      select arrivals.rid from (arrivals inner JOIN destinations on (arrivals.rid = destinations.rid and arrivals.op_time = destinations.op_time)
        and arrivals.sorder1 <destinations.sorder2);
  end;
  $$ language plpgsql;
select * from single_route_trip_search(1,40,'Monday');
-- 1.2.2
create or replace function combination_route_trip_search(arrival integer, destination integer, weekday varchar (10))
  Returns table (rid1 integer, rid2 integer, transferStation integer) as $$
  BEGIN
    execute drop_tables();
    create temp table dt as select routescheds.rid, routescheds.op_time from routescheds where day=weekday and seatsavailable>0;
    create temp table rid_day as
      (select r.rid,r.sid,r.stationorder,r.isstop, dt.op_time from routeinfo as r inner join dt on r.rid=dt.rid);
    delete from rid_day where isstop = false;
    create temp table arrivalsRoutes as select rid_day.rid,rid_day.op_time from rid_day where sid = arrival;

    create temp table arrivals as (select rid_day.rid,rid_day.sid, rid_day.stationorder as sorder1 from(rid_day inner join arrivalsRoutes
      on (rid_day.rid = arrivalsRoutes.rid and rid_day.op_time = arrivalsRoutes.op_time)));

    create temp table arrivalstaorder as select arrivals.rid, arrivals.sorder1 from arrivals where arrivals.sid = arrival;

    create temp table destinationsRoutes as select rid_day.rid,rid_day.op_time from rid_day where sid = destination;

    create temp table destinations as (select rid_day.rid,rid_day.sid, rid_day.stationorder as sorder2 from (rid_day inner join destinationsRoutes
      on (rid_day.rid = destinationsRoutes.rid and rid_day.op_time = destinationsRoutes.op_time)));

    create temp table deststaorder as select destinations.rid, destinations.sorder2 from destinations where destinations.sid = destination;

    create temp table combination_table as (select arrivals.rid as arid,destinations.rid as drid,arrivals.sid as asid,destinations.sid as dsid,
                                                   arrivals.sorder1 as aorder, destinations.sorder2 as dorder
      from arrivals cross join destinations);-- on arrivals.rid <> destinations.rid);

    delete from combination_table where arid=drid;
    delete from combination_table where (asid = arrival or dsid = destination or
      asid=destination or dsid=arrival);
    --delete from combination_table where (aorder <= arrivalstaorder.asorder and arid = arrivalstaorder.rid);
      create temp table aaa as select arid, drid, asid, dsid, dorder from(combination_table inner join arrivalstaorder on (combination_table.aorder > arrivalstaorder.sorder1 and combination_table.arid = arrivalstaorder.rid));
      create temp table bbb as select arid, drid, asid, dsid from(aaa inner join deststaorder on (aaa.dorder < deststaorder.sorder2 and aaa.drid = deststaorder.rid));
      create temp table ccc as
        select distinct arid, drid, asid, dsid
        from bbb;
    return query
      select arid,drid,asid
        from ccc where asid=dsid;
  end;
  $$ language plpgsql;

--select * from combination_route_trip_search(3, 30, 'Wednesday');

create or replace function drop_seq()
  returns void as $$
  BEGIN
    drop table if exists temp_tab3,temp_tab4,t3,t4;
  end;
  $$ language plpgsql;


create or replace function get_single_sequence(routeid integer, ar_st integer, de_st integer)
  Returns table (curst integer, targst integer) as $$
  Declare
    max1 integer;
    max2 integer;
  BEGIN
    execute drop_seq();
    create temp table temp_tab3 as (select sid, stationorder from routeinfo where rid=routeid);
    create temp table temp_tab4 as (select sid,stationorder from temp_tab3 where (stationorder between
      (select stationorder from temp_tab3 where sid=ar_st) And (select stationorder from temp_tab3 where sid=de_st)));
    create temp table t3 as select * from temp_tab4;
    create temp table t4 as select * from temp_tab4;
    delete from t3 where sid=ar_st;
    delete from t4 where sid=de_st;
    max1 = (select max(stationorder) from t3);
    max2 = (select max(stationorder) as max2 from t4);

    if max1>max2 then
      return query
        (select t3.sid as cur_st,t4.sid as targ_st from t3 join t4 on
          t3.stationorder = t4.stationorder+1);
    ELSE
      return query
        (select t3.sid as cur_st,t4.sid as targ_st from t3 join t4 on
          t3.stationorder = t4.stationorder-1);
    END IF;
  end;
  $$ language plpgsql;
--1.2.4.7, 1.2.4.8
create or replace function single_route_trip_dist(arrival integer, destination integer, weekday varchar (10))
  Returns table (rid integer,dist integer) as $$
  declare
    dist5 integer;
  BEGIN
    execute drop_tables();
    create temp table dt as select routescheds.rid from routescheds where day=weekday;
    create temp table rid_day as
      (select r.rid,r.sid,r.stationorder,r.isstop from routeinfo as r inner join dt on r.rid=dt.rid);
    delete from rid_day where isstop = false;
    create temp table arrivals as (select rid_day.rid,rid_day.stationorder as sorder1 from rid_day where sid = arrival);
    create temp table destinations as (select rid_day.rid,rid_day.stationorder as sorder2 from rid_day where sid = destination);

    create temp table ret1 as (select arrivals.rid from (arrivals inner JOIN destinations on arrivals.rid = destinations.rid
        and arrivals.sorder1 <destinations.sorder2));
    --instead call single_route_trip_search(...);
    create temp table ret2 (rids integer,dists integer);

      while (select count(ret1.rid) from ret1)<>0 LOOP
        drop table if exists t1, join1;
        create temp table t1 as
          select * from get_single_sequence((select min(ret1.rid) from ret1),arrival, destination);
        create temp table join1 as
          select * from t1 as t join distances as d on (t.curst = d.curr_station and t.targst = d.target_station);

        Insert into ret2(rids,dists) values ((select min(ret1.rid) from ret1),(select sum(distance) from join1));

        delete from ret1 where ret1.rid =(select min(ret1.rid) from ret1);
        delete from t1;
    end loop;
    return query
      select * from ret2;
  end;
  $$ language plpgsql;

--1.2.4.1
create or replace function get_stop_amt(route_id integer, ar_st integer, de_st integer)
  Returns integer as $$
  declare
    --ret4 integer;
  BEGIN
    drop table if exists temp_tab,temp_tab2;
    create temp table temp_tab as (select sid, stationorder from routeinfo where rid=route_id and isstop);
    create temp table temp_tab2 as (select temp_tab.sid,temp_tab.stationorder from temp_tab where (temp_tab.stationorder between
      (select stationorder from temp_tab where temp_tab.sid=ar_st) And (select stationorder from temp_tab where temp_tab.sid=de_st)));
    --(select count(*) from temp_tab2);
    return (select count(*) from temp_tab2);
  end;
  $$ language plpgsql;

create or replace function single_route_trip_stops(arrival integer, destination integer, weekday varchar (10))
  Returns table (rid integer,num_stop integer) as $$
  BEGIN
    execute drop_tables();
    create temp table dt as select routescheds.rid from routescheds where day=weekday;
    create temp table rid_day as
      (select r.rid,r.sid,r.stationorder,r.isstop from routeinfo as r inner join dt on r.rid=dt.rid);
    delete from rid_day where isstop = false;
    create temp table arrivals as (select rid_day.rid,rid_day.stationorder as sorder1 from rid_day where sid = arrival);
    create temp table destinations as (select rid_day.rid,rid_day.stationorder as sorder2 from rid_day where sid = destination);

    create temp table ret1 as (select arrivals.rid from (arrivals inner JOIN destinations on arrivals.rid = destinations.rid
        and arrivals.sorder1 <destinations.sorder2));

    --instead call single_route_trip_search(...);
    create temp table ret2 (rids integer,nstop1 integer);

      while (select count(ret1.rid) from ret1)<>0 LOOP
        Insert into ret2(rids,nstop1) values ((select min(ret1.rid) from ret1),
                            (get_stop_amt((select min(ret1.rid) from ret1),arrival,destination)));
        delete from ret1 where ret1.rid=(select min(ret1.rid) from ret1);
    end loop;
    return query
      select * from ret2;
  end;
  $$ language plpgsql;

--1.2.4.2
create or replace function get_station_amt(route_id integer, ar_st integer, de_st integer)
  Returns integer as $$
  declare
    ret4 integer;
  BEGIN
    drop table if exists temp_tab,temp_tab2;
    create temp table temp_tab as (select sid, stationorder from routeinfo where rid=route_id);
    create temp table temp_tab2 as (select sid,stationorder from temp_tab where (stationorder between
      (select stationorder from temp_tab where sid=ar_st) And (select stationorder from temp_tab where sid=de_st)));
    ret4 :=  (select count(*) from temp_tab2);
    return ret4;
  end;
  $$ language plpgsql;


create or replace function single_route_trip_stations(arrival integer, destination integer, weekday varchar (10))
  Returns table (rid integer,num_stations integer) as $$
  BEGIN
    execute drop_tables();
    create temp table dt as select routescheds.rid from routescheds where day=weekday;
    create temp table rid_day as
      (select r.rid,r.sid,r.stationorder,r.isstop from routeinfo as r inner join dt on r.rid=dt.rid);
    delete from rid_day where isstop = false;
    create temp table arrivals as (select rid_day.rid,rid_day.stationorder as sorder1 from rid_day where sid = arrival);
    create temp table destinations as (select rid_day.rid,rid_day.stationorder as sorder2 from rid_day where sid = destination);

    create temp table ret1 as (select arrivals.rid from (arrivals inner JOIN destinations on arrivals.rid = destinations.rid
        and arrivals.sorder1 <destinations.sorder2));

    --ret 1 has all valid rids with the trip characteristics
    create temp table ret2 (rids integer,nstat1 integer);
    while (select count(ret1.rid) from ret1)<>0 LOOP
      Insert into ret2(rids,nstat1) values ((select min(ret1.rid) from ret1),
                              (get_station_amt((select min(ret1.rid) from ret1),arrival,destination)));
        delete from ret1 where ret1.rid=(select min(ret1.rid) from ret1);
    end loop;
    return query
      select * from ret2;
  end;
  $$ language plpgsql;

--1.2.4.3 & 1.2.4.4
--price
--drop function single_route_trip_price(arrival integer, destination integer, weekday varchar);
create or replace function single_route_trip_price(arrival integer, destination integer, weekday varchar (10))
  Returns table (rid integer,price double precision) as $$
  BEGIN
    execute drop_tables();
    create temp table dt as select routescheds.rid,routescheds.tid from routescheds where day=weekday;
    create temp table rid_day as
      (select r.rid,r.sid,r.stationorder,r.isstop from routeinfo as r inner join dt on r.rid=dt.rid);
    delete from rid_day where isstop = false;
    create temp table arrivals as (select rid_day.rid,rid_day.stationorder as sorder1 from rid_day where sid = arrival);
    create temp table destinations as (select rid_day.rid,rid_day.stationorder as sorder2 from rid_day where sid = destination);

    create temp table ret1 as (select arrivals.rid from (arrivals inner JOIN destinations on arrivals.rid = destinations.rid
        and arrivals.sorder1 <destinations.sorder2));

    --ret 1 has all valid rids with the trip characteristics
    create temp table ret2 (rids integer,dists integer);
      while (select count(ret1.rid) from ret1)<>0 LOOP
        drop table if exists t1, join1;
        create temp table t1 as
          select * from get_single_sequence((select min(ret1.rid) from ret1),arrival, destination);
        create temp table join1 as
          select * from t1 as t join distances as d on (t.curst = d.curr_station and t.targst = d.target_station);

        Insert into ret2(rids,dists) values ((select min(ret1.rid) from ret1),(select sum(join1.distance) from join1));

        delete from ret1 where ret1.rid =(select min(ret1.rid) from ret1);
        delete from t1;
    end loop;
    --ret2 has rid and distances
    --dt has rids and tids
      create temp table ret3 as select ret2.rids as rid, ret2.dists as dists, dt.tid from ret2 join dt on ret2.rids = dt.rid;
      create temp table ret4 as select ret3.rid, ret3.dists, ret3.dists*cost_per_km as price
        from(ret3 join trains on ret3.tid = trains.tid);
    return query
      select ret4.rid, ret4.price from ret4;
  end;
  $$ language plpgsql;

--1.2.4.5 & 1.2.4.6 time
create or replace function single_route_trip_time(arrival integer, destination integer, weekday varchar (10))
  Returns table (rid integer,duration double precision) as $$
  BEGIN
    execute drop_tables();
    create temp table dt as select routescheds.rid,routescheds.tid from routescheds where day=weekday;
    create temp table rid_day as
      (select r.rid,r.sid,r.stationorder,r.isstop from routeinfo as r inner join dt on r.rid=dt.rid);
    delete from rid_day where isstop = false;
    create temp table arrivals as (select rid_day.rid,rid_day.stationorder as sorder1 from rid_day where sid = arrival);
    create temp table destinations as (select rid_day.rid,rid_day.stationorder as sorder2 from rid_day where sid = destination);

    create temp table ret1 as (select arrivals.rid from (arrivals inner JOIN destinations on arrivals.rid = destinations.rid
        and arrivals.sorder1 <destinations.sorder2));

    --ret 1 has all valid rids with the trip characteristics
    create temp table ret2 (rids integer,dists integer);
      while (select count(ret1.rid) from ret1)<>0 LOOP
        drop table if exists t1, join1;
        create temp table t1 as
          select * from get_single_sequence((select min(ret1.rid) from ret1),arrival, destination);
        create temp table join1 as
          select * from t1 as t join distances as d on (t.curst = d.curr_station and t.targst = d.target_station);

        Insert into ret2(rids,dists) values ((select min(ret1.rid) from ret1),(select sum(join1.distance) from join1));

        delete from ret1 where ret1.rid =(select min(ret1.rid) from ret1);
        delete from t1;
    end loop;
    --ret2 has rid and distances
    --dt has rids and tids
      create temp table ret3 as select ret2.rids as rid, ret2.dists as dists, dt.tid from ret2 join dt on ret2.rids = dt.rid;
      create temp table ret4 as select ret3.rid, ret3.dists*cost_per_km as time
        from(ret3 join trains on ret3.tid = trains.tid);
    return query
      select * from ret4;



  end;
  $$ language plpgsql;


--Add Reservation 1.2.5 (created trigger to block if full or add and update available seats
create or replace function edit_seatsAvailable()
  Returns trigger AS
$$
BEGIN
  Update routescheds
  set seatsAvailable = seatsAvailable - 1
  where new.rID = rID and new.tID=tID and new.day = day and new.op_time = op_time;

  return new;
End
$$
  Language plpgsql;

drop trigger if exists ed_seatsAvailable on routescheds;
Create Trigger ed_seatsAvailable
  After INSERT
  on bookings
  for each row
EXECUTE PROCEDURE edit_seatsAvailable();

create or replace function full_booking()
  Returns trigger AS
$$
Begin
  If  ((select Distinct seatsAvailable from routescheds
      where new.rID = rID and new.tID=tID and (new.day = day) and new.op_time = op_time)=0) then
    return null;
  ELSE
    return new;
  End IF;
End
$$
  Language plpgsql;

drop trigger if exists block_reservation on routescheds;
Create trigger block_reservation
  before INSERT
  on bookings
  for each row
Execute procedure full_booking();

--insert into bookings (rid, day, op_time, tid, cid) values (22, 'Saturday', '02:28:00', 328, 100706);

--1.3.1 all trains that pass through a specific station at a given day/time
--need to consider specific stop time at a station
create or replace function find_trains(stationid integer,dayid char(10),hour time)
  returns table(ret_tid integer) as $$
  begin
    drop table if exists temp_tab1, temp_tab2, ret_sids;
    create temp table temp_tab1 as select routescheds.rid as rid1, routescheds.tid as tid1,routescheds.op_time as otime1
      from routescheds where routescheds.day = dayid;
    create temp table temp_tab2 as select tid1, routeinfo.sid as sid1
      from temp_tab1 join routeinfo on rid1 = routeinfo.rid ;
    return query
      select tid1 from temp_tab2 where sid1 =stationid;
  end;
  $$ language plpgsql;
select * from find_trains(2, 'Saturday', time '02:28:00') order by ret_tid; -- should return 328, takes route 22



--1.3.2 all routes that travel on more than one rail line
create or replace function find_multi_line()
  returns table(ret_rid integer) as $$
  declare
    routeID integer;
    count1 integer;
    count2 integer;
  begin
    drop table if exists temp_tab1, ret_tab, stations_of_route, sta_and_rail ;
    create temp table temp_tab1 as select * from routes;
    create temp table ret_tab (rids integer);

    while(select count(temp_tab1.rid) from temp_tab1)>0 loop
      drop table if exists stations_of_route, sta_and_rail;
      routeID = (select min(temp_tab1.rid) from temp_tab1);
      create temp table stations_of_route as select rid, sid from routeinfo where rid = routeID;
      create temp table sta_and_rail as select rid, stations_of_route.sid, railid from (railline_info inner join stations_of_route on railline_info.sid = stations_of_route.sid);
      count1 = 1;
      count2 = 0;
      loop
        if (select count(railid) from (select railid from sta_and_rail where sta_and_rail.railid = count1) as t1)>1 then
          count2 = count2 + 1;
        end if;
        if(count2>=2) then
          insert into ret_tab(rids) values (routeID);
          exit;
        end if;
        count1 = count1 + 1;
        if(count1>(select count(railid) from raillines)) then
          exit;
        end if;
      end loop;
    delete from temp_tab1 where temp_tab1.rid = routeID;
    end loop;
    return query
      select * from ret_tab;
  end;
  $$ language plpgsql;

--select * from find_multi_line();

--1.3.3 find the seemingly similar routes that differ by at least 1 stop
create or replace function find_similar_routes(routeID integer)
  returns table(ret_rid integer) as $$
  declare
    route integer;
    issimilar boolean;
    issame boolean;
    tempvar1 integer;
    temprecord1 record;
    temprecord2 record;
  begin
    drop table if exists temp_tab1,temp_tab2,temp_tab3,ret_tab;
    create temp table temp_tab1 as select * from routeinfo where rid = routeID order by stationorder;
    create temp table temp_tab2 as select * from routes;
    delete from temp_tab2 where temp_tab2.rid = routeID;
    create temp table ret_tab (rids integer);
    for temprecord1 in select * from temp_tab2 order by rid loop
      drop table if exists temp_tab1, temp_tab3;
      create temp table temp_tab1 as select * from routeinfo where rid = routeID order by stationorder;
      create temp table temp_tab3 as select * from routeinfo where routeinfo.rid = temprecord1.rid;
      issimilar = true;
      issame = true;
      for temprecord2 in select * from temp_tab3 order by temp_tab3.stationorder loop
        if(temprecord2.sid = (select temp_tab1.sid from temp_tab1 where (temp_tab1.stationorder = (select min(temp_tab1.stationorder) from temp_tab1)))) then
          if (temprecord2.isstop != (select temp_tab1.isstop from temp_tab1 where temp_tab1.sid = temprecord2.sid)) then
            issame = false;
          end if;

        else
          issimilar = false;
        end if;
        delete from temp_tab1 where temp_tab1.stationorder = (select min(temp_tab1.stationorder) from temp_tab1);

        if(issimilar=false) then exit;
        end if;
        --if ((select count(temp_tab1.sid)from temp_tab1) = 0) then exit;
        --end if;
      end loop;

      if(issimilar = true and issame = false) then
        insert into ret_tab (rids) values (temprecord1.rid);
      end if;

    end loop;

    return query
      select * from ret_tab;
  end;
  $$language plpgsql;

--should return route 380
select * from find_similar_routes(11);


--1.3.4 find all the stations which every train passes through
create or replace function stations_all_trains()
  returns table(ret_sid integer) as $$
  begin
    drop table if exists temp_tab1, temp_tab2, ret_sids;
    create temp table temp_tab1 as select routescheds.rid as rid1, routescheds.tid as tid1 from routescheds;
    create temp table temp_tab2 as select tid1, routeinfo.sid as sid1 from temp_tab1 join routeinfo on rid1 = routeinfo.rid ;
    create temp table ret_sids as select sid1, count(distinct tid1) as ct from temp_tab2 group by sid1;
    return query
      select sid1 from ret_sids where ct = (select count(trains.tid) from trains);
  end;
  $$ language plpgsql;
--select * from stations_all_trains();

--1.3.5 all the trains that do not stop at specific station
create or replace function search_no_stop(station_num integer)
  returns table(ret_tid integer) as $$
  begin
    drop table if exists temp_tab1, temp_tab2, ret_tids,t;
    create temp table temp_tab1 as select routescheds.rid as rid1, routescheds.tid as tid1 from routescheds;
    create temp table temp_tab2 as select tid1 from temp_tab1 join (select * from routeinfo where routeinfo.sid = station_num) as r
      on rid1 = r.rid and r.isstop=true;

    create temp table t as select trains.tid from trains;
    while (select count(temp_tab2.tid1) from temp_tab2)<>0 LOOP
        delete from t where t.tid = (select min(tid1) from temp_tab2);
        delete from temp_tab2 where tid1 = (select min(tid1) from temp_tab2);
    end loop;
    return query
      select * from t;
  end;
  $$ language plpgsql;
--select * from search_no_stop(10);

--drop function search_XX_stop(xx double precision);
--1.3.6 routes that stop at at least xx% of stations they visit.
create or replace function search_XX_stop(xx double precision)
  returns table(ret_rid integer, percentage numeric(4,2)) as $$
  begin
    drop table if exists temp_tab, temp_tab1,join1,ret1;
    create temp table temp_tab as select r.rid,count(r.sid) as nstops from routeinfo as r where r.isstop = true group by r.rid;
    create temp table temp_tab1 as select r1.rid,count(r1.sid) as nstats from routeinfo as r1 group by r1.rid;
    create temp table join1 as select temp_tab.rid, nstops, nstats from temp_tab join temp_tab1 on temp_tab.rid = temp_tab1.rid;
    create temp table ret1 as select join1.rid,(cast(nstops as float) / cast(nstats as float)) as percentage from join1;
    return query
      select ret1.rid, cast(ret1.percentage as numeric(4,2)) as percent from ret1 where ret1.percentage>=xx;
  end;
  $$ language plpgsql;
select * from search_xx_stop(.50)

--1.3.7 Display schedule of a route
--for a specified route, display days hours and trains
create or replace function display_route_schedule(route_num integer)
  returns table(days char(10), hours time, t_num integer) as $$
  begin
    return query
      select r.day, r.op_time, r.tid from routescheds as r where r.rid = route_num;
  end;
  $$ language plpgsql;
--select * from display_route_schedule(50);

--1.3.8 Display availability of a route at every stop on a specific day/time combo
create or replace function route_availability(route_num integer, weekday varchar (10), hour time)
  returns table(seats_available integer, rid integer) as $$
  begin
    return query
      select r.seatsavailable, r.rid from routescheds as r where r.rid = route_num and r.op_time = hour and r.day = weekday and r.seatsavailable>0;
  end;
  $$ language plpgsql;
select * from route_availability(36, 'Wednesday', '08:30:00');

--select * from find_multi_line();
--select * from single_route_trip_search(1,4,'Sunday');
--select * from combination_route_trip_search(1,3,'Sunday');
--select * from single_route_trip_dist(1,4,'Sunday');
--select * from single_route_trip_stops(1,4,'Sunday');
--select * from single_route_trip_stations(1,4,'Sunday');
--select * from single_route_trip_price(1,4,'Sunday');
--select * from single_route_trip_search (1, 6, 'Saturday');