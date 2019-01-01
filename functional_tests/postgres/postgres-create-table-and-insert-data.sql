create table person (
 id serial
 , bignumber bigint
 , numericnumber numeric
 , cash money
 , name_first varchar(255)
 , name_last varchar(255)
 , sometext text
 , somebinary bytea
 , atimestamp timestamp without time zone
 , anothertimestamp timestamp with time zone
 , adate date
 , atime time
 , ainterval interval
 , aboolean boolean
 , anull integer
 , ajson json
 );

insert into person values
(1, 9223372036854775806, 1234.12234949493, 124.55::money, 'Joe', 'Smith', 'blah blah blah', E'\\xDEADBEEF',
 '1999-01-08 04:05:06', '1999-01-08 04:05:06 -8:00', '1999-01-08', '04:05:06', '3 4:05:06', true, null,
 '{"bar": "baz", "balance": 7.77, "active": false}'::json);
