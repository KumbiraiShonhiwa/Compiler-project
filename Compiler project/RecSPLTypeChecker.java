import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RecSPLTypeChecker {

    private Map<Integer, Node> nodeMap;
    private SymbolTable symbolTable;

    public RecSPLTypeChecker(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.nodeMap = new HashMap<>();
    }

    public boolean typecheck(String inputFileName) {
        try {
            loadASTFromXML(inputFileName);
            Node rootNode = nodeMap.values().stream()
                                      .filter(node -> node.getSymbol().equals("PROG"))
                                      .findFirst().orElse(null);
            if (rootNode == null) {
                System.err.println("No valid root node found.");
                return false;
            }
            return typecheckNode(rootNode);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadASTFromXML(String inputFileName) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File(inputFileName));

        NodeList inNodes = doc.getElementsByTagName("IN");
        for (int i = 0; i < inNodes.getLength(); i++) {
            Element inElement = (Element) inNodes.item(i);
            int unid = Integer.parseInt(inElement.getElementsByTagName("UNID").item(0).getTextContent());
            String symb = inElement.getElementsByTagName("SYMB").item(0).getTextContent();
            Node node = new Node(unid, symb);
            nodeMap.put(unid, node);
        }

        NodeList leafNodes = doc.getElementsByTagName("LEAF");
        for (int i = 0; i < leafNodes.getLength(); i++) {
            Element leafElement = (Element) leafNodes.item(i);
            int unid = Integer.parseInt(leafElement.getElementsByTagName("UNID").item(0).getTextContent());
            String tokenSymbol = leafElement.getElementsByTagName("TERMINAL").item(0).getTextContent();
            Node leafNode = new Node(unid, tokenSymbol);
            nodeMap.put(unid, leafNode);
        }

        NodeList parentNodes = doc.getElementsByTagName("CHILDREN");
        for (int i = 0; i < parentNodes.getLength(); i++) {
            Element childrenElement = (Element) parentNodes.item(i);
            int parentUnid = Integer.parseInt(childrenElement.getParentNode().getFirstChild().getTextContent());
            Node parentNode = nodeMap.get(parentUnid);

            NodeList childIdList = childrenElement.getElementsByTagName("ID");
            for (int j = 0; j < childIdList.getLength(); j++) {
                int childUnid = Integer.parseInt(childIdList.item(j).getTextContent());
                parentNode.addChild(childUnid); // Add child ID instead of Node object
            }
        }
    }

    private boolean typecheckNode(Node node) {
        // Recursive type-checking logic based on node.getSymbol()
        if (node.getSymbol().equals("PROG")) {
            for (int childId : node.getChildren()) { // Iterate over child IDs
                Node childNode = nodeMap.get(childId); // Get the child node
                if (childNode != null && !typecheckNode(childNode)) { // Null check
                    return false;
                }
            }
        }
        // Handle other node types similarly
        return true;
    }
}
