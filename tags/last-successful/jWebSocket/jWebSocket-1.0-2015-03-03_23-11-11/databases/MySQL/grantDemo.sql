/* This script creates the uses jwsDemo and grants him the "demo" rights 
 * This database user is supposed to be used for non-authenticated external user access for demo purposes only!
 */

/* use this database schema */
use `jwebsocket`;

/*  demo tables */
grant select on `jwebsocket`.`demo_master` to `jwsDemo`@`localhost`;
grant select on `jwebsocket`.`demo_child` to `jwsDemo`@`localhost`;
grant select on `jwebsocket`.`demo_lookup` to `jwsDemo`@`localhost`;
