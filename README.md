# Operating-System-Projects
Projects from class
### Language Used ###
- PostgreSQL
- Java
### Description ###
This term project was an implementation of a railway database system. The database contains tables of:

```
stations, (station id, name, address, start/end operation time)

trains, (train id, name, description, total seats, speed, cost per km)

railline, (rail id, station id[], speed limit. A railline is a set of contiguous stations, and it has a speed limit)

route, (route id. Unlike a railline, a route is a path that a train travel through. 
                  Each train has to follow a specific route, but may travel through multiple raillines.)
                  
routeInfo, (route id, station id, station order, is stop(true or false). A route may pass though a station but does not stop at it)

routeScheds, (route id, train id, seats available, operating time)

customers, (customer id, name, address, number)

bookings, (booking id, route id, train id, customer id, operating time)
```

A user can achieve multiple operations through user interface, including:

```
Add a new customer.

Edit a customer.

View a customer's information.

Given two stations, find all routes that pass through both of them. (no transfer).

Given two stations, find all routes combination that pass through stations but have one transfer (from a route to another route).

Reserve a seat on a specific route schedule.

Find all trains that pass through a specific station at a specific time.

Find the routes that travel more than one rail line.

Find routes that pass through the same STATIONS but don’t have the same STOPS.

Find any stations through which all trains pass through.

Find all the trains that do not stop at a specific station.

Find routes that stop at least at XX% of the Stations they visit.

Display the schedule of a route.

Find the availability of a route at every stop on a specific day and time.
```

### File Description ###
 - db.sql: used to create tables

 - data.sql: contains all the data of the railway system

 - functions.sql: contains all functions mentioned above

 - Database.java: user interface


---
