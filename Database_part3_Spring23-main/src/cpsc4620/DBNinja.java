package cpsc4620;

import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.crypto.Data;

/*
 * This file is where most of your code changes will occur You will write the code to retrieve
 * information from the database, or save information to the database
 * 
 * The class has several hard coded static variables used for the connection, you will need to
 * change those to your connection information
 * 
 * This class also has static string variables for pickup, delivery and dine-in. If your database
 * stores the strings differently (i.e "pick-up" vs "pickup") changing these static variables will
 * ensure that the comparison is checking for the right string in other places in the program. You
 * will also need to use these strings if you store this as boolean fields or an integer.
 * 
 * 
 */

/**
 * A utility class to help add and retrieve information from the database
 */

public final class DBNinja {
	private static Connection conn;
	private static Statement stat;
	private static PreparedStatement pre;

	// Change these variables to however you record dine-in, pick-up and delivery,
	// and sizes and crusts
	public final static String pickup = "Pickup";
	public final static String delivery = "Delivery";
	public final static String dine_in = "Dinein";

	public final static String size_s = "Small";
	public final static String size_m = "Medium";
	public final static String size_l = "Large";
	public final static String size_xl = "XLarge";

	public final static String crust_thin = "Thin";
	public final static String crust_orig = "Original";
	public final static String crust_pan = "Pan";
	public final static String crust_gf = "Gluten-Free";

	private static boolean connect_to_db() throws SQLException, IOException {

		try {
			conn = DBConnector.make_connection();
			return true;
		} catch (SQLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

	}

	public static void addOrder(Order o, String UserSelectedPizzaSizeoption, String UserSelectedPizzacrustoption,
			ArrayList<Integer> selectedToppInfo, ArrayList<Integer> pizzaDiscountIds,
			ArrayList<Integer> DiscountIdsOrder, String orderDeliveryAddress) throws SQLException, IOException {
		// connect_to_db();
		/*
		 * add code to add the order to the DB. Remember that we're not just
		 * adding the order to the order DB table, but we're also recording
		 * the necessary data for the delivery, dinein, and pickup tables
		 */
		int custId = o.getCustID(), ostatus = o.getIsComplete(), OrderID = -1;
		double ccost = o.getCustPrice(), cprice = o.getBusPrice();
		String otype = o.getOrderType(), odate = o.getDate();
		boolean isOrderAlreadyExist = true;

		// Order Table Insert Code
		String sqlOrderInsertQuery = "Insert into ordertable(CustomerId,OrderType,OrderDateTime,OrderStatus,OrderCost,OrderPrice)values('"
				+ custId + "','" + otype + "','" + odate + "','" + ostatus + "','" + ccost + "','" + cprice + "')";
		OrderID = checkCustomerOrderExistForTheDay(custId, otype, odate);
		if (OrderID == -1) {
			insertQueryExecution(sqlOrderInsertQuery);
			// Get OrderId
			OrderID = getOrderIDFromOrderTable(custId, odate);
			isOrderAlreadyExist = false;
		} else {
			String sqlQueryToUpdateOrder = "update ordertable set OrderCost = OrderCost+" + ccost
					+ ", OrderPrice = OrderPrice+" + cprice + " where OrderId =" + OrderID + ";";

			updateOrderPrice(sqlQueryToUpdateOrder);
		}

		// public final static String Dinein = dine_in;
		// Delivery, pickup, dine_in tables Insertion code
		if (!isOrderAlreadyExist) {
			if (otype.equals(delivery)) {
				String deliverySqlQuery = "Insert into delivery(OrderId,CustomerAddress)value('" + OrderID + "','"
						+ orderDeliveryAddress + "')";

				// System.out.println(deliverySqlQuery);
				insertQueryExecution(deliverySqlQuery);
			} else if (otype.equals(pickup)) {
				String pickupSqlQuery = "Insert into pickup(OrderId)values('" + OrderID + "')";

				// System.out.println(pickupSqlQuery);
				insertQueryExecution(pickupSqlQuery);
			} else if (otype.equals(dine_in)) {
				Random random = new Random();
				int tableNum = 0;
				while (true) {
					tableNum = random.nextInt(20);
					if (tableNum != 0)
						break;
				}

				String dineInSqlQuery = "Insert into dinein(OrderId,TableNum)value('" + OrderID + "','" + tableNum
						+ "')";

				insertQueryExecution(dineInSqlQuery);
				// System.out.println(dineInSqlQuery);
			}
		}

		// Pizza Table insertion code
		Pizza pinfo = new Pizza(0, UserSelectedPizzacrustoption, UserSelectedPizzaSizeoption, OrderID, "InProcess",
				odate, ccost, cprice);
		addPizza(pinfo);

		// pizzaToppings Tabble insertion code

		Dictionary<Integer, Integer> pizzaToppingDetails = new Hashtable<Integer, Integer>();

		for (int i = 0; i < selectedToppInfo.size(); i++) {
			for (int j = 0; j < (selectedToppInfo.size() - 1 - i); j++) {
				if (selectedToppInfo.get(j) > selectedToppInfo.get(j + 1)) {
					int temp = selectedToppInfo.get(j);
					selectedToppInfo.set(j, selectedToppInfo.get(j + 1));
					selectedToppInfo.set(j + 1, temp);

				}
			}
		}

		for (int i = 0; i < selectedToppInfo.size(); i++) {
			int duplicateCount = 0;
			int k = 0;
			int x = selectedToppInfo.get(i);
			for (int j = i + 1; j < selectedToppInfo.size(); j++) {
				int y = selectedToppInfo.get(j);
				if (x == y) {
					duplicateCount++;

					k = j;
				}
			}
			pizzaToppingDetails.put(selectedToppInfo.get(i), duplicateCount);
			if (k != 0) {
				i = k;
			}

		}
		int pizzaOrderId = getMaxPizzaOrderId();

		for (Enumeration enn = pizzaToppingDetails.keys(); enn.hasMoreElements();) {
			int keyValue = Integer.parseInt(enn.nextElement().toString());
			String pizzaSqlQuery = "Insert into pizzatoppingsinfo(PizzaOrderId,ToppingId,ExtraTopping)value('"
					+ pizzaOrderId + "','" + keyValue + "','" + pizzaToppingDetails.get(keyValue) + "');";
			// System.out.println(pizzaSqlQuery);
			insertQueryExecution(pizzaSqlQuery);
		}

		// Code for Inserting into pizzaDiscount table

		for (int i = 0; i < pizzaDiscountIds.size(); i++) {
			int discountValue = pizzaDiscountIds.get(i);
			String pizzaDiscountSqlQuery = "Insert into pizzadiscount(PizzaOrderId,DiscountID)value('" + pizzaOrderId
					+ "','" + discountValue + "');";
			// System.out.println(pizzaDiscountSqlQuery);
			insertQueryExecution(pizzaDiscountSqlQuery);
		}

		// Code for Inserting into orderDiscount

		for (int i = 0; i < DiscountIdsOrder.size(); i++) {
			int orderDiscountValue = DiscountIdsOrder.get(i);
			String orderDiscountSqlQuery = "Insert into orderDiscount(OrderId,DiscountID)value('" + OrderID + "','"
					+ orderDiscountValue + "');";
			// System.out.println(orderDiscountSqlQuery);
			if (!checkDiscountAlreadyAppliedToOrder(OrderID, orderDiscountValue)) {
				insertQueryExecution(orderDiscountSqlQuery);
			}

		}

		// Dictionary<String, Double> pizzaAmt
		// =CalculatePizzaPrice(UserSelectedPizzaSizeoption,
		// UserSelectedPizzacrustoption, selectedToppInfo,
		// pizzaDiscountIds, DiscountIdsOrder);

		CalculateAndApplyOrderDiscount(OrderID, DiscountIdsOrder);

		for (int i = 0; i < selectedToppInfo.size(); i++) {
			String sqlQueryForToppingInvUpdate = "update toppingsinfo set Inventory = Inventory -1 where ToppingId ="
					+ selectedToppInfo.get(i) + ";";
			updateToppingInfoTable(sqlQueryForToppingInvUpdate);
		}

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void CalculateAndApplyOrderDiscount(int OrderId, ArrayList<Integer> DiscountIdsOrder)
			throws SQLException, IOException {
		connect_to_db();
		ArrayList<Discount> discount = new ArrayList<Discount>();
		String sqlQuery = "select * from ordertable where OrderId = " + OrderId + ";";
		String sqlQuerydiscount = "select * from discount;";
		double orderPrice = 0.0, orderCost = 0.0;

		conn = DBConnector.make_connection();
		stat = conn.createStatement();
		ResultSet res = stat.executeQuery(sqlQuery);

		while (res.next()) {
			orderCost = res.getDouble("OrderCost");
			orderPrice = res.getDouble("OrderPrice");
		}

		res = stat.executeQuery(sqlQuerydiscount);
		while (res.next()) {
			Discount d = new Discount(res.getInt("DiscountID"), res.getString("DiscountType"),
					res.getDouble("DiscountDollarOff"), res.getBoolean("IsPercentOff"));

			discount.add(d);
		}

		double orderPriceAfterOrderDiscoun = orderPrice;
		double orderCostAfterOrderDiscount = orderCost;

		if (!DiscountIdsOrder.isEmpty()) {
			for (Discount d : discount) {
				if (DiscountIdsOrder.contains(d.getDiscountID())) {
					if (d.isPercent()) {
						Double value = ((d.getAmount()) / 100) * orderPrice;
						orderPriceAfterOrderDiscoun -= value;
					} else if (!d.isPercent()) {
						orderPriceAfterOrderDiscoun -= d.getAmount();
					}
				}
			}
		}
		String sqlQueryForOrderTableUpdate = "update ordertable set OrderCost=" + orderCostAfterOrderDiscount
				+ " , OrderPrice =" + orderPriceAfterOrderDiscoun + "  where OrderId =" + OrderId + "";

		stat.executeUpdate(sqlQueryForOrderTableUpdate);

		conn.close();
	}

	public static void updateToppingInfoTable(String sqlQuery) throws SQLException, IOException {
		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			stat.executeUpdate(sqlQuery);
		} else {
			System.out.print("Not connected to db");
		}

		conn.close();
	}

	public static Dictionary<String, Double> CalculatePizzaPrice(String UserSelectedPizzaSizeoption,
			String UserSelectedPizzacrustoption, ArrayList<Integer> selectedToppInfo,
			ArrayList<Integer> pizzaDiscountIds, ArrayList<Integer> DiscountIdsOrder) throws SQLException, IOException {
		connect_to_db();
		Dictionary<String, Double> PizzaAmtCalculatedValues = new Hashtable<String, Double>();

		double pizzaPrice = 0.0, PizzaCost = 0.0, ToppingPrice = 0.0, ToppingCost = 0.0,
				pizzaPriceAfterPizzaDiscount = 0.0, PizzaCostAfterPizzaDiscount = 0.0;
		ArrayList<Topping> toppingsinfo = new ArrayList<Topping>();
		List<Integer> toppingsIdsOnly = new ArrayList<Integer>();
		ArrayList<Discount> discount = new ArrayList<Discount>();
		String sqlQuerypizzastandards = "select ps.PizzaSize, ps.PizzaCrust,ps.PizzaPrice, ps.PizzaCost from pizzastandards as ps where ps.PizzaSize = '"
				+ UserSelectedPizzaSizeoption + "' and ps.PizzaCrust='" + UserSelectedPizzacrustoption + "';";
		String sqlQuerytoppingsinfo = "select * from toppingsinfo;";
		String sqlQuerydiscount = "select * from discount;";
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(sqlQuerypizzastandards);
		while (rset.next()) {
			pizzaPrice = rset.getDouble("PizzaPrice");
			PizzaCost = rset.getDouble("PizzaCost");
		}
		rset = stmt.executeQuery(sqlQuerytoppingsinfo);
		while (rset.next()) {

			Topping t = new Topping(rset.getInt("ToppingId"), rset.getString("Name"), rset.getDouble("SmallUnits"),
					rset.getDouble("MediumUnits"), rset.getDouble("LargeUnits"), rset.getDouble("ExtraLangeUnits"),
					rset.getDouble("Toppings_Cost"), rset.getDouble("Toppings_Price"), 0, rset.getInt("Inventory"));

			toppingsinfo.add(t);
			toppingsIdsOnly.add(t.getTopID());
		}

		rset = stmt.executeQuery(sqlQuerydiscount);
		while (rset.next()) {
			Discount d = new Discount(rset.getInt("DiscountID"), rset.getString("DiscountType"),
					rset.getDouble("DiscountDollarOff"), rset.getBoolean("IsPercentOff"));

			discount.add(d);
		}

		for (int i = 0; i < selectedToppInfo.size(); i++) {
			int toppingID = selectedToppInfo.get(i);

			ArrayList<Topping> tdetail = getToppingsDetailsById(toppingID);
			for (Topping t : tdetail) {
				ToppingPrice += t.getBusPrice();
				ToppingCost += t.getCustPrice();
			}

		}

		pizzaPrice += ToppingPrice;
		PizzaCost += ToppingCost;

		pizzaPriceAfterPizzaDiscount = pizzaPrice;
		PizzaCostAfterPizzaDiscount = PizzaCost;

		if (!pizzaDiscountIds.isEmpty()) {
			for (Discount d : discount) {
				if (pizzaDiscountIds.contains(d.getDiscountID())) {
					if (d.isPercent()) {
						Double value = ((d.getAmount()) / 100) * pizzaPrice;
						pizzaPriceAfterPizzaDiscount -= value;
					} else if (!d.isPercent()) {
						pizzaPriceAfterPizzaDiscount -= d.getAmount();
					}
				}
			}
		}

		PizzaAmtCalculatedValues.put("PizzaPrice", pizzaPriceAfterPizzaDiscount);
		PizzaAmtCalculatedValues.put("PizzaCost", PizzaCostAfterPizzaDiscount);

		conn.close();
		return PizzaAmtCalculatedValues;

	}

	public static ArrayList<Topping> getToppingsDetailsById(int toppingID) throws SQLException, IOException {
		connect_to_db();
		/*
		 * This function actually returns the toppings. The toppings
		 * should be returned in alphabetical order if you don't
		 * plan on using a printInventory function
		 */

		ArrayList<Topping> top = new ArrayList<>();

		String sqlQueryViewInv = "select * from toppingsinfo as tinfo where tinfo.ToppingId = " + toppingID + ";";

		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			ResultSet res = stat.executeQuery(sqlQueryViewInv);

			while (res.next()) {

				Topping t = new Topping(res.getInt("ToppingId"), res.getString("Name"), res.getInt("SmallUnits"),
						res.getInt("MediumUnits"), res.getInt("LargeUnits"), res.getInt("ExtraLangeUnits"),
						res.getDouble("Toppings_Cost"), res.getDouble("Toppings_Price"), 0, res.getInt("Inventory"));

				top.add(t);
			}
		} else {
			System.out.print("Not connected to db");
		}

		conn.close();

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		return top;

	}

	public static int checkCustomerOrderExistForTheDay(int custId, String otype, String odate)
			throws SQLException, IOException {
		int orderIdinfo = -1;
		connect_to_db();
		String sqlQueryforOrderCheck = "select ot.OrderId from ordertable as ot where ot.CustomerId =" + custId
				+ " and ot.OrderType = '" + otype + "' and ot.OrderDateTime = '" + odate + "'";

		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(sqlQueryforOrderCheck);

		while (rset.next()) {
			orderIdinfo = rset.getInt("OrderId");
		}
		conn.close();

		return orderIdinfo;

	}

	public static boolean checkDiscountAlreadyAppliedToOrder(int OrderId, int orderDiscountValue)
			throws SQLException, IOException {
		connect_to_db();

		boolean isOrderDiscountAlreadyExist = true;

		String sqlQueryToCheckDiscountExist = "select * from orderDiscount as od where od.OrderId =" + OrderId
				+ " and od.DiscountID = " + orderDiscountValue + ";";
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(sqlQueryToCheckDiscountExist);
		if (rset.next() == false) {
			isOrderDiscountAlreadyExist = false;
		}
		return isOrderDiscountAlreadyExist;
	}

	public static void insertQueryExecution(String sqlQuery) throws SQLException, IOException {
		connect_to_db();
		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			stat.execute(sqlQuery);

		} else {
			System.out.print("Not connected to database");
		}

		conn.close();
	}

	public static int getOrderIDFromOrderTable(int custId, String orderDate) throws SQLException, IOException {
		connect_to_db();
		int ret = -1;
		String query = "select MAX(ot.OrderId) as 'OrderId' from ordertable as ot where ot.CustomerId ='" + custId
				+ "' and ot.OrderDateTime = '" + orderDate + "';";
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(query);

		while (rset.next()) {
			ret = rset.getInt("OrderId");
		}
		conn.close();
		return ret;

	}

	public static void addPizza(Pizza p) throws SQLException, IOException {
		// connect_to_db();
		/*
		 * Add the code needed to insert the pizza into into the database.
		 * Keep in mind adding pizza discounts to that bridge table and
		 * instance of topping usage to that bridge table if you have't accounted
		 * for that somewhere else.
		 */

		int oId = p.getOrderID();
		String pSize = p.getSize(), pState = p.getPizzaState(), pDateTime = p.getPizzaDate(), pCrust = p.getCrustType();
		double pPrice = p.getBusPrice(), pCost = p.getCustPrice();

		String pizzaSqlQuery = "Insert into pizza(OrderId,PizzaCrust,PizzaState,PizzaDateTime,PizzaCost,PizzaPrice,PizzaSize)value('"
				+ oId + "','" + pSize + "','" + pState + "','" + pDateTime + "','" + pCost + "','" + pPrice + "','"
				+ pCrust + "')";

		// System.out.println(pizzaSqlQuery);
		insertQueryExecution(pizzaSqlQuery);

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static int getMaxPizzaID() throws SQLException, IOException {
		connect_to_db();
		/*
		 * A function I needed because I forgot to make my pizzas auto increment in my
		 * DB.
		 * It goes and fetches the largest PizzaID in the pizza table.
		 * You wont need to implement this function if you didn't forget to do that
		 */

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		return -1;
	}

	public static void useTopping(Pizza p, Topping t, boolean isDoubled) throws SQLException, IOException // this
																											// function
																											// will
																											// update
																											// toppings
																											// inventory
																											// in SQL
																											// and add
																											// entities
																											// to the
																											// Pizzatops
																											// table.
																											// Pass in
																											// the p
																											// pizza
																											// that is
																											// using t
																											// topping
	{
		connect_to_db();
		/*
		 * This function should 2 two things.
		 * We need to update the topping inventory every time we use t topping
		 * (accounting for extra toppings as well)
		 * and we need to add that instance of topping usage to the pizza-topping bridge
		 * if we haven't done that elsewhere
		 * Ideally, you should't let toppings go negative. If someone tries to use
		 * toppings that you don't have, just print
		 * that you've run out of that topping.
		 */

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void usePizzaDiscount(Pizza p, Discount d) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Helper function I used to update the pizza-discount bridge table.
		 * You might use this, you might not depending on where / how to want to update
		 * this table
		 */

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void useOrderDiscount(Order o, Discount d) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Helper function I used to update the pizza-discount bridge table.
		 * You might use this, you might not depending on where / how to want to update
		 * this table
		 */

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void addCustomer(Customer c) throws SQLException, IOException {
		connect_to_db();
		/*
		 * This should add a customer to the database
		 */
		String FirstName, LastName, Phone_Number, sqlqueryCust;

		FirstName = c.getFName();
		LastName = c.getLName();
		Phone_Number = c.getPhone();

		sqlqueryCust = "INSERT INTO customertable(First_Name,Last_Name,Phone_Number) value('" + FirstName + "','"
				+ LastName + "','" + Phone_Number + "')";

		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			stat.execute(sqlqueryCust);

		} else {
			System.out.print("Not connected to database");
		}

		conn.close();

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void CompleteOrder(Order o) throws SQLException, IOException {
		connect_to_db();
		/*
		 * add code to mark an order as complete in the DB. You may have a boolean field
		 * for this, or maybe a completed time timestamp. However you have it.
		 */

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void AddToInventory(Topping t, double toAdd) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Adds toAdd amount of topping to topping t.
		 */
		int toppingID = t.getTopID();

		String Sqlquery = "update toppingsinfo set Inventory =Inventory +? where ToppingId =?";

		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			pre = conn.prepareStatement(Sqlquery);
			pre.setDouble(1, toAdd);
			pre.setInt(2, toppingID);
			pre.executeUpdate();

		} else {
			System.out.print("Not connected to database");
		}

		conn.close();

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void printInventory() throws SQLException, IOException {
		connect_to_db();

		/*
		 * I used this function to PRINT (not return) the inventory list.
		 * When you print the inventory (either here or somewhere else)
		 * be sure that you print it in a way that is readable.
		 * 
		 * 
		 * 
		 * The topping list should also print in alphabetical order
		 */

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static ArrayList<Topping> getInventory() throws SQLException, IOException {
		connect_to_db();
		/*
		 * This function actually returns the toppings. The toppings
		 * should be returned in alphabetical order if you don't
		 * plan on using a printInventory function
		 */

		ArrayList<Topping> top = new ArrayList<>();

		String sqlQueryViewInv = "select topinfo.ToppingId as ID,topinfo.Name,(topinfo.Inventory) as CurINVT, topinfo.Toppings_Price, topinfo.Toppings_Cost, topinfo.Inventory, topinfo.SmallUnits, topinfo.MediumUnits, topinfo.LargeUnits, topinfo.ExtraLangeUnits from toppingsinfo as topinfo order by topinfo.Name;";

		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			ResultSet res = stat.executeQuery(sqlQueryViewInv);

			while (res.next()) {

				Topping t = new Topping(res.getInt("ID"), res.getString("Name"), res.getInt("SmallUnits"),
						res.getInt("MediumUnits"), res.getInt("LargeUnits"), res.getInt("ExtraLangeUnits"),
						res.getInt("Toppings_Cost"), res.getInt("Toppings_Price"), 0, res.getInt("CurINVT"));

				top.add(t);
			}
		} else {
			System.out.print("Not connected to db");
		}

		conn.close();

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		return top;
	}

	public static ArrayList<Order> getCurrentOrders() throws SQLException, IOException {
		connect_to_db();
		/*
		 * This function should return an arraylist of all of the orders.
		 * Remember that in Java, we account for supertypes and subtypes
		 * which means that when we create an arrayList of orders, that really
		 * means we have an arrayList of dineinOrders, deliveryOrders, and pickupOrders.
		 * 
		 * Also, like toppings, whenever we print out the orders using menu function 4
		 * and 5
		 * these orders should print in order from newest to oldest.
		 */

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		return null;
	}

	public static ArrayList<Order> getInCompleteOrders() throws SQLException, IOException {
		connect_to_db();
		ArrayList<Order> ord = new ArrayList<Order>();

		String needToUpdateSqlQuery = "select ot.OrderId, ot.CustomerId,ot.OrderType,ot.OrderDateTime,ot.OrderStatus,ot.OrderCost,ot.OrderPrice from ordertable as ot where ot.OrderStatus ='0' order by OrderDateTime desc;";

		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			ResultSet res = stat.executeQuery(needToUpdateSqlQuery);

			while (res.next()) {

				Order o = new Order(res.getInt("OrderId"), res.getInt("CustomerId"), res.getString("OrderType"),
						res.getString("OrderDateTime"), res.getDouble("OrderCost"), res.getDouble("OrderPrice"),
						res.getInt("OrderStatus"));

				ord.add(o);
			}
		} else {
			System.out.print("Not connected to db");
		}

		conn.close();
		return ord;
	}

	public static void updateOrderStatus(int OrderId) throws SQLException, IOException {

		String needToUpdateSqlQuery = "update ordertable set OrderStatus ='Completed' where OrderId =" + OrderId + ";";

		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			stat.executeUpdate(needToUpdateSqlQuery);
		} else {
			System.out.print("Not connected to db");
		}

		conn.close();
	}

	public static void updateOrderPrice(String sqlQuery) throws SQLException, IOException {
		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			stat.executeUpdate(sqlQuery);
		} else {
			System.out.print("Not connected to db");
		}

		conn.close();
	}

	public static ArrayList<Order> sortOrders(ArrayList<Order> list) {
		/*
		 * This was a function that I used to sort my arraylist based on date.
		 * You may or may not need this function depending on how you fetch
		 * your orders from the DB in the getCurrentOrders function.
		 */

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		return null;

	}

	public static boolean checkDate(int year, int month, int day, String dateOfOrder) {
		// Helper function I used to help sort my dates. You likely wont need these

		return false;
	}

	/*
	 * The next 3 private functions help get the individual components of a SQL
	 * datetime object.
	 * You're welcome to keep them or remove them.
	 */
	private static int getYear(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(0, 4));
	}

	private static int getMonth(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(5, 7));
	}

	private static int getDay(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(8, 10));
	}

	public static double getBaseCustPrice(String size, String crust) throws SQLException, IOException {
		connect_to_db();
		double bp = 0.0;
		// add code to get the base price (for the customer) for that size and crust
		// pizza Depending on how
		// you store size & crust in your database, you may have to do a conversion

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		return bp;
	}

	public static String getCustomerName(int CustID) throws SQLException, IOException {
		/*
		 * This is a helper function I used to fetch the name of a customer
		 * based on a customer ID. It actually gets called in the Order class
		 * so I'll keep the implementation here. You're welcome to change
		 * how the order print statements work so that you don't need this function.
		 */
		connect_to_db();
		String ret = "";
		String query = "Select First_Name, Last_Name From customertable WHERE CustomerId=" + CustID + ";";
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(query);

		while (rset.next()) {
			ret = rset.getString(1) + " " + rset.getString(2);
		}
		conn.close();
		return ret;
	}

	public static int getMaxPizzaOrderId() throws SQLException, IOException {
		connect_to_db();
		int ret = -1;
		String query = "select MAX(p.PizzaOrderId) as 'PizzaOrderId' from pizza as p ;";
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(query);

		while (rset.next()) {
			ret = rset.getInt("PizzaOrderId");
		}
		conn.close();
		return ret;
	}

	public static int getCustomerID(String cFname, String cLname) throws SQLException, IOException {
		/*
		 * This is a helper function I used to fetch the name of a customer
		 * based on a customer ID. It actually gets called in the Order class
		 * so I'll keep the implementation here. You're welcome to change
		 * how the order print statements work so that you don't need this function.
		 */
		connect_to_db();
		int ret = -1;
		String query = "select  MAX(c.CustomerId) as 'CustomerId' from customertable as c where c.First_Name ='"
				+ cFname + "' and c.Last_Name ='" + cLname + "'";
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(query);

		while (rset.next()) {
			ret = rset.getInt("CustomerId");
		}
		conn.close();
		return (ret == -1 ? 0 : ret);
	}

	public static double getBaseBusPrice(String size, String crust) throws SQLException, IOException {
		connect_to_db();
		double bp = 0.0;
		// add code to get the base cost (for the business) for that size and crust
		// pizza Depending on how
		// you store size and crust in your database, you may have to do a conversion

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		return bp;
	}

	public static ArrayList<Discount> getDiscountList() throws SQLException, IOException {
		ArrayList<Discount> discountInfo = new ArrayList<Discount>();
		connect_to_db();
		// returns a list of all the discounts.

		String discountSqlQuery = "select d.DiscountID, d.DiscountType, d.IsPercentOff, d.DiscountDollarOff from discount as d";

		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			ResultSet res = stat.executeQuery(discountSqlQuery);

			while (res.next()) {
				// System.out.println("CustID="+res.getInt("CustomerId")+" | Name=
				// "+res.getString("First_Name")+" "+res.getString("Last_Name")+", Phone=
				// "+res.getString("Phone_Number"));
				Discount dis = new Discount(res.getInt("DiscountID"), res.getString("DiscountType"),
						res.getDouble("DiscountDollarOff"), res.getBoolean("IsPercentOff"));
				discountInfo.add(dis);
			}
		} else {
			System.out.print("Not connected to db");
		}

		conn.close();

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		return discountInfo;
	}

	public static ArrayList<Customer> getCustomerList() throws SQLException, IOException {
		ArrayList<Customer> custs = new ArrayList<Customer>();
		connect_to_db();
		/*
		 * return an arrayList of all the customers. These customers should
		 * print in alphabetical order, so account for that as you see fit.
		 */

		String sqlQuery = "select * from customertable order by Last_Name,First_Name,Phone_Number;";
		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			ResultSet res = stat.executeQuery(sqlQuery);

			while (res.next()) {
				// System.out.println("CustID="+res.getInt("CustomerId")+" | Name=
				// "+res.getString("First_Name")+" "+res.getString("Last_Name")+", Phone=
				// "+res.getString("Phone_Number"));
				Customer c = new Customer(res.getInt("CustomerId"), res.getString("First_Name"),
						res.getString("Last_Name"), res.getString("Phone_Number"));
				custs.add(c);
			}
		} else {
			System.out.print("Not connected to db");
		}

		conn.close();
		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		return custs;
	}

	public static ArrayList<Order> getOrderList() throws SQLException, IOException {
		ArrayList<Order> orderlist = new ArrayList<Order>();
		connect_to_db();
		/*
		 * return an arrayList of all the customers. These customers should
		 * print in alphabetical order, so account for that as you see fit.
		 */

		String sqlQuery = "select ot.OrderId, ot.CustomerId,ot.OrderType,ot.OrderDateTime,case when ot.OrderStatus='Completed' Then true else false end as 'OrderStatus' ,ot.OrderCost,ot.OrderPrice from ordertable as ot;";
		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			ResultSet res = stat.executeQuery(sqlQuery);

			while (res.next()) {
				// System.out.println("CustID="+res.getInt("CustomerId")+" | Name=
				// "+res.getString("First_Name")+" "+res.getString("Last_Name")+", Phone=
				// "+res.getString("Phone_Number"));

				String date = res.getDate("OrderDateTime").toString();
				Order o = new Order(res.getInt("OrderId"), res.getInt("CustomerId"), res.getString("OrderType"), date,
						res.getDouble("OrderCost"), res.getDouble("OrderPrice"), res.getInt("OrderStatus"));
				orderlist.add(o);
			}
		} else {
			System.out.print("Not connected to db");
		}

		conn.close();
		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		return orderlist;
	}

	public static int getNextOrderID() throws SQLException, IOException {
		/*
		 * A helper function I had to use because I forgot to make
		 * my OrderID auto increment...You can remove it if you
		 * did not forget to auto increment your orderID.
		 */

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		return -1;
	}

	public static void printToppingPopReport() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Prints the ToppingPopularity view. Remember that these views
		 * need to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * I'm not picky about how they print (other than that it should
		 * be in alphabetical order by name), just make sure it's readable.
		 */

		String sqlQuery = "select pt.Topping,pt.ToppingCount from popular_toppings as pt order by pt.Topping";

		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			ResultSet res = stat.executeQuery(sqlQuery);
			System.out.println("Topping\t\t\tToppingCount");
			while (res.next()) {

				System.out.printf("%-21s %6s %n", res.getString("Topping"), res.getInt("ToppingCount"));
			}
		} else {
			System.out.print("Not connected to db");
		}

		conn.close();

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void printProfitByPizzaReport() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Prints the ProfitByPizza view. Remember that these views
		 * need to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * I'm not picky about how they print, just make sure it's readable.
		 */

		String sqlQuery = "select pp.PizzaSize,pp.PizzaCrust,ifnull(pp.Profit,0.0) as 'Profit' ,ifnull(pp.LastOrderDate,'null') as 'LastOrderDate'  from profitbypizza as pp";

		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			ResultSet res = stat.executeQuery(sqlQuery);
			System.out.printf("%-15s %-15s %-17s %-17s %n", "PizzaSize", "PizzaCrust", "Profit", "LastOrderDate");
			while (res.next()) {

				System.out.printf("%-15s %-15s %-17s %-17s  %n", res.getString("PizzaSize"),
						res.getString("PizzaCrust"), res.getBigDecimal("Profit"), res.getString("LastOrderDate"));
			}
		} else {
			System.out.print("Not connected to db");
		}

		conn.close();

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void printProfitByOrderType() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Prints the ProfitByOrderType view. Remember that these views
		 * need to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * I'm not picky about how they print, just make sure it's readable.
		 */

		String sqlQuery = "select po.CustomerType,po.OrderDate,po.TotalOrderPrice,po.TotalOrderCost,po.Profit from profitbyordertype as po";

		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			ResultSet res = stat.executeQuery(sqlQuery);
			System.out.printf("%-15s %-15s %-17s %-17s %-17s  %n", "CustomerType", "OrderDate", "TotalOrderPrice",
					"TotalOrderCost", "Profit");
			while (res.next()) {

				System.out.printf("%-15s %-15s %-17s %-17s %-17s  %n", res.getString("CustomerType"),
						res.getString("OrderDate"), res.getBigDecimal("TotalOrderPrice"),
						res.getBigDecimal("TotalOrderCost"), res.getBigDecimal("Profit"));
			}
		} else {
			System.out.print("Not connected to db");
		}

		conn.close();

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void DisplayOrderInformation(String sqlQuery) throws SQLException, IOException {
		connect_to_db();
		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			ResultSet res = stat.executeQuery(sqlQuery);

			System.out.println();
			if (res.next() == false) {
				System.out.println("Soryy no data exist :)");
			} else {
				do {
					System.out.println("CustID=" + res.getInt("OrderId") + " | Date Placed= "
							+ res.getString("OrderDateTime") + " | Customer name= " + res.getString("First_Name") + " "
							+ res.getString("Last_Name") + ", OrderType= " + res.getString("OrderType")
							+ ", IsComplete= "
							+ res.getString("OrderStatus"));
				} while (res.next());
			}

		} else {
			System.out.print("Not connected to database");
		}
		conn.close();
	}

	public static String DisplayDeliveryInformation(int OrderId) throws SQLException, IOException {
		String sqlQuery = "select * from delivery as d where d.OrderId=" + OrderId + ";";
		String Address = "";
		connect_to_db();
		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			ResultSet res = stat.executeQuery(sqlQuery);

			while (res.next()) {
				Address = res.getString("CustomerAddress");
			}

		} else {
			System.out.print("Not connected to database");
		}
		conn.close();
		return Address;
	}

	public static int DisplayTableNumber(int OrderId) throws SQLException, IOException {
		String sqlQuery = "select * from dinein as d where d.OrderId=" + OrderId + ";";
		int TableNumber = -1;
		connect_to_db();
		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			ResultSet res = stat.executeQuery(sqlQuery);

			while (res.next()) {
				TableNumber = res.getInt("TableNum");
			}

		} else {
			System.out.print("Not connected to database");
		}
		conn.close();

		return TableNumber;
	}

	public static boolean checkInventoryIsLess(int ToppingId) throws SQLException, IOException {
		connect_to_db();
		boolean isInventoryLess = false;
		String sqlquery = "select * from toppingsinfo as tinfo where tinfo.ToppingId=" + ToppingId + ";";

		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			ResultSet res = stat.executeQuery(sqlquery);
			int toppinglevel = 0;
			while (res.next()) {
				toppinglevel = res.getInt("Inventory");
				if (toppinglevel <= 10) {
					isInventoryLess = true;
				}
			}
		} else {
			System.out.print("Not connected to db");
		}

		conn.close();
		return isInventoryLess;
	}

	public static String getToppingNameByID(int ToppingId) throws SQLException, IOException {
		String toppingName = "";

		connect_to_db();
		String sqlquery = "select * from toppingsinfo as tinfo where tinfo.ToppingId=" + ToppingId + ";";

		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			stat = conn.createStatement();
			ResultSet res = stat.executeQuery(sqlquery);

			while (res.next()) {
				toppingName = res.getString("Name");

			}
		} else {
			System.out.print("Not connected to db");
		}

		conn.close();
		return toppingName;
	}

	public static void AddInventoryByToppingId(int ToppingId, double toAdd) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Adds toAdd amount of topping to topping t.
		 */
		int toppingID = ToppingId;

		String Sqlquery = "update toppingsinfo set Inventory =Inventory +? where ToppingId =?";

		if (connect_to_db()) {
			conn = DBConnector.make_connection();
			pre = conn.prepareStatement(Sqlquery);
			pre.setDouble(1, toAdd);
			pre.setInt(2, toppingID);
			pre.executeUpdate();

		} else {
			System.out.print("Not connected to database");
		}

		conn.close();
	}
}
