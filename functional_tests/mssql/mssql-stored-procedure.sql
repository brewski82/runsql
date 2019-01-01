CREATE PROCEDURE sp_test
    @invar integer
AS
BEGIN
    SELECT @invar from person;
END
