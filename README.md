Compiler Project
Overview
This project is a compiler that processes input files written in a specific programming language and generates BASIC code as output. The compiler performs lexing, parsing, semantic analysis, type checking, and code generation.

Requirements
Java Development Kit (JDK) 8 or higher installed on your system.
Ensure that your input files are formatted correctly as per the specifications of the compiler.
Building the Project
Clone the repository or download the project files to your local machine.

Open a terminal or command prompt and navigate to the project directory.

Compile the Java files:


javac *.java
Create the JAR file:


jar cfm CompilerProject.jar MANIFEST.MF *.class
Running the Compiler
To run the compiler, use the following command:

java -jar CompilerProject.jar <inputFile> <outputFile>

Parameters
<inputFile>: The path to the input file (e.g., input.txt) containing the source code to be compiled.
<outputFile>: The path to the output file (e.g., output.txt) where the generated BASIC code will be written.
Example

java -jar CompilerProject.jar input.txt output.txt
This command will read input.txt, process it through the compiler, and write the output to output.txt.

Please enure that you have the following xml files (empty) in the folder directory to output the tokens xml and syntax tree xml.

Output
The compiler will generate an output file containing the translated BASIC code based on the input provided.
If additional outputs are generated (such as a syntax tree or tokens), they will be handled as specified in the code.
Error Handling
If the program encounters any issues, error messages will be displayed in the terminal, indicating what went wrong. Ensure that your input file follows the expected syntax and formatting.

Contribution
Feel free to contribute to the project by submitting issues or pull requests. Your feedback and suggestions are welcome!

License
This project is licensed under the MIT License - see the LICENSE file for details.

You can add sections for more details like features, known issues, or additional usage examples as necessary. Let me know if you need further modifications or additional sections!