import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class ConvertToBasic {

    private HashMap<String, String> symbolTable;  // To track string variables (text-type variables)
    private int lineNumber;  // To keep track of the line numbers in the BASIC code
    private static final int MAX_STACK_SIZE = 30;  // For simulating the stack

    public ConvertToBasic() {
        this.symbolTable = new HashMap<>();
        this.lineNumber = 10;  // Start line numbering from 10
    }

    // Function to read from the output.txt file and convert to BASIC code
    public void convertToBasic(String inputFilePath, String outputFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             FileWriter fw = new FileWriter(outputFilePath)) {

            // Write the stack declaration as the first line of BASIC code
            fw.write(lineNumber + " DIM M(7," + MAX_STACK_SIZE + ")\n");
            lineNumber += 10;

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    continue;  // Skip empty lines
                }

                String basicCode = translateToBasic(line);
                if (basicCode != null) {
                    fw.write(basicCode + "\n");
                }
            }

            System.out.println("Conversion to BASIC completed successfully. Output written to " + outputFilePath);

        } catch (IOException e) {
            System.err.println("Error reading/writing files: " + e.getMessage());
        }
    }

    // Function to translate each line to BASIC using pattern matching
    private String translateToBasic(String line) {
        String basicLine = null;

        // Example patterns (based on the provided document):
        if (line.startsWith("IF")) {
            basicLine = translateIfStatement(line);
        } else if (line.contains(":=")) {
            basicLine = translateAssignment(line);
        } else if (line.startsWith("LABEL")) {
            basicLine = translateLabel(line);
        } else if (line.startsWith("GOTO")) {
            basicLine = translateGoto(line);
        } else if (line.startsWith("CALL")) {
            basicLine = translateFunctionCall(line);
        } else if (line.startsWith("RETURN")) {
            basicLine = lineNumber + " RETURN";  // Direct translation of RETURN
            lineNumber += 10;
        }

        return basicLine;
    }

    // Translate assignment statements (x := y)
    private String translateAssignment(String line) {
        String[] parts = line.split(":=");
        if (parts.length == 2) {
            String left = parts[0].trim();
            String right = parts[1].trim();

            // Handle string variables (replace with BASIC-style string variables if necessary)
            if (isStringVariable(left)) {
                left = mapStringVariable(left);
            }

            if (isStringVariable(right)) {
                right = mapStringVariable(right);
            }

            return lineNumber + " LET " + left + " = " + right;
        }
        return null;
    }

    // Translate IF statements
    private String translateIfStatement(String line) {
        // Example format: "IF cond THEN LabelName"
        if (line.contains("THEN")) {
            String[] parts = line.split("THEN");
            String condition = parts[0].replace("IF", "").trim();
            String label = parts[1].trim();

            // Add line number
            String basicLine = lineNumber + " IF " + condition + " THEN " + (lineNumber + 10);
            lineNumber += 10;
            return basicLine;
        }
        return null;
    }

    // Translate LABEL statements
    private String translateLabel(String line) {
        // Example: "LABEL SomeLabel"
        String labelName = line.replace("LABEL", "").trim();
        return lineNumber + " REM " + labelName;
    }

    // Translate GOTO statements
    private String translateGoto(String line) {
        // Example: "GOTO SomeLabel"
        String labelName = line.replace("GOTO", "").trim();
        return lineNumber + " GOTO " + (lineNumber + 10);  // Map label to its corresponding line number
    }

    // Translate function calls (CALL SomeFunction)
    private String translateFunctionCall(String line) {
        // Example: "CALL SomeFunction"
        String functionName = line.replace("CALL", "").trim();
        return lineNumber + " GOSUB " + (lineNumber + 10);  // Map function to its corresponding line number
    }

    // Helper function to check if a variable is a string variable
    private boolean isStringVariable(String variable) {
        // You can use your symbol table here to check for string variables
        // For now, let's assume all variables ending with $ are strings
        return symbolTable.containsKey(variable) || variable.endsWith("$");
    }

    // Helper function to map string variables to BASIC string variables
    private String mapStringVariable(String variable) {
        if (!symbolTable.containsKey(variable)) {
            String basicVar = generateBasicStringVariable();
            symbolTable.put(variable, basicVar);
        }
        return symbolTable.get(variable);
    }

    // Generate a BASIC-style string variable (A$, B$, ..., Z$)
    private String generateBasicStringVariable() {
        char variableName = (char) ('A' + symbolTable.size());  // Generate A$, B$, etc.
        return variableName + "$";
    }

    public static void main(String[] args) {
        ConvertToBasic converter = new ConvertToBasic();
        converter.convertToBasic("output.txt", "basic_output.bas");
    }
}
