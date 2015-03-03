# SQL Manager 2005 for MySQL 3.7.0.1
# ---------------------------------------
# Host     : localhost
# Port     : 3306
# Database : crud_jws


SET FOREIGN_KEY_CHECKS=0;

CREATE DATABASE `jws_extjs_crud`
    CHARACTER SET 'latin1'
    COLLATE 'latin1_swedish_ci';

#
# Structure for the `tb_user` table : 
#

CREATE TABLE `tb_user` (
  `iduser` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(40) DEFAULT NULL,
  `last_name` varchar(40) DEFAULT NULL,
  `address` text,
  `zip_code` varchar(30) DEFAULT NULL,
  `city_address` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`iduser`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=latin1 PACK_KEYS=0;

#
# Data for the `tb_user` table  (LIMIT 0,500)
#

INSERT INTO `tb_user` (`iduser`, `name`, `last_name`, `address`, `zip_code`, `city_address`) VALUES 
  (24,'Frederish','Hiltom','27th street between 42 y 44','567-432-10','London, UK'),
  (25,'Peter','Thonsom','23th street #34102','567-120-78','Bahamas'),
  (26,'Juan','del Bosco','3 and 14, 123 street','561-2-133-12','Madrid, Spain'),
  (27,'Amon ','Goeth','12nd street #4120','567-12-1234-1','Berlin, Germany'),
  (28,'Ramanish','Babalish','4312, 45th street','123-562-125-67','Budapest, Turkia'),
  (42,'Juan Miguel','Fernandez','561, 21st street','672-12-432-12','Havana Cuba');

COMMIT;

