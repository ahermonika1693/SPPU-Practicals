package macro;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

class PassTwo {

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

    public static void main(String[] args) {
        try {
            PassTwo pass = new PassTwo();
            pass.passTwo();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    LinkedHashMap<String, Param> mntTable = new LinkedHashMap<>();
    LinkedHashMap<String, Param> alaTable = new LinkedHashMap<>();
    ArrayList<String> mdtLines = new ArrayList();

    PassTwo() throws IOException {
        init();
    }

    private void init() throws FileNotFoundException, IOException {
        FileReader mntFile = new FileReader("mnt.txt");
        BufferedReader mntBf = new BufferedReader(mntFile);

        FileReader mdtFile = new FileReader("mdt.txt");
        BufferedReader mdtBf = new BufferedReader(mdtFile);

        FileReader alaFile = new FileReader("ala.txt");
        BufferedReader alaBf = new BufferedReader(alaFile);

        String currentLine;
        String[] line;

        while ((currentLine = mntBf.readLine()) != null) {
            line = currentLine.split("\\s+");
            mntTable.put(line[1], new Param(Integer.parseInt(line[0]), Integer.parseInt(line[2])));
        }

        while ((currentLine = alaBf.readLine()) != null) {
            line = currentLine.split("\\s+");
            if (line.length > 2) {
                alaTable.put(line[1], new Param(Integer.parseInt(line[0]), line[2]));
            } else {
                alaTable.put(line[1], new Param(Integer.parseInt(line[0]), ""));
            }
        }

        while ((currentLine = mdtBf.readLine()) != null) {
            mdtLines.add(currentLine);
        }

        mntBf.close();
        mdtBf.close();
        alaBf.close();
    }

    private void putArgInAla(String key, String value) {
        key = key.replaceAll("[&, ]", "");
        value = value.replaceAll("[, ]", "");
        if (alaTable.containsKey(key)) {
            Param obj = alaTable.get(key);
            String val = "";
            if (!value.isEmpty()) {
                val = value;
            } else {
                val = obj.value;
            }
            alaTable.put(key, new Param(obj.index, val));
        }
    }

    private void handleArguments(String[] line, String[] mdtLine, int startIndex) {
        for (int i = startIndex; i < line.length && i < mdtLine.length; i++) {
            String currentArg = mdtLine[i];

            if (line[i].contains("&")) {
                currentArg = line[i];
            }

            // check for default or keyword argument
            if (currentArg.contains("=")) {
                String[] temp = line[i].split("=");
                if (temp.length > 1) {
                    putArgInAla(temp[0], temp[1]);
                } else {
                    putArgInAla(temp[0], "");
                }
            } else {
                putArgInAla(currentArg, line[i]);
            }
        }
    }

    public String getAlaValue(String key) {
        key = key.replaceAll("[&#, ]", "");
        int index = Integer.parseInt(key);

        for (Map.Entry<String, Param> m : alaTable.entrySet()) {
            Param obj = m.getValue();
            if (obj.index == index) {
                return obj.value;
            }
        }

        return "";
    }

    public void passTwo() throws FileNotFoundException, IOException {
        FileReader inputFile = new FileReader("output.txt");
        BufferedReader inputBf = new BufferedReader(inputFile);

        FileWriter expandedFile = new FileWriter("expanded.txt");

        String currentLine;
        String[] line;

        boolean isMacroPresent = false;
        boolean isLabelPresent = false;
        boolean isFirstLine = true;

        int mdtIndex = -1;

        while ((currentLine = inputBf.readLine()) != null) {
            line = currentLine.split("\\s+");

            // check for label
            if (mntTable.containsKey(line[0])) {
                isLabelPresent = false;
                isMacroPresent = true;
                mdtIndex = mntTable.get(line[0]).mdtIndex;
            }

            if (line.length > 1 && mntTable.containsKey(line[1])) {
                isLabelPresent = true;
                isMacroPresent = true;
                mdtIndex = mntTable.get(line[1]).mdtIndex;
            }

            if (!isMacroPresent) {
                expandedFile.write(currentLine + "\n");
                continue;
            }

            isFirstLine = true;

            // since using array
            mdtIndex -= 1;

            while ((mdtIndex < mdtLines.size()) && !mdtLines.get(mdtIndex).equals("MEND")) {
                String[] mdtLine = mdtLines.get(mdtIndex).split("\\s+");

                if (isFirstLine) {
                    if (isLabelPresent) {
                        putArgInAla(mdtLine[0], line[0]);
                        handleArguments(line, mdtLine, 2);
                    } else {
                        handleArguments(line, mdtLine, 1);
                    }
                } else {
                    // lines other than macro definition
                    for (int i = 0; i < mdtLine.length; i++) {
                        if (mdtLine[i].contains("#")) {
                            mdtLine[i] = getAlaValue(mdtLine[i]);
                        }
                    }
                }

                if (!isFirstLine) {
                    for (int i = 0; i < mdtLine.length; i++) {
                        if (!mdtLine[i].equals("MEND")) {
                            expandedFile.write(mdtLine[i] + "\t");
                        }
                    }
                    expandedFile.write("\n");
                }

                mdtIndex++;
                isFirstLine = false;
            }
        }

        expandedFile.close();
        inputFile.close();
    }
}
