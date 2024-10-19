// import java.util.ArrayList;
// import java.util.List;

// public class InnerNode extends Node {
//     private List<String> children;

//     public InnerNode(String parentUnid, String unid, String label) {
//         super(parentUnid, unid, label);
//         this.children = new ArrayList<>();
//     }

//     // Method to add a child node by UNID
//     public void addChild(String childUnid) {
//         children.add(childUnid);
//     }

//     // Method to get all children
//     public List<String> getChildren() {
//         return children;
//     }

//     @Override
//     public String toString() {
//         return "InnerNode{" +
//                 "unid='" + getUnid() + '\'' +
//                 ", label='" + getLabel() + '\'' +
//                 ", parentUnid='" + getParentUnid() + '\'' +
//                 ", children=" + children +
//                 '}';
//     }
// }
