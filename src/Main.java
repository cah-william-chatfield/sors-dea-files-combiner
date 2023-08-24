
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Main {
    public static final Path HOME = Path.of(System.getProperty("user.home"));
    public static final Path INPUT_FILE_DIR = HOME.resolve("Downloads\\SORS-2023");
    public static final Path OUTPUT_FILE = HOME.resolve("Downloads\\SORS-2023.txt");

    public static void main(String[] args) {
        try {
            try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(OUTPUT_FILE))) {
                try (DirectoryStream<Path> inputFiles = Files.newDirectoryStream(INPUT_FILE_DIR)) {
                    for (Path inputFile : inputFiles) {
                        if (Files.isRegularFile(inputFile) && !inputFile.toString().contains("NOEVENTS")) {
                            try (Stream<String> lines = Files.lines(inputFile)) {
                                lines.forEach(line -> {
                                    String trimmedLine = line.trim();
                                    if (!trimmedLine.contains("*") && !trimmedLine.contains("SORS")) {
                                        out.println(trimmedLine);
                                    }
                                });
                            }
                        }
                    }
                }
                System.out.println("SORS job complete!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
