/* This script creates the uses jwsDemo and grants him the "demo" rights 
 * This database user is supposed to be used for external user access for usual work purposes!
 */

/* use this database schema */
use `jwebsocket`;

/*  demo tables */
grant select,insert,delete,update on `jwebsocket`.`demo_master` to `jwsDemo`@`localhost`;
grant select,insert,delete,update on `jwebsocket`.`demo_child` to `jwsDemo`@`localhost`;
grant select,insert,delete,update on `jwebsocket`.`demo_lookup` to `jwsDemo`@`localhost`;

/* necessary access to stored procedures */
grant execute on procedure `jwebsocket`.`getSequence` to `jwsDemo`@`localhost`;
/* This script creates the uses jwsApp and grants him the "application" rights 
 * This database user is supposed to be used for external user access for usual work purposes!
 */

/* use this database schema */
use `jwebsocket`;

/*  demo tables */
grant select,insert,delete,update on `jwebsocket`.`demo_master` to `jwsApp`@`localhost`;
grant select,insert,delete,update on `jwebsocket`.`demo_child` to `jwsApp`@`localhost`;
grant select,insert,delete,update on `jwebsocket`.`demo_lookup` to `jwsApp`@`localhost`;

/* necessary access to stored procedures */
grant execute on procedure `jwebsocket`.`getSequence` to `jwsApp`@`localhost`;
/* This script creates the uses jwsSys and grants him the "system" rights
 * This database user is supposed to be used for external user access for usual work purposes!
 */

/* use this database schema */
use `jwebsocket`;

/*  grant access to all tables for sys user */
grant select,insert,delete,update on `jwebsocket`.* to `jwsSys`@`localhost`;

/* necessary access to stored procedures */
grant execute on procedure `jwebsocket`.`getSequence` to `jwsSys`@`localhost`;

/* special rights to create tables */
grant create on `jwebsocket`.* to `jwsSys`@`localhost`;
grant drop on `jwebsocket`.* to `jwsSys`@`localhost`;
