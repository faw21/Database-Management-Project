import java.util.*;
import java.io.*;
import java.sql.*;

public class Database{
	private static Connection con;
	private Statement stmt;
	private PreparedStatement pstmt;
	private ResultSet rs;
	private String query;
	//add new customer information
	private void addCustomer(){
		Scanner inScan = new Scanner(System.in);
		try{
			query = "select * from add_customer(?,?,?,?,?)";
			pstmt = con.prepareStatement(query);
			System.out.println("Please type your first name:");
			pstmt.setString(1, inScan.nextLine());
			System.out.println("Please type your last name:");
			pstmt.setString(2, inScan.nextLine());
			System.out.println("Please type your street address:");
			pstmt.setString(3, inScan.nextLine());
			System.out.println("Please type your town:");
			pstmt.setString(4, inScan.nextLine());
			System.out.println("Please type your postal code:");
			pstmt.setString(5, inScan.nextLine());

			rs = pstmt.executeQuery();
			if(rs.next())
				System.out.println("You have successfully signed up! Your customer id is: " + rs.getInt(1));
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	//Edit customer information
	private void editCustomer(){
		Scanner inScan = new Scanner(System.in);
		int id = 0;
		int input = 0;
		String newInfo = "";
		boolean keepLoop = true;
		while(keepLoop){
			System.out.println("What is your 6-digits customer ID?");
			try{
				id = Integer.parseInt(inScan.nextLine());
				if(id<100000||id>999999) throw new Exception();

				keepLoop = false;
			}
			catch(Exception e){
				System.out.println("You must type a valid input.");
			}
		}

		try{
			query = "select * from customers where cid = ?";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, id);
			rs = pstmt.executeQuery();
			if(!rs.next()) System.out.println("the customer ID does not exist.");
			else{
				System.out.println("Here is your information:");
				System.out.println("Customer ID: " + rs.getInt(1));
				System.out.println("Name: " + rs.getString(2) + " " + rs.getString(3));
				System.out.println("Address: " + rs.getString(4));
				System.out.println("Town: " + rs.getString(5));
				System.out.println("Postal Code: " + rs.getString(6));
			}
		}
		catch(Exception e){
			System.out.println(e);
		}
		keepLoop = true;
		while(keepLoop){
			System.out.println("What information do you want to modify?(1-5): \n(1.first name, 2.last name, 3.address, 4.town, 5.postal code) ");
			try{
				input = Integer.parseInt(inScan.nextLine());
				if(input>5||input<1) throw new Exception();
				while(true){
					System.out.println("Enter your new information: ");
					newInfo = inScan.nextLine();
					if(input==1) query = "update customers set fn = ? where cid = ?";
					if(input==2) query = "update customers set ln = ? where cid = ?";
					if(input==3) query = "update customers set street = ? where cid = ?";
					if(input==4) query = "update customers set town = ? where cid = ?";
					if(input==5) query = "update customers set postalcode = ? where cid = ?";
					pstmt = con.prepareStatement(query);
					
					pstmt.setString(1, newInfo);
					pstmt.setInt(2, id);
					pstmt.executeUpdate();
					System.out.println("Press 'c' to continue changing your information, or press any other key to quit.");
					if(!inScan.nextLine().equals("c")) keepLoop = false;
					break;
				}
			}
			catch(Exception e){
				System.out.println(e);
			}
		}
	}
	//To view the customer information
	private void viewCustomer(){
		Scanner inScan = new Scanner(System.in);
		int id = 0;
		boolean keepLoop = true;
		while(keepLoop){
			System.out.println("What is your 6-digits customer ID?");
			try{
				id = Integer.parseInt(inScan.nextLine());
				if(id<100000||id>999999) throw new Exception();

				keepLoop = false;
			}
			catch(Exception e){
				System.out.println("You must type a valid input.");
			}
		}

		

		try{
			query = "select * from customers where cid = ?";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, id);
			rs = pstmt.executeQuery();
			if(!rs.next()) System.out.println("the customer ID does not exist.");
			else{
				System.out.println("Here is your information:");
				System.out.println("Customer ID: " + rs.getInt(1));
				System.out.println("Name: " + rs.getString(2) + " " + rs.getString(3));
				System.out.println("Address: " + rs.getString(4));
				System.out.println("Town: " + rs.getString(5));
				System.out.println("Postal Code: " + rs.getString(6));
			}
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	//1.2.1. single route search which has no transfer
	private void singleSearch(){
		Scanner inScan = new Scanner(System.in);
		int asid = 0;
		int dsid = 0;
		int selection = 0;
		String day = "";
		boolean keepLoop = true;
		while(keepLoop){
			try{
				System.out.println("What is your departure station?(1-50)");
				asid = Integer.parseInt(inScan.nextLine());
				if(asid<1||asid>50) throw new Exception();

				System.out.println("What is your destination station?(1-50)");
				dsid = Integer.parseInt(inScan.nextLine());
				if(dsid<1||dsid>50||asid==dsid) throw new Exception();

				System.out.println("What day?(Mon-Sun, full word with first char capital)");
				day = inScan.nextLine();
				if(!day.equals("Monday")&&!day.equals("Tuesday")&&!day.equals("Wednesday")&&!day.equals("Thursday")
					&&!day.equals("Friday")&&!day.equals("Saturday")&&!day.equals("Sunday")) throw new Exception();

				System.out.println("How would you like the results to be sorted(1-9)?\n"
									+ "\t1. no sorting\n" + "\t2. stops(ascending)\n" + "\t3.stations(descending)\n"
									 + "\t4. price(ascending)\n" + "\t5. price(descending)\n" + "\t6. timespan(ascending)\n"
									  + "\t7. timespan(descending)\n" + "\t8. distances(ascending)\n" + "\t9. distances(descending)  ");
				selection = Integer.parseInt(inScan.nextLine());
				if(selection<1||selection>9) throw new Exception();
				keepLoop = false;
			}
			catch(Exception e){
				System.out.println("You must type a valid input.");
			}
		}

		try{
			switch (selection){
				case 1: query = "select * from single_route_trip_search(?,?,?)";
				break;
				case 2: query = "select * from single_route_trip_stops(?,?,?) order by num_stop";
				break;
				case 3: query = "select * from single_route_trip_stations(?,?,?) order by num_stations desc";
				break;
				case 4: query = "select * from single_route_trip_price(?,?,?) order by price";
				break;
				case 5: query = "select * from single_route_trip_price(?,?,?) order by price desc";
				break;
				case 6: query = "select * from single_route_trip_time(?,?,?) order by duration";
				break;
				case 7: query = "select * from single_route_trip_time(?,?,?) order by duration desc";
				break;
				case 8: query = "select * from single_route_trip_dist(?,?,?) order by dist";
				break;
				case 9: query = "select * from single_route_trip_dist(?,?,?) order by dist desc";
				break;
			}
			
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, asid);
			pstmt.setInt(2, dsid);
			pstmt.setString(3, day);
			rs = pstmt.executeQuery();
			int counter = 0;
			while(rs.next()){
				counter++;
				if(counter==1)System.out.println("Results that pass from " + asid + " and " + dsid + ": ");
				if(selection==1) System.out.println(counter + ". Route " + rs.getInt(1));
				if(selection==2) System.out.println(counter + ". Route " + rs.getInt(1) + " Stop number: " + rs.getInt(2));
				if(selection==3) System.out.println(counter + ". Route " + rs.getInt(1) + " Station number: " + rs.getInt(2));
				if(selection==4) System.out.println(counter + ". Route " + rs.getInt(1) + " Price: " + rs.getDouble(2));
				if(selection==5) System.out.println(counter + ". Route " + rs.getInt(1) + " Price: " + rs.getDouble(2));
				if(selection==6) System.out.println(counter + ". Route " + rs.getInt(1) + " Duration: " + rs.getDouble(2));
				if(selection==7) System.out.println(counter + ". Route " + rs.getInt(1) + " Duration: " + rs.getDouble(2));
				if(selection==8) System.out.println(counter + ". Route " + rs.getInt(1) + " Distance: " + rs.getInt(2));
				if(selection==9) System.out.println(counter + ". Route " + rs.getInt(1) + " Distance: " + rs.getInt(2));
				if(counter%10==0){
					System.out.println("Type 'y' to load more results, or any other input to stop.");
					if(!inScan.nextLine().equals("y")) break;
				}
			}
			if(counter==0) System.out.println("Sorry. There is no match.");
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	//1.2.2 combination search which has one transfer station
	private void combSearch(){
		Scanner inScan = new Scanner(System.in);
		int asid = 0;
		int dsid = 0;
		String day = "";
		boolean keepLoop = true;
		while(keepLoop){
			try{
				System.out.println("What is your departure station?(1-50)");
				asid = Integer.parseInt(inScan.nextLine());
				if(asid<1||asid>50) throw new Exception();

				System.out.println("What is your destination station?(1-50)");
				dsid = Integer.parseInt(inScan.nextLine());
				if(dsid<1||dsid>50||asid==dsid) throw new Exception();

				System.out.println("What day?(Mon-Sun, full word with first char capital)");
				day = inScan.nextLine();
				if(!day.equals("Monday")&&!day.equals("Tuesday")&&!day.equals("Wednesday")&&!day.equals("Thursday")
					&&!day.equals("Friday")&&!day.equals("Saturday")&&!day.equals("Sunday")) throw new Exception();

				keepLoop = false;
			}
			catch(Exception e){
				System.out.println("You must type a valid input.");
			}
		}

		try{
			query = "select * from combination_route_trip_search(?,?,?) order by rid1";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, asid);
			pstmt.setInt(2, dsid);
			pstmt.setString(3, day);
			rs = pstmt.executeQuery();
			int counter = 0;
			while(rs.next()){
				counter++;
				if(counter==1)System.out.println("Results that pass from " + asid + " and " + dsid + ": ");
				System.out.println(counter + ". Route " + rs.getInt(1) + " -> " + rs.getInt(2) + ", transfer station: " + rs.getInt(3));
				if(counter%10==0){
					System.out.println("Type 'y' to load more results, or any other input to stop.");
					if(!inScan.nextLine().equals("y")) break;
				}
			}
			if(counter==0) System.out.println("Sorry. There is no match.");
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	//1.2.5. add reservation from user
	private void addReservation(){
		Scanner inScan = new Scanner(System.in);
		int rid = 0;
		int cid = 0;
		int tid = 0;
		String day = "";
		String time = "00:00:00";
		boolean keepLoop = true;
		while(keepLoop){
			try{
				System.out.println("What is your customer ID: ");
				cid = Integer.parseInt(inScan.nextLine());

				System.out.println("Please input the route ID you want to reserve: ");
				rid = Integer.parseInt(inScan.nextLine());
				keepLoop = false;
			}
			catch(Exception e){
				System.out.println("You must type a valid input.");
			}
		}

		try{
			query = "select * from routes where rid = ?";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, rid);
			rs = pstmt.executeQuery();
			if(!rs.next()) System.out.println("Route " + rid + " does not exist!");
			else{
				query = "select * from routescheds where rid = ?";
				pstmt = con.prepareStatement(query);
				pstmt.setInt(1, rid);
				rs = pstmt.executeQuery();
				int counter = 0;
				while(rs.next()){
					counter++;
					if(counter==1)System.out.println("Schedules of route " + rid + " that are available: ");
					if(rs.getInt(5)>0) System.out.println("Day: " + rs.getString(2) + " Time: " +
						rs.getTime(3) + " Train: " + rs.getInt(4));
				}
				if(counter==0) System.out.println("Sorry. There is no match.");
			}
			java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("HH:mm:ss");
	  		java.sql.Time date_reg = new java.sql.Time ((df.parse(time)).getTime());
			keepLoop = true;
			while(keepLoop){
			try{
				System.out.println("What day you want to reserve(Mon-Sun, full word with first char capital): ");
				day = inScan.nextLine();
				if(!day.equals("Monday")&&!day.equals("Tuesday")&&!day.equals("Wednesday")&&!day.equals("Thursday")
					&&!day.equals("Friday")&&!day.equals("Saturday")&&!day.equals("Sunday")) throw new Exception();

				System.out.println("What time? (format: hh:mm");
				time = inScan.nextLine();
				date_reg = new java.sql.Time ((df.parse(time+":00")).getTime());

				System.out.println("What train?");
				tid = Integer.parseInt(inScan.nextLine());
				keepLoop = false;
			}
			catch(Exception e){
				System.out.println("You must type a valid input.");
			}
			query = "insert into bookings values (?, ?, ?, ?, ?)";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, rid);
			pstmt.setInt(2, tid);
			pstmt.setString(3, day);
			pstmt.setTime(4, date_reg);
			pstmt.setInt(5, cid);
			pstmt.executeUpdate();
			System.out.println("Success! You just booked a route!");
			}	
			
			
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	//1.3.1. Find all trains that pass through a specific station at a specific day/time combination
	private void method131(){
		Scanner inScan = new Scanner(System.in);
		int sid = 0;
		String day = "";
		String time = "00:00:00";
		try{
			java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("HH:mm:ss");
	  		java.sql.Time date_reg = new java.sql.Time ((df.parse(time)).getTime());
		
			boolean keepLoop = true;
			while(keepLoop){
				try{
					System.out.println("What station are you looking for? ");
					sid = Integer.parseInt(inScan.nextLine());
					if(sid<1||sid>50) throw new Exception();

					System.out.println("What day?(Mon-Sun, full word with first char capital)");
					day = inScan.nextLine();
					if(!day.equals("Monday")&&!day.equals("Tuesday")&&!day.equals("Wednesday")&&!day.equals("Thursday")
						&&!day.equals("Friday")&&!day.equals("Saturday")&&!day.equals("Sunday")) throw new Exception();

					System.out.println("What time? (format: hh:mm");
					time = inScan.nextLine();
					date_reg = new java.sql.Time ((df.parse(time+":00")).getTime());
					keepLoop = false;
				}
				catch(Exception e){
					System.out.println("You must type a valid input.");
				}
			}

			try{
				query = "select * from find_trains(?, ?, ?) order by ret_tid";
				pstmt = con.prepareStatement(query);
				pstmt.setInt(1, sid);
				pstmt.setString(2, day);
				pstmt.setTime(3, date_reg);
				rs = pstmt.executeQuery();
				int counter = 0;
				while(rs.next()){
					counter++;
					if(counter==1)System.out.println("Trains that pass through " + sid + ": ");
					System.out.println(counter + ". Train " + rs.getInt(1));
					if(counter%10==0){
						System.out.println("Type 'y' to load more results, or any other input to stop.");
						if(!inScan.nextLine().equals("y")) break;
					}
				}
				if(counter==0) System.out.println("Sorry. There is no match.");
				//System.out.println(date_reg);
			}
			catch(Exception e){
				System.out.println(e);
			}
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	//1.3.2. Find the routes that travel more than one rail line
	private void method132(){
		Scanner inScan = new Scanner(System.in);
		try{
			query = "select * from find_multi_line() order by ret_rid";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			int counter = 0;
			while(rs.next()){
				counter++;
				if(counter==1)System.out.println("Routes that travel more than one line: ");
				System.out.println(counter + ". Train " + rs.getInt(1));
				if(counter%10==0){
					System.out.println("Type 'y' to load more results, or any other input to stop.");
					if(!inScan.nextLine().equals("y")) break;
				}
			}
			if(counter==0) System.out.println("Sorry. There is no match.");
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	//1.3.3. Find routes that pass through the same stations but don’t have the same stops
	private void method133(){
		Scanner inScan = new Scanner(System.in);
		int rid = 0;
		boolean keepLoop = true;
		while(keepLoop){
			try{
				System.out.println("Please input the route ID: ");
				rid = Integer.parseInt(inScan.nextLine());
				keepLoop = false;
			}
			catch(Exception e){
				System.out.println("You must type a valid input.");
			}
		}

		try{
			query = "select * from routes where rid = ?";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, rid);
			rs = pstmt.executeQuery();
			if(!rs.next()) System.out.println("Route " + rid + " does not exist!");
			else{
				query = "select * from find_similar_routes(?) order by ret_rid";
				pstmt = con.prepareStatement(query);
				pstmt.setInt(1, rid);
				rs = pstmt.executeQuery();
				int counter = 0;
				while(rs.next()){
					counter++;
					if(counter==1)System.out.println("Routes that pass through more than two lines: ");
					System.out.println(counter + ". Route " + rs.getInt(1));
					if(counter%10==0){
						System.out.println("Type 'y' to load more results, or any other input to stop.");
						if(!inScan.nextLine().equals("y")) break;
					}
					
				}
				if(counter==0) System.out.println("Sorry. There is no match.");
			}
			
			
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	//1.3.4. Find any stations through which all trains pass through
	private void method134(){
		Scanner inScan = new Scanner(System.in);
		try{
			query = "select * from stations_all_trains() order by ret_sid";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			int counter = 0;
			while(rs.next()){
				counter++;
				if(counter==1)System.out.println("Stations that every train passes through: ");
				System.out.println(counter + ". Station " + rs.getInt(1));
				if(counter%10==0){
					System.out.println("Type 'y' to load more results, or any other input to stop.");
					if(!inScan.nextLine().equals("y")) break;
				}
			}
			if(counter==0) System.out.println("Sorry. There is no match.");
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	//1.3.5. Find all the trains that do not stop at a specific station
	private void method135(){
		Scanner inScan = new Scanner(System.in);
		int sid = 0;
		boolean keepLoop = true;
		while(keepLoop){
			try{
				System.out.println("Please input the station number: ");
				sid = Integer.parseInt(inScan.nextLine());
				if(sid<1||sid>50) throw new Exception();
				keepLoop = false;
			}
			catch(Exception e){
				System.out.println("You must type a valid input.");
			}
		}

		try{
			query = "select * from search_no_stop(?) order by ret_tid";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, sid);
			rs = pstmt.executeQuery();
			int counter = 0;
			while(rs.next()){
				counter++;
				if(counter==1)System.out.println("Trains that do not stop at station " + sid + ": ");
				System.out.println(counter + ". Train " + rs.getInt(1));
				if(counter%10==0){
					System.out.println("Type 'y' to load more results, or any other input to stop.");
					if(!inScan.nextLine().equals("y")) break;
				}
				
			}
			if(counter==0) System.out.println("Sorry. There is no match.");
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	//1.3.6. Find routes that stop at least at XX% of the Stations they visit
	private void method136(){
		Scanner inScan = new Scanner(System.in);
		double percentage = 0;
		boolean keepLoop = true;
		while(keepLoop){
			try{
				System.out.println("Please input the percentage: ");
				percentage = Double.parseDouble(inScan.nextLine());
				if(percentage<=0||percentage>=1) throw new Exception();
				keepLoop = false;
			}
			catch(Exception e){
				System.out.println("You must type a valid input.");
			}
		}

		try{
			query = "select * from search_XX_stop(?) order by ret_rid";
			pstmt = con.prepareStatement(query);
			pstmt.setDouble(1, percentage);
			rs = pstmt.executeQuery();
			int counter = 0;
			while(rs.next()){
				counter++;
				if(counter==1)System.out.println("Routes that stop at at least " + percentage*100 + "% of stations they visit: ");
				System.out.println(counter + ". Route " + rs.getInt(1));
				if(counter%10==0){
					System.out.println("Type 'y' to load more results, or any other input to stop.");
					if(!inScan.nextLine().equals("y")) break;
				}
				
			}
			if(counter==0) System.out.println("Sorry. There is no match.");
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	//1.3.7. Display the schedule of a route
	private void method137(){
		Scanner inScan = new Scanner(System.in);
		int rid = 0;
		boolean keepLoop = true;
		while(keepLoop){
			try{
				System.out.println("Please input the route ID you want to display schedule: ");
				rid = Integer.parseInt(inScan.nextLine());
				keepLoop = false;
			}
			catch(Exception e){
				System.out.println("You must type a valid input.");
			}
		}

		try{
			query = "select * from routes where rid = ?";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, rid);
			rs = pstmt.executeQuery();
			if(!rs.next()) System.out.println("Route " + rid + " does not exist!");
			else{
				query = "select * from display_route_schedule(?)";
				pstmt = con.prepareStatement(query);
				pstmt.setInt(1, rid);
				rs = pstmt.executeQuery();
				int counter = 0;
				while(rs.next()){
					counter++;
					if(counter==1)System.out.println("The schedules of " + rid + ": ");
					System.out.println(counter + ". Day: " + rs.getString(1) + "  Time: " + rs.getTime(2) + "  Train id: " + rs.getInt(3));
					if(counter%10==0){
						System.out.println("Type 'y' to load more results, or any other input to stop.");
						if(!inScan.nextLine().equals("y")) break;
					}
				
				}
			if(counter==0) System.out.println("Sorry. There is no match.");
			}
			
			
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	//1.3.8. Find the availability of a route at every stop on a specific day and time
	private void method138(){
		Scanner inScan = new Scanner(System.in);
		int rid = 0;
		String day = "";
		String time = "00:00:00";
		try{
			java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("HH:mm:ss");
	  		java.sql.Time date_reg = new java.sql.Time ((df.parse(time)).getTime());
		
			boolean keepLoop = true;
			while(keepLoop){
				try{
					System.out.println("Please enter the route ID: ");
					rid = Integer.parseInt(inScan.nextLine());

					System.out.println("What day?(Mon-Sun, full word with first char capital)");
					day = inScan.nextLine();
					if(!day.equals("Monday")&&!day.equals("Tuesday")&&!day.equals("Wednesday")&&!day.equals("Thursday")
						&&!day.equals("Friday")&&!day.equals("Saturday")&&!day.equals("Sunday")) throw new Exception();

					System.out.println("What time? (format: hh:mm)");
					time = inScan.nextLine();
					date_reg = new java.sql.Time ((df.parse(time+":00")).getTime());
					keepLoop = false;
				}
				catch(Exception e){
					System.out.println("You must type a valid input.");
				}
			}

			try{
				query = "select * from route_availability(?, ?, ?)";
				pstmt = con.prepareStatement(query);
				pstmt.setInt(1, rid);
				pstmt.setString(2, day);
				pstmt.setTime(3, date_reg);
				rs = pstmt.executeQuery();
				int counter = 0;
				while(rs.next()){
					counter++;
					if(counter==1)System.out.println("Route schedules that have seats available: ");
					System.out.println(counter + ". Route " + rs.getInt(2) + ", Seats available: " + rs.getInt(1));
					if(counter%10==0){
						System.out.println("Type 'y' to load more results, or any other input to stop.");
						if(!inScan.nextLine().equals("y")) break;
					}
				}
				if(counter==0) System.out.println("Sorry. There is no match.");
				//System.out.println(date_reg);
			}
			catch(Exception e){
				System.out.println(e);
			}
		}
		catch(Exception e){
			System.out.println(e);
		}
	}

	public boolean ask(){//ask user if he/she wants to do operations again
		Scanner inScan = new Scanner(System.in);
		String input = "";
		boolean keepLoop = true;
		while(keepLoop){
			System.out.println("Is there anything else you want to do? (y/n)");
			try{
				input = inScan.nextLine();
				if(!input.equals("y")&&!input.equals("n"))
					throw new Exception("e");
				keepLoop = false;
			}
			catch(Exception e){
				System.out.println("You must type a valid input.");
			}
		}
		if(input.equals("y")){
			return true;
		}
		else 
		{
			System.out.println("Thank you for using ExpressRailway program. GoodBye");
			return false;	
		}
	}

	public void selectOperation(){
		boolean keepLoop = true;
		int selection = 0;
		Scanner inScan = new Scanner(System.in);
		while(keepLoop){//the loop that keep asking user to enter valid input
			System.out.println("What operation do you want to do?");
			System.out.println("\t1. Customer Information Services");
			System.out.println("\t2. Trip Information and Reservations");
			System.out.println("\t3. Advanced Searches");
			System.out.println("\t4. Exit");
			System.out.println("\nPlease type the number of operation(1-4): ");

			
			try{
				selection = Integer.parseInt(inScan.nextLine());
				if(selection!=1&&selection!=2&&selection!=3&&selection!=4)
					throw new Exception("e");
				keepLoop = false;//if the input is valid, set keepLoop to false, then the while loop may be terminated
			}
			catch(Exception e){
				System.out.println("You must type a valid input.");
			}
		}

		if(selection == 1){
			keepLoop = true;
			while(keepLoop){//ask for valid input
				System.out.println("Customer Information Services:");
				System.out.println("\t1. Add Customer Information");
				System.out.println("\t2. Edit Customer Information");
				System.out.println("\t3. View Customer Information");
				System.out.println("\nPlease type the number of selection(1-3): ");
				try{
					selection = Integer.parseInt(inScan.nextLine());
					if(selection!=1&&selection!=2&&selection!=3)
						throw new Exception("e");
					keepLoop = false;
				}
				catch(Exception e){
					System.out.println("You must type a valid input.");
				}
			}
			if(selection == 1) addCustomer();//execute methods
			else if(selection == 2) editCustomer();//execute methods
			else if(selection == 3) viewCustomer();//execute methods
		}
		else if(selection == 2){
			keepLoop = true;
			while(keepLoop){
				System.out.println("Trip Information and Reservations:");
				System.out.println("\t1. Trip Search without Transfer");
				System.out.println("\t2. Trip Search with one Transfer");
				System.out.println("\t3. Add Reservations");
				System.out.println("\nPlease type the number of selection(1-3): ");
				try{
					selection = Integer.parseInt(inScan.nextLine());
					if(selection!=1&&selection!=2&&selection!=3)
						throw new Exception("e");
					keepLoop = false;
				}
				catch(Exception e){
					System.out.println("You must type a valid input.");
				}
			}
			if(selection == 1) singleSearch();//execute methods
			else if(selection == 2) combSearch();//execute methods
			else if(selection == 3) addReservation();//execute methods
		}
		else if(selection == 3){
			keepLoop = true;
			while(keepLoop){
				System.out.println("Advanced Searches:");
				System.out.println("\t1. Find all trains that pass through a specific station at a specific day/time combination");
				System.out.println("\t2. Find the routes that travel more than one rail line");
				System.out.println("\t3. Find routes that pass through the same stations but don’t have the same stops");
				System.out.println("\t4. Find any stations through which all trains pass through");
				System.out.println("\t5. Find all the trains that do not stop at a specific station");
				System.out.println("\t6. Find routes that stop at least at XX% of the Stations they visit");
				System.out.println("\t7. Display the schedule of a route");
				System.out.println("\t8. Find the availability of a route at every stop on a specific day and time");
				System.out.println("\nPlease type the number of selection(1-3): ");
				try{
					selection = Integer.parseInt(inScan.nextLine());
					if(selection!=1&&selection!=2&&selection!=3&&selection!=4
						&&selection!=5&&selection!=6&&selection!=7&&selection!=8)
						throw new Exception("e");
					keepLoop = false;
				}
				catch(Exception e){
					System.out.println("You must type a valid input.");
				}
			}
			if(selection == 1) method131();//execute methods
			else if(selection == 2) method132();//execute methods
			else if(selection == 3) method133();//execute methods
			else if(selection == 4) method134();
			else if(selection == 5) method135();
			else if(selection == 6) method136();
			else if(selection == 7) method137();
			else if(selection == 8) method138();

		}
		else if(selection == 4){
			System.out.println("Thank you for using ExpressRailway program. GoodBye");
			System.exit(0);
		}
	}

	public void printWelcome(){
		System.out.println("Welcome to the ExpressRailway!\n");
	}

	public Database(){
		boolean keepLoop = true;
		while(keepLoop){
			printWelcome();//print the welcome message
			selectOperation();//let user select which operation he/she is going to use
			keepLoop = ask();//ask user if he/she wants to continue

		}
	}

	public static void main(String[] args) throws Exception{
		try{
			Class.forName("org.postgresql.Driver");
			con = DriverManager.getConnection(
				"jdbc:postgresql://localhost:5432/postgres","postgres", "postgres");
			Database db = new Database(); //Run the main program
		}
		catch(Exception e){
			System.out.println(e);
		}
		finally{
			con.close();
		}

	}
}