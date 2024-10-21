
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
public class Node {
    int unid;
    String symbol;
    List<Node> children = new ArrayList<Node>();
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
    public String toXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<IN>\n");
        xml.append("<PARENT>").append(unid).append("</PARENT>\n");
        xml.append("<UNID>").append(unid).append("</UNID>\n");
        xml.append("<SYMB>").append(symbol).append("</SYMB>\n");
        xml.append("<CHILDREN>\n");
        for (Node childId : children) {
            xml.append("<ID>").append(childId).append("</ID>\n");
        }
        xml.append("</CHILDREN>\n");
        xml.append("</IN>\n");
        return xml.toString();
    }

    public int getUnid() {
        return unid;
    }
}
