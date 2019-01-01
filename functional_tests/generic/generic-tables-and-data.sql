create table generic_person (
  a_char char
  , a_varchar varchar(10)
--  , a_bit bit
  , a_numeric NUMERIC(10,2)
  , a_decimal decimal(10,2)
  , a_integer integer
  , a_smallint smallint
  , a_float float
  , a_real real
  , a_double double precision
  , a_date date
  , a_time time
--  , a_timestamp datetime
);

insert into generic_person values
('a', 'ab', 55.5, 54.5, 22, 10, 43.2, 23.3, 223434.22, '2000-01-01', '04:05:06');
