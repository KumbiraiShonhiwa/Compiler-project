
public class SymbolInfo {

    String type;
    Object value;
    String internalName;

    SymbolInfo(String type, Object value, String internalName) {
        this.type = type;
        this.value = value;
        this.internalName = internalName;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public String getName() {
 
       return internalName;
    }
}