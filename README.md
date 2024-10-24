Parser Project README
Overview
This project implements a parser that processes a structured input file (e.g., input8.txt), performs lexical analysis, syntax parsing, semantic analysis, type checking, and code generation, and outputs tokens, a syntax tree, and final translated code.

The project is structured around the following key steps:

Lexing: Converts the input file into tokens.
Parsing: Generates a syntax tree from the tokens.
Semantic Analysis: Checks for function and variable consistency.
Type Checking: Ensures type correctness of the parsed program.
Code Generation: Outputs final translated code and converts it into BASIC format.
Prerequisites
Before running the project, make sure you have:

Java Development Kit (JDK) installed.
A correctly structured input file (e.g., input8.txt) in the specified directory.
Project Structure
Input File:

Path to the input file: Compiler project//input8.txt. Update this path based on your project structure.
The input file contains the source code to be lexed, parsed, and processed by the compiler components.
Output Files:

tokens_output.xml: This file will contain the generated tokens in XML format.
syntax_tree.xml: This file will contain the parsed syntax tree in XML format.
output.txt: Intermediate output generated from the code.
basic_output.bas: Final BASIC translated code.
How to Run
Set up the Input File: Ensure that your input file (e.g., input8.txt) is located in the specified directory Compiler project. Adjust the path in the code if necessary.

Run the code using VS Code the command used was machine specific but we will provide the command:

c:; cd 'c:\Users\Kumbirai\OneDrive\Documents\GitHub\Compiler-project'; & 'C:\Program Files\Eclipse Adoptium\jdk-17.0.12.7-hotspot\bin\java.exe' '-XX:+ShowCodeDetailsInExceptionMessages' '-cp' 'C:\Users\Kumbirai\AppData\Roaming\Code\User\workspaceStorage\9221b6f5151aa2cec215095f87fb4bbe\redhat.java\jdt_ws\Compiler-project_301c5eb5\bin' 'MainClass' 

Lexing: Tokenizes the input file and outputs the tokens into tokens_output.xml.
Parsing: Builds a syntax tree and outputs it into syntax_tree.xml.
Semantic Analysis: Checks for consistency in functions and variables.
Type Checking: Verifies type correctness.
Code Generation: Generates the final code and converts it into BASIC format (basic_output.bas).
Review the Output:

Syntax Tree: Open syntax_tree.xml to review the generated syntax tree.
Translated Code: Open basic_output.bas for the final BASIC code output.
Error Handling
If the program encounters any issues during execution, an error message will be printed in the console with relevant details. Ensure that the input file is properly formatted and the paths are correctly set.

Modifications
Adjust Input/Output Paths: Modify the inputFile, xmlOutputFile, and xmlOutputFileSyntaxTree variables in the code to point to different files as needed.

Troubleshooting
File Not Found: Ensure that the input8.txt file exists in the specified location.
Lexical Errors: if the lexer encounters invalid tokens
Syntax Errors: If parsing or semantic analysis fails.
Type Checking Failures: The type checker will print a failure message if any type errors are detected.
Contact
If you encounter any issues or have any questions, feel free to reach out via the project repository or email.