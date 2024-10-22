import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
    
public class ASTLoader {
    private Map<Integer, Node> nodeMap = new HashMap<>();
// Load the AST from an XML file
public Node loadASTFromXML(String inputFileName) throws Exception {
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(new File(inputFileName));

    //  Create and store the root node
    Element rootElement = (Element) doc.getElementsByTagName("ROOT").item(0);
    String rootUnidText = rootElement.getElementsByTagName("UNID").item(0).getTextContent().trim();
    int rootUnid = Integer.parseInt(rootUnidText);
    String rootSymb = rootElement.getElementsByTagName("SYMB").item(0).getTextContent().trim();
    
    Node rootNode = new Node(rootUnid, rootSymb);
    nodeMap.put(rootUnid, rootNode);
    System.out.println("Node added: UNID=" + rootUnid + ", SYMB=" + rootSymb);

    // Process INNER NODES
    NodeList inNodes = doc.getElementsByTagName("IN");
    for (int i = 0; i < inNodes.getLength(); i++) {
        Element inElement = (Element) inNodes.item(i);
        String unidText = inElement.getElementsByTagName("UNID").item(0).getTextContent().trim();
        int unid = Integer.parseInt(unidText);

        String symb = inElement.getElementsByTagName("SYMB").item(0).getTextContent().trim();
        Node node = new Node(unid, symb);
        nodeMap.put(unid, node);
        System.out.println("Added inner node: UNID=" + unid + ", SYMB=" + symb);
    }

    //  Process LEAF NODES
    NodeList leafNodes = doc.getElementsByTagName("LEAF");
    for (int i = 0; i < leafNodes.getLength(); i++) {
        Element leafElement = (Element) leafNodes.item(i);
        String unidText = leafElement.getElementsByTagName("UNID").item(0).getTextContent().trim();
        int unid = Integer.parseInt(unidText);

        String symb = leafElement.getElementsByTagName("TERMINAL").item(0).getTextContent().trim();
        Node leafNode = new Node(unid, symb);
        nodeMap.put(unid, leafNode);
        System.out.println("Added leaf node: UNID=" + unid + ", SYMB=" + symb);
    }

    // Link children to their parents
    NodeList childrenElements = doc.getElementsByTagName("CHILDREN");
    for (int i = 0; i < childrenElements.getLength(); i++) {
        Element childrenElement = (Element) childrenElements.item(i);
        // Get the parent UNID from the element that contains the CHILDREN
        Element parentElement = (Element) childrenElement.getParentNode();
        String parentUnidText = parentElement.getElementsByTagName("UNID").item(0).getTextContent().trim();
        int parentUnid = Integer.parseInt(parentUnidText);
        Node parentNode = nodeMap.get(parentUnid);
        if (parentNode == null) {
            throw new IllegalStateException("Parent node with UNID " + parentUnid + " not found in the node map");
        }

        NodeList childIdList = childrenElement.getElementsByTagName("ID");
        for (int j = 0; j < childIdList.getLength(); j++) {
            String childUnidText = childIdList.item(j).getTextContent().trim();
            int childUnid = Integer.parseInt(childUnidText);
            Node childNode = nodeMap.get(childUnid);
            if (childNode == null) {
                throw new IllegalStateException("Child node with UNID " + childUnid + " not found in the node map");
            }
            parentNode.addChild(childNode);
            System.out.println("Linked child node: PARENT_UNID=" + parentUnid + ", CHILD_UNID=" + childUnid);
        }
    }

    return rootNode; 
}

}
