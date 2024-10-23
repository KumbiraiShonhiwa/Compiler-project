
public class LeafNode extends Node {

    int parentId; // Parent Node ID
    int unid; // Unique Node ID
    Node parent = null; // Parent Node
    String terminal; // Terminal symbol


    public LeafNode(int parentId, int unid, String terminal,String symbol) {
        super(unid, symbol);
        this.parentId = parentId;
        this.terminal = terminal;
        this.unid = unid;

    }

    

    public String toXML() {

        return "<LEAF>\n"
                + "<PARENT>" + parentId + "</PARENT>\n"
                + "<UNID>" + unid + "</UNID>\n"
                + "<TERMINAL>" + terminal + "</TERMINAL>\n"
                + "</LEAF>\n";
    }

    public int getUnid() {
        return unid;
    }
}
