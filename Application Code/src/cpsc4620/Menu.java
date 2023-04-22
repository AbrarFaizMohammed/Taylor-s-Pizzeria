package cpsc4620;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;

import init.DBIniter;
import java.sql.Statement;
import java.text.*;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This file is where the front end magic happens.
 * 
 * You will have to write the functionality of each of these menu options' respective functions.
 * 
 * This file should need to access your DB at all, it should make calls to the DBNinja that will do all the connections.
 * 
 * You can add and remove functions as you see necessary. But you MUST have all 8 menu functions (9 including exit)
 * 
 * Simply removing menu functions because you don't know how to implement it will result in a major error penalty (akin to your program crashing)
 * 
 * Speaking of crashing. Your program shouldn't do it. Use exceptions, or if statements, or whatever it is you need to do to keep your program from breaking.
 * 
 * 
 */

public class Menu {
	private static String custFName = "", custLName = "", custPhone = "";

	public static void main(String[] args) throws SQLException, IOException, ParseException {
		System.out.println("Welcome to Taylor's Pizzeria!");

		int menu_option = 0;

		// present a menu of options and take their selection

		PrintMenu();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		DBIniter.init();
		String option = reader.readLine();
		menu_option = Integer.parseInt(option);

		while (menu_option != 9) {
			switch (menu_option) {
				case 1:// enter order
					EnterOrder();
					break;
				case 2:// view customers
					viewCustomers();
					break;
				case 3:// enter customer
					EnterCustomer();
					break;
				case 4:// view order
						// open/closed/date
					ViewOrders();
					break;
				case 5:// mark order as complete
					MarkOrderAsComplete();
					break;
				case 6:// view inventory levels
					ViewInventoryLevels();
					break;
				case 7:// add to inventory
					AddInventory();
					break;
				case 8:// view reports
					PrintReports();
					break;
			}
			PrintMenu();
			option = reader.readLine();
			menu_option = Integer.parseInt(option);
		}

	}

	public static void PrintMenu() {
		System.out.println("\n\nPlease enter a menu option:");
		System.out.println("1. Enter a new order");
		System.out.println("2. View Customers ");
		System.out.println("3. Enter a new Customer ");
		System.out.println("4. View orders");
		System.out.println("5. Mark an order as completed");
		System.out.println("6. View Inventory Levels");
		System.out.println("7. Add Inventory");
		System.out.println("8. View Reports");
		System.out.println("9. Exit\n\n");
		System.out.println("Enter your option: ");
	}

	// allow for a new order to be placed
	public static void EnterOrder() throws SQLException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		/*
		 * EnterOrder should do the following:
		 * Ask if the order is for an existing customer -> If yes, select the customer.
		 * If no -> create the customer (as if the menu option 2 was selected).
		 * 
		 * Ask if the order is delivery, pickup, or dinein (ask for orderType specific
		 * information when needed)
		 * 
		 * Build the pizza (there's a function for this)
		 * 
		 * ask if more pizzas should be be created. if yes, go back to building your
		 * pizza.
		 * 
		 * Apply order discounts as needed (including to the DB)
		 * 
		 * apply the pizza to the order (including to the DB)
		 * 
		 * return to menu
		 */
		String userResponse;
		char ch;
		int count = 0;
		boolean isResponseValid = true;
		do {
			if (count == 0) {
				System.out.println("Is this order for an existing customer? Answer y/n:");
			} else {
				System.out.println(
						"'Please enter the Valid response'. Is this order for an existing customer? Answer y/n:");
			}

			userResponse = reader.readLine();
			String regex = "^(y|Y|n|N)$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher((CharSequence) userResponse);
			if (!matcher.matches()) {
				isResponseValid = false;
				count++;
			} else {
				isResponseValid = true;
				count = 0;
			}
		} while (!isResponseValid);

		ch = userResponse.toUpperCase().charAt(0);

		EnterOrderWithUserResponseAsYes(ch);

		System.out.println("Finished adding order...Returning to menu...");
	}

	public static void EnterOrderWithUserResponseAsYes(char chinput) throws SQLException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		int custID = 0, count = 0;
		String userResponse;
		boolean isuserResponseIsValid = true, isPizzaSizeResponseValid = true, isDeliveryResponseValid = true,
				isPizzaCrustResponseValid = true, isToppingResponseValid = true;
		List<Integer> customerIDs = new ArrayList<Integer>();
		ArrayList<Customer> custinfo = DBNinja.getCustomerList();

		for (Customer c : custinfo) {
			customerIDs.add(c.getCustID());
		}
		if (chinput == 'Y') {
			System.out.println("Here's a list of curent customer:");
			viewCustomers();
			// Code to get Custemer Id.
			do {
				if (count == 0) {
					System.out.println("which customer is this order for? Enter ID Number");
				} else {
					System.out.println(
							"'Please enter the correct response. 'which customer is this order for? Enter ID Number");
				}
				userResponse = reader.readLine();
				String regex = "^[0-9]{3}|[0-9]{2}|[0-9]$";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher((CharSequence) userResponse);

				if (!matcher.matches()) {
					isuserResponseIsValid = false;
					count++;
				} else {
					isuserResponseIsValid = true;
					count = 0;
					custID = Integer.parseInt(userResponse);
				}

				if (!customerIDs.contains(custID)) {
					isuserResponseIsValid = false;
					count++;
				}

			} while (!isuserResponseIsValid);

		} else {
			EnterCustomer();
			custID = DBNinja.getCustomerID(custFName, custLName);

		}
		// Code to get order delivery type information
		System.out.println("Is this order for:" + "\n" + "1.) " + DBNinja.dine_in + "\n" + "2.) " + DBNinja.pickup
				+ "\n3.) " + DBNinja.delivery);
		String displayOptions, UserSelectedDeliveryoption = "";
		int ch1 = 0;

		do {
			if (count == 0) {
				System.out.println("Enter the number of your choice:");
			} else {
				System.out.println("'Please enter the correct response'. Enter the number of your choice:");
			}
			displayOptions = reader.readLine();
			String regex = "^(1|2|3)$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher((CharSequence) displayOptions);
			if (!matcher.matches()) {
				isDeliveryResponseValid = false;
				count++;
			} else {
				isDeliveryResponseValid = true;
				count = 0;
				ch1 = Integer.parseInt(displayOptions);
				if (ch1 == 1) {
					UserSelectedDeliveryoption = DBNinja.dine_in;
				} else if (ch1 == 2) {
					UserSelectedDeliveryoption = DBNinja.pickup;
				} else {
					UserSelectedDeliveryoption = DBNinja.delivery;
				}
			}

		} while (!isDeliveryResponseValid);
		String orderDeliveryAddress = "";
		if (UserSelectedDeliveryoption == DBNinja.delivery) {
			int addcount = 0;
			boolean isValidAddressEntered = true;
			do {
				if (addcount == 0) {
					System.out.println("Please enter your delviery address:");
				} else {
					System.out.println("->Please enter the valid address:");
				}
				orderDeliveryAddress = reader.readLine();
				if (orderDeliveryAddress.trim().isEmpty()) {
					addcount++;
					isValidAddressEntered = false;
				} else {
					addcount = 0;
					isValidAddressEntered = true;
				}
			} while (!isValidAddressEntered);

		}

		System.out.println("Let's build a pizza!");

		// Code to get pizza size information.
		System.out.println("What size is this pizza?" + "\n" + "1.) " + DBNinja.size_s + "\n" + "2.) " + DBNinja.size_m
				+ "\n3.) " + DBNinja.size_l + "\n4.) " + DBNinja.size_xl);
		String displayOptionsPizzaSize, UserSelectedPizzaSizeoption = "";
		int chh = 0;

		do {
			if (count == 0) {
				System.out.println("Enter the curresponding number:");
			} else {
				System.out.println("'Please enter the correct response'. Enter the curresponding number:");
			}
			displayOptionsPizzaSize = reader.readLine();
			String regex = "^(1|2|3|4)$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher((CharSequence) displayOptionsPizzaSize);
			if (!matcher.matches()) {
				isPizzaSizeResponseValid = false;
				count++;
			} else {
				isPizzaSizeResponseValid = true;
				count = 0;
				chh = Integer.parseInt(displayOptionsPizzaSize);
				if (chh == 1) {
					UserSelectedPizzaSizeoption = DBNinja.size_s;
				} else if (chh == 2) {
					UserSelectedPizzaSizeoption = DBNinja.size_m;
				} else if (chh == 3) {
					UserSelectedPizzaSizeoption = DBNinja.size_l;
				} else if (chh == 4) {
					UserSelectedPizzaSizeoption = DBNinja.size_xl;
				}
			}

		} while (!isPizzaSizeResponseValid);

		// code for crust of pizza
		System.out.println("What crust for this pizza?" + "\n" + "1.) " + DBNinja.crust_thin + "\n" + "2.) "
				+ DBNinja.crust_orig + "\n3.) " + DBNinja.crust_pan + "\n4.)" + DBNinja.crust_gf);
		String displayOptionsPizzacrust, UserSelectedPizzacrustoption = "";
		int ch2 = 0;
		do {
			if (count == 0) {
				System.out.println("Enter the curresponding number:");
			} else {
				System.out.println("'Please enter the correct response'. Enter the curresponding number:");
			}

			displayOptionsPizzacrust = reader.readLine();
			String regex = "^(1|2|3|4)$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher((CharSequence) displayOptionsPizzacrust);
			if (!matcher.matches()) {
				isPizzaCrustResponseValid = false;
				count++;
			} else {
				isPizzaCrustResponseValid = true;
				count = 0;
				ch2 = Integer.parseInt(displayOptionsPizzacrust);
				if (ch2 == 1) {
					UserSelectedPizzacrustoption = DBNinja.crust_thin;
				} else if (ch2 == 2) {
					UserSelectedPizzacrustoption = DBNinja.crust_orig;
				} else if (ch2 == 3) {
					UserSelectedPizzacrustoption = DBNinja.crust_pan;
				} else {
					UserSelectedPizzacrustoption = DBNinja.crust_gf;
				}
			}

		} while (!isPizzaCrustResponseValid);

		// code for topping selection
		List<Integer> ToppingIDs = new ArrayList<Integer>();
		ArrayList<Topping> Toppingdetails = DBNinja.getInventory();
		int numberOfTimesMenuprinted = 0;
		boolean isextraToppingResponceValid = true, check = true;
		for (Topping t : Toppingdetails) {
			ToppingIDs.add(t.getTopID());
		}
		ToppingIDs.add(-1);
		ArrayList<Integer> selectedToppInfo = new ArrayList<Integer>();
		String displayOptionsTopping;
		int toppID = Integer.MAX_VALUE;

		do {
			if (numberOfTimesMenuprinted == 0) {
				if (count == 0) {
					ViewInventoryLevels();
				}

			} else {
				System.out.println("printing current topping list...");
				ViewInventoryLevels();

			}

			if (count == 0 && check) {
				System.out.println(
						"which topping do you want to add? Enter the TopID. Enter -1 to stop adding toppings:");
			} else {
				System.out.println(
						"'Please enter the correct response'.which topping do you want to add? Enter the TopID. Enter -1 to stop adding toppings:");
			}

			displayOptionsTopping = reader.readLine();
			String regex = "^([0-9]{3}|[0-9]{2}|[0-9]|-1)$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher((CharSequence) displayOptionsTopping);

			if (!matcher.matches()) {
				isToppingResponseValid = false;
				count++;
				toppID = Integer.MAX_VALUE;
			} else {
				toppID = Integer.parseInt(displayOptionsTopping);
				if (toppID == -1) {
					isToppingResponseValid = true;
				} else {
					isToppingResponseValid = false;
					selectedToppInfo.add(toppID);
				}

				count = 0;

			}

			if (!ToppingIDs.contains(toppID)) {
				isToppingResponseValid = false;
				check = false;
			} else {
				check = true;
			}

			if (count == 0 && check) {
				numberOfTimesMenuprinted++;
			}

			if (numberOfTimesMenuprinted > 0 && check && toppID != -1) {
				String userExtraToppingResponse;
				do {
					System.out.println("Do you want to add extra toppings? Enter y/n ");
					userExtraToppingResponse = reader.readLine();
					String regexs = "^(y|Y|n|N)$";
					Pattern pattern1 = Pattern.compile(regexs);
					Matcher matcher1 = pattern1.matcher((CharSequence) userExtraToppingResponse);
					if (!matcher1.matches()) {
						isextraToppingResponceValid = false;

					} else {
						isextraToppingResponceValid = true;

					}
				} while (!isextraToppingResponceValid);

				String regexs1 = "^(n|N)$";
				Pattern pattern11 = Pattern.compile(regexs1);
				Matcher matcher11 = pattern11.matcher((CharSequence) userExtraToppingResponse);

				if (matcher11.matches()) {
					isToppingResponseValid = true;
				}
			}

		} while (!isToppingResponseValid);

		// Code for pizza discount
		int dicountcount = 0;
		boolean isdiscontRequiredResponceValid = true;
		ArrayList<Discount> discountinfo = DBNinja.getDiscountList();
		List<Integer> DiscountIDs = new ArrayList<Integer>();
		ArrayList<Integer> pizzaDiscountIds = new ArrayList<Integer>();

		for (Discount d : discountinfo) {
			DiscountIDs.add(d.getDiscountID());
		}
		DiscountIDs.add(-1);
		do {
			if (dicountcount == 0) {
				System.out.println("Do you want to add dicounts to this pizza? Enter y/n:");
			} else {
				System.out.println(
						"'Please enter the correct responce'. Do you want to add dicounts to this pizza? Enter y/n:");
			}

			String userDiscountResponse = reader.readLine();
			String regexsdiscount = "^(y|Y|n|N)$";
			Pattern patterndiscount = Pattern.compile(regexsdiscount);
			Matcher matcherdiscount = patterndiscount.matcher((CharSequence) userDiscountResponse);

			if (!matcherdiscount.matches()) {
				dicountcount++;
				isdiscontRequiredResponceValid = false;
			} else {
				dicountcount = 0;
				isdiscontRequiredResponceValid = true;
			}

			String regexsdiscount1 = "^(y|Y)$";
			Pattern patterndiscount1 = Pattern.compile(regexsdiscount1);
			Matcher matcherdiscount1 = patterndiscount1.matcher((CharSequence) userDiscountResponse);
			int countdisc = 0;
			boolean isDiscountIdResponseValid = true;

			if (isdiscontRequiredResponceValid && matcherdiscount1.matches()) {

				do {
					for (Discount d : discountinfo) {
						System.out.println(d);
					}
					if (countdisc == 0) {
						System.out
								.println("Which pizza discount do you want to add? Enter -1 to stop adding discount:");
					} else {
						System.out.println(
								"'Please enter the correct Responce'. Which pizza discount do you want to add? Enter -1 to stop adding discount:");
					}
					String discountoptionResponse = reader.readLine();
					String regexsdiscount2 = "^([0-9]{3}|[0-9]{2}|[0-9]|-1)$";
					Pattern patterndiscount2 = Pattern.compile(regexsdiscount2);
					Matcher matcherdiscount2 = patterndiscount2.matcher((CharSequence) discountoptionResponse);
					int resp = Integer.MAX_VALUE;

					if (!matcherdiscount2.matches()) {
						countdisc++;
						isDiscountIdResponseValid = false;
					} else {
						resp = Integer.parseInt(discountoptionResponse);
						if (!DiscountIDs.contains(resp)) {

							isDiscountIdResponseValid = false;
							countdisc++;
						} else {
							if (resp == -1) {
								isDiscountIdResponseValid = true;
							} else {
								isDiscountIdResponseValid = false;
								pizzaDiscountIds.add(resp);
							}

							countdisc = 0;
						}

					}
				} while (!isDiscountIdResponseValid);

			}

		} while (!isdiscontRequiredResponceValid);

		// Code for order discount
		int orderdicountcount = 0;
		boolean isorderdiscontRequiredResponceValid = true;
		ArrayList<Discount> orderdiscountinfo = DBNinja.getDiscountList();
		List<Integer> orderDiscountIDs = new ArrayList<Integer>();
		ArrayList<Integer> DiscountIdsOrder = new ArrayList<Integer>();

		for (Discount d : orderdiscountinfo) {
			orderDiscountIDs.add(d.getDiscountID());
		}
		orderDiscountIDs.add(-1);
		do {
			if (orderdicountcount == 0) {
				System.out.println("Do you want to add dicounts to this order? Enter y/n:");
			} else {
				System.out.println(
						"'Please enter the correct responce'. Do you want to add dicounts to this order? Enter y/n:");
			}

			String userDiscountResponse = reader.readLine();
			String regexsdiscount = "^(y|Y|n|N)$";
			Pattern patterndiscount = Pattern.compile(regexsdiscount);
			Matcher matcherdiscount = patterndiscount.matcher((CharSequence) userDiscountResponse);

			if (!matcherdiscount.matches()) {
				orderdicountcount++;
				isorderdiscontRequiredResponceValid = false;
			} else {
				orderdicountcount = 0;
				isorderdiscontRequiredResponceValid = true;
			}

			String regexsdiscount1 = "^(y|Y)$";
			Pattern patterndiscount1 = Pattern.compile(regexsdiscount1);
			Matcher matcherdiscount1 = patterndiscount1.matcher((CharSequence) userDiscountResponse);
			int countdisc = 0;
			boolean isDiscountIdResponseValid1 = true;

			if (isorderdiscontRequiredResponceValid && matcherdiscount1.matches()) {

				do {
					for (Discount d : orderdiscountinfo) {
						System.out.println(d);
					}
					if (countdisc == 0) {
						System.out
								.println("Which order discount do you want to add? Enter -1 to stop adding discount:");
					} else {
						System.out.println(
								"'Please enter the correct Responce'. Which order discount do you want to add? Enter -1 to stop adding discount:");
					}
					String discountoptionResponse = reader.readLine();
					String regexsdiscount2 = "^([0-9]{3}|[0-9]{2}|[0-9]|-1)$";
					Pattern patterndiscount2 = Pattern.compile(regexsdiscount2);
					Matcher matcherdiscount2 = patterndiscount2.matcher((CharSequence) discountoptionResponse);
					int resp = Integer.MAX_VALUE;

					if (!matcherdiscount2.matches()) {
						countdisc++;
						isDiscountIdResponseValid1 = false;
					} else {
						resp = Integer.parseInt(discountoptionResponse);
						if (!orderDiscountIDs.contains(resp)) {

							isDiscountIdResponseValid1 = false;
							countdisc++;
						} else {
							if (resp == -1) {
								isDiscountIdResponseValid1 = true;
							} else {
								isDiscountIdResponseValid1 = false;
								DiscountIdsOrder.add(resp);
							}

							countdisc = 0;
						}

					}
				} while (!isDiscountIdResponseValid1);

			}
		} while (!isorderdiscontRequiredResponceValid);

		Dictionary<String, Double> pizzaAmt = DBNinja.CalculatePizzaPrice(UserSelectedPizzaSizeoption,
				UserSelectedPizzacrustoption, selectedToppInfo, pizzaDiscountIds, DiscountIdsOrder);
		double finalPizzaPrice = pizzaAmt.get("PizzaPrice");
		double finalPizzaCost = pizzaAmt.get("PizzaCost");
		Order finalOrderinfo = new Order(0, custID, UserSelectedDeliveryoption, LocalDate.now().toString(),
				finalPizzaCost, finalPizzaPrice, 0);

		DBNinja.addOrder(finalOrderinfo, UserSelectedPizzaSizeoption, UserSelectedPizzacrustoption, selectedToppInfo,
				pizzaDiscountIds, DiscountIdsOrder, orderDeliveryAddress);

		// Check user selected topping Inventory Level is less
		int count1 = 0;
		boolean isTotalInventoryInteger = true;
		for (int i = 0; i < selectedToppInfo.size(); i++) {
			int inventory = 0;
			if (DBNinja.checkInventoryIsLess(selectedToppInfo.get(i))) {
				String ToppingName = DBNinja.getToppingNameByID(selectedToppInfo.get(i));

				do {
					if (count1 == 0) {
						System.out.println(
								ToppingName + " topping inventory is very low. How many units would you like to add?");
					} else {
						System.out.println("Please make sure to enter number only." + ToppingName
								+ " topping inventory is very low. How many units would you like to add?");
					}
					String inputData = reader.readLine();

					String regex = "^[0-9]|[0-9]{2}|[0-9]{3}|[0-9]{4}$";
					Pattern pattern = Pattern.compile(regex);
					Matcher matcher = pattern.matcher((CharSequence) inputData);
					if (!matcher.matches()) {
						isTotalInventoryInteger = false;
						count1++;
					} else {
						isTotalInventoryInteger = true;
						inventory = Integer.parseInt(inputData);
					}

				} while (!isTotalInventoryInteger);

				DBNinja.AddInventoryByToppingId(selectedToppInfo.get(i), inventory);

			}
		}

	}

	public static void viewCustomers() throws SQLException, IOException {
		/*
		 * Simply print out all of the customers from the database.
		 */

		ArrayList<Customer> custinfo = DBNinja.getCustomerList();

		for (Customer c : custinfo) {
			System.out.println(c);
		}

	}

	public static boolean PhoneNumberFormateCheck(String ContactNum) {
		String regex = "^[0-9]{3}-[0-9]{3}-[0-9]{4}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher((CharSequence) ContactNum);
		return matcher.matches();
	}

	// Enter a new customer in the database
	public static void EnterCustomer() throws SQLException, IOException {
		/*
		 * Ask what the name of the customer is. YOU MUST TELL ME (the grader) HOW TO
		 * FORMAT THE FIRST NAME, LAST NAME, AND PHONE NUMBER.
		 * If you ask for first and last name one at a time, tell me to insert First
		 * name <enter> Last Name (or separate them by different print statements)
		 * If you want them in the same line, tell me (First Name <space> Last Name).
		 * 
		 * same with phone number. If there's hyphens, tell me XXX-XXX-XXXX. For spaces,
		 * XXX XXX XXXX. For nothing XXXXXXXXXXXX.
		 * 
		 * I don't care what the format is as long as you tell me what it is, but if I
		 * have to guess what your input is I will not be a happy grader
		 * 
		 * Once you get the name and phone number (and anything else your design might
		 * have) add it to the DB
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String customerName, FirstName, LastName, Phone_Number;
		boolean iscustomerNameFormatCorrect = true, isCustomerContactInfoCorrect = true;
		int count = 0, contactCount = 0;
		do {
			if (count == 0) {
				System.out.println("Please Enter the Customer name (First Name <space> Last Name):");
			} else {
				System.out.println(
						"Please Enter the Customer name in correct formate --> (First Name <space> Last Name):");
			}
			customerName = reader.readLine();

			String[] nameArray = customerName.split(" ");

			int nameArrayLength = nameArray.length;
			FirstName = nameArray[0];
			LastName = (nameArrayLength > 1) ? nameArray[1] : " ";

			if (nameArrayLength != 2 || FirstName.isEmpty() || LastName.isEmpty()) {
				iscustomerNameFormatCorrect = false;
				count++;
			} else {
				iscustomerNameFormatCorrect = true;
			}
		} while (!iscustomerNameFormatCorrect);

		do {
			if (contactCount == 0) {
				System.out.println("what is this customer's phone number (XXX-XXX-XXXX):");
			} else {
				System.out.println("Please enter this customer's phone number in correct format --> (XXX-XXX-XXXX):");
			}

			Phone_Number = reader.readLine();
			isCustomerContactInfoCorrect = PhoneNumberFormateCheck(Phone_Number);

			if (!isCustomerContactInfoCorrect) {
				contactCount++;
			} else {
				contactCount = 0;
			}

		} while (!isCustomerContactInfoCorrect);
		custFName = FirstName;
		custLName = LastName;
		custPhone = Phone_Number;
		Customer cust = new Customer(0, FirstName, LastName, Phone_Number);

		DBNinja.addCustomer(cust);

	}

	public static boolean DateFormateCheck(String date) {
		String regex = "^[0-9]{4}-(1[0-2]|0[1-9])-(3[01]|[12][0-9]|0[1-9])$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher((CharSequence) date);
		return matcher.matches();
	}

	// View any orders that are not marked as completed
	public static void ViewOrders() throws SQLException, IOException, ParseException {
		/*
		 * This should be subdivided into two options: print all orders (using
		 * simplified view) and print all orders (using simplified view) since a
		 * specific date.
		 * 
		 * Once you print the orders (using either sub option) you should then ask which
		 * order I want to see in detail
		 * 
		 * When I enter the order, print out all the information about that order, not
		 * just the simplified view.
		 * 
		 */
		System.out.println("would to like to:" + "\n" + "(a) display all orders" + "\n"
				+ "(b) display orders since a specific date");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String displayOptions, dateString = "", sqlQueryView = "";
		char ch;
		int count = 0;
		Date date;
		do {
			if (count > 0) {
				System.out.println("Please enter appropriate display options as 'a' or 'b':");
			}
			displayOptions = reader.readLine();
			ch = displayOptions.charAt(0);
			count = (!(ch == 'a' || ch == 'b')) ? (count + 1) : 0;
		} while (!(ch == 'a' || ch == 'b'));

		if (ch == 'a') {
			sqlQueryView = "select ot.OrderId, ot.OrderDateTime,ifnull(ct.First_Name,'INSTORE') as 'First_Name',ifnull(ct.Last_Name,'CUSTOMER') as 'Last_Name',ot.OrderType,case when ot.OrderStatus = 'Completed' then 'true' else 'false' end as OrderStatus from ordertable as ot left join customertable as ct on ct.CustomerId = ot.CustomerId order by OrderDateTime desc";

		} else if (ch == 'b') {
			do {
				System.out.println("what is the date you want to restrict by? Formate(YYYY-MM-DD)");
				dateString = reader.readLine();
			} while (!(DateFormateCheck(dateString)));
			sqlQueryView = ("select ot.OrderId, ot.OrderDateTime,ifnull(ct.First_Name,'INSTORE') as 'First_Name',ifnull(ct.Last_Name,'CUSTOMER') as 'Last_Name',ot.OrderType,case when ot.OrderStatus = 'Completed' then 'true' else 'false' end as OrderStatus from ordertable as ot left join customertable as ct on ct.CustomerId = ot.CustomerId where ot.OrderDateTime >='"
					+ dateString + "' order by OrderDateTime desc");

		}

		DBNinja.DisplayOrderInformation(sqlQueryView);
		if (ch == 'a') {
			int count1 = 0, userValue = -1;
			String userResponse;
			int orderIdResponse = -1;
			boolean isUserResponseValid = true;
			ArrayList<Order> orderDetails = DBNinja.getOrderList();
			List<Integer> OrderIds = new ArrayList<Integer>();
			for (Order oinfo : orderDetails) {
				OrderIds.add(oinfo.getOrderID());
			}

			do {
				if (count1 == 0) {
					System.out.println("Which order would you like to see in detail? Enter the number:");
				} else {
					System.out.println(
							"'Please enter the correct response'. Which order would you like to see in detail? Enter the number:");
				}
				userResponse = reader.readLine();
				String regex = "^[0-9]|[0-9]{2}|[0-9]{3}|[0-9]{4}$";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher((CharSequence) userResponse);
				if (!matcher.matches()) {
					isUserResponseValid = false;
					count1++;
				} else {
					count1 = 0;
					userValue = Integer.parseInt(userResponse);
					if (!(OrderIds.contains(userValue))) {
						count1++;
						isUserResponseValid = false;
					} else {
						isUserResponseValid = true;
					}

				}

				if (isUserResponseValid) {
					for (Order orderInfo : orderDetails) {
						if (orderInfo.getOrderID() == userValue) {

							if (DBNinja.delivery.equals(orderInfo.getOrderType())) {
								System.out.println(orderInfo.toString() + " | Delivery Address = "
										+ DBNinja.DisplayDeliveryInformation(orderInfo.getOrderID()));
							} else if (DBNinja.dine_in.equals(orderInfo.getOrderType())) {
								System.out.println(orderInfo.toString() + " | Table Number = "
										+ DBNinja.DisplayTableNumber(orderInfo.getOrderID()));
							} else {
								System.out.println(orderInfo.toString());
							}

							break;
						}
					}
				}

			} while (!isUserResponseValid);

		}
	}

	// When an order is completed, we need to make sure it is marked as complete
	public static void MarkOrderAsComplete() throws SQLException, IOException {
		/*
		 * All orders that are created through java (part 3, not the 7 orders from part
		 * 2) should start as incomplete
		 * 
		 * When this function is called, you should print all of the orders marked as
		 * complete
		 * and allow the user to choose which of the incomplete orders they wish to mark
		 * as complete
		 * 
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		ArrayList<Order> InprocessOrderList = DBNinja.getInCompleteOrders();
		List<Integer> InprocessOrderIds = new ArrayList<Integer>();
		int count = 0, oID = -1;
		boolean isUserResponseValid = true;
		for (Order O : InprocessOrderList) {
			InprocessOrderIds.add(O.getOrderID());
			System.out.println(O.toSimplePrint());
		}
		if (!InprocessOrderList.isEmpty()) {
			do {
				if (count == 0) {
					System.out.println("Which order would you like to marke as complete? Enter orderId:");
				} else {
					System.out.println(
							"'-->Please enter the vaild response'. Which order would you like to marke as complete? Enter orderId:");

				}
				String userResponse = reader.readLine();
				String regex = "^[0-9]|[0-9]{2}|[0-9]{3}|[0-9]{4}$";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher((CharSequence) userResponse);

				if (!matcher.matches()) {
					count++;
					isUserResponseValid = false;
				} else {
					oID = Integer.parseInt(userResponse);
					if (InprocessOrderIds.contains(oID)) {
						isUserResponseValid = true;
						count = 0;
					} else {
						isUserResponseValid = false;
						count++;
					}
				}

			} while (!isUserResponseValid);
		} else {
			System.out.println("You are not having any orders to marke as completed.");
		}

		if (oID != -1) {
			DBNinja.updateOrderStatus(oID);
		}

	}

	// See the list of inventory and it's current level
	public static void ViewInventoryLevels() throws SQLException, IOException {
		// print the inventory. I am really just concerned with the ID, the name, and
		// the current inventory

		ArrayList<Topping> topinfo = DBNinja.getInventory();
		System.out.println("ID\tName\t\t\tCurINVT");
		for (Topping t : topinfo) {
			System.out.printf("%-6s %-21s %6s %n", t.getTopID(), t.getTopName(), t.getCurINVT());
		}

	}

	// Select an inventory item and add more to the inventory level to re-stock the
	// inventory
	public static void AddInventory() throws SQLException, IOException {
		/*
		 * This should print the current inventory and then ask the user which topping
		 * they want to add more to and how much to add
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		int topID = 0, inventory = 0, count = 0, count1 = 0;
		boolean istopIdValid = true, isTotalInventoryInteger = true;
		List<Integer> toppingIDs = new ArrayList<Integer>();
		ArrayList<Topping> topinfo = DBNinja.getInventory();

		for (Topping t : topinfo) {
			toppingIDs.add(t.getTopID());
		}

		ViewInventoryLevels();
		do {
			if (count == 0) {
				System.out.println("which topping do you want to add inventory to? Enter the number:");
			} else {
				System.out.println(
						"\"Please enter the correct ID as shown above.\" which topping do you want to add inventory to? Enter the number:");
			}

			String inputData = reader.readLine();

			String regex = "^[0-9]|[0-9]{2}|[0-9]{3}|[0-9]{4}$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher((CharSequence) inputData);
			if (!matcher.matches()) {
				istopIdValid = false;
				count++;
			} else {
				istopIdValid = true;
				topID = Integer.parseInt(inputData);
			}

			if (!toppingIDs.contains(topID)) {
				istopIdValid = false;
				count++;
			}
		} while (!istopIdValid);

		do {
			if (count1 == 0) {
				System.out.println("How many units would you like to add?");
			} else {
				System.out.println("Please make sure to enter number only. How many units would you like to add?");
			}
			String inputData = reader.readLine();

			String regex = "^[0-9]|[0-9]{2}|[0-9]{3}|[0-9]{4}$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher((CharSequence) inputData);
			if (!matcher.matches()) {
				isTotalInventoryInteger = false;
				count1++;
			} else {
				isTotalInventoryInteger = true;
				inventory = Integer.parseInt(inputData);
			}

		} while (!isTotalInventoryInteger);

		for (Topping tinfo : topinfo) {
			if (tinfo.getTopID() == topID) {
				Topping tDetail = new Topping(topID, tinfo.getTopName(), tinfo.getPerAMT(), tinfo.getMedAMT(),
						tinfo.getLgAMT(), tinfo.getXLAMT(), tinfo.getCustPrice(), tinfo.getBusPrice(), 0,
						tinfo.getCurINVT());
				DBNinja.AddToInventory(tDetail, inventory);
				break;
			}
		}

	}

	// A function that builds a pizza. Used in our add new order function
	public static Pizza buildPizza(int orderID) throws SQLException, IOException {

		/*
		 * This is a helper function for first menu option.
		 * 
		 * It should ask which size pizza the user wants and the crustType.
		 * 
		 * Once the pizza is created, it should be added to the DB.
		 * 
		 * We also need to add toppings to the pizza. (Which means we not only need to
		 * add toppings here, but also our bridge table)
		 * 
		 * We then need to add pizza discounts (again, to here and to the database)
		 * 
		 * Once the discounts are added, we can return the pizza
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		Pizza ret = null;

		return ret;
	}

	private static int getTopIndexFromList(int TopID, ArrayList<Topping> tops) {
		/*
		 * This is a helper function I used to get a topping index from a list of
		 * toppings
		 * It's very possible you never need to use a function like this
		 * 
		 */
		int ret = -1;

		return ret;
	}

	public static void PrintReports() throws SQLException, NumberFormatException, IOException {
		/*
		 * This function calls the DBNinja functions to print the three reports.
		 * 
		 * You should ask the user which report to print
		 */

		System.out.println("which report do you wish to print? Enter" + "\n" + "(a) ToppingPopularity" + "\n"
				+ "(b) profitbypizza" + "\n" + "(c) profitbyordertype");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String displayOptions, dateString = "", sqlQueryView = "";
		char ch;
		int count = 0;
		Date date;
		do {
			if (count > 0) {
				System.out.println("Please enter appropriate display options as 'a' or 'b' or 'c':");
			}
			displayOptions = reader.readLine();
			ch = displayOptions.charAt(0);
			count = (!(ch == 'a' || ch == 'b' || ch == 'c')) ? (count + 1) : count;
		} while (!(ch == 'a' || ch == 'b' || ch == 'c'));

		if (ch == 'a') {
			DBNinja.printToppingPopReport();
		} else if (ch == 'b') {
			DBNinja.printProfitByPizzaReport();
		} else if (ch == 'c') {
			DBNinja.printProfitByOrderType();
		}

	}

}

// Prompt - NO CODE SHOULD TAKE PLACE BELOW THIS LINE
// DO NOT EDIT ANYTHING BELOW HERE, I NEED IT FOR MY TESTING DIRECTORY. IF YOU
// EDIT SOMETHING BELOW, IT BREAKS MY TESTER WHICH MEANS YOU DO NOT GET GRADED
// (0)

/*
 * CPSC 4620 Project: Part 3 â€“ Java Application Due: Thursday 11/30 @ 11:59 pm
 * 125 pts
 * 
 * For this part of the project you will complete an application that will
 * interact with your database. Much of the code is already completed, you will
 * just need to handle the functionality to retrieve information from your
 * database and save information to the database.
 * Note, this program does little to no verification of the input that is
 * entered in the interface or passed to the objects in constructors or setters.
 * This means that any junk data you enter will not be caught and will propagate
 * to your database, if it does not cause an exception. Be careful with the data
 * you enter! In the real world, this program would be much more robust and much
 * more complex.
 * 
 * Program Requirements:
 * 
 * Add a new order to the database: You must be able to add a new order with
 * pizzas to the database. The user interface is there to handle all of the
 * input, and it creates the Order object in the code. It then calls
 * DBNinja.addOrder(order) to save the order to the database. You will need to
 * complete addOrder. Remember addOrder will include adding the order as well as
 * the pizzas and their toppings. Since you are adding a new order, the
 * inventory level for any toppings used will need to be updated. You need to
 * check to see if there is inventory available for each topping as it is added
 * to the pizza. You can not let the inventory level go negative for this
 * project. To complete this operation, DBNinja must also be able to return a
 * list of the available toppings and the list of known customers, both of which
 * must be ordered appropropriately.
 * 
 * View Customers: This option will display each customer and their associated
 * information. The customer information must be ordered by last name, first
 * name and phone number. The user interface exists for this, it just needs the
 * functionality in DBNinja
 * 
 * Enter a new customer: The program must be able to add the information for a
 * new customer in the database. Again, the user interface for this exists, and
 * it creates the Customer object and passes it to DBNinja to be saved to the
 * database. You need to write the code to add this customer to the database.
 * You do need to edit the prompt for the user interface in Menu.java to specify
 * the format for the phone number, to make sure it matches the format in your
 * database.
 * 
 * View orders: The program must be able to display orders and be sorted by
 * order date/time from most recent to oldest. The program should be able to
 * display open orders, all the completed orders or just the completed order
 * since a specific date (inclusive) The user interface exists for this, it just
 * needs the functionality in DBNinja
 * 
 * Mark an order as completed: Once the kitchen has finished prepping an order,
 * they need to be able to mark it as completed. When an order is marked as
 * completed, all of the pizzas should be marked as completed in the database.
 * Open orders should be sorted as described above for option #4. Again, the
 * user interface exists for this, it just needs the functionality in DBNinja
 * 
 * View Inventory Levels: This option will display each topping and its current
 * inventory level. The toppings should be sorted in alphabetical order. Again,
 * the user interface exists for this, it just needs the functionality in
 * DBNinja
 * 
 * Add Inventory: When the inventory level of an item runs low, the restaurant
 * will restock that item. When they do so, they need to enter into the
 * inventory how much of that item was added. They will select a topping and
 * then say how many units were added. Note: this is not creating a new topping,
 * just updating the inventory level. Make sure that the inventory list is
 * sorted as described in option #6. Again, the user interface exists for this,
 * it just needs the functionality in DBNinja
 * 
 * View Reports: The program must be able to run the 3 profitability reports
 * using the views you created in Part 2. Again, the user interface exists for
 * this, it just needs the functionality in DBNinja
 * 
 * Modify the package DBConnector to contain your database connection
 * information, this is the same information you use to connect to the database
 * via MySQL Workbench. You will use DBNinja.connect_to_db to open a connection
 * to the database. Be aware of how many open database connections you make and
 * make sure the database is properly closed!
 * Your code needs to be secure, so any time you are adding any sort of
 * parameter to your query that is a String, you need to use PreparedStatements
 * to prevent against SQL injections attacks. If your query does not involve any
 * parameters, or if your queries parameters are not coming from a String
 * variable, then you can use a regular Statement instead.
 * 
 * The Files: Start by downloading the starter code files from Canvas. You will
 * see that the user interface and the java interfaces and classes that you need
 * for the assignment are already completed. Review all these files to
 * familiarize yourself with them. They contain comments with instructions for
 * what to complete. You should not need to change the user interface except to
 * change prompts to the user to specify data formats (i.e. dashes in phone
 * number) so it matches your database. You also should not need to change the
 * entity object code, unless you want to remove any ID fields that you did not
 * add to your database.
 * 
 * You could also leave the ID fields in place and just ignore them. If you have
 * any data types that donâ€™t match (i.e. string size options as integers
 * instead of strings), make the conversion when you pull the information from
 * the database or add it to the database. You need to handle data type
 * differences at that time anyway, so it makes sense to do it then instead of
 * making changes to all of the files to handle the different data type or
 * format.
 * 
 * The Menu.java class contains the actual user interface. This code will
 * present the user with a menu of options, gather the necessary inputs, create
 * the objects, and call the necessary functions in DBNinja. Again, you will not
 * need to make changes to this file except to change the prompt to tell me what
 * format you expect the phone number in (with or without dashes).
 * 
 * There is also a static class called DBNinja. This will be the actual class
 * that connects to the database. This is where most of the work will be done.
 * You will need to complete the methods to accomplish the tasks specified.
 * 
 * Also in DBNinja, there are several public static strings for different
 * crusts, sizes and order types. By defining these in one place and always
 * using those strings we can ensure consistency in our data and in our
 * comparisons. You donâ€™t want to have â€œSMALLâ€� â€œsmallâ€� â€œSmallâ€� and
 * â€œPersonalâ€� in your database so it is important to stay consistent. These
 * strings will help with that. You can change what these strings say in DBNinja
 * to match your database, as all other code refers to these public static
 * strings.
 * 
 * Start by changing the class attributes in DBConnector that contain the data
 * to connect to the database. You will need to provide your database name,
 * username and password. All of this is available is available in the Chapter
 * 15 lecture materials. Once you have that done, you can begin to build the
 * functions that will interact with the database.
 * 
 * The methods you need to complete are already defined in the DBNinja class and
 * are called by Menu.java, they just need the code. Two functions are completed
 * (getInventory and getTopping), although for a different database design, and
 * are included to show an example of connecting and using a database. You will
 * need to make changes to these methods to get them to work for your database.
 * 
 * Several extra functions are suggested in the DBNinja class. Their
 * functionality will be needed in other methods. By separating them out you can
 * keep your code modular and reduce repeated code. I recommend completing your
 * code with these small individual methods and queries. There are also
 * additional methods suggested in the comments, but without the method template
 * that could be helpful for your program. HINT, make sure you test your SQL
 * queries in MySQL Workbench BEFORE implementing them in codeâ€¦it will save
 * you a lot of debugging time!
 * 
 * If the code in the DBNinja class is completed correctly, then the program
 * should function as intended. Make sure to TEST, to ensure your code works!
 * Remember that you will need to include the MySQL JDBC libraries when building
 * this application. Otherwise you will NOT be able to connect to your database.
 * 
 * Compiling and running your code: The starter code that will compile and
 * â€œrunâ€�, but it will not do anything useful without your additions. Because
 * so much code is being provided, there is no excuse for submitting code that
 * does not compile. Code that does not compile and run will receive a 0, even
 * if the issue is minor and easy to correct.
 * 
 * Help: Use MS Teams to ask questions. Do not wait until the last day to ask
 * questions or get started!
 * 
 * Submission You will submit your assignment on Canvas. Your submission must
 * include: â€¢ Updated DB scripts from Part 2 (all 5 scripts, in a folder, even
 * if some of them are unchanged). â€¢ All of the class code files along with a
 * README file identifying which class files in the starter code you changed.
 * Include the README even if it says â€œI have no special instructions to
 * shareâ€�. â€¢ Zip the DB Scripts, the class files (i.e. the application), and
 * the README file(s) into one compressed ZIP file. No other formats will be
 * accepted. Do not submit the lib directory or an IntellJ or other IDE project,
 * just the code.
 * 
 * Testing your submission Your project will be tested by replacing your
 * DBconnector class with one that connects to a special test server. Then your
 * final SQL files will be run to recreate your database and populate the tables
 * with data. The Java application will then be built with the new DBconnector
 * class and tested.
 * 
 * No late submissions will be accepted for this assignment.
 */

// INITNUM: 2908503