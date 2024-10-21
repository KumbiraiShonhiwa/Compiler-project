import java.util.List;

public class FunctionSignature {
    private String functionName;      // The name of the function
    private List<String> paramTypes;  // The list of parameter types
    private String returnType;        // The return type of the function

    public FunctionSignature(String functionName,List<String> paramTypes, String returnType) {
        this.functionName = functionName;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
    }

    FunctionSignature(String functionName,String returnType, List<String> parameterTypes) {
        this.functionName = functionName;
        this.paramTypes = parameterTypes;
        this.returnType = returnType;
    }

    // Getter for parameter types
    public List<String> getParamTypes() {
        return paramTypes;
    }

    // Getter for return type
    public String getReturnType() {
        return returnType;
    }

    public String getFunctionName() {
        return functionName;
    }

    // Helper function to check if the provided arguments match the signature
    public boolean matchesArgumentTypes(List<String> providedTypes) {
        if (providedTypes.size() != paramTypes.size()) {
            return false; // Different number of arguments
        }

        // Check if each provided argument type matches the corresponding parameter type
        for (int i = 0; i < providedTypes.size(); i++) {
            if (!providedTypes.get(i).equals(paramTypes.get(i))) {
                return false; // Type mismatch
            }
        }

        return true; // All types match
    }
}
