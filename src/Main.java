
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                System.out.println("Usage: java -jar sors-dea-files-combiner.jar <input-file-dir> <output-file-name>");
                System.exit(1);
            }
            Path home = Path.of(System.getProperty("user.home"));
            Path inputFileDir = home.resolve(args[0]);
            Path outputFile = home.resolve(args[1]);
            try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(outputFile))) {                      // Open output file
                try (DirectoryStream<Path> inputFiles = Files.newDirectoryStream(inputFileDir)) {               // Get all input files
                    for (Path inputFile : inputFiles) {                                                         // Loop over all input files
                        if (Files.isRegularFile(inputFile) && !inputFile.toString().contains("NOEVENTS")) {     // Check file for correctness
                            try (Stream<String> lines = Files.lines(inputFile)) {                               // Get all lines in file
                                Iterator<String> i = lines.iterator();                                          // Get lines iterator
                                while (i.hasNext()) {                                                           // Loop over all lines
                                    String line = i.next().trim();                                              // Get next line
                                    if (!line.contains("*") && !line.contains("SORS")) {                        // Check line for correctness
                                        out.println(line);                                                      // Write line to output file
                                    }
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("SORS job complete!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
