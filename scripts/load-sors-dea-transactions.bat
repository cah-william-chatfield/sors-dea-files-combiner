@echo off
rem Load SORS DEA Transaction Data - Loads file given on command line or the below DEFAULT_FILE_TO_LOAD
rem Author: Bill Chatfield

rem Set this to the file you want to load.
set DEFAULT_FILE_TO_LOAD=%USERPROFILE%\OneDrive - Cardinal Health\Documents\SORS\Accrual Cycle Reports\DEA Transaction Data\Transaction.csv

rem It should be CSV format, for example:
rem registrant_dea,ndc,order_quantity,customer_dea,dea_form,transaction_date
rem "RK0416900","69315090510","00000003","BM9629417","","12312020"
rem "RK0416900","00832112005","00000006","FL7273193","","12312020"
rem "RK0416900","59417010310","00000002","BD4433859","20XK00034","12312020"

rem Use a command line argument, if one is given. Otherwise, use the default.
if "%1"=="" (
    set FILE_TO_LOAD=%DEFAULT_FILE_TO_LOAD%
) else (
    set FILE_TO_LOAD=%~1
)

CHOICE /M "Load %FILE_TO_LOAD% "
if %ERRORLEVEL% EQU 2 goto :END

rem References:
rem https://cloud.google.com/bigquery/docs/batch-loading-data#bq
rem https://cloud.google.com/bigquery/docs/reference/bq-cli-reference#bq_load

bq load ^
	--source_format=CSV ^
	--replace=true ^
	--skip_leading_rows=1 ^
	edna-rsh-pqra-pr-cah:INJUNCTIVE_RELIEF.SORS_DEA_Transaction ^
	"%FILE_TO_LOAD%" ^
	registrant_dea:STRING,ndc:STRING,order_quantity:INTEGER,customer_dea:STRING,dea_form:STRING,transaction_date:STRING

:END
