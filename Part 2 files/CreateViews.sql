/*SQL Script written by Mohammed Abrar Faiz and Sai Vipraj Telukoti*/

USE Pizza_Project_G17;



CREATE VIEW profitbypizza AS
SELECT  PS.PizzASize AS 'PizzaSize', 
        PS.PizzaCrust AS 'PizzaCrust',
        sum(PZ.Pizzaprice-PZ.PizzaCost) AS Profit, 
        DATE_FORMAT(max(PZ.PizzaDateTime), '%M %e %Y') AS LastOrderDate FROM pizzastandards PS
LEFT JOIN pizza  PZ ON PS.PizzASize=PZ.PizzASize and PS.PizzaCrust=PZ.PizzaCrust 
GROUP BY PS.PizzASize,PS.PizzaCrust 
 ORDER BY profit desc;



CREATE VIEW popular_toppings AS
	SELECT TI.Name AS Topping,
       COUNT(*) + COALESCE(SUM(PT.ExtraTopping), 0) AS ToppingCount
FROM toppingsinfo TI
LEFT JOIN pizzatoppingsinfo PT ON TI.ToppingId = PT.ToppingId
GROUP BY TI.Name
ORDER BY Topping;
		


CREATE VIEW profitbyordertype AS
SELECT  OrderType AS CustomerType, 
        DATE_FORMAT(OrderDateTime,'%Y %M') AS OrderDate, 
        OrderPrice AS TotalOrderPrice ,
        OrderCost AS TotalOrderCost , 
	    (OrderPrice-OrderCost) AS Profit FROM ordertable 
UNION
SELECT  '', 
        'Grand Total', 
       SUM( OrderPrice )AS TotalOrderPrice ,
       SUM( OrderCost) AS TotalOrderCost , 
	   SUM( (OrderPrice-OrderCost) )AS Profit FROM ordertable;


SELECT * FROM popular_toppings;
SELECT * FROM profitbypizza;
SELECT * FROM profitbyordertype;


