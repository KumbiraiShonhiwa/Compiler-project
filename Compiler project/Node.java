import java.util.ArrayList;
import java.util.List;

public class Node {
    private int unid;  // Unique identifier for the node
    private String symbol;  // Symbol or type of the node
    private List<Node> children = new ArrayList<>();  // List of child nodes

    public Node(int unid, String symbol) {
        this.unid = unid;
        this.symbol = symbol;
    }

    // New constructor
    public Node(String symbol, List<Node> children) {
        this.symbol = symbol;
       // this.children = new ArrayList<>(children); // Clone the list to avoid external modifications
    }

    public void addChild(Node child) {
        children.add(child);
    }

   

    public int getUnid() {
        return unid;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public List<Node> getChildren() {
        return children;    }

    public Node getChild(int index) {
        if (index < 0 || index >= children.size()) {
            throw new IndexOutOfBoundsException("Invalid child index: " + index);
        }
        return children.get(index);
    }

    @Override
    public String toString() {
        return "Node{" +
                "unid=" + unid +
                ", symbol='" + symbol + '\'' +
                ", children=" + children.size() +
                '}';
    }

    // Method to convert the node to XML format for debugging or export
    public String toXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<IN>\n");
        xml.append("<PARENT>").append(unid).append("</PARENT>\n");
        xml.append("<UNID>").append(unid).append("</UNID>\n");
        xml.append("<SYMB>").append(symbol).append("</SYMB>\n");
        xml.append("<CHILDREN>\n");
        for (Node child : children) {
            xml.append("<CHILD>\n");
            xml.append(child.toXML());
            xml.append("</CHILD>\n");
        }
        xml.append("</CHILDREN>\n");
        xml.append("</IN>\n");
        return xml.toString();
    }
}