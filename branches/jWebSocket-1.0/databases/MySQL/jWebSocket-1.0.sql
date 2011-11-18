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
 ('sq_pk_system_log',34);
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
  `app_version` varchar(20) DEFAULT NULL,
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
 (33,'2011-05-23 15:22:33','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Chrome','13.0.767.1',49,NULL,'ws://127.0.0.1:80/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.36 (KHTML, like Gecko) Chrome/13.0.767.1 Safari/534.36\"}','native',NULL,NULL,NULL);
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
