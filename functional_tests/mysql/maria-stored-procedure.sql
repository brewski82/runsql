
CREATE PROCEDURE sp_test
(IN con INTEGER)
BEGIN
  SELECT con from person;
END
