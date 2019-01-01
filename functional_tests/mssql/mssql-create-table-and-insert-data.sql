create table person (
 id int
 , bignumber bigint
 , numericnumber numeric(20,5)
 , cash decimal(5,2)
 , amoney money
 , asmallmoney smallmoney
 , asmallint smallint
 , atinyint tinyint
 , afloat float
 , areal real
 , name_first varchar(255)
 , name_last varchar(255)
 , sometext text
 , somebinary binary(3)
 , atimestamp datetime
 , adate date
 , atime time
 , abit bit
 , anull integer
 , anvarchar nvarchar(3)
);

insert into person values
(
  1 -- id int
  , 9223372036854775806 -- bignumber
  , 1234.1223 -- numericnumber
  , 124.55 -- cash
  , $4000000.34 -- amoney
  , $200123.45 -- asmallmoney
  , 432 -- asmallint
  , 42 -- atinyint
  , 123.66 -- afloat
  , 123.66 -- areal
  , 'Joe' -- name_first
  , 'Smith' -- name_last
  , 'blah blah blah' -- sometext
  , cast(32 as binary(3)) -- somebinary
  , '1999-01-08 04:05:06' -- atimestamp
  , '1999-01-08' -- adate
  , '04:05:06' -- atime
  , 1 -- abit
  , null -- anull
  , N'ABC' -- anvarchar
);
