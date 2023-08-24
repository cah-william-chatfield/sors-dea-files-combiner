rem Set this to the file you want to load.
set FILE_TO_LOAD=%USERPROFILE%\Downloads\SORS-2023.csv

bq load ^
	--source_format=CSV ^
	--replace=true ^
	--skip_leading_rows=1 ^
	edna-rsh-pqra-pr-cah:INJUNCTIVE_RELIEF.SORS_DEA_Transaction ^
	"%FILE_TO_LOAD%" ^
	registrant_dea:STRING,ndc:STRING,order_quantity:INTEGER,customer_dea:STRING,dea_form:STRING,transaction_date:STRING
