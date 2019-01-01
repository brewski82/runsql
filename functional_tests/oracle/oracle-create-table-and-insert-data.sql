alter session set nls_date_format='yyyy-mm-dd hh24:mi:ss';

create table person (
 id number
 , bignumber number
 , numericnumber number
 , cash number(10,2)
 , afloat binary_float
 , adouble binary_double
 , name_first varchar(255)
 , name_last varchar2(255)
 , atimestamp timestamp
 , adate date
);

insert into person values
(
  1 -- id number
  , 9223372036854775806 -- number
  , 1234.1223 -- number
  , 124.55 -- number(10,2)
  , 123.66 -- binary_float
  , 123.66 -- binary_double
  , 'Joe' -- name_first
  , 'Smith' -- name_last
  , to_date('1999-01-08 04:05:06', 'yyyy-mm-dd hh24:mi:ss') -- atimestamp
  , to_date('1999-01-08', 'yyyy-mm-dd') -- adate
);
