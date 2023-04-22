/*SQL Script written by Mohammed Abrar Faiz and Sai Vipraj Telukoti*/


USE Pizza_Project_G17;

INSERT INTO customertable(CustomerId,First_Name,Last_Name,Phone_Number)
VALUES
(1,'Ellis','Beck','864-254-5861'),
(2,'Kurt','McKinney','864-474-9953'),
(3,'Calvin','Sanders','864-232-8944'),
(4,'Lance','Benton','864-878-5679');



INSERT INTO ordertable(OrderId,CustomerId,OrderType,OrderPrice,OrderCost,OrderDateTime,OrderStatus) VALUES
(1,NULL,'dinein',13.50,3.68,'2023-03-05 12:03:00','Completed'),
(2,NULL,'dinein',10.60,3.23,'2023-03-03 12:05:00','Completed'),
(3,1,'pickup',10.75,3.30,'2023-03-03 21:30:00','Completed'),
(4,1,'delivery',14.50,5.59,'2023-03-05 19:11:00','Completed'),
(5,2,'pickup',16.85,7.85,'2023-03-02 17:30:00','Completed'),
(6,3,'delivery',13.25,3.20,'2023-03-02 18:17:00','Completed'),
(7,4,'delivery',12,3.75,'2023-03-06 20:32:00','Completed');

INSERT INTO pizzastandards(PizzaSize,PizzaCrust,PizzaPrice,PizzaCost)
VALUES

('Small','Thin',3,0.5),
('Small','Original',3,0.75),
('Small','Pan',3.5,1),
('Small','Gluten-Free',4,2),
('Medium','Thin',5,1),
('Medium','Original',5,1.5),
('Medium','Pan',6,2.25),
('Medium','Gluten-Free',6.25,3),
('Large','Thin',8,1.25),
('Large','Original',8,2),
('Large','Pan',9,3),
('Large','Gluten-Free',9.5,4),
('XLarge','Thin',10,2),
('XLarge','Original',10,3),
('XLarge','Pan',11.5,4.5),
('XLarge','Gluten-Free',12.5,6);




INSERT INTO dinein (OrderID,TableNum) VALUES
(1,14),
(2,04);

INSERT INTO pickup (OrderID) VALUES
(3),
(5);

INSERT INTO delivery (OrderID,CustomerAddress) VALUES
(4,'115 Party Blvd, Anderson, SC, 29621'),
(6,'6745 Wessex St Anderson, SC, 29621'),
(7,'8879 Suburban Home, Anderson, SC, 29621');


INSERT INTO pizza(PizzaOrderId,OrderID,PizzaSize,PizzaCrust,PizzaState,PizzaDateTime,Pizzaprice,PizzaCost) VALUES


(1,1,'Large','Thin','Completed','2023-03-05 12:03:00',13.50,3.68),
(2,2,'Medium','Pan','Completed','2023-03-03 12:05:00',10.60,3.23),
(3,2,'Small','Original','Completed','2023-03-03 12:05:00',6.75,1.40),
(4,3,'Large','Original','Completed','2023-03-03 21:30:00',10.75,3.30),
(5,3,'Large','Original','Completed','2023-03-03 21:30:00',10.75,3.30),
(6,3,'Large','Original','Completed','2023-03-03 21:30:00',10.75,3.30),
(7,3,'Large','Original','Completed','2023-03-03 21:30:00',10.75,3.30),
(8,3,'Large','Original','Completed','2023-03-03 21:30:00',10.75,3.30),
(9,3,'Large','Original','Completed','2023-03-03 21:30:00',10.75,3.30),
(10,4,'XLarge','Original','Completed','2023-03-05 19:11:00',14.50,5.59),
(11,4,'XLarge','Original','Completed','2023-03-05 19:11:00',17,5.59),
(12,4,'XLarge','Original','Completed','2023-03-05 19:11:00',14,5.68),
(13,5,'XLarge','Gluten-Free','Completed','2023-03-03 17:30:00',16.85,7.85),
(14,6,'Large','Thin','Completed','2023-03-02 18:17:00',13.25,3.20),
(15,7,'Large','Thin','Completed','2023-03-06 20:32:00',12,3.75),
(16,7,'Large','Thin','Completed','2023-03-06 20:32:00',12,2.55);



INSERT INTO discount(DiscountID,DiscountType,IsPercentOff,DiscountDollarOff)
VALUES
(1,'Employee',true,0.15),
(2,'Lunch Special Medium',false,1),
(3,'Lunch Special Large',false,2),
(4,'Speciality Pizza',false,1.5),
(5,'Gameday Special',true,0.20);



INSERT INTO pizzadiscount(PizzaOrderId,DiscountID) VALUES

(1,3),
(2,2),
(3,4),
(11,4),
(13,4);


INSERT INTO orderDiscount(OrderID,DiscountID) VALUES

(4,5),
(7,1);

INSERT INTO toppingsinfo (ToppingId,Name,Toppings_Price,Toppings_Cost,Inventory,SmallUnits,MediumUnits,LargeUnits,ExtraLangeUnits)

VALUES 




(1,'Pepperoni',1.25,0.2,100,2,2.75,3.5,4.5),
(2,'Sausage',1.25,0.15,100,2.5,3,3.5,4.25),
(3,'Ham',1.5,0.15,78,2,2.5,3.25,4),
(4,'Chicken',1.75,0.25,56,1.5,2,2.25,3),
(5,'Green Pepper',0.5,0.02,79,1,1.5,2,2.5),
(6,'Onion',0.5,0.02,85,1,1.5,2,2.75),
(7,'Roma Tomato',0.75,0.03,86,2,3,3.5,4.5),
(8,'Mushrooms',0.75,0.1,52,1.5,2,2.5,3),
(9,'Black Olives',0.6,0.1,39,0.75,1,1.5,2),
(10,'Pineapple',1,0.25,15,1,1.25,1.75,2),
(11,'Jalapenos',0.5,0.05,64,0.5,0.75,1.25,1.75),
(12,'Banana Peppers',0.5,0.05,36,0.6,1,1.3,1.75),
(13,'Regular Cheese',1.5,0.12,250,2,3.5,5,7),
(14,'Four Cheese Blend',2,0.15,150,2,3.5,5,7),
(15,'Feta Cheese',2,0.18,75,1.75,3,4,5.5),
(16,'Goat Cheese ',2,0.2,54,1.6,2.75,4,5.5),
(17,'Bacon',1.5,0.25,89,1,1.5,2,3);







INSERT INTO pizzatoppingsinfo(PizzaOrderId,ToppingID,ExtraTopping)
VALUES

(1,13, TRUE),
(1,1, FALSE),
(1,2, FALSE),
(2,15, FALSE),
(2,9, FALSE),
(2,7, FALSE),
(2,8, FALSE),
(2,12, FALSE),
(3,13, FALSE),
(3,4, FALSE),
(3,12, FALSE),
(4,1, FALSE),
(4,13, FALSE),
(5,1, FALSE),
(5,13, FALSE),
(6,1, FALSE),
(6,13, FALSE),
(7,1, FALSE),
(7,13, FALSE),
(8,1, FALSE),
(8,13, FALSE),
(9,1, FALSE),
(9,13, FALSE),
(10,1, FALSE),
(10,2, FALSE),
(10,14, FALSE),
(11,3, TRUE),
(11,10, TRUE),
(11,14, FALSE),
(12,11, FALSE),
(12,17, FALSE),
(12,14, FALSE),
(13,5, FALSE),
(13,6, FALSE),
(13,7, FALSE),
(13,8, FALSE),
(13,9, FALSE),
(13,16, FALSE),
(14,4, FALSE),
(14,5, FALSE),
(14,6, FALSE),
(14,8, FALSE),
(15,14, TRUE),
(16,13, FALSE),
(16,1, TRUE),
(16,14, TRUE);
