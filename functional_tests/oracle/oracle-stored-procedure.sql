CREATE FUNCTION sp_test(input_no IN NUMBER)
   RETURN NUMBER
   IS acc_bal varchar(255);
   BEGIN
      SELECT *
      INTO acc_bal
      FROM dual;
      RETURN(input_no);
    END;
