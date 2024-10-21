public class LeafNode extends Node
 {
    int parentId; // Parent Node ID
    int unid; // Unique Node ID
    String terminal; // Terminal symbol

    public LeafNode(int parentId, int unid, String terminal) {
        super(unid, terminal);
        this.parentId = parentId;
      
       
    }

    public String toXML() {
        return "<LEAF>\n" +
                "<PARENT>" + parentId + "</PARENT>\n" +
                "<UNID>" + unid + "</UNID>\n" +
                "<TERMINAL>" + terminal + "</TERMINAL>\n" +
                "</LEAF>\n";
    }

    public int getUnid() {
        return unid;
    }
}
