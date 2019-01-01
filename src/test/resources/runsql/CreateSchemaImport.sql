create table person (
  person_id integer not null
  , first_name varchar(255)
  , last_name varchar(255)
  , birth_date date
  , is_employed boolean
  , last_login timestamp
);

create table import_person (
  person_id integer not null
  , first_name varchar(255)
  , last_name varchar(255)
  , birth_date date
  , is_employed boolean
  , last_login timestamp
);
