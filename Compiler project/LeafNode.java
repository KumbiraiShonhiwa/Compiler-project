public class LeafNode {
    int parentId; // Parent Node ID
    int unid; // Unique Node ID
    String terminal; // Terminal symbol

    public LeafNode(int parentId, int unid, String terminal) {
        this.parentId = parentId;
        this.unid = unid;
        this.terminal = terminal;
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
