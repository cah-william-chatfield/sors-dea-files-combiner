import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;

/**
 * Creates one combined CSV file from all the SORS DEA data files and loads that CSV file.
 * @author william.chatfield
 */
public class Main {

    /**
     * Needed to find the "bq" command used to load the data. This may need to be updated if GCP SDK/CLI is
     * installed in a different location. Switching to the Java API would fix this problem.
     */
    public static final String GCP_SDK_BIN = "C:\\Program Files (x86)\\Google\\Cloud SDK\\google-cloud-sdk\\bin";

    /**
     * This could be changed if the user doesn't have access to this area.
     */
    public static final String TABLE_TO_LOAD = "edna-rsh-pqra-pr-cah:INJUNCTIVE_RELIEF.SORS_DEA_Transaction";

    /**
     * Starts non-static context, checks command line arguments, handles exceptions
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length >= 2) {
                System.exit(new Main().combineAndLoad(args));
            } else {
                System.out.println("Usage: java -jar sors-dea-files-combiner.jar <input-file-dir-1> <input-file-dir-2> ...");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected int combineAndLoad(String[] args) throws IOException, InterruptedException {
        // TODO - Take zip files as input instead of unzipped directories to eliminate a step
        String outputFileName = "Combined DEA Data.csv";
        combineFiles(outputFileName, args);
        int exitCode = load(outputFileName);
        if (exitCode == 0) {
            System.out.println("SORS job complete!");
        } else {
            System.err.println("SORS job failed. You may need to login using this command: gcloud auth login");
        }
        return exitCode;
    }
    /**
     * Combines files into one CSV file
     * @param inputFileDirs Directory containing input files
     * @param outputFile    Where the output should go
     * @throws IOException  If directory can't be opened and other possibilities from writeFile
     */
    protected void combineFiles(String outputFile, String[] inputFileDirs) throws IOException {
        System.out.printf("Combining files from directories:%n%s%ninto file: %s%n", String.join("\n", inputFileDirs), outputFile);
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(Paths.get(outputFile)))) {      // Open output file
            out.println("registrant_dea,ndc,order_quantity,customer_dea,dea_form,transaction_date");   // Write headers
            for (String inputFileDir : inputFileDirs) {                                                // Loop over all input dirs
                try (DirectoryStream<Path> inputFiles = Files.newDirectoryStream(Paths.get(inputFileDir))) { // Get all input files
                    for (Path inputFile : inputFiles) {                                                      // Loop over all input files
                        writeFile(out, inputFile);
                    }
                }
            }
        }
    }

    /**
     * Writes a single file to the output, removing unneeded lines and columns, converting to CSV
     * @param out       Where the output goes
     * @param inputFile Where the input comes from
     * @throws IOException  If the file cannot be opened and other possibilities from writeLine
     */
    protected void writeFile(PrintWriter out, Path inputFile) throws IOException {
        if (Files.isRegularFile(inputFile) && !inputFile.toString().contains("NOEVENTS")) { // Check file for correctness
            try (Stream<String> lines = Files.lines(inputFile)) {                           // Get all lines in file
                writeLines(out, lines);
            }
        }
    }

    /**
     * Writes lines to the output, removing unneeded lines and columns, converting to CSV
     * @param out   Where the output goes
     * @param lines The input
     */
    protected void writeLines(PrintWriter out, Stream<String> lines) {
        Iterator<String> i = lines.iterator();                      // Get iterator for lines
        while (i.hasNext()) {                                       // Loop over all lines
            String line = i.next().trim();                          // Get next line
            if (!line.contains("*") && !line.contains("SORS")) {    // Check line for correctness
                writeLine(out, line);                               // Write line to output file
            }
        }
    }

    /**
     * Removes unnecessary fields, converts line to CSV, writes line to output.
     * <p>
     * Only columns A, D, E, G, H & I are needed. They are as follows:
     * <ol>
     *  <li>A (#0) - Registrant DEA</li>
     *  <li>D (#3) - NDC</li>
     *  <li>E (#4) - Order Quantity</li>
     *  <li>G (#6) - Customer DEA</li>
     *  <li>H (#7) - DEA Form</li>
     *  <li>I (#8) - Transaction Date</li>
     * </ol>
     * </p>
     * @param line The raw input line
     */
    protected void writeLine(PrintWriter out, String line) {
        String[] fields = line.split("\\|");
        out.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n", fields[0], fields[3], fields[4], fields[6], fields[7], fields[8]);
    }

    protected ProcessBuilder appendToPath(ProcessBuilder pb, @SuppressWarnings("SameParameterValue") String dir) {
        Map<String, String> env = pb.environment();
        // Windows Path key could be any variation of letter case. But the Map.get method is case-sensitive.
        // UNIX path could have multiple variables under different letter cases.
        @SuppressWarnings("SimplifyStreamApiCallChains")
        String pathKey = env.keySet()
        		.stream()
        		.filter(e -> e.equalsIgnoreCase("path"))
        		.sorted()
        		.collect(toList())  // Keeping this form for compatibility with old Eclipse running with Java 8
        		.get(0);
        String pathValue = env.get(pathKey);
        String fixedPathValue = pathValue + File.pathSeparator + dir;
        env.put(pathKey, fixedPathValue);
        return pb;
    }

    protected int load(String fileToLoad) throws IOException, InterruptedException {
        // TODO - Switch to use Java API instead of calling out to "bq" command
        // TODO - Check if user is authenticated and if not run: gcloud auth login
        System.out.printf("Loading data from file: %s%n", fileToLoad);
        // References:
        // https://cloud.google.com/bigquery/docs/batch-loading-data#bq
        // https://cloud.google.com/bigquery/docs/reference/bq-cli-reference#bq_load
        ProcessBuilder pb = new ProcessBuilder(
                "cmd.exe",    // Needed to run bq because bq is a command file, bq.cmd, not an executable
                "/c",                    // Specifies that following params are interpreted as a command by cmd.exe
                "bq",                    // Command to be run by cmd.exe, which is found in the Path
                "load",                  // bq is to run its "load" command
                "--source_format=CSV",   // The format of the file to be loaded is CSV
                "--replace=true",        // Delete target table before loading
                "--skip_leading_rows=1", // Skip over first row of source file, which is headers, not data
                TABLE_TO_LOAD,           // The full name of the table where the data is to be loaded
                fileToLoad,              // The source file
                "registrant_dea:STRING," +           // Name and type of 1st field in source file & target table
                        "ndc:STRING," +              // Name and type of 2nd field in source file & target table
                        "order_quantity:INTEGER," +  // Name and type of 3rd field in source file & target table
                        "customer_dea:STRING," +     // Name and type of 4th field in source file & target table
                        "dea_form:STRING," +         // Name and type of 5th field in source file & target table
                        "transaction_date:STRING"    // Name and type of 6th field in source file & target table
        );
        // GCP_SDK_BIN must be in the path so that the "bq" command can be found
        return appendToPath(pb, GCP_SDK_BIN).inheritIO().start().waitFor();
    }
}
