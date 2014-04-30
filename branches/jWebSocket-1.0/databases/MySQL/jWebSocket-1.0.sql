-- phpMyAdmin SQL Dump
-- version 3.4.10.1
-- http://www.phpmyadmin.net
--
-- Servidor: localhost
-- Tiempo de generación: 30-04-2014 a las 04:44:32
-- Versión del servidor: 5.5.20
-- Versión de PHP: 5.3.10

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Create schema jwebsocket
--

CREATE DATABASE IF NOT EXISTS jwebsocket;
USE jwebsocket;

--
-- Estructura de tabla para la tabla `child`
--

DROP TABLE IF EXISTS `child`;
CREATE TABLE `child` (
  `child_id` int(10) unsigned NOT NULL DEFAULT '0',
  `master_id` int(10) unsigned NOT NULL,
  `child_string` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`child_id`),
  KEY `idx_master_child` (`master_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `child`
--

INSERT INTO `child` (`child_id`, `master_id`, `child_string`) VALUES
(1, 1, 'Child #1 for Master #1'),
(2, 1, 'Child #2 for Master #1'),
(3, 1, 'Child #3 for Master #1'),
(4, 2, 'Child #1 for Master #2'),
(5, 2, 'Child #2 for Master #2'),
(6, 2, 'Child #3 for Master #2');

--
-- Disparadores `child`
--
DROP TRIGGER IF EXISTS `tr_child_bi`;
DELIMITER //
CREATE TRIGGER `tr_child_bi` BEFORE INSERT ON `child`
 FOR EACH ROW BEGIN
  if( ( new.child_id = 0 ) || ( new.child_id is null ) ) then
    set new.child_id = getSequence( 'sq_pk_child' );
  end if;
END
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `lookup`
--

CREATE TABLE IF NOT EXISTS `lookup` (
  `lookup_id` int(10) unsigned NOT NULL DEFAULT '0',
  `lookup_string` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`lookup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `lookup`
--

INSERT INTO `lookup` (`lookup_id`, `lookup_string`) VALUES
(1, 'Lookup Value #1'),
(2, 'Lookup Value #2');

--
-- Disparadores `lookup`
--
DROP TRIGGER IF EXISTS `tr_lookup_bi`;
DELIMITER //
CREATE TRIGGER `tr_lookup_bi` BEFORE INSERT ON `lookup`
 FOR EACH ROW BEGIN
  if( ( new.lookup_id = 0 ) || ( new.lookup_id is null ) ) then
    set new.lookup_id = getSequence( 'sq_pk_lookup' );
  end if;
END
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `master`
--

CREATE TABLE IF NOT EXISTS `master` (
  `master_id` int(10) unsigned NOT NULL DEFAULT '0',
  `lookup_id` int(10) unsigned DEFAULT NULL,
  `master_int` int(10) unsigned DEFAULT NULL,
  `master_float` float DEFAULT NULL,
  `master_string` varchar(80) DEFAULT NULL,
  `master_clob` text,
  PRIMARY KEY (`master_id`),
  KEY `idx_master_lookup` (`lookup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `master`
--

INSERT INTO `master` (`master_id`, `lookup_id`, `master_int`, `master_float`, `master_string`, `master_clob`) VALUES
(1, 1, 1, 1, 'Master Row #1', 'Arbitrary Text Row #1'),
(2, 2, 2, 2, 'Master Row #2', 'Arbitrary Text Row #2'),
(3, 1, 3, 3, 'Master Row #3', 'Arbitrary Text Row #3'),
(4, 2, 4, 4, 'Master Row #4', 'Arbitrary Text Row #4'),
(5, 1, 5, 5, 'Master Row #5', 'Arbitrary Text Row #5');

--
-- Disparadores `master`
--
DROP TRIGGER IF EXISTS `tr_master_bi`;
DELIMITER //
CREATE TRIGGER `tr_master_bi` BEFORE INSERT ON `master`
 FOR EACH ROW BEGIN
  if( ( new.master_id = 0 ) || ( new.master_id is null ) ) then
    set new.master_id = getSequence( 'sq_pk_master' );
  end if;
END
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `rights`
--

CREATE TABLE IF NOT EXISTS `rights` (
  `right_id` int(10) unsigned NOT NULL DEFAULT '0',
  `name` varchar(80) NOT NULL,
  `description` text,
  PRIMARY KEY (`right_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `rights`
--

INSERT INTO `rights` (`right_id`, `name`, `description`) VALUES
(1, 'org.jwebsocket.plugins.system.broadcast', NULL),
(2, 'org.jwebsocket.plugins.system.send', NULL),
(3, 'org.jwebsocket.plugins.jdbc.select', NULL),
(4, 'org.jwebsocket.plugins.jdbc.update', NULL),
(5, 'org.jwebsocket.plugins.jdbc.insert', NULL),
(6, 'org.jwebsocket.plugins.jdbc.delete', NULL),
(7, 'org.jwebsocket.plugins.jdbc.querySQL', NULL),
(8, 'org.jwebsocket.plugins.jdbc.updateSQL', NULL),
(9, 'org.jwebsocket.plugins.jdbc.execSQL', NULL),
(10, 'org.jwebsocket.plugins.channels.subscribe', NULL),
(11, 'org.jwebsocket.plugins.channels.unsubscribe', NULL),
(12, 'org.jwebsocket.plugins.channels.authorize', NULL),
(13, 'org.jwebsocket.plugins.channels.publish', NULL),
(14, 'org.jwebsocket.plugins.channels.getChannels', NULL),
(15, 'org.jwebsocket.plugins.channels.getSubscribers', NULL),
(16, 'org.jwebsocket.plugins.channels.getPublishers', NULL),
(17, 'org.jwebsocket.plugins.channels.getSubscriptions', NULL),
(18, 'org.jwebsocket.plugins.channels.createChannel', NULL),
(19, 'org.jwebsocket.plugins.channels.removeChannel', NULL),
(20, 'org.jwebsocket.plugins.channels.manageSystemChannels', NULL),
(21, 'org.jwebsocket.plugins.reporting.getReports', NULL),
(22, 'org.jwebsocket.plugins.reporting.generateReport', NULL),
(23, 'org.jwebsocket.plugins.reporting.uploadTemplate', NULL),
(24, 'org.jwebsocket.plugins.filesystem.load', NULL),
(25, 'org.jwebsocket.plugins.filesystem.save', NULL),
(26, 'org.jwebsocket.plugins.filesystem.append', NULL),
(27, 'org.jwebsocket.plugins.filesystem.delete', NULL),
(28, 'org.jwebsocket.plugins.filesystem.exists', NULL),
(29, 'org.jwebsocket.plugins.filesystem.send', NULL),
(30, 'org.jwebsocket.plugins.filesystem.getFilelist', NULL),
(31, 'org.jwebsocket.plugins.filesystem.observe', NULL),
(32, 'org.jwebsocket.plugins.jms.sendAndListen.queue.testQueue', NULL),
(33, 'org.jwebsocket.plugins.jms.listen.topic.testTopic', NULL),
(34, 'org.jwebsocket.plugins.jms.send.topic.testTopic', NULL),
(35, 'org.jwebsocket.plugins.jms.sendAndListen.topic.stockTopic', NULL),
(36, 'org.jwebsocket.plugins.jms.createSession', NULL),
(37, 'org.jwebsocket.plugins.jms.createConnection', NULL),
(38, 'org.jwebsocket.plugins.jms.createQueue', NULL),
(39, 'org.jwebsocket.plugins.jms.createConsumer', NULL),
(40, 'org.jwebsocket.plugins.jms.rw.test.queue', NULL),
(41, 'org.jwebsocket.plugins.itemstorage.write_collection', NULL),
(42, 'org.jwebsocket.plugins.itemstorage.read_collection', NULL),
(43, 'org.jwebsocket.plugins.itemstorage.write_item', NULL),
(44, 'org.jwebsocket.plugins.itemstorage.read_item', NULL),
(45, 'org.jwebsocket.plugins.itemstorage.write_definition', NULL),
(46, 'org.jwebsocket.plugins.itemstorage.read_definition', NULL),
(47, 'org.jwebsocket.plugins.itemstorage.clear_database', NULL),
(48, 'org.jwebsocket.plugins.admin.shutdown', NULL),
(49, 'org.jwebsocket.plugins.admin.gc', NULL),
(50, 'org.jwebsocket.plugins.admin.getConnections', NULL),
(51, 'org.jwebsocket.plugins.itemstorage.write_collection', NULL),
(52, 'org.jwebsocket.plugins.itemstorage.read_collection', NULL),
(53, 'org.jwebsocket.plugins.itemstorage.write_item', NULL),
(54, 'org.jwebsocket.plugins.itemstorage.read_item', NULL),
(55, 'org.jwebsocket.plugins.itemstorage.write_definition', NULL),
(56, 'org.jwebsocket.plugins.itemstorage.read_definition', NULL),
(57, 'org.jwebsocket.plugins.scripting.reloadApp.*', NULL),
(58, 'org.jwebsocket.plugins.scripting.deploy.*', NULL),
(59, 'org.jwebsocket.plugins.scripting.simplechat.register', NULL),
(60, 'org.jwebsocket.plugins.loadbalancer.clustersInfo', NULL),
(61, 'org.jwebsocket.plugins.loadbalancer.registerServiceEndPoint', NULL),
(62, 'org.jwebsocket.plugins.loadbalancer.deregisterServiceEndPoint', NULL),
(63, 'org.jwebsocket.plugins.loadbalancer.shutdownEndPoint', NULL),
(64, 'org.jwebsocket.plugins.quota.quota_create', NULL),
(65, 'org.jwebsocket.plugins.quota.quota_remove', NULL),
(66, 'org.jwebsocket.plugins.quota.quota_query', NULL),
(67, 'org.jwebsocket.plugins.quota.quota_update', NULL),
(68, 'org.jwebsocket.plugins.sms.sendSMS', NULL),
(69, 'org.jwebsocket.plugins.sms.generateReport', NULL),
(70, 'org.jwebsocket.plugins.sms.auditReports', NULL),
(71, 'org.jwebsocket.plugins.rpc.rpc', NULL),
(72, 'org.jwebsocket.plugins.rpc.rrpc', NULL),
(73, 'org.jwebsocket.plugins.rpc.org.jwebsocket.rpc.sample.Messaging.getMyMessages', NULL),
(74, 'org.jwebsocket.plugins.rpc.org.jwebsocket.rpc.sample.SampleRPCLibrary.rrpcTest2(', NULL),
(75, 'org.jwebsocket.plugins.rpc.org.jwebsocket.rpc.sample.SampleRPCLibrary.rrpcTest1(', NULL),
(76, 'org.jwebsocket.plugins.rpc.org.jwebsocket.rpc.sample.SampleRPCLibrary.rrpcTest1(', NULL),
(77, 'org.jwebsocket.plugins.rpc.org.jwebsocket.rpc.sample.SampleRPCLibrary.rrpcTest1(', NULL),
(78, 'org.jwebsocket.plugins.rpc.org.jwebsocket.rpc.sample.SampleRPCLibrary.getMD5', NULL),
(79, 'org.jwebsocket.plugins.rpc.org.jwebsocket.rpc.sample.Messaging.getMyMessages', NULL),
(80, 'org.jwebsocket.plugins.chat.broadcast', NULL),
(81, 'org.jwebsocket.plugins.rpc.org.jwebsocket.rpc.sample.SampleRPCLibrary.runIntDemo', NULL),
(82, 'org.jwebsocket.plugins.rpc.org.jwebsocket.rpc.sample.SampleRPCLibrary.runStringD', NULL),
(83, 'org.jwebsocket.plugins.rpc.org.jwebsocket.rpc.sample.SampleRPCLibrary.runListDem', NULL),
(84, 'org.jwebsocket.plugins.rpc.org.jwebsocket.rpc.sample.SampleRPCLibrary.runMapDemo', NULL),
(85, 'org.jwebsocket.plugins.rpc.org.jwebsocket.rpc.sample.SampleRPCLibrary.runOverloa', NULL),
(86, 'org.jwebsocket.plugins.rpc.org.jwebsocket.rpc.sample.SampleRPCLibrary.runOverloa', NULL),
(87, 'org.jwebsocket.plugins.rpc.org.jwebsocket.rpc.sample.SampleRPCLibrary.runOverloa', NULL),
(88, 'org.jwebsocket.plugins.rpc.org.jwebsocket.rpc.sample.SampleRPCLibrary.runOverloa', NULL);

--
-- Disparadores `rights`
--
DROP TRIGGER IF EXISTS `tr_right_bi`;
DELIMITER //
CREATE TRIGGER `tr_right_bi` BEFORE INSERT ON `rights`
 FOR EACH ROW BEGIN
  if( ( new.right_id = 0 ) || ( new.right_id is null ) ) then
    set new.right_id = getSequence( 'sq_pk_rights' );
  end if;
END
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `roles`
--

CREATE TABLE IF NOT EXISTS `roles` (
  `role_id` int(10) unsigned NOT NULL DEFAULT '0',
  `name` varchar(30) NOT NULL,
  `description` text,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `roles`
--

INSERT INTO `roles` (`role_id`, `name`, `description`) VALUES
(1, 'Guest', 'Guest Role'),
(2, 'User', 'User Role'),
(3, 'Admin', 'Administrator Role');

--
-- Disparadores `roles`
--
DROP TRIGGER IF EXISTS `tr_role_bi`;
DELIMITER //
CREATE TRIGGER `tr_role_bi` BEFORE INSERT ON `roles`
 FOR EACH ROW BEGIN
  if( ( new.role_id = 0 ) || ( new.role_id is null ) ) then
    set new.role_id = getSequence( 'sq_pk_roles' );
  end if;
END
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `roles_rights`
--

CREATE TABLE IF NOT EXISTS `roles_rights` (
  `role_id` int(10) unsigned NOT NULL,
  `right_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`role_id`,`right_id`),
  KEY `fk_rhr_rights` (`right_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `roles_rights`
--

INSERT INTO `roles_rights` (`role_id`, `right_id`) VALUES
(1, 1),
(2, 1),
(3, 1),
(1, 2),
(2, 2),
(3, 2),
(1, 3),
(2, 3),
(3, 3),
(3, 4),
(3, 5),
(3, 6),
(2, 7),
(3, 7),
(3, 8),
(3, 9),
(1, 10),
(3, 10),
(1, 11),
(3, 11),
(1, 12),
(3, 12),
(1, 13),
(3, 13),
(1, 14),
(3, 14),
(1, 15),
(3, 15),
(1, 16),
(3, 16),
(1, 17),
(3, 17),
(1, 18),
(3, 18),
(1, 19),
(3, 19),
(3, 21),
(3, 22),
(3, 23),
(1, 24),
(2, 24),
(3, 24),
(2, 25),
(3, 25),
(2, 26),
(3, 26),
(2, 27),
(3, 27),
(2, 28),
(3, 28),
(1, 29),
(2, 29),
(3, 29),
(1, 30),
(2, 30),
(3, 30),
(2, 31),
(3, 31),
(1, 32),
(3, 32),
(1, 33),
(3, 33),
(1, 34),
(3, 34),
(1, 35),
(3, 35),
(1, 36),
(3, 36),
(1, 37),
(3, 37),
(1, 38),
(3, 38),
(1, 39),
(3, 39),
(1, 40),
(3, 40),
(3, 41),
(3, 42),
(3, 43),
(3, 44),
(3, 45),
(3, 46),
(3, 47),
(3, 48),
(3, 49),
(3, 50),
(3, 51),
(3, 52),
(3, 53),
(3, 54),
(3, 55),
(3, 56),
(1, 57),
(2, 57),
(3, 57),
(1, 58),
(2, 58),
(3, 58),
(1, 59),
(2, 59),
(3, 59),
(3, 60),
(3, 61),
(3, 62),
(3, 63),
(3, 64),
(3, 65),
(2, 66),
(3, 66),
(3, 67),
(3, 68),
(3, 69),
(3, 70),
(1, 71),
(2, 71),
(3, 71),
(2, 72),
(3, 72),
(1, 73),
(2, 73),
(3, 73),
(1, 74),
(2, 74),
(3, 74),
(1, 75),
(2, 75),
(3, 75),
(1, 76),
(2, 76),
(3, 76),
(1, 77),
(2, 77),
(3, 77),
(1, 78),
(2, 78),
(3, 78),
(1, 79),
(2, 79),
(3, 79),
(1, 80),
(2, 80),
(3, 80),
(1, 81),
(1, 82),
(1, 83),
(1, 84),
(1, 85),
(1, 86),
(1, 87),
(1, 88);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `user_id` int(10) unsigned NOT NULL DEFAULT '0',
  `firstname` varchar(30) DEFAULT NULL,
  `lastname` varchar(30) DEFAULT NULL,
  `loginname` varchar(25) NOT NULL,
  `password` varchar(20) NOT NULL,
  `enabled` tinyint(3) unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `users`
--

INSERT INTO `users` (`user_id`, `firstname`, `lastname`, `loginname`, `password`, `enabled`) VALUES
(1, 'Alexander', 'Schulze', 'aschulze', '123', 1),
(2, 'Rebecca', 'Schulze', 'rschulze', '321', 1),
(3, 'guest', 'guest', 'guest', 'guest', 1),
(4, 'user', 'user', 'user', 'user', 1),
(5, 'root', 'root', 'root', 'root', 1);

--
-- Disparadores `users`
--
DROP TRIGGER IF EXISTS `tr_user_bi`;
DELIMITER //
CREATE TRIGGER `tr_user_bi` BEFORE INSERT ON `users`
 FOR EACH ROW BEGIN
  if( ( new.user_id = 0 ) || ( new.user_id is null ) ) then
    set new.user_id = getSequence( 'sq_pk_users' );
  end if;
END
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `user_roles`
--

CREATE TABLE IF NOT EXISTS `user_roles` (
  `user_id` int(10) unsigned NOT NULL,
  `role_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `fk_uhr_roles` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `user_roles`
--

INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES
(1, 1),
(2, 1),
(3, 1),
(4, 1),
(5, 1),
(1, 2),
(2, 2),
(3, 2),
(4, 2),
(5, 2),
(1, 3),
(2, 3),
(5, 3);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `sequences`
--

CREATE TABLE IF NOT EXISTS `sequences` (
  `seq_id` varchar(40) NOT NULL,
  `next_val` int(10) unsigned DEFAULT '1',
  PRIMARY KEY (`seq_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Volcado de datos para la tabla `sequences`
--

INSERT INTO `sequences` (`seq_id`, `next_val`) VALUES
('sq_pk_child', 7),
('sq_pk_lookup', 3),
('sq_pk_master', 6),
('sq_pk_rights', 10),
('sq_pk_roles', 4),
('sq_pk_users', 6),
('sq_pk_system_log', 562);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `system_log`
--

CREATE TABLE IF NOT EXISTS `system_log` (
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
-- Volcado de datos para la tabla `system_log`
--

INSERT INTO `system_log` (`id`, `time_stamp`, `event_type`, `customer`, `app_name`, `app_version`, `app_module`, `app_dialog`, `ip`, `user_name`, `browser`, `browser_version`, `data_size`, `process_time`, `url`, `message`, `json`, `ws_version`, `session_id`, `name_space`, `token_type`) VALUES
(5, '2011-05-21 20:37:28', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1%0', 'root', 'n/a', 'n/a', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', NULL, NULL, NULL, NULL, NULL),
(6, '2011-05-21 21:04:49', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1%0', 'root', 'Chrome', '11.0.696.68', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', NULL, NULL, NULL, NULL, NULL),
(7, '2011-05-21 21:12:33', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1%0', 'root', 'Chrome', '11.0.696.68', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', NULL, NULL, NULL, NULL, NULL),
(8, '2011-05-21 21:13:32', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '127.0.0.1', 'root', 'Opera', '9.80', 49, NULL, NULL, 'This is an message from the automated test suite.', NULL, NULL, NULL, NULL, NULL),
(9, '2011-05-21 21:14:34', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1%0', 'root', 'Firefox', '4.0.1', 49, NULL, NULL, 'This is an message from the automated test suite.', NULL, NULL, NULL, NULL, NULL),
(10, '2011-05-21 21:15:01', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1%0', 'root', 'Firefox', '4.0.1', 49, NULL, NULL, 'This is an message from the automated test suite.', NULL, NULL, NULL, NULL, NULL),
(11, '2011-05-21 21:16:04', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1%0', 'root', 'Safari', '5.0.5.533.21.1', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', NULL, NULL, NULL, NULL, NULL),
(12, '2011-05-21 21:16:20', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1%0', 'root', 'Safari', '5.0.5.533.21.1', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', NULL, NULL, NULL, NULL, NULL),
(15, '2011-05-21 21:30:18', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1%0', 'root', 'Safari', '5.0.5.533.21.1', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', NULL, 'native', NULL, NULL, NULL),
(17, '2011-05-21 21:34:46', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1%0', 'root', 'Safari', '5.0.5.533.21.1', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', NULL, 'native', NULL, NULL, NULL),
(18, '2011-05-21 21:35:14', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1%0', 'root', 'Firefox', '4.0.1', 49, NULL, NULL, 'This is an message from the automated test suite.', NULL, 'flash 10.1.102', NULL, NULL, NULL),
(19, '2011-05-21 21:41:06', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1%0', 'root', 'Chrome', '11.0.696.68', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', NULL, 'native', NULL, NULL, NULL),
(20, '2011-05-21 21:41:33', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1%0', 'root', 'Firefox', '4.0.1', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', NULL, 'flash 10.1.102', NULL, NULL, NULL),
(21, '2011-05-21 21:46:56', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1%0', 'root', 'Safari', '5.0.5.533.21.1', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', NULL, 'native', NULL, NULL, NULL),
(22, '2011-05-21 21:47:19', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '127.0.0.1', 'root', 'Opera', '9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', NULL, 'native', NULL, NULL, NULL),
(23, '2011-05-21 21:49:51', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1%0', 'root', 'Safari', '5.0.5.533.21.1', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; en-us) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1"}', 'native', NULL, NULL, NULL),
(24, '2011-05-21 21:50:25', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '127.0.0.1', 'root', 'Opera', '9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Macintosh; Intel Mac OS X 10.6.7; U; en) Presto/2.8.131 Version/11.10"}', 'native', NULL, NULL, NULL),
(25, '2011-05-21 21:58:35', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '127.0.0.1', 'root', 'Opera', '11.10/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Macintosh; Intel Mac OS X 10.6.7; U; en) Presto/2.8.131 Version/11.10"}', 'native', NULL, NULL, NULL),
(26, '2011-05-21 21:59:35', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '127.0.0.1', 'root', 'Opera', '11.10/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Macintosh; Intel Mac OS X 10.6.7; U; en) Presto/2.8.131 Version/11.10"}', 'native', NULL, NULL, NULL),
(27, '2011-05-21 22:01:05', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '127.0.0.1', 'root', 'Opera', '11.10/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Macintosh; Intel Mac OS X 10.6.7; U; en) Presto/2.8.131 Version/11.10"}', 'native', NULL, NULL, NULL),
(28, '2011-05-21 22:01:08', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '127.0.0.1', 'root', 'Opera', '11.10/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Macintosh; Intel Mac OS X 10.6.7; U; en) Presto/2.8.131 Version/11.10"}', 'native', NULL, NULL, NULL),
(29, '2011-05-23 22:10:39', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '127.0.0.1', 'root', 'Chrome', '12.0.742.60', 49, NULL, 'ws://127.0.0.1:80/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.60 Safari/534.30"}', 'native', NULL, NULL, NULL),
(30, '2011-05-23 22:10:40', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '127.0.0.1', 'root', 'Chrome', '12.0.742.60', 49, NULL, 'ws://127.0.0.1:80/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.60 Safari/534.30"}', 'native', NULL, NULL, NULL),
(31, '2011-05-23 22:16:05', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '127.0.0.1', 'root', 'Firefox', '4.0.1', 49, NULL, 'ws://127.0.0.1:80/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1"}', 'flash 10.2.159', NULL, NULL, NULL),
(32, '2011-05-23 22:18:17', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '127.0.0.1', 'root', 'Safari', '5.0.533.16', 49, NULL, 'ws://127.0.0.1:80/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16"}', 'native', NULL, NULL, NULL),
(33, '2011-05-23 22:22:33', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '127.0.0.1', 'root', 'Chrome', '13.0.767.1', 49, NULL, 'ws://127.0.0.1:80/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.36 (KHTML, like Gecko) Chrome/13.0.767.1 Safari/534.36"}', 'native', NULL, NULL, NULL),
(34, '2011-05-24 00:15:00', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '12.0.742.60', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.60 Safari/534.30"}', 'native', NULL, NULL, NULL),
(35, '2011-05-24 00:15:40', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '12.0.742.60', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.60 Safari/534.30"}', 'native', NULL, NULL, NULL),
(36, '2011-05-24 00:18:51', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '12.0.742.60', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.60 Safari/534.30"}', 'native', NULL, NULL, NULL),
(37, '2011-05-24 00:19:18', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '4.0.1', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1"}', 'flash 10.2.159', NULL, NULL, NULL),
(38, '2011-05-24 00:19:31', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '4.0.1', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1"}', 'flash 10.2.159', NULL, NULL, NULL),
(39, '2011-05-24 00:24:51', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '4.0.1', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1"}', 'flash 10.2.159', NULL, NULL, NULL),
(40, '2011-05-24 00:25:43', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Safari', '5.0.533.16', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16"}', 'native', NULL, NULL, NULL),
(41, '2011-05-24 00:26:03', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.11/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11"}', 'flash 10.2.159', NULL, NULL, NULL),
(42, '2011-05-24 00:27:36', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '12.0.742.60', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.60 Safari/534.30"}', 'native', NULL, NULL, NULL),
(43, '2011-05-24 00:28:05', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '4.0.1', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1"}', 'flash 10.2.159', NULL, NULL, NULL),
(44, '2011-05-24 00:28:31', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Safari', '5.0.533.16', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16"}', 'native', NULL, NULL, NULL),
(45, '2011-05-24 00:28:45', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.11/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11"}', 'flash 10.2.159', NULL, NULL, NULL),
(46, '2011-05-24 00:28:53', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.11/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11"}', 'flash 10.2.159', NULL, NULL, NULL),
(47, '2011-05-24 00:36:28', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '12.0.742.60', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.60 Safari/534.30"}', 'native', NULL, NULL, NULL),
(48, '2011-05-24 00:36:41', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '4.0.1', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1"}', 'flash 10.2.159', NULL, NULL, NULL),
(49, '2011-05-24 00:37:06', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Safari', '5.0.533.16', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16"}', 'native', NULL, NULL, NULL),
(50, '2011-05-24 00:37:17', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.11/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11"}', 'flash 10.2.159', NULL, NULL, NULL),
(51, '2011-05-24 00:37:22', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.11/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11"}', 'flash 10.2.159', NULL, NULL, NULL),
(52, '2011-05-24 00:38:57', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '8.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.767.1; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)"}', 'flash 10.3.181', NULL, NULL, NULL),
(53, '2011-05-24 00:39:12', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '8.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.767.1; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)"}', 'flash 10.3.181', NULL, NULL, NULL),
(54, '2011-05-24 00:39:31', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '8.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.767.1; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)"}', 'flash 10.3.181', NULL, NULL, NULL),
(55, '2011-05-24 00:41:36', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '8.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.767.1; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)"}', 'flash 10.3.181', NULL, NULL, NULL),
(56, '2011-05-24 00:41:52', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a10 (10519)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '8.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.767.1; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)"}', 'flash 10.3.181', NULL, NULL, NULL),
(57, '2011-05-27 22:40:16', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '12.0.742.68', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.68 Safari/534.30"}', 'native', NULL, NULL, NULL),
(58, '2011-05-27 22:47:10', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '12.0.742.68', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.68 Safari/534.30"}', 'native', NULL, NULL, NULL),
(59, '2011-05-27 22:47:25', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '12.0.742.68', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.68 Safari/534.30"}', 'native', NULL, NULL, NULL),
(60, '2011-05-27 22:57:15', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '12.0.742.68', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.68 Safari/534.30"}', 'native', NULL, NULL, NULL),
(61, '2011-05-27 23:03:30', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '12.0.742.68', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.68 Safari/534.30"}', 'native', NULL, NULL, NULL),
(62, '2011-05-27 23:03:39', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '12.0.742.68', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.68 Safari/534.30"}', 'native', NULL, NULL, NULL),
(63, '2011-05-27 23:04:20', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '4.0.1', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1"}', 'flash 10.2.159', NULL, NULL, NULL),
(64, '2011-05-27 23:04:59', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Safari', '5.0.533.16', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16"}', 'native', NULL, NULL, NULL),
(65, '2011-05-27 23:05:27', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.11/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11"}', 'flash 10.2.159', NULL, NULL, NULL),
(66, '2011-05-27 23:09:05', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '8.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.772.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)"}', 'flash 10.3.181', NULL, NULL, NULL),
(67, '2011-05-27 23:09:26', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '8.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.772.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)"}', 'flash 10.3.181', NULL, NULL, NULL),
(68, '2011-05-27 23:10:11', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '8.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.772.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)"}', 'flash 10.3.181', NULL, NULL, NULL),
(69, '2011-05-27 23:10:58', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '8.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/13.0.772.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)"}', 'flash 10.3.181', NULL, NULL, NULL),
(70, '2011-07-28 18:30:00', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '13.0.782.107', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.107 Safari/535.1"}', 'native', NULL, NULL, NULL),
(71, '2011-07-28 18:30:26', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '5.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0"}', 'flash 10.3.181', NULL, NULL, NULL),
(72, '2011-07-28 18:31:07', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Safari', '5.0.533.16', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16"}', 'native', NULL, NULL, NULL),
(73, '2011-07-28 18:31:25', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.11/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11"}', 'flash 10.3.181', NULL, NULL, NULL),
(74, '2011-07-28 18:34:21', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(75, '2011-07-28 18:34:38', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(76, '2011-07-28 18:35:03', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(77, '2011-07-28 18:35:26', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(78, '2011-07-28 21:32:30', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '13.0.782.107', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.107 Safari/535.1"}', 'native', NULL, NULL, NULL),
(79, '2011-08-15 21:01:28', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(80, '2011-08-15 21:05:08', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(81, '2011-08-15 21:05:25', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(82, '2011-08-15 21:05:49', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(83, '2011-08-15 21:05:54', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(84, '2011-08-15 21:06:13', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(85, '2011-08-15 22:04:03', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(86, '2011-08-15 23:50:11', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(87, '2011-08-15 23:51:43', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(88, '2011-08-15 23:51:49', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(89, '2011-08-16 00:47:11', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(90, '2011-08-16 22:23:14', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(92, '2011-08-16 22:25:38', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(93, '2011-08-16 22:25:44', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(94, '2011-08-16 22:27:02', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(95, '2011-08-16 23:16:20', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '6.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0"}', 'native', NULL, NULL, NULL),
(96, '2011-08-16 23:16:56', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(97, '2011-08-16 23:21:37', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '6.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0"}', 'native', NULL, NULL, NULL),
(98, '2011-08-16 23:22:34', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '6.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0"}', 'native', NULL, NULL, NULL),
(99, '2011-08-16 23:25:16', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '6.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0"}', 'native', NULL, NULL, NULL),
(100, '2011-08-16 23:26:30', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '6.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0"}', 'native', NULL, NULL, NULL),
(101, '2011-08-16 23:27:46', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '6.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0"}', 'native', NULL, NULL, NULL),
(102, '2011-08-16 23:33:50', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '6.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0"}', 'native', NULL, NULL, NULL),
(103, '2011-08-16 23:34:00', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '6.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0"}', 'native', NULL, NULL, NULL),
(104, '2011-08-17 16:12:40', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(105, '2011-08-17 16:13:36', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(106, '2011-08-17 16:14:57', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(107, '2011-08-17 16:15:39', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(108, '2011-08-17 16:16:22', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(109, '2011-08-17 16:21:42', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(110, '2011-08-17 16:23:20', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(111, '2011-08-17 16:24:58', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(112, '2011-08-17 16:25:37', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(113, '2011-08-17 16:26:38', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(114, '2011-08-17 16:27:08', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(115, '2011-08-17 17:59:43', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(116, '2011-08-17 18:00:15', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '5.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0"}', 'flash 10.3.183', NULL, NULL, NULL),
(117, '2011-08-17 18:00:33', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '5.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0"}', 'flash 10.3.183', NULL, NULL, NULL),
(118, '2011-08-17 18:03:20', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0a11 (10530)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.11/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11"}', 'flash 10.3.183', NULL, NULL, NULL),
(119, '2011-08-17 18:03:38', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.11/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11"}', 'flash 10.3.183', NULL, NULL, NULL),
(120, '2011-08-17 18:04:53', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(121, '2011-08-17 18:52:39', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10817)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Safari', '5.0.533.16', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16"}', 'native', NULL, NULL, NULL),
(122, '2011-08-17 18:53:21', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10817)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Safari', '5.0.533.16', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16"}', 'native', NULL, NULL, NULL),
(123, '2011-08-17 18:56:39', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(124, '2011-08-17 18:56:58', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10817)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL);
INSERT INTO `system_log` (`id`, `time_stamp`, `event_type`, `customer`, `app_name`, `app_version`, `app_module`, `app_dialog`, `ip`, `user_name`, `browser`, `browser_version`, `data_size`, `process_time`, `url`, `message`, `json`, `ws_version`, `session_id`, `name_space`, `token_type`) VALUES
(125, '2011-08-17 18:57:29', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10817)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(126, '2011-08-17 18:58:42', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10817)', 'test automation', 'full tests', '127.0.0.1', 'root', 'Opera', '11.11/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11"}', 'native', NULL, NULL, NULL),
(127, '2011-08-17 18:59:18', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(128, '2011-08-17 22:13:07', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10817)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(129, '2011-08-17 22:16:11', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10817)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(130, '2011-08-17 22:24:24', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10817)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(131, '2011-08-17 22:27:02', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '5.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0"}', 'flash 10.3.183', NULL, NULL, NULL),
(132, '2011-08-17 22:35:48', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10817)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.849.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/15.0.849.0 Safari/535.1"}', 'native', NULL, NULL, NULL),
(133, '2011-08-17 22:36:13', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '5.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0"}', 'flash 10.3.183', NULL, NULL, NULL),
(134, '2011-08-17 22:36:40', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10817)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Safari', '5.0.533.16', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16"}', 'native', NULL, NULL, NULL),
(135, '2011-08-17 22:37:07', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.11/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11"}', 'flash 10.3.183', NULL, NULL, NULL),
(136, '2011-08-17 22:37:33', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10817)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(137, '2011-08-20 20:07:40', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10820)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.854.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.854.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(138, '2011-08-20 20:07:53', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10820)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.854.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.854.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(139, '2011-08-20 20:08:35', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10820)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '5.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0"}', 'flash 10.3.183', NULL, NULL, NULL),
(140, '2011-08-20 20:10:16', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10812)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 10.3.183', NULL, NULL, NULL),
(141, '2011-08-20 20:10:27', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10820)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 10.3.183', NULL, NULL, NULL),
(142, '2011-08-20 20:14:06', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10820)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(143, '2011-08-26 17:45:59', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10826)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(144, '2011-08-26 17:46:50', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10826)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '5.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0"}', 'flash 10.3.183', NULL, NULL, NULL),
(145, '2011-08-26 21:11:43', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10826)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(146, '2011-08-26 21:12:45', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10826)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(147, '2011-08-26 21:14:16', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10826)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(148, '2011-08-26 21:16:51', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10826)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(149, '2011-08-26 21:17:28', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10826)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(150, '2011-08-26 21:29:41', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10826)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(151, '2011-08-26 21:30:05', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10826)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '5.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0"}', 'flash 10.3.183', NULL, NULL, NULL),
(152, '2011-08-26 21:30:28', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10826)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '5.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0"}', 'flash 10.3.183', NULL, NULL, NULL),
(153, '2011-08-26 21:30:40', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10826)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '5.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0"}', 'flash 10.3.183', NULL, NULL, NULL),
(154, '2011-08-26 21:31:48', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10826)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 10.3.183', NULL, NULL, NULL),
(155, '2011-08-26 21:32:17', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10826)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(156, '2011-08-31 16:42:34', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(157, '2011-08-31 16:42:50', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(158, '2011-08-31 17:22:05', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(159, '2011-08-31 17:28:30', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(160, '2011-08-31 17:33:11', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(161, '2011-08-31 17:34:50', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(162, '2011-08-31 17:36:57', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(163, '2011-08-31 17:37:20', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(164, '2011-08-31 17:37:56', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(165, '2011-08-31 17:38:56', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(166, '2011-08-31 17:39:27', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(167, '2011-08-31 17:42:56', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(168, '2011-08-31 17:45:28', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(169, '2011-08-31 17:45:57', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(170, '2011-08-31 17:47:30', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(171, '2011-08-31 17:47:55', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(172, '2011-08-31 17:50:52', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(173, '2011-08-31 17:51:30', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(174, '2011-08-31 18:07:14', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(175, '2011-08-31 18:07:19', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(176, '2011-08-31 18:08:19', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(177, '2011-08-31 18:08:24', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(178, '2011-08-31 18:09:34', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(179, '2011-08-31 18:09:41', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(180, '2011-08-31 18:09:46', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(181, '2011-08-31 18:10:37', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(182, '2011-08-31 18:10:46', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(183, '2011-08-31 18:12:16', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(184, '2011-08-31 18:12:25', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(185, '2011-08-31 18:13:33', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(186, '2011-08-31 18:13:39', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(187, '2011-08-31 18:17:36', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(188, '2011-08-31 18:36:52', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.861.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.861.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(189, '2011-08-31 23:30:36', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(190, '2011-09-01 21:28:51', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(191, '2011-09-01 21:29:20', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(192, '2011-09-01 21:29:47', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(193, '2011-09-01 22:14:20', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(194, '2011-09-01 22:14:55', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(195, '2011-09-02 17:09:44', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10831)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(196, '2011-09-02 17:10:06', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10902)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(197, '2011-09-02 17:10:17', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10902)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(198, '2011-09-02 17:20:59', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10902)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(199, '2011-09-02 17:21:26', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10902)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '5.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0"}', 'flash 10.3.183', NULL, NULL, NULL),
(200, '2011-09-02 17:23:02', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10902)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 10.3.183', NULL, NULL, NULL),
(201, '2011-09-02 17:23:08', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10902)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 10.3.183', NULL, NULL, NULL),
(202, '2011-09-02 17:24:20', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10902)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(203, '2011-09-02 18:04:09', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10902)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(204, '2011-09-02 18:30:15', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10902)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(223, '2011-09-06 18:09:03', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(227, '2011-09-06 18:09:26', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(231, '2011-09-06 18:22:40', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(235, '2011-09-06 18:23:28', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(239, '2011-09-06 18:23:54', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(243, '2011-09-06 18:25:15', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(247, '2011-09-06 20:26:34', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(251, '2011-09-06 20:27:39', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(255, '2011-09-06 20:28:20', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '5.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0"}', 'flash 10.3.183', NULL, NULL, NULL),
(259, '2011-09-06 20:29:16', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 10.3.183', NULL, NULL, NULL),
(263, '2011-09-06 20:29:58', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(275, '2011-09-06 21:19:39', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (nightly build 10906)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.865.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.865.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(279, '2011-09-06 21:20:02', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '5.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0"}', 'flash 10.3.183', NULL, NULL, NULL),
(283, '2011-09-06 21:20:47', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (nightly build 10906)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Safari', '5.0.533.16', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16"}', 'native', NULL, NULL, NULL),
(287, '2011-09-06 21:21:26', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 10.3.183', NULL, NULL, NULL),
(288, '2011-09-06 21:21:29', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 10.3.183', NULL, NULL, NULL),
(295, '2011-09-06 21:21:47', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(299, '2011-09-08 19:54:32', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (nightly build 10908)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.874.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.0 Safari/535.2"}', 'native', NULL, NULL, NULL),
(303, '2011-09-08 19:55:06', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (nightly build 10908)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '6.0.2', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0.2) Gecko/20100101 Firefox/6.0.2"}', 'native', NULL, NULL, NULL),
(307, '2011-09-08 19:56:41', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (nightly build 10908)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Safari', '5.0.533.16', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16"}', 'native', NULL, NULL, NULL),
(311, '2011-09-08 19:57:05', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 10.3.183', NULL, NULL, NULL),
(312, '2011-09-08 19:57:05', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (10905)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 10.3.183', NULL, NULL, NULL),
(319, '2011-09-08 19:57:24', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (nightly build 10908)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 10.3.183', NULL, NULL, NULL),
(323, '2011-09-08 19:57:55', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (nightly build 10908)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(327, '2011-09-14 22:51:25', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (nightly build 10914)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.874.12', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.12 Safari/535.2"}', 'native', NULL, NULL, NULL),
(331, '2011-09-14 22:51:39', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (nightly build 10914)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.874.12', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.12 Safari/535.2"}', 'native', NULL, NULL, NULL),
(335, '2011-09-14 22:52:38', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (nightly build 10914)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '5.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0"}', 'flash 10.3.183', NULL, NULL, NULL),
(339, '2011-09-14 22:54:51', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (nightly build 10914)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Safari', '5.0.533.16', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16"}', 'native', NULL, NULL, NULL),
(343, '2011-09-14 22:56:34', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (nightly build 10914)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 10.3.183', NULL, NULL, NULL),
(347, '2011-09-14 22:57:17', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (nightly build 10914)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(351, '2011-09-20 19:08:38', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (build 10919)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.874.15', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.15 Safari/535.2"}', 'native', NULL, NULL, NULL),
(355, '2011-09-20 19:09:15', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (build 10919)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.874.15', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.15 Safari/535.2"}', 'native', NULL, NULL, NULL),
(359, '2011-09-20 19:09:24', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (build 10919)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.874.15', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.15 Safari/535.2"}', 'native', NULL, NULL, NULL);
INSERT INTO `system_log` (`id`, `time_stamp`, `event_type`, `customer`, `app_name`, `app_version`, `app_module`, `app_dialog`, `ip`, `user_name`, `browser`, `browser_version`, `data_size`, `process_time`, `url`, `message`, `json`, `ws_version`, `session_id`, `name_space`, `token_type`) VALUES
(363, '2011-09-20 19:10:50', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (build 10919)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.874.15', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.15 Safari/535.2"}', 'native', NULL, NULL, NULL),
(367, '2011-09-20 19:10:57', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (build 10919)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.874.15', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.15 Safari/535.2"}', 'native', NULL, NULL, NULL),
(371, '2011-09-22 16:23:21', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (build 10919)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.874.21', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.21 Safari/535.2"}', 'native', NULL, NULL, NULL),
(375, '2011-09-22 16:23:34', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (build 10919)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '15.0.874.21', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.21 Safari/535.2"}', 'native', NULL, NULL, NULL),
(376, '2011-09-30 01:31:20', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10929)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.891.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.5 (KHTML, like Gecko) Chrome/16.0.891.0 Safari/535.5"}', 'native', NULL, NULL, NULL),
(380, '2011-09-30 01:31:38', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10929)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.891.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.5 (KHTML, like Gecko) Chrome/16.0.891.0 Safari/535.5"}', 'native', NULL, NULL, NULL),
(384, '2011-09-30 01:31:52', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10929)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.891.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.5 (KHTML, like Gecko) Chrome/16.0.891.0 Safari/535.5"}', 'native', NULL, NULL, NULL),
(388, '2011-09-30 22:39:09', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.891.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.5 (KHTML, like Gecko) Chrome/16.0.891.0 Safari/535.5"}', 'native', NULL, NULL, NULL),
(392, '2011-09-30 22:41:01', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '6.0.2', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0.2) Gecko/20100101 Firefox/6.0.2"}', 'native', NULL, NULL, NULL),
(396, '2011-09-30 22:44:30', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b1 (nightly build 10914)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 10.3.183', NULL, NULL, NULL),
(397, '2011-09-30 22:45:01', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 10.3.183', NULL, NULL, NULL),
(401, '2011-09-30 22:45:37', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 10.3.183', NULL, NULL, NULL),
(405, '2011-09-30 22:48:36', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '9.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(409, '2011-10-05 23:37:26', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.899.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.6 (KHTML, like Gecko) Chrome/16.0.899.0 Safari/535.6"}', 'native', NULL, NULL, NULL),
(413, '2011-10-05 23:38:24', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.899.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.6 (KHTML, like Gecko) Chrome/16.0.899.0 Safari/535.6"}', 'native', NULL, NULL, NULL),
(417, '2011-10-24 21:11:48', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.912.4', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7"}', 'native', NULL, NULL, NULL),
(421, '2011-10-24 23:24:18', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.912.4', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7"}', 'native', NULL, NULL, NULL),
(425, '2011-10-24 23:24:35', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.912.4', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7"}', 'native', NULL, NULL, NULL),
(429, '2011-10-24 23:39:37', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.912.4', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7"}', 'native', NULL, NULL, NULL),
(433, '2011-10-24 23:40:40', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.912.4', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7"}', 'native', NULL, NULL, NULL),
(437, '2011-10-24 23:52:47', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.912.4', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7"}', 'native', NULL, NULL, NULL),
(441, '2011-10-25 00:35:53', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.912.4', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7"}', 'native', NULL, NULL, NULL),
(445, '2011-10-25 00:36:25', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.912.4', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7"}', 'native', NULL, NULL, NULL),
(449, '2011-10-25 00:38:13', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.912.4', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.4 Safari/535.7"}', 'native', NULL, NULL, NULL),
(453, '2011-10-25 00:42:50', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Firefox', '5.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0"}', 'flash 10.3.183', NULL, NULL, NULL),
(457, '2011-10-25 00:46:29', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 10.3.183', NULL, NULL, NULL),
(461, '2011-10-25 00:47:30', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b3 (nightly build 11024)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '7.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(465, '2011-10-31 21:38:26', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b3 (nightly build 11024)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.912.15', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.15 Safari/535.7"}', 'native', NULL, NULL, NULL),
(469, '2011-10-31 21:49:50', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b3 (nightly build 11024)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '16.0.912.15', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.15 Safari/535.7"}', 'native', NULL, NULL, NULL),
(473, '2011-11-28 21:30:39', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11125)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '17.0.942.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8"}', 'native', NULL, NULL, NULL),
(477, '2011-11-28 22:35:08', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11125)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '17.0.942.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8"}', 'native', NULL, NULL, NULL),
(481, '2011-11-28 22:40:25', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11125)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '17.0.942.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8"}', 'native', NULL, NULL, NULL),
(485, '2011-11-28 22:41:30', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11125)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '17.0.942.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8"}', 'native', NULL, NULL, NULL),
(489, '2011-11-28 22:41:48', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11125)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '17.0.942.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8"}', 'native', NULL, NULL, NULL),
(493, '2011-11-28 22:41:59', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11125)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '17.0.942.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8"}', 'native', NULL, NULL, NULL),
(497, '2011-11-28 22:42:25', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11125)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '17.0.942.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8"}', 'native', NULL, NULL, NULL),
(501, '2011-12-01 21:20:08', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11125)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', NULL, 'Chrome', '17.0.942.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8"}', 'native', NULL, NULL, NULL),
(505, '2011-12-01 21:22:51', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11125)', 'test automation', 'full tests', '127.0.0.1', NULL, 'Firefox', '8.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:8.0) Gecko/20100101 Firefox/8.0"}', 'native', NULL, NULL, NULL),
(509, '2011-12-01 21:24:58', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11125)', 'test automation', 'full tests', '127.0.0.1', NULL, 'Firefox', '8.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:8.0) Gecko/20100101 Firefox/8.0"}', 'native', NULL, NULL, NULL),
(513, '2011-12-01 21:28:02', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11125)', 'test automation', 'full tests', '127.0.0.1', NULL, 'Firefox', '8.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:8.0) Gecko/20100101 Firefox/8.0"}', 'native', NULL, NULL, NULL),
(517, '2011-12-01 21:29:13', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11125)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', NULL, 'Safari', '5.0.533.16', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16"}', 'native', NULL, NULL, NULL),
(521, '2011-12-01 21:35:02', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11125)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', NULL, 'Chrome', '17.0.942.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8"}', 'native', NULL, NULL, NULL),
(525, '2011-12-01 22:13:53', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11201)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '17.0.942.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8"}', 'native', NULL, NULL, NULL),
(529, '2011-12-01 22:32:40', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11201)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '17.0.942.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8"}', 'native', NULL, NULL, NULL),
(533, '2011-12-01 22:57:43', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11201)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '17.0.942.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.8 (KHTML, like Gecko) Chrome/17.0.942.0 Safari/535.8"}', 'native', NULL, NULL, NULL),
(537, '2011-12-01 22:58:54', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11201)', 'test automation', 'full tests', '127.0.0.1', 'root', 'Firefox', '8.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:8.0) Gecko/20100101 Firefox/8.0"}', 'native', NULL, NULL, NULL),
(541, '2011-12-01 23:00:04', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11201)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Safari', '5.0.533.16', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16"}', 'native', NULL, NULL, NULL),
(545, '2011-12-01 23:01:11', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b2 (nightly build 10930)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 11.0.1', NULL, NULL, NULL),
(549, '2011-12-01 23:01:46', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11201)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Opera', '11.50/9.80', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Opera/9.80 (Windows NT 6.1; U; en) Presto/2.9.168 Version/11.50"}', 'flash 11.0.1', NULL, NULL, NULL),
(553, '2011-12-01 23:02:48', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b4 (nightly build 11201)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Internet Explorer', '7.0', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0; .NET4.0C; AskTB5.6)"}', 'flash 11.0.1', NULL, NULL, NULL),
(557, '2011-12-29 22:51:18', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b5 (nightly build 11222)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '17.0.963.12', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.12 Safari/535.11"}', 'native', NULL, NULL, NULL),
(561, '2011-12-30 23:27:01', 'loggingTest', 'jWebSocket.org', 'jWebSocket', '1.0b5 (nightly build 11222)', 'test automation', 'full tests', '0:0:0:0:0:0:0:1', 'root', 'Chrome', '17.0.963.12', 49, NULL, 'ws://localhost:8787/jWebSocket/jWebSocket', 'This is an message from the automated test suite.', '{"userAgent":"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.12 Safari/535.11"}', 'native', NULL, NULL, NULL);

--
-- Disparadores `system_log`
--
DROP TRIGGER IF EXISTS `tr_system_log_bi`;
DELIMITER //
CREATE TRIGGER `tr_system_log_bi` BEFORE INSERT ON `system_log`
 FOR EACH ROW BEGIN
  if( ( new.id = 0 ) || ( new.id is null ) ) then
    set new.id = getSequence( 'sq_pk_system_log' );
  end if;
END
//
DELIMITER ;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `child`
--
ALTER TABLE `child`
  ADD CONSTRAINT `fk_master_child` FOREIGN KEY (`master_id`) REFERENCES `master` (`master_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Filtros para la tabla `master`
--
ALTER TABLE `master`
  ADD CONSTRAINT `fk_master_lookup` FOREIGN KEY (`lookup_id`) REFERENCES `lookup` (`lookup_id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Filtros para la tabla `roles_rights`
--
ALTER TABLE `roles_rights`
  ADD CONSTRAINT `fk_rhr_rights` FOREIGN KEY (`right_id`) REFERENCES `rights` (`right_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_rhr_roles` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Filtros para la tabla `user_roles`
--
ALTER TABLE `user_roles`
  ADD CONSTRAINT `fk_uhr_roles` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_uhr_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
