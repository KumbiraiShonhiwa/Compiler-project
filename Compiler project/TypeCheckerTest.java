// public class TypeCheckerTest {

//     public static void main(String[] args) {
//         try {
//             // load the tree from the xml file
//             ASTLoader astLoader = new ASTLoader();
//             Node rootNode = astLoader.loadASTFromXML("syntax_tree.xml");

//             // initialize Symbol Table
//             SymbolTable symbolTable = new SymbolTable();

//             // type-checking
//             RecSPLTypeChecker typeChecker = new RecSPLTypeChecker(symbolTable);
//             boolean isTypeCorrect = typeChecker.typecheck(rootNode);

//             // displays tyoe checking results
//             if (isTypeCorrect) {
//                 System.out.println("The program is type-correct.");
//             } else {
//                 System.out.println("The program has type errors.");
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
// }
