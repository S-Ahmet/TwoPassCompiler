package symboltable;

public class Symbol {

    private String name;
    private String type;
    private String scope;
    private int memoryLocation;

    public Symbol(String name, String type, String scope, int memoryLocation) {
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.memoryLocation = memoryLocation;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getScope() {
        return scope;
    }

    public int getMemoryLocation() {
        return memoryLocation;
    }

    @Override
    public String toString() {
        return "Name: " + name +
                " | Type: " + type +
                " | Scope: " + scope +
                " | Memory: " + memoryLocation;
    }
}