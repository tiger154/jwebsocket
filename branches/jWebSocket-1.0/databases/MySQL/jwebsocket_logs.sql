# SQL Manager 2005 for MySQL 3.7.0.1
# ---------------------------------------
# Host     : localhost
# Port     : 3306
# Database : jwebsocket_logs


SET FOREIGN_KEY_CHECKS=0;

CREATE DATABASE `jwebsocket_logs`
    CHARACTER SET 'latin1'
    COLLATE 'latin1_swedish_ci';

#
# Structure for the `logs_table` table:
#

CREATE TABLE IF NOT EXISTS `logs_table` (
	id int(20) unsigned NOT NULL auto_increment, 
	message varchar(10000),
	time_stamp DATETIME DEFAULT CURRENT_TIMESTAMP, 
	level varchar(20), 
	class_name varchar(255), 
	method_name varchar(255), 
	line_number int(20),
	filename varchar(300), 
	logger_name varchar(255), 
	thread_name varchar(255), 
	stack_trace varchar(1024) not null, 
	username varchar(50) not null, 
	ip_number varchar(40), 
	hostname varchar(255), 
	product varchar(255), 
	module varchar(255), 
	classification varchar(255), 
	version varchar(255), 
	environment varchar(255), 
	error_code varchar(255), 
	system varchar(255), 
	system_version varchar(255), 
	condition_value varchar(255), 
	source varchar(255), 
	target varchar(255),
	connector_id varchar(255), 
	primary key (id)) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=latin1 PACK_KEYS=0;