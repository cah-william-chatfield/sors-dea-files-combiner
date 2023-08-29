rem Load SORS DEA Transaction Data
rem Author: Bill Chatfield

rem Set this to the file you want to load.
set DEFAULT_FILE_TO_LOAD=%USERPROFILE%\OneDrive - Cardinal Health\Documents\SORS\Accrual Cycle Reports\DEA Transaction Data\Transaction.csv

rem Use a command line argument, if one is given. Otherwise, use the default.
if "%1"=="" (
    set FILE_TO_LOAD=%DEFAULT_FILE_TO_LOAD%
) else (
    set FILE_TO_LOAD=%1
)

bq load ^
	--source_format=CSV ^
	--replace=true ^
	--skip_leading_rows=1 ^
	edna-rsh-pqra-pr-cah:INJUNCTIVE_RELIEF.SORS_DEA_Transaction ^
	"%FILE_TO_LOAD%" ^
	registrant_dea:STRING,ndc:STRING,order_quantity:INTEGER,customer_dea:STRING,dea_form:STRING,transaction_date:STRING
