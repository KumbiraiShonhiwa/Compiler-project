
import java.util.ArrayList;
import java.util.List;
public class SyntaxTree {

    Node root;
    List<Node> innerNodes = new ArrayList<>();
    List<LeafNode> leafNodes = new ArrayList<>();

    public SyntaxTree(Node root) {
        this.root = root;
    }

    public void addInnerNode(Node node) {
        innerNodes.add(node);
    }

    public void addLeafNode(LeafNode leaf) {
        leafNodes.add(leaf);
    }

    public String toXML() {
        StringBuilder xml = new StringBuilder("<SYNTREE>\n");

        // Root Node
        xml.append("<ROOT>\n");
        xml.append("<UNID>").append(root.unid).append("</UNID>\n");
        xml.append("<SYMB>").append(root.symbol).append("</SYMB>\n");
        xml.append("<CHILDREN>\n");
        for (int childId : root.children) {
            xml.append("<ID>").append(childId).append("</ID>\n");
        }
        xml.append("</CHILDREN>\n");
        xml.append("</ROOT>\n");

        // Inner Nodes
        xml.append("<INNERNODES>\n");
        for (Node node : innerNodes) {
            xml.append(node.toXML());
        }
        xml.append("</INNERNODES>\n");

        // Leaf Nodes
        xml.append("<LEAFNODES>\n");
        for (LeafNode leaf : leafNodes) {
            xml.append(leaf.toXML());
        }
        xml.append("</LEAFNODES>\n");

        xml.append("</SYNTREE>");
        return xml.toString();
    }
}
