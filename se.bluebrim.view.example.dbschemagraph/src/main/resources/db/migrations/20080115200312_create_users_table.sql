# See http://hsqldb.org/doc/guide/ch02.html#N104B3

CREATE TABLE User (
  	Id INTEGER IDENTITY,
	UserName varchar(15) NULL,
	Password varchar(30) NOT NULL,
	Name varchar(40) NULL,
	Email varchar(50) NULL,
	Phone varchar(20) NULL,
	Mobile varchar(20) NULL,
	Role int DEFAULT 0 NOT NULL 
);
