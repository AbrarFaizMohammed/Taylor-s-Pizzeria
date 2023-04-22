/*SQL Script written by Mohammed Abrar Faiz and Sai Vipraj Telukoti*/

CREATE SCHEMA Pizza_Project_G17;

USE Pizza_Project_G17;

CREATE TABLE `customertable` (
  `CustomerId` int NOT NULL AUTO_INCREMENT,
  `First_Name` varchar(25) NOT NULL,
  `Last_Name` varchar(25) NOT NULL,
  `Phone_Number` varchar(20) NOT NULL,
  PRIMARY KEY (`CustomerId`),
  UNIQUE KEY `Customer_ID_UNIQUE` (`CustomerId`)
);


CREATE TABLE `ordertable` (
  `OrderId` int NOT NULL AUTO_INCREMENT,
  `CustomerId` int DEFAULT NULL,
  `OrderType` varchar(12) NOT NULL,
  `OrderDateTime` datetime NOT NULL,
  `OrderStatus` varchar(14) NOT NULL,
  `OrderCost` decimal(8,2) NOT NULL,
  `OrderPrice` decimal(8,2) NOT NULL,
  PRIMARY KEY (`OrderId`),
  KEY `CustomerId` (`CustomerId`),
  CONSTRAINT `ordertable_ibfk_1` FOREIGN KEY (`CustomerId`) REFERENCES `customertable` (`CustomerId`),
  CONSTRAINT `ordertable_chk_1` CHECK (((`ordertype` = _utf8mb4'dinein') or (`ordertype` = _utf8mb4'delivery') or (`ordertype` = _utf8mb4'pickup')))
);


CREATE TABLE `toppingsinfo` (
  `ToppingId` int NOT NULL AUTO_INCREMENT,
  `Name` varchar(50) NOT NULL,
  `Toppings_Price` decimal(8,2) NOT NULL,
  `Toppings_Cost` decimal(8,2) NOT NULL,
  `Inventory` int NOT NULL,
  `SmallUnits` decimal(8,2) NOT NULL,
  `MediumUnits` decimal(8,2) NOT NULL,
  `LargeUnits` decimal(8,2) NOT NULL,
  `ExtraLangeUnits` decimal(8,2) NOT NULL,
  PRIMARY KEY (`ToppingId`),
  UNIQUE KEY `ToppingsID_UNIQUE` (`ToppingId`)
);

CREATE TABLE `pizzastandards` (
  `PizzaSize` varchar(25) NOT NULL,
  `PizzaCrust` varchar(25) NOT NULL,
  `PizzaPrice` decimal(8,2) NOT NULL,
  `PizzaCost` decimal(8,2) NOT NULL,
  PRIMARY KEY (`PizzaSize`,`PizzaCrust`)
);



CREATE TABLE `pizza` (
  `PizzaOrderId` int NOT NULL AUTO_INCREMENT ,
  `OrderId` int NOT NULL,
  `PizzaSize` varchar(50) NOT NULL,
  `PizzaState` varchar(20) NOT NULL,
  `PizzaDateTime` datetime DEFAULT NULL,
  `PizzaCost` decimal(8,2) NOT NULL,
  `PizzaPrice` decimal(8,2) NOT NULL,
  `PizzaCrust` varchar(50) NOT NULL,
  PRIMARY KEY (`PizzaOrderId`),
   KEY `OrderId` (`OrderId`),
  KEY `PizzaSize` (`PizzaSize`,`PizzaCrust`),
  FOREIGN KEY (`PizzaSize`,PizzaCrust) REFERENCES `pizzastandards` (`PizzaSize`,PizzaCrust),
  CONSTRAINT `FK_OrderID_PI` FOREIGN KEY (`OrderId`) REFERENCES `ordertable` (`OrderId`)
) ;

CREATE TABLE `pizzatoppingsinfo` (
  `PizzaOrderId` int NOT NULL,
  `ToppingId` int NOT NULL,
  `ExtraTopping` int NOT NULL,
  PRIMARY KEY (`PizzaOrderId`,`ToppingId`),
  CONSTRAINT `FK_PizzaID_PT` FOREIGN KEY (`PizzaOrderId`) REFERENCES `pizza` (`PizzaOrderId`),
  CONSTRAINT `FK_ToppingID_PT` FOREIGN KEY (`ToppingId`) REFERENCES `toppingsinfo` (`ToppingId`)
) ;


CREATE TABLE `dinein` (
  `OrderId` int NOT NULL,
  `TableNum` int NOT NULL,
  PRIMARY KEY (`OrderId`),
  CONSTRAINT `FK_OrderID_dinein` FOREIGN KEY (`OrderId`) REFERENCES `ordertable` (`OrderId`)
);

CREATE TABLE `pickup` (
  `OrderId` int NOT NULL,
  PRIMARY KEY (`OrderId`),
  CONSTRAINT `FK_OrderID_pickup` FOREIGN KEY (`OrderId`) REFERENCES `ordertable` (`OrderId`)
  
);

CREATE TABLE `delivery` (
  `OrderId` int NOT NULL,
  `CustomerAddress` varchar(50) NOT NULL,
  PRIMARY KEY (`OrderId`),
  CONSTRAINT `FK_OrderID_Delivery` FOREIGN KEY (`OrderId`) REFERENCES `ordertable` (`OrderId`)
) ;


CREATE TABLE `discount` (
  `DiscountID` int NOT NULL AUTO_INCREMENT,
  `DiscountType` varchar(25) NOT NULL,
  `IsPercentOff` tinyint(1) DEFAULT NULL,
  `DiscountDollarOff` decimal(8,2) DEFAULT NULL,
  PRIMARY KEY (`DiscountID`)
) ;


CREATE TABLE `pizzadiscount` (
  `PizzaOrderId` int NOT NULL,
  `DiscountID` int NOT NULL,
  PRIMARY KEY (`PizzaOrderId`,`DiscountID`),
  KEY `DiscountID` (`DiscountID`),
  FOREIGN KEY (`PizzaOrderId`) REFERENCES `pizza` (`PizzaOrderId`),
  FOREIGN KEY (`DiscountID`) REFERENCES `discount` (`DiscountID`)
);

CREATE TABLE `orderDiscount` (
  `OrderId` int NOT NULL,
  `DiscountID` int NOT NULL,
  PRIMARY KEY (`OrderId`,`DiscountID`),
  CONSTRAINT `FK_OrderID_OD` FOREIGN KEY (`OrderId`) REFERENCES `ordertable` (`OrderId`),
  CONSTRAINT `FK_DiscountID_OD` FOREIGN KEY (`DiscountID`) REFERENCES `discount` (`DiscountID`)
) ;
