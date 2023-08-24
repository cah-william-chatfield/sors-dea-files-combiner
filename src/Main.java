

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        // TODO Auto-generated method stub
        BufferedReader br = null;
        FileReader fr = null;

        try {
            File[] files = new File("C:\\Reports\\SORS_Report\\ApriltoJune2020\\FileFTP").listFiles();
            List<String> fileList = new ArrayList<String>();

            for (File file : files) {
                //System.out.println(file.getName());
                if (file.isFile() && !file.getName().contains("NOEVENTS")) {
                    fileList.add(file.getName());
                }
            }
            String currentLine;
            for (int i = 0; i < fileList.size(); i++) {
                System.out.println(i+1 + " : "+ fileList.size());
                BufferedReader extractedFileReader = new BufferedReader(
                        new FileReader("C:\\Reports\\SORS_Report\\ApriltoJune2020\\FileFTP\\" + fileList.get(i)));
                BufferedWriter tempFileWriter = new BufferedWriter(
                        new FileWriter("C:\\Reports\\SORS_Report\\ApriltoJune2020\\TempFilesFTP\\" + fileList.get(i)));
                String lineToRemove1 = "*";
                String lineToRemove2 = "SORS";
                //System.out.println(results.get(i));
                while ((currentLine = extractedFileReader.readLine()) != null) {
                    String trimmedLine = currentLine.trim();
                    if (trimmedLine.contains(lineToRemove1) || trimmedLine.contains(lineToRemove2))
                        continue;
							/*String txnDateYear = trimmedLine.substring(53, 57);
							if(txnDateYear.equals("2018"))
								continue;
							String txnDate = trimmedLine.substring(49, 53);
							if (txnDate.equals("0403")||txnDate.equals("0404")||txnDate.equals("0405")||txnDate.equals("0406")||txnDate.equals("0407"))
								continue;*/
                    tempFileWriter.write(currentLine + System.getProperty("line.separator"));
                    //System.out.println(currentLine);
                    // for appending the file
                    File finalTextFile = new File("C:\\Reports\\SORS_Report\\ApriltoJune2020\\ApriltoJune2020FTPRun.txt");
                    FileWriter fWriter = new FileWriter(finalTextFile, true);
                    BufferedWriter bWriter = new BufferedWriter(fWriter);
                    PrintWriter pWriter = new PrintWriter(bWriter);
                    pWriter.print(currentLine + System.getProperty("line.separator"));
                    pWriter.close();
                }

                tempFileWriter.close();
                extractedFileReader.close();

            }

            System.out.println("SORS Job Complete!");

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                if (br != null)
                    br.close();

                if (fr != null)
                    fr.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }

        }

    }

}
