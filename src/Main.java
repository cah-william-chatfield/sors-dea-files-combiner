
import java.io.*;

public class Main {
    public static final File HOME = new File(System.getProperty("user.home"));
    public static final File INPUT_FILE_DIR = new File(HOME,"Downloads\\SORS-2023");
    public static final File OUTPUT_FILE = new File(HOME,"Downloads\\SORS-2023.txt");

    public static File[] getFilesInDirectory(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("Not a directory or I/O error: " + dir);
        }
        return files;
    }

    public static void main(String[] args) {
        try {
            try (PrintWriter out = new PrintWriter(new FileWriter(OUTPUT_FILE))) {
                File[] inputFiles = getFilesInDirectory(INPUT_FILE_DIR);
                for (File inputFile : inputFiles) {
                    if (inputFile.isFile() && !inputFile.getName().contains("NOEVENTS")) {
                        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                String trimmedLine = line.trim();
                                if (!trimmedLine.contains("*") && !trimmedLine.contains("SORS")) {
                                    out.println(trimmedLine);
                                }
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
