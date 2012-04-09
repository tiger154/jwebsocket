-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.1.45-community


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


--
-- Create schema jwebsocket
--

CREATE DATABASE IF NOT EXISTS jwebsocket;
USE jwebsocket;

--
-- Definition of table `demo_child`
--

DROP TABLE IF EXISTS `demo_child`;
CREATE TABLE `demo_child` (
  `child_id` int(10) unsigned NOT NULL DEFAULT '0',
  `master_id` int(10) unsigned NOT NULL,
  `child_string` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`child_id`),
  KEY `idx_demo_master_child` (`master_id`),
  CONSTRAINT `fk_demo_master_child` FOREIGN KEY (`master_id`) REFERENCES `demo_master` (`master_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `demo_child`
--

/*!40000 ALTER TABLE `demo_child` DISABLE KEYS */;
INSERT INTO `demo_child` (`child_id`,`master_id`,`child_string`) VALUES 
 (1,1,'Child #1 for Master #1'),
 (2,1,'Child #2 for Master #1'),
 (3,1,'Child #3 for Master #1'),
 (4,2,'Child #1 for Master #2'),
 (5,2,'Child #2 for Master #2'),
 (6,2,'Child #3 for Master #2');
/*!40000 ALTER TABLE `demo_child` ENABLE KEYS */;


--
-- Definition of trigger `tr_demo_child_bi`
--

DROP TRIGGER /*!50030 IF EXISTS */ `tr_demo_child_bi`;

DELIMITER $$

CREATE DEFINER = `root`@`localhost` TRIGGER `tr_demo_child_bi` BEFORE INSERT ON `demo_child` FOR EACH ROW BEGIN
  if( ( new.child_id = 0 ) || ( new.child_id is null ) ) then
    set new.child_id = getSequence( 'sq_pk_demo_child' );
  end if;
END $$

DELIMITER ;

--
-- Definition of table `demo_lookup`
--

DROP TABLE IF EXISTS `demo_lookup`;
CREATE TABLE `demo_lookup` (
  `lookup_id` int(10) unsigned NOT NULL DEFAULT '0',
  `lookup_string` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`lookup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `demo_lookup`
--

/*!40000 ALTER TABLE `demo_lookup` DISABLE KEYS */;
INSERT INTO `demo_lookup` (`lookup_id`,`lookup_string`) VALUES 
 (1,'Lookup Value #1'),
 (2,'Lookup Value #2');
/*!40000 ALTER TABLE `demo_lookup` ENABLE KEYS */;


--
-- Definition of trigger `tr_demo_lookup_bi`
--

DROP TRIGGER /*!50030 IF EXISTS */ `tr_demo_lookup_bi`;

DELIMITER $$

CREATE DEFINER = `root`@`localhost` TRIGGER `tr_demo_lookup_bi` BEFORE INSERT ON `demo_lookup` FOR EACH ROW BEGIN
  if( ( new.lookup_id = 0 ) || ( new.lookup_id is null ) ) then
    set new.lookup_id = getSequence( 'sq_pk_demo_lookup' );
  end if;
END $$

DELIMITER ;

--
-- Definition of table `demo_master`
--

DROP TABLE IF EXISTS `demo_master`;
CREATE TABLE `demo_master` (
  `master_id` int(10) unsigned NOT NULL DEFAULT '0',
  `lookup_id` int(10) unsigned DEFAULT NULL,
  `master_int` int(10) unsigned DEFAULT NULL,
  `master_float` float DEFAULT NULL,
  `master_string` varchar(80) DEFAULT NULL,
  `master_clob` text,
  PRIMARY KEY (`master_id`),
  KEY `idx_demo_master_lookup` (`lookup_id`),
  CONSTRAINT `fk_demo_master_lookup` FOREIGN KEY (`lookup_id`) REFERENCES `demo_lookup` (`lookup_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `demo_master`
--

/*!40000 ALTER TABLE `demo_master` DISABLE KEYS */;
INSERT INTO `demo_master` (`master_id`,`lookup_id`,`master_int`,`master_float`,`master_string`,`master_clob`) VALUES 
 (1,1,1,1,'Master Row #1','Arbitrary Text Row #1'),
 (2,2,2,2,'Master Row #2','Arbitrary Text Row #2'),
 (3,1,3,3,'Master Row #3','Arbitrary Text Row #3'),
 (4,2,4,4,'Master Row #4','Arbitrary Text Row #4'),
 (5,1,5,5,'Master Row #5','Arbitrary Text Row #5');
/*!40000 ALTER TABLE `demo_master` ENABLE KEYS */;


--
-- Definition of trigger `tr_demo_master_bi`
--

DROP TRIGGER /*!50030 IF EXISTS */ `tr_demo_master_bi`;

DELIMITER $$

CREATE DEFINER = `root`@`localhost` TRIGGER `tr_demo_master_bi` BEFORE INSERT ON `demo_master` FOR EACH ROW BEGIN
  if( ( new.master_id = 0 ) || ( new.master_id is null ) ) then
    set new.master_id = getSequence( 'sq_pk_demo_master' );
  end if;
END $$

DELIMITER ;

--
-- Definition of table `demo_rights`
--

DROP TABLE IF EXISTS `demo_rights`;
CREATE TABLE `demo_rights` (
  `right_id` int(10) unsigned NOT NULL DEFAULT '0',
  `name` varchar(80) NOT NULL,
  `description` text,
  PRIMARY KEY (`right_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `demo_rights`
--

/*!40000 ALTER TABLE `demo_rights` DISABLE KEYS */;
INSERT INTO `demo_rights` (`right_id`,`name`,`description`) VALUES 
 (1,'org.jwebsocket.plugins.system.broadcast',NULL),
 (2,'org.jwebsocket.plugins.system.send',NULL),
 (3,'org.jwebsocket.plugins.jdbc.select',NULL),
 (4,'org.jwebsocket.plugins.jdbc.update',NULL),
 (5,'org.jwebsocket.plugins.jdbc.insert',NULL),
 (6,'org.jwebsocket.plugins.jdbc.delete',NULL),
 (7,'org.jwebsocket.plugins.jdbc.querySQL',NULL),
 (8,'org.jwebsocket.plugins.jdbc.updateSQL',NULL),
 (9,'org.jwebsocket.plugins.jdbc.execSQL',NULL);
/*!40000 ALTER TABLE `demo_rights` ENABLE KEYS */;


--
-- Definition of trigger `tr_demo_right_bi`
--

DROP TRIGGER /*!50030 IF EXISTS */ `tr_demo_right_bi`;

DELIMITER $$

CREATE DEFINER = `root`@`localhost` TRIGGER `tr_demo_right_bi` BEFORE INSERT ON `demo_rights` FOR EACH ROW BEGIN
  if( ( new.right_id = 0 ) || ( new.right_id is null ) ) then
    set new.right_id = getSequence( 'sq_pk_demo_rights' );
  end if;
END $$

DELIMITER ;

--
-- Definition of table `demo_roles`
--

DROP TABLE IF EXISTS `demo_roles`;
CREATE TABLE `demo_roles` (
  `role_id` int(10) unsigned NOT NULL DEFAULT '0',
  `name` varchar(30) NOT NULL,
  `description` text,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `demo_roles`
--

/*!40000 ALTER TABLE `demo_roles` DISABLE KEYS */;
INSERT INTO `demo_roles` (`role_id`,`name`,`description`) VALUES 
 (1,'Guest','Guest Role'),
 (2,'User','User Role'),
 (3,'Admin','Administrator Role');
/*!40000 ALTER TABLE `demo_roles` ENABLE KEYS */;


--
-- Definition of trigger `tr_demo_role_bi`
--

DROP TRIGGER /*!50030 IF EXISTS */ `tr_demo_role_bi`;

DELIMITER $$

CREATE DEFINER = `root`@`localhost` TRIGGER `tr_demo_role_bi` BEFORE INSERT ON `demo_roles` FOR EACH ROW BEGIN
  if( ( new.role_id = 0 ) || ( new.role_id is null ) ) then
    set new.role_id = getSequence( 'sq_pk_demo_roles' );
  end if;
END $$

DELIMITER ;

--
-- Definition of table `demo_roles_rights`
--

DROP TABLE IF EXISTS `demo_roles_rights`;
CREATE TABLE `demo_roles_rights` (
  `role_id` int(10) unsigned NOT NULL,
  `right_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`role_id`,`right_id`),
  KEY `fk_demo_rhr_rights` (`right_id`),
  CONSTRAINT `fk_demo_rhr_rights` FOREIGN KEY (`right_id`) REFERENCES `demo_rights` (`right_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_demo_rhr_roles` FOREIGN KEY (`role_id`) REFERENCES `demo_roles` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `demo_roles_rights`
--

/*!40000 ALTER TABLE `demo_roles_rights` DISABLE KEYS */;
INSERT INTO `demo_roles_rights` (`role_id`,`right_id`) VALUES 
 (1,1),
 (2,1),
 (3,1),
 (1,2),
 (2,2),
 (3,2),
 (3,3),
 (3,4),
 (3,5),
 (3,6),
 (3,7),
 (3,8),
 (3,9);
/*!40000 ALTER TABLE `demo_roles_rights` ENABLE KEYS */;


--
-- Definition of table `demo_user_roles`
--

DROP TABLE IF EXISTS `demo_user_roles`;
CREATE TABLE `demo_user_roles` (
  `user_id` int(10) unsigned NOT NULL,
  `role_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `fk_demo_uhr_roles` (`role_id`),
  CONSTRAINT `fk_demo_uhr_roles` FOREIGN KEY (`role_id`) REFERENCES `demo_roles` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_demo_uhr_user` FOREIGN KEY (`user_id`) REFERENCES `demo_users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `demo_user_roles`
--

/*!40000 ALTER TABLE `demo_user_roles` DISABLE KEYS */;
INSERT INTO `demo_user_roles` (`user_id`,`role_id`) VALUES 
 (1,1),
 (2,1),
 (3,1),
 (4,1),
 (5,1),
 (1,2),
 (2,2),
 (3,2),
 (4,2),
 (5,2),
 (1,3),
 (3,3),
 (5,3);
/*!40000 ALTER TABLE `demo_user_roles` ENABLE KEYS */;


--
-- Definition of table `demo_users`
--

DROP TABLE IF EXISTS `demo_users`;
CREATE TABLE `demo_users` (
  `user_id` int(10) unsigned NOT NULL DEFAULT '0',
  `firstname` varchar(30) DEFAULT NULL,
  `lastname` varchar(30) DEFAULT NULL,
  `loginname` varchar(25) NOT NULL,
  `password` varchar(20) NOT NULL,
  `enabled` tinyint(3) unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `demo_users`
--

/*!40000 ALTER TABLE `demo_users` DISABLE KEYS */;
INSERT INTO `demo_users` (`user_id`,`firstname`,`lastname`,`loginname`,`password`,`enabled`) VALUES 
 (1,'Alexander','Schulze','aschulze','123',1),
 (2,'Rebecca','Schulze','rschulze','321',1),
 (3,'guest','guest','guest','guest',1),
 (4,'user','user','user','user',1),
 (5,'root','root','root','root',1);
/*!40000 ALTER TABLE `demo_users` ENABLE KEYS */;


--
-- Definition of trigger `tr_demo_user_bi`
--

DROP TRIGGER /*!50030 IF EXISTS */ `tr_demo_user_bi`;

DELIMITER $$

CREATE DEFINER = `root`@`localhost` TRIGGER `tr_demo_user_bi` BEFORE INSERT ON `demo_users` FOR EACH ROW BEGIN
  if( ( new.user_id = 0 ) || ( new.user_id is null ) ) then
    set new.user_id = getSequence( 'sq_pk_demo_users' );
  end if;
END $$

DELIMITER ;

--
-- Definition of table `sequences`
--

DROP TABLE IF EXISTS `sequences`;
CREATE TABLE `sequences` (
  `seq_id` varchar(40) NOT NULL,
  `next_val` int(10) unsigned DEFAULT '1',
  PRIMARY KEY (`seq_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `sequences`
--

/*!40000 ALTER TABLE `sequences` DISABLE KEYS */;
INSERT INTO `sequences` (`seq_id`,`next_val`) VALUES 
 ('sq_pk_demo_child',7),
 ('sq_pk_demo_lookup',3),
 ('sq_pk_demo_master',6),
 ('sq_pk_demo_rights',10),
 ('sq_pk_demo_roles',4),
 ('sq_pk_demo_users',6),
 ('sq_pk_system_log',562);
/*!40000 ALTER TABLE `sequences` ENABLE KEYS */;


--
-- Definition of table `system_log`
--

DROP TABLE IF EXISTS `system_log`;
CREATE TABLE `system_log` (
  `id` int(11) NOT NULL DEFAULT '0',
  `time_stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `event_type` varchar(20) DEFAULT NULL,
  `customer` varchar(25) DEFAULT NULL,
  `app_name` varchar(25) DEFAULT NULL,
  `app_version` varchar(30) DEFAULT NULL,
  `app_module` varchar(25) DEFAULT NULL,
  `app_dialog` varchar(25) DEFAULT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `user_name` varchar(40) DEFAULT NULL,
  `browser` varchar(20) DEFAULT NULL,
  `browser_version` varchar(15) DEFAULT NULL,
  `data_size` int(11) DEFAULT NULL,
  `process_time` int(11) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `message` varchar(132) DEFAULT NULL,
  `json` text,
  `ws_version` varchar(25) DEFAULT NULL,
  `session_id` varchar(32) DEFAULT NULL,
  `name_space` varchar(50) DEFAULT NULL,
  `token_type` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `system_log`
--

/*!40000 ALTER TABLE `system_log` DISABLE KEYS */;
INSERT INTO `system_log` (`id`,`time_stamp`,`event_type`,`customer`,`app_name`,`app_version`,`app_module`,`app_dialog`,`ip`,`user_name`,`browser`,`browser_version`,`data_size`,`process_time`,`url`,`message`,`json`,`ws_version`,`session_id`,`name_space`,`token_type`) VALUES 
 (5,'2011-05-21 13:37:28','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','n/a','n/a',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (6,'2011-05-21 14:04:49','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Chrome','11.0.696.68',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (7,'2011-05-21 14:12:33','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Chrome','11.0.696.68',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (8,'2011-05-21 14:13:32','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Opera','9.80',49,NULL,NULL,'This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (9,'2011-05-21 14:14:34','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Firefox','4.0.1',49,NULL,NULL,'This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (10,'2011-05-21 14:15:01','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Firefox','4.0.1',49,NULL,NULL,'This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (11,'2011-05-21 14:16:04','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Safari','5.0.5.533.21.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (12,'2011-05-21 14:16:20','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Safari','5.0.5.533.21.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (15,'2011-05-21 14:30:18','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Safari','5.0.5.533.21.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,'native',NULL,NULL,NULL),
 (17,'2011-05-21 14:34:46','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Safari','5.0.5.533.21.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,'native',NULL,NULL,NULL),
 (18,'2011-05-21 14:35:14','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Firefox','4.0.1',49,NULL,NULL,'This is an message from the automated test suite.',NULL,'flash 10.1.102',NULL,NULL,NULL),
 (19,'2011-05-21 14:41:06','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Chrome','11.0.696.68',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,'native',NULL,NULL,NULL),
 (20,'2011-05-21 14:41:33','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Firefox','4.0.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,'flash 10.1.102',NULL,NULL,NULL),
 (21,'2011-05-21 14:46:56','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Safari','5.0.5.533.21.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,'native',NULL,NULL,NULL),
 (22,'2011-05-21 14:47:19','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Opera','9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,'native',NULL,NULL,NULL),
 (23,'2011-05-21 14:49:51','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Safari','5.0.5.533.21.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; en-us) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1\"}','native',NULL,NULL,NULL),
 (24,'2011-05-21 14:50:25','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Opera','9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Macintosh; Intel Mac OS X 10.6.7; U; en) Presto/2.8.131 Version/11.10\"}','native',NULL,NULL,NULL),
 (25,'2011-05-21 14:58:35','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Opera','11.10/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Macintosh; Intel Mac OS X 10.6.7; U; en) Presto/2.8.131 Version/11.10\"}','native',NULL,NULL,NULL),
 (26,'2011-05-21 14:59:35','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Opera','11.10/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Macintosh; Intel Mac OS X 10.6.7; U; en) Presto/2.8.131 Version/11.10\"}','native',NULL,NULL,NULL),
 (27,'2011-05-21 15:01:05','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Opera','11.10/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Macintosh; Intel Mac OS X 10.6.7; U; en) Presto/2.8.131 Version/11.10\"}','native',NULL,NULL,NULL),
 (28,'2011-05-21 15:01:08','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Opera','11.10/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Macintosh; Intel Mac OS X 10.6.7; U; en) Presto/2.8.131 Version/11.10\"}','native',NULL,NULL,NULL),
 (29,'2011-05-23 15:10:39','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Chrome','12.0.742.60',49,NULL,'ws://127.0.0.1:80/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.60 Safari/534.30\"}','native',NULL,NULL,NULL),
 (30,'2011-05-23 15:10:40','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Chrome','12.0.742.60',49,NULL,'ws://127.0.0.1:80/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.60 Safari/534.30\"}','native',NULL,NULL,NULL),
 (31,'2011-05-23 15:16:05','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Firefox','4.0.1',49,NULL,'ws://127.0.0.1:80/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1\"}','flash 10.2.159',NULL,NULL,NULL),
 (32,'2011-05-23 15:18:17','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Safari','5.0.533.16',49,NULL,'ws://127.0.0.1:80/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16\"}','native',NULL,NULL,NULL),
 (33,'2011-05-23 15:22:33','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Chrome','13.0.767.1',49,NULL,'ws://127.0.0.1:80/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.36 (KHTML, like Gecko) Chrome/13.0.767.1 Safari/534.36\"}','native',NULL,NULL,NULL),
 (34,'2011-05-23 17:15:00','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','12.0.742.60',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.60 Safari/534.30\"}','native',NULL,NULL,NULL),
 (35,'2011-05-23 17:15:40','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','12.0.742.60',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.60 Safari/534.30\"}','native',NULL,NULL,NULL),
 (36,'2011-05-23 17:18:51','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','12.0.742.60',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.60 Safari/534.30\"}','native',NULL,NULL,NULL),
 (37,'2011-05-23 17:19:18','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','4.0.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1\"}','flash 10.2.159',NULL,NULL,NULL),
 (38,'2011-05-23 17:19:31','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','4.0.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1\"}','flash 10.2.159',NULL,NULL,NULL),
 (39,'2011-05-23 17:24:51','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','4.0.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1\"}','flash 10.2.159',NULL,NULL,NULL),
 (40,'2011-05-23 17:25:43','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Safari','5.0.533.16',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16\"}','native',NULL,NULL,NULL),
 (41,'2011-05-23 17:26:03','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.11/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11\"}','flash 10.2.159',NULL,NULL,NULL),
 (42,'2011-05-23 17:27:36','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','12.0.742.60',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.60 Safari/534.30\"}','native',NULL,NULL,NULL),
 (43,'2011-05-23 17:28:05','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','4.0.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1\"}','flash 10.2.159',NULL,NULL,NULL),
 (44,'2011-05-23 17:28:31','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Safari','5.0.533.16',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16\"}','native',NULL,NULL,NULL),
 (45,'2011-05-23 17:28:45','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.11/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11\"}','flash 10.2.159',NULL,NULL,NULL),
 (46,'2011-05-23 17:28:53','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.11/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11\"}','flash 10.2.159',NULL,NULL,NULL),
 (47,'2011-05-23 17:36:28','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','12.0.742.60',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.60 Safari/534.30\"}','native',NULL,NULL,NULL),
 (48,'2011-05-23 17:36:41','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','4.0.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1\"}','flash 10.2.159',NULL,NULL,NULL),
 (49,'2011-05-23 17:37:06','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Safari','5.0.533.16',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16\"}','native',NULL,NULL,NULL),
 (50,'2011-05-23 17:37:17','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.11/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11\"}','flash 10.2.159',NULL,NULL,NULL),
 (51,'2011-05-23 17:37:22','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.11/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11\"}','flash 10.2.159',NULL,NULL,NULL),
 (52,'2011-05-23 17:38:57','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','8.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.767.1; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)\"}','flash 10.3.181',NULL,NULL,NULL),
 (53,'2011-05-23 17:39:12','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','8.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.767.1; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)\"}','flash 10.3.181',NULL,NULL,NULL),
 (54,'2011-05-23 17:39:31','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','8.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.767.1; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)\"}','flash 10.3.181',NULL,NULL,NULL),
 (55,'2011-05-23 17:41:36','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','8.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.767.1; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)\"}','flash 10.3.181',NULL,NULL,NULL),
 (56,'2011-05-23 17:41:52','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','8.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.767.1; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)\"}','flash 10.3.181',NULL,NULL,NULL),
 (57,'2011-05-27 15:40:16','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','12.0.742.68',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.68 Safari/534.30\"}','native',NULL,NULL,NULL),
 (58,'2011-05-27 15:47:10','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','12.0.742.68',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.68 Safari/534.30\"}','native',NULL,NULL,NULL),
 (59,'2011-05-27 15:47:25','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','12.0.742.68',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.68 Safari/534.30\"}','native',NULL,NULL,NULL),
 (60,'2011-05-27 15:57:15','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','12.0.742.68',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.68 Safari/534.30\"}','native',NULL,NULL,NULL),
 (61,'2011-05-27 16:03:30','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','12.0.742.68',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.68 Safari/534.30\"}','native',NULL,NULL,NULL),
 (62,'2011-05-27 16:03:39','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','12.0.742.68',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.68 Safari/534.30\"}','native',NULL,NULL,NULL),
 (63,'2011-05-27 16:04:20','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','4.0.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1\"}','flash 10.2.159',NULL,NULL,NULL),
 (64,'2011-05-27 16:04:59','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Safari','5.0.533.16',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16\"}','native',NULL,NULL,NULL),
 (65,'2011-05-27 16:05:27','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.11/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11\"}','flash 10.2.159',NULL,NULL,NULL),
 (66,'2011-05-27 16:09:05','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','8.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.772.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)\"}','flash 10.3.181',NULL,NULL,NULL),
 (67,'2011-05-27 16:09:26','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','8.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.772.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)\"}','flash 10.3.181',NULL,NULL,NULL),
 (68,'2011-05-27 16:10:11','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','8.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.772.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)\"}','flash 10.3.181',NULL,NULL,NULL),
 (69,'2011-05-27 16:10:58','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','8.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.772.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)\"}','flash 10.3.181',NULL,NULL,NULL),
 (70,'2011-07-28 11:30:00','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','13.0.782.107',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.107 Safari/535.1\"}','native',NULL,NULL,NULL),
 (71,'2011-07-28 11:30:26','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','5.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0\"}','flash 10.3.181',NULL,NULL,NULL),
 (72,'2011-07-28 11:31:07','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Safari','5.0.533.16',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16\"}','native',NULL,NULL,NULL),
 (73,'2011-07-28 11:31:25','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.11/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11\"}','flash 10.3.181',NULL,NULL,NULL),
 (74,'2011-07-28 11:34:21','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (75,'2011-07-28 11:34:38','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (76,'2011-07-28 11:35:03','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (77,'2011-07-28 11:35:26','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (78,'2011-07-28 14:32:30','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','13.0.782.107',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.107 Safari/535.1\"}','native',NULL,NULL,NULL),
 (79,'2011-08-15 14:01:28','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (80,'2011-08-15 14:05:08','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (81,'2011-08-15 14:05:25','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (82,'2011-08-15 14:05:49','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (83,'2011-08-15 14:05:54','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (84,'2011-08-15 14:06:13','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (85,'2011-08-15 15:04:03','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (86,'2011-08-15 16:50:11','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (87,'2011-08-15 16:51:43','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (88,'2011-08-15 16:51:49','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (89,'2011-08-15 17:47:11','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (90,'2011-08-16 15:23:14','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (92,'2011-08-16 15:25:38','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (93,'2011-08-16 15:25:44','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (94,'2011-08-16 15:27:02','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (95,'2011-08-16 16:16:20','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','6.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0\"}','native',NULL,NULL,NULL),
 (96,'2011-08-16 16:16:56','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (97,'2011-08-16 16:21:37','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','6.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0\"}','native',NULL,NULL,NULL),
 (98,'2011-08-16 16:22:34','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','6.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0\"}','native',NULL,NULL,NULL),
 (99,'2011-08-16 16:25:16','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','6.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0\"}','native',NULL,NULL,NULL),
 (100,'2011-08-16 16:26:30','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','6.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0\"}','native',NULL,NULL,NULL),
 (101,'2011-08-16 16:27:46','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','6.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0\"}','native',NULL,NULL,NULL),
 (102,'2011-08-16 16:33:50','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','6.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0\"}','native',NULL,NULL,NULL),
 (103,'2011-08-16 16:34:00','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','6.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0\"}','native',NULL,NULL,NULL),
 (104,'2011-08-17 09:12:40','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (105,'2011-08-17 09:13:36','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (106,'2011-08-17 09:14:57','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (107,'2011-08-17 09:15:39','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (108,'2011-08-17 09:16:22','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (109,'2011-08-17 09:21:42','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (110,'2011-08-17 09:23:20','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (111,'2011-08-17 09:24:58','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (112,'2011-08-17 09:25:37','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (113,'2011-08-17 09:26:38','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (114,'2011-08-17 09:27:08','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (115,'2011-08-17 10:59:43','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (116,'2011-08-17 11:00:15','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','5.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0\"}','flash 10.3.183',NULL,NULL,NULL),
 (117,'2011-08-17 11:00:33','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','5.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0\"}','flash 10.3.183',NULL,NULL,NULL),
 (118,'2011-08-17 11:03:20','loggingTest','jWebSocket.org','jWebSocket','1.0a11 (10530)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.11/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11\"}','flash 10.3.183',NULL,NULL,NULL),
 (119,'2011-08-17 11:03:38','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.11/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11\"}','flash 10.3.183',NULL,NULL,NULL),
 (120,'2011-08-17 11:04:53','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (121,'2011-08-17 11:52:39','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10817)','test automation','full tests','0:0:0:0:0:0:0:1','root','Safari','5.0.533.16',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16\"}','native',NULL,NULL,NULL),
 (122,'2011-08-17 11:53:21','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10817)','test automation','full tests','0:0:0:0:0:0:0:1','root','Safari','5.0.533.16',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16\"}','native',NULL,NULL,NULL),
 (123,'2011-08-17 11:56:39','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (124,'2011-08-17 11:56:58','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10817)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (125,'2011-08-17 11:57:29','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10817)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (126,'2011-08-17 11:58:42','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10817)','test automation','full tests','127.0.0.1','root','Opera','11.11/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11\"}','native',NULL,NULL,NULL),
 (127,'2011-08-17 11:59:18','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (128,'2011-08-17 15:13:07','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10817)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (129,'2011-08-17 15:16:11','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10817)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (130,'2011-08-17 15:24:24','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10817)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (131,'2011-08-17 15:27:02','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','5.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0\"}','flash 10.3.183',NULL,NULL,NULL),
 (132,'2011-08-17 15:35:48','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10817)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.849.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1\"}','native',NULL,NULL,NULL),
 (133,'2011-08-17 15:36:13','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','5.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0\"}','flash 10.3.183',NULL,NULL,NULL),
 (134,'2011-08-17 15:36:40','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10817)','test automation','full tests','0:0:0:0:0:0:0:1','root','Safari','5.0.533.16',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16\"}','native',NULL,NULL,NULL),
 (135,'2011-08-17 15:37:07','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.11/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11\"}','flash 10.3.183',NULL,NULL,NULL),
 (136,'2011-08-17 15:37:33','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10817)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (137,'2011-08-20 13:07:40','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10820)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.854.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.854.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (138,'2011-08-20 13:07:53','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10820)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.854.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.854.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (139,'2011-08-20 13:08:35','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10820)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','5.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0\"}','flash 10.3.183',NULL,NULL,NULL),
 (140,'2011-08-20 13:10:16','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10812)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 10.3.183',NULL,NULL,NULL),
 (141,'2011-08-20 13:10:27','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10820)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 10.3.183',NULL,NULL,NULL),
 (142,'2011-08-20 13:14:06','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10820)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (143,'2011-08-26 10:45:59','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10826)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (144,'2011-08-26 10:46:50','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10826)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','5.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0\"}','flash 10.3.183',NULL,NULL,NULL),
 (145,'2011-08-26 14:11:43','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10826)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (146,'2011-08-26 14:12:45','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10826)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (147,'2011-08-26 14:14:16','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10826)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (148,'2011-08-26 14:16:51','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10826)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (149,'2011-08-26 14:17:28','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10826)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (150,'2011-08-26 14:29:41','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10826)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (151,'2011-08-26 14:30:05','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10826)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','5.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0\"}','flash 10.3.183',NULL,NULL,NULL),
 (152,'2011-08-26 14:30:28','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10826)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','5.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0\"}','flash 10.3.183',NULL,NULL,NULL),
 (153,'2011-08-26 14:30:40','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10826)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','5.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0\"}','flash 10.3.183',NULL,NULL,NULL),
 (154,'2011-08-26 14:31:48','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10826)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 10.3.183',NULL,NULL,NULL),
 (155,'2011-08-26 14:32:17','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10826)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (156,'2011-08-31 09:42:34','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (157,'2011-08-31 09:42:50','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (158,'2011-08-31 10:22:05','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (159,'2011-08-31 10:28:30','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (160,'2011-08-31 10:33:11','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (161,'2011-08-31 10:34:50','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (162,'2011-08-31 10:36:57','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (163,'2011-08-31 10:37:20','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL);
INSERT INTO `system_log` (`id`,`time_stamp`,`event_type`,`customer`,`app_name`,`app_version`,`app_module`,`app_dialog`,`ip`,`user_name`,`browser`,`browser_version`,`data_size`,`process_time`,`url`,`message`,`json`,`ws_version`,`session_id`,`name_space`,`token_type`) VALUES 
 (164,'2011-08-31 10:37:56','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (165,'2011-08-31 10:38:56','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (166,'2011-08-31 10:39:27','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (167,'2011-08-31 10:42:56','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (168,'2011-08-31 10:45:28','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (169,'2011-08-31 10:45:57','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (170,'2011-08-31 10:47:30','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (171,'2011-08-31 10:47:55','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (172,'2011-08-31 10:50:52','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (173,'2011-08-31 10:51:30','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (174,'2011-08-31 11:07:14','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (175,'2011-08-31 11:07:19','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (176,'2011-08-31 11:08:19','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (177,'2011-08-31 11:08:24','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (178,'2011-08-31 11:09:34','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (179,'2011-08-31 11:09:41','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (180,'2011-08-31 11:09:46','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (181,'2011-08-31 11:10:37','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (182,'2011-08-31 11:10:46','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (183,'2011-08-31 11:12:16','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (184,'2011-08-31 11:12:25','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (185,'2011-08-31 11:13:33','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (186,'2011-08-31 11:13:39','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (187,'2011-08-31 11:17:36','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (188,'2011-08-31 11:36:52','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.861.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (189,'2011-08-31 16:30:36','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (190,'2011-09-01 14:28:51','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (191,'2011-09-01 14:29:20','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (192,'2011-09-01 14:29:47','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (193,'2011-09-01 15:14:20','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (194,'2011-09-01 15:14:55','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (195,'2011-09-02 10:09:44','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10831)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (196,'2011-09-02 10:10:06','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10902)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (197,'2011-09-02 10:10:17','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10902)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (198,'2011-09-02 10:20:59','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10902)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (199,'2011-09-02 10:21:26','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10902)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','5.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0\"}','flash 10.3.183',NULL,NULL,NULL),
 (200,'2011-09-02 10:23:02','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10902)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 10.3.183',NULL,NULL,NULL),
 (201,'2011-09-02 10:23:08','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10902)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 10.3.183',NULL,NULL,NULL),
 (202,'2011-09-02 10:24:20','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10902)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (203,'2011-09-02 11:04:09','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10902)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (204,'2011-09-02 11:30:15','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10902)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (223,'2011-09-06 11:09:03','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (227,'2011-09-06 11:09:26','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (231,'2011-09-06 11:22:40','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (235,'2011-09-06 11:23:28','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (239,'2011-09-06 11:23:54','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (243,'2011-09-06 11:25:15','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (247,'2011-09-06 13:26:34','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (251,'2011-09-06 13:27:39','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (255,'2011-09-06 13:28:20','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','5.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0\"}','flash 10.3.183',NULL,NULL,NULL),
 (259,'2011-09-06 13:29:16','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 10.3.183',NULL,NULL,NULL),
 (263,'2011-09-06 13:29:58','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (275,'2011-09-06 14:19:39','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (nightly build 10906)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.865.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (279,'2011-09-06 14:20:02','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','5.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0\"}','flash 10.3.183',NULL,NULL,NULL),
 (283,'2011-09-06 14:20:47','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (nightly build 10906)','test automation','full tests','0:0:0:0:0:0:0:1','root','Safari','5.0.533.16',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16\"}','native',NULL,NULL,NULL),
 (287,'2011-09-06 14:21:26','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 10.3.183',NULL,NULL,NULL),
 (288,'2011-09-06 14:21:29','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 10.3.183',NULL,NULL,NULL),
 (295,'2011-09-06 14:21:47','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (299,'2011-09-08 12:54:32','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (nightly build 10908)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.874.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.0 Safari/535.2\"}','native',NULL,NULL,NULL),
 (303,'2011-09-08 12:55:06','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (nightly build 10908)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','6.0.2',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0.2) Gecko/20100101 Firefox/6.0.2\"}','native',NULL,NULL,NULL),
 (307,'2011-09-08 12:56:41','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (nightly build 10908)','test automation','full tests','0:0:0:0:0:0:0:1','root','Safari','5.0.533.16',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16\"}','native',NULL,NULL,NULL),
 (311,'2011-09-08 12:57:05','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 10.3.183',NULL,NULL,NULL),
 (312,'2011-09-08 12:57:05','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (10905)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 10.3.183',NULL,NULL,NULL),
 (319,'2011-09-08 12:57:24','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (nightly build 10908)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 10.3.183',NULL,NULL,NULL),
 (323,'2011-09-08 12:57:55','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (nightly build 10908)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (327,'2011-09-14 15:51:25','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (nightly build 10914)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.874.12',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.12 Safari/535.2\"}','native',NULL,NULL,NULL),
 (331,'2011-09-14 15:51:39','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (nightly build 10914)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.874.12',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.12 Safari/535.2\"}','native',NULL,NULL,NULL),
 (335,'2011-09-14 15:52:38','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (nightly build 10914)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','5.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0\"}','flash 10.3.183',NULL,NULL,NULL),
 (339,'2011-09-14 15:54:51','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (nightly build 10914)','test automation','full tests','0:0:0:0:0:0:0:1','root','Safari','5.0.533.16',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16\"}','native',NULL,NULL,NULL),
 (343,'2011-09-14 15:56:34','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (nightly build 10914)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 10.3.183',NULL,NULL,NULL),
 (347,'2011-09-14 15:57:17','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (nightly build 10914)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (351,'2011-09-20 12:08:38','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (build 10919)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.874.15',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.15 Safari/535.2\"}','native',NULL,NULL,NULL),
 (355,'2011-09-20 12:09:15','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (build 10919)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.874.15',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.15 Safari/535.2\"}','native',NULL,NULL,NULL),
 (359,'2011-09-20 12:09:24','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (build 10919)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.874.15',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.15 Safari/535.2\"}','native',NULL,NULL,NULL),
 (363,'2011-09-20 12:10:50','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (build 10919)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.874.15',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.15 Safari/535.2\"}','native',NULL,NULL,NULL),
 (367,'2011-09-20 12:10:57','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (build 10919)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.874.15',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.15 Safari/535.2\"}','native',NULL,NULL,NULL),
 (371,'2011-09-22 09:23:21','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (build 10919)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.874.21',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.21 Safari/535.2\"}','native',NULL,NULL,NULL),
 (375,'2011-09-22 09:23:34','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (build 10919)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','15.0.874.21',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.21 Safari/535.2\"}','native',NULL,NULL,NULL),
 (376,'2011-09-29 18:31:20','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10929)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.891.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.5 (KHTML, like Gecko) Chrome/16.0.891.0 Safari/535.5\"}','native',NULL,NULL,NULL),
 (380,'2011-09-29 18:31:38','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10929)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.891.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.5 (KHTML, like Gecko) Chrome/16.0.891.0 Safari/535.5\"}','native',NULL,NULL,NULL),
 (384,'2011-09-29 18:31:52','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10929)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.891.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.5 (KHTML, like Gecko) Chrome/16.0.891.0 Safari/535.5\"}','native',NULL,NULL,NULL),
 (388,'2011-09-30 15:39:09','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.891.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.5 (KHTML, like Gecko) Chrome/16.0.891.0 Safari/535.5\"}','native',NULL,NULL,NULL),
 (392,'2011-09-30 15:41:01','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','6.0.2',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0.2) Gecko/20100101 Firefox/6.0.2\"}','native',NULL,NULL,NULL),
 (396,'2011-09-30 15:44:30','loggingTest','jWebSocket.org','jWebSocket','1.0b1 (nightly build 10914)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 10.3.183',NULL,NULL,NULL),
 (397,'2011-09-30 15:45:01','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 10.3.183',NULL,NULL,NULL),
 (401,'2011-09-30 15:45:37','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 10.3.183',NULL,NULL,NULL),
 (405,'2011-09-30 15:48:36','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','9.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (409,'2011-10-05 16:37:26','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.899.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.6 (KHTML, like Gecko) Chrome/16.0.899.0 Safari/535.6\"}','native',NULL,NULL,NULL),
 (413,'2011-10-05 16:38:24','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.899.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.6 (KHTML, like Gecko) Chrome/16.0.899.0 Safari/535.6\"}','native',NULL,NULL,NULL),
 (417,'2011-10-24 14:11:48','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.912.4',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7\"}','native',NULL,NULL,NULL),
 (421,'2011-10-24 16:24:18','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.912.4',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7\"}','native',NULL,NULL,NULL),
 (425,'2011-10-24 16:24:35','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.912.4',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7\"}','native',NULL,NULL,NULL),
 (429,'2011-10-24 16:39:37','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.912.4',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7\"}','native',NULL,NULL,NULL),
 (433,'2011-10-24 16:40:40','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.912.4',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7\"}','native',NULL,NULL,NULL),
 (437,'2011-10-24 16:52:47','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.912.4',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7\"}','native',NULL,NULL,NULL),
 (441,'2011-10-24 17:35:53','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.912.4',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7\"}','native',NULL,NULL,NULL),
 (445,'2011-10-24 17:36:25','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.912.4',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7\"}','native',NULL,NULL,NULL),
 (449,'2011-10-24 17:38:13','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.912.4',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7\"}','native',NULL,NULL,NULL),
 (453,'2011-10-24 17:42:50','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Firefox','5.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0\"}','flash 10.3.183',NULL,NULL,NULL),
 (457,'2011-10-24 17:46:29','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 10.3.183',NULL,NULL,NULL),
 (461,'2011-10-24 17:47:30','loggingTest','jWebSocket.org','jWebSocket','1.0b3 (nightly build 11024)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','7.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (465,'2011-10-31 14:38:26','loggingTest','jWebSocket.org','jWebSocket','1.0b3 (nightly build 11024)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.912.15',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.15 Safari/535.7\"}','native',NULL,NULL,NULL),
 (469,'2011-10-31 14:49:50','loggingTest','jWebSocket.org','jWebSocket','1.0b3 (nightly build 11024)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','16.0.912.15',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.15 Safari/535.7\"}','native',NULL,NULL,NULL),
 (473,'2011-11-28 13:30:39','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11125)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','17.0.942.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8\"}','native',NULL,NULL,NULL),
 (477,'2011-11-28 14:35:08','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11125)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','17.0.942.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8\"}','native',NULL,NULL,NULL),
 (481,'2011-11-28 14:40:25','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11125)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','17.0.942.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8\"}','native',NULL,NULL,NULL),
 (485,'2011-11-28 14:41:30','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11125)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','17.0.942.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8\"}','native',NULL,NULL,NULL),
 (489,'2011-11-28 14:41:48','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11125)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','17.0.942.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8\"}','native',NULL,NULL,NULL),
 (493,'2011-11-28 14:41:59','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11125)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','17.0.942.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8\"}','native',NULL,NULL,NULL),
 (497,'2011-11-28 14:42:25','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11125)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','17.0.942.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8\"}','native',NULL,NULL,NULL),
 (501,'2011-12-01 13:20:08','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11125)','test automation','full tests','0:0:0:0:0:0:0:1',NULL,'Chrome','17.0.942.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8\"}','native',NULL,NULL,NULL),
 (505,'2011-12-01 13:22:51','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11125)','test automation','full tests','127.0.0.1',NULL,'Firefox','8.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:8.0) Gecko/20100101 Firefox/8.0\"}','native',NULL,NULL,NULL),
 (509,'2011-12-01 13:24:58','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11125)','test automation','full tests','127.0.0.1',NULL,'Firefox','8.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:8.0) Gecko/20100101 Firefox/8.0\"}','native',NULL,NULL,NULL),
 (513,'2011-12-01 13:28:02','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11125)','test automation','full tests','127.0.0.1',NULL,'Firefox','8.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:8.0) Gecko/20100101 Firefox/8.0\"}','native',NULL,NULL,NULL),
 (517,'2011-12-01 13:29:13','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11125)','test automation','full tests','0:0:0:0:0:0:0:1',NULL,'Safari','5.0.533.16',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16\"}','native',NULL,NULL,NULL),
 (521,'2011-12-01 13:35:02','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11125)','test automation','full tests','0:0:0:0:0:0:0:1',NULL,'Chrome','17.0.942.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8\"}','native',NULL,NULL,NULL),
 (525,'2011-12-01 14:13:53','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11201)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','17.0.942.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8\"}','native',NULL,NULL,NULL),
 (529,'2011-12-01 14:32:40','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11201)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','17.0.942.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8\"}','native',NULL,NULL,NULL),
 (533,'2011-12-01 14:57:43','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11201)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','17.0.942.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8\"}','native',NULL,NULL,NULL),
 (537,'2011-12-01 14:58:54','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11201)','test automation','full tests','127.0.0.1','root','Firefox','8.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:8.0) Gecko/20100101 Firefox/8.0\"}','native',NULL,NULL,NULL),
 (541,'2011-12-01 15:00:04','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11201)','test automation','full tests','0:0:0:0:0:0:0:1','root','Safari','5.0.533.16',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16\"}','native',NULL,NULL,NULL),
 (545,'2011-12-01 15:01:11','loggingTest','jWebSocket.org','jWebSocket','1.0b2 (nightly build 10930)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 11.0.1',NULL,NULL,NULL),
 (549,'2011-12-01 15:01:46','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11201)','test automation','full tests','0:0:0:0:0:0:0:1','root','Opera','11.50/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50\"}','flash 11.0.1',NULL,NULL,NULL),
 (553,'2011-12-01 15:02:48','loggingTest','jWebSocket.org','jWebSocket','1.0b4 (nightly build 11201)','test automation','full tests','0:0:0:0:0:0:0:1','root','Internet Explorer','7.0',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)\"}','flash 11.0.1',NULL,NULL,NULL),
 (557,'2011-12-29 14:51:18','loggingTest','jWebSocket.org','jWebSocket','1.0b5 (nightly build 11222)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','17.0.963.12',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.12 Safari/535.11\"}','native',NULL,NULL,NULL),
 (561,'2011-12-30 15:27:01','loggingTest','jWebSocket.org','jWebSocket','1.0b5 (nightly build 11222)','test automation','full tests','0:0:0:0:0:0:0:1','root','Chrome','17.0.963.12',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.12 Safari/535.11\"}','native',NULL,NULL,NULL);
/*!40000 ALTER TABLE `system_log` ENABLE KEYS */;


--
-- Definition of trigger `tr_system_log_bi`
--

DROP TRIGGER /*!50030 IF EXISTS */ `tr_system_log_bi`;

DELIMITER $$

CREATE DEFINER = `root`@`localhost` TRIGGER `tr_system_log_bi` BEFORE INSERT ON `system_log` FOR EACH ROW BEGIN
  if( ( new.id = 0 ) || ( new.id is null ) ) then
    set new.id = getSequence( 'sq_pk_system_log' );
  end if;
END $$

DELIMITER ;

--
-- Definition of function `getSequence`
--

DROP FUNCTION IF EXISTS `getSequence`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */ $$
CREATE DEFINER=`root`@`localhost` FUNCTION `getSequence`( aId VARCHAR(40) ) RETURNS int(11)
BEGIN
  DECLARE lNextVal INT;
  select next_val into lNextVal from sequences where seq_id = aId;
  update sequences set next_val = next_val + 1 where seq_id = aId;
  return lNextVal;
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

--
-- Definition of procedure `initSequences`
--

DROP PROCEDURE IF EXISTS `initSequences`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `initSequences`()
BEGIN
  DECLARE lMaxId INT;

  select max( master_id ) into lMaxId from demo_master;
  call setSequence( 'sq_pk_demo_master', lMaxId + 1 );

  select max( child_id ) into lMaxId from demo_child;
  call setSequence( 'sq_pk_demo_child', lMaxId + 1 );

  select max( lookup_id ) into lMaxId from demo_lookup;
  call setSequence( 'sq_pk_demo_lookup', lMaxId + 1 );

  select max( user_id ) into lMaxId from demo_users;
  call setSequence( 'sq_pk_demo_users', lMaxId + 1 );

  select max( role_id ) into lMaxId from demo_roles;
  call setSequence( 'sq_pk_demo_roles', lMaxId + 1 );

  select max( right_id ) into lMaxId from demo_rights;
  call setSequence( 'sq_pk_demo_rights', lMaxId + 1 );

  select max( id ) into lMaxId from system_log;
  call setSequence( 'sq_pk_system_log', lMaxId + 1 );

END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

--
-- Definition of procedure `setSequence`
--

DROP PROCEDURE IF EXISTS `setSequence`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `setSequence`( aId VARCHAR(40), aNextVal INT )
BEGIN

  DECLARE lCnt INT;
  select count( * ) into lCnt from sequences where seq_id = aId;

  if( aNextVaL is null ) then
    set aNextVal = 1;
  end if;

  if( lCnt = 0 ) then
    insert into sequences ( seq_id, next_val ) values ( aId, aNextVal );
  else
    update sequences set next_val = aNextVal where seq_id = aId;
  end if;
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;



/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
