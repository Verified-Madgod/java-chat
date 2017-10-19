CREATE DATABASE chat;
USE chat; 

CREATE USER 'admin'@'localhost' IDENTIFIED BY 'password';
GRANT ALL ON chat.* TO 'admin'@'localhost';

CREATE TABLE logs (
	ID INT(11) NOT NULL AUTO_INCREMENT,
	timestamp datetime,
	username VARCHAR(20),
	message VARCHAR(255),
	primary key (ID)
);