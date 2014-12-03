/* This script creates the uses jwsSys and grants him the "system" rights
 * This database user is supposed to be used for internal administrator access only!
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
