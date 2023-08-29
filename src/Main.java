import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Creates one combined CSV file from all the SORS DEA data files.
 * @author william.chatfield
 */
public class Main {

    public static final String SDK_BIN = "C:\\Program Files (x86)\\Google\\Cloud SDK\\google-cloud-sdk\\bin";

    /**
     * Starts non-static context, checks command line arguments, handles exceptions
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length >= 2) {
                String outputFileName = "Combined DEA Data.csv";
                Main loader = new Main();
                loader.combineFiles(outputFileName, args);
                int exitCode = loader.load(outputFileName);
                if (exitCode == 0) {
                    System.out.println("SORS job complete!");
                } else {
                    System.err.println("SORS job failed. You may need to login using this command: gcloud auth login");
                }
                System.exit(exitCode);
            } else {
                System.out.println("Usage: java -jar sors-dea-files-combiner.jar <input-file-dir> ...");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Combines files into one CSV file
     * @param inputFileDirs Directory containing input files
     * @param outputFile    Where the output should go
     * @throws IOException  If directory can't be opened and other possibilities from writeFile
     */
    protected void combineFiles(String outputFile, String[] inputFileDirs) throws IOException {
        System.out.printf("Combining files from directories %s into file %s%n", String.join(",", inputFileDirs), outputFile);
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(Path.of(outputFile)))) {        // Open output file
            out.println("registrant_dea,ndc,order_quantity,customer_dea,dea_form,transaction_date");   // Write headers
            for (String inputFileDir : inputFileDirs) {
                try (DirectoryStream<Path> inputFiles = Files.newDirectoryStream(Path.of(inputFileDir))) { // Get all input files
                    for (Path inputFile : inputFiles) {                                                    // Loop over all input files
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

    protected ProcessBuilder appendToPath(ProcessBuilder pb, String dir) {
        Map<String, String> env = pb.environment();
        // Windows Path key could be any variation of letter case. But the Map.get method is case-sensitive.
        // UNIX path could have multiple variables under different letter cases.
        String pathKey = env.keySet().stream().filter(e -> e.equalsIgnoreCase("path")).sorted().toList().get(0);
        String pathValue = env.get(pathKey);
        String fixedPathValue = pathValue + File.pathSeparator + dir;
        env.put(pathKey, fixedPathValue);
        return pb;
    }

    protected int load(String fileToLoad) throws IOException, InterruptedException {
        // TODO - Check if user is authenticated and if not run: gcloud auth login
        System.out.printf("Loading data from file: %s%n", fileToLoad);
        ProcessBuilder pb = new ProcessBuilder("cmd", "/c",
                "bq", "load", "--source_format=CSV", "--replace=true",
                "--skip_leading_rows=1", "edna-rsh-pqra-pr-cah:INJUNCTIVE_RELIEF.SORS_DEA_Transaction",
                fileToLoad,
                "registrant_dea:STRING,ndc:STRING,order_quantity:INTEGER,customer_dea:STRING," +
                        "dea_form:STRING,transaction_date:STRING"
        );
        return appendToPath(pb, SDK_BIN).inheritIO().start().waitFor();
    }
}
