package macro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

class Param {
    int index;
    int mdtIndex;
    String value;

    Param(int index, int mdtIndex) {
        this.index = index;
        this.mdtIndex = mdtIndex;
    }

    Param(int index, String value) {
        this.index = index;
        this.value = value;
    }
}

public class PassOne {

    static final String INPUT = "input.txt";

    public static void main(String[] args) {
        PassOne pass = new PassOne();
        try {
            pass.passOne();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    LinkedHashMap<String, Param> mntTable = new LinkedHashMap<>();
    LinkedHashMap<String, Param> alaTable = new LinkedHashMap<>();
    LinkedHashMap<Integer, String> mdtTable = new LinkedHashMap<>();

    public void passOne() throws FileNotFoundException, IOException {
        FileReader inputFile = new FileReader(INPUT);
        BufferedReader inputBf = new BufferedReader(inputFile);

        FileWriter outputFile = new FileWriter("./output.txt");

        String currentLine;
        String[] line;

        boolean isMacroPresent = false;
        boolean isLabelPresent = false;

        int index = 0;
        int mntIndex = 1;
        int mdtIndex = 1;
        int alaIndex = 1;

        while ((currentLine = inputBf.readLine()) != null) {
            line = currentLine.split("\\s+");

            // check for macro
            if (line[0].equals("MACRO")) {
                currentLine = inputBf.readLine();
                line = currentLine.split("\\s+");

                isMacroPresent = true;
                String macroName = "";

                // check for label
                if (line[0].contains("&")) {
                    isLabelPresent = true;
                    macroName = line[1];
                    line[0] = line[0].replaceAll("[&, ]", "");
                    alaTable.put(line[0], new Param(alaIndex++, ""));
                    index = 2;
                }

                if (!isLabelPresent) {
                    macroName = line[0];
                    index = 1;
                }

                // enter into mnt table
                mntTable.put(macroName, new Param(mntIndex++, mdtIndex));

                // processes the arguments
                for (int i = index; i < line.length; i++) {
                    // check for argument
                    if (line[i].contains("&")) {
                        line[i] = line[i].replaceAll("[&, ]", "");
                        // check for default or keyword argument
                        if (line[i].contains("=")) {
                            String[] temp = line[i].split("=");
                            if (temp.length > 1) {
                                // default argument
                                alaTable.put(temp[0], new Param(alaIndex++, temp[1]));
                            } else {
                                // keyword argument
                                alaTable.put(temp[0], new Param(alaIndex++, ""));
                            }
                        } else {
                            // regular argument
                            alaTable.put(line[i], new Param(alaIndex++, ""));
                        }
                    }
                }
                // enter into mdt
                mdtTable.put(mdtIndex++, currentLine);
            } else if (line[0].equals("MEND")) {
                isMacroPresent = false;
                isLabelPresent = false;
                mdtTable.put(mdtIndex++, line[0]);
            } else if (isMacroPresent) {
                String temp = "";
                for (int i = 0; i < line.length; i++) {
                    if (line[i].contains("&")) {
                        line[i] = line[i].replaceAll("[&, ]", "");
                        line[i] = "#" + alaTable.get(line[i]).index;
                    }
                    temp += line[i] + "\t";
                }
                mdtTable.put(mdtIndex++, temp);
            } else {
                outputFile.write(currentLine + "\n");
            }
        }

        System.out.println("--------------- MNT Table -----------------");
        FileWriter mntFile = new FileWriter("mnt.txt");
        for (Map.Entry<String, Param> m : mntTable.entrySet()) {
            Param obj = m.getValue();
            System.out.println(obj.index + "\t" + m.getKey() + "\t" + obj.mdtIndex);
            mntFile.write(obj.index + "\t" + m.getKey() + "\t" + obj.mdtIndex + "\n");
        }
        mntFile.close();

        System.out.println("--------------- ALA Table -----------------");
        FileWriter alaFile = new FileWriter("ala.txt");
        for (Map.Entry<String, Param> m : alaTable.entrySet()) {
            Param obj = m.getValue();
            System.out.println(obj.index + "\t" + m.getKey() + "\t" + obj.value);
            alaFile.write(obj.index + "\t" + m.getKey() + "\t" + obj.value + "\n");
        }
        alaFile.close();

        System.out.println("--------------- MDT Table -----------------");
        FileWriter mdtFile = new FileWriter("mdt.txt");
        for (Map.Entry<Integer, String> m : mdtTable.entrySet()) {
            System.out.println(m.getValue());
            mdtFile.write(m.getValue() + "\n");
        }

        mdtFile.close();
        inputFile.close();
        outputFile.close();
    }
}