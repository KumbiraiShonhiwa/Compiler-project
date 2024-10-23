10 DIM M(7,30)
20 LET V_a = 10
20 LET V_b = 5
20 LET V_result1 = 10+10+5
20 LET V_c = CALL_F_localfunc(V_a,V_b,V_c)
20 LET STOP  REM BEGIN V_innerresult = 0
20 IF V_innerresult grt V_c THEN 30
30 RETURN
