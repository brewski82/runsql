CREATE FUNCTION sp_test(return_value integer) RETURNS integer AS $$
DECLARE
    quantity integer := 30;
BEGIN
    RAISE NOTICE 'Quantity here is %', quantity;
    RETURN return_value;
END;
$$ LANGUAGE plpgsql;
