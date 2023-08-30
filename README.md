# sors-dea-files-combiner
Combines all files downloaded from the DEA into one 
file and then loads it into Edna. The table it loads
the data into is defined in the source code. The data
to load is specified by providing directory names on
the command line. These are the directories where
the DEA data was unzipped. So you have to do this
step before you run the program.

<h1>Upload Authentication</h1>

To load data into Edna, which is in GCP, you have to be
authenticated with GCP. You have to run this command
in a Google Cloud SDK Shell:

    gcloud auth login

<h1>Intellij</h1>

A run configuration is already setup in the Intellij
project. Just change the command line arguments to the
directories where you unzipped the data files for the
years you want to load. Each command line argument is
the name of a directory.

The jar file can also be run from the command line or
in a script:

    java -jar out/artifacts/sors_dea_files_combiner_jar/sors-dea-files-combiner.jar <dea-files-dir-1> <dea-files-dir-2> ...

<h1>Eclipse</h1>

The source code includes an Eclipse project. A run 
configuration is included for Eclipse in the file 
Main.launch. You will have to change the Program
Arguments in the Arguments tab of the Run Configuration
to the directories where you unzipped the data files.

Eclipse can also generate a Runnable Jar File. Go to
File -> Export -> Java -> Runnable Jar file. Then it
can be run from the command line, or in a script:

    java -jar sors-dea-files-combiner.jar <dea-files-dir-1> <dea-files-dir-2> ...

<h1>TODO</h1>
The program uses "bq" to upload the data into Edna.
This part of the code was just translated into Java
from the script scripts/load-sors-dea-transactions.bat.
This means it has to locate the "bq" program at runtime.
It also makes it Windows-specific because it uses the Windows
location of "bq". It would be better for it to use the
BigQuery Java API:

     https://cloud.google.com/bigquery/docs/batch-loading-data#java

