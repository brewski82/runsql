create table person (
 id int
 , bignumber bigint
 , numericnumber numeric(20,5)
 , cash decimal(5,2)
 , name_first varchar(255)
 , name_last varchar(255)
 , sometext text
 , somebinary binary(3)
 , atimestamp timestamp
 , adate date
 , atime time
 , abit bit(1)
 , anull integer
 , ajson json
 );

insert into person values
(1, 9223372036854775806, 1234.1223, 124.55, 'Joe', 'Smith', 'blah blah blah', 'a',
 '1999-01-08 04:05:06', '1999-01-08', '04:05:06', b'1', null,
 '{"bar": "baz", "balance": 7.77, "active": false}');
