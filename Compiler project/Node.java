
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Node {

    int unid;
    String symbol;
    List<Node> children = new ArrayList<Node>();
    Node parent = null;

    public Node(int unid, String symbol, Node parent) {
        this.unid = unid;
        this.symbol = symbol;
        this.parent = parent;
    }

    public Node(int unid, String symbol) {
        this.unid = unid;
        this.symbol = symbol;
    }

    public void addChild(Node childId) {
        children.add(childId);
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public String getSymbol() {
        return symbol;
    }

    public List<Node> getChildren() {
        return children;
    }

    public Node getChild(int i) {
        return children.get(i);
    }

    public String toXML() {
        StringBuilder xml = new StringBuilder();
        if (parent != null) {
            xml.append("<IN>\n");
            xml.append("<PARENT>").append(parent.unid).append("</PARENT>\n");
            xml.append("<UNID>").append(unid).append("</UNID>\n");
            xml.append("<SYMB>").append(symbol).append("</SYMB>\n");
            xml.append("<CHILDREN>\n");
            for (Node childId : children) {
                xml.append("<ID>").append(childId.unid).append("</ID>\n");
            }
            xml.append("</CHILDREN>\n");
            xml.append("</IN>\n");
        }
        return xml.toString();

    }

    public int getUnid() {
        return unid;
    }

    public Node getChild(String symbol) {
        for (Node child : children) {
            if (child.symbol.equals(symbol)) {
                return child;
            }
        }
        return null;
    }
    
    public boolean hasChild(String symbol) {
        for (Node child : children) {
            if (child.symbol.equals(symbol)) {
                return true; // Return true if a matching child is found
            }
        }
        return false; // Return false if no matching child is found
    }

    public Node getFirstChild() {
        if (!children.isEmpty()) {
            return children.get(0); // Return the first child if it exists
        }
        return null; 
    }
}
