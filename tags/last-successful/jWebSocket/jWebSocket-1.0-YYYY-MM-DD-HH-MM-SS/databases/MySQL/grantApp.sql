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
