package symboltable;

import java.util.ArrayList;
import java.util.List;

public class SymbolTable {

    private List<Symbol> symbols = new ArrayList<>();
    private int memoryCounter = 1000;

    public boolean addSymbol(String name, String type, String scope) {

        if (exists(name, scope)) {
            return false;
        }

        symbols.add(new Symbol(name, type, scope, memoryCounter));
        memoryCounter += 4;

        return true;
    }

    public boolean exists(String name, String scope) {
        for (Symbol symbol : symbols) {
            if (symbol.getName().equals(name) && symbol.getScope().equals(scope)) {
                return true;
            }
        }
        return false;
    }

    public String getType(String name, String scope) {
        for (Symbol symbol : symbols) {
            if (symbol.getName().equals(name) && symbol.getScope().equals(scope)) {
                return symbol.getType();
            }
        }
        return null;
    }

    public List<Symbol> getSymbols() {
        return symbols;
    }

    public void printTable() {
        System.out.println("\n--- SYMBOL TABLE ---");
        for (Symbol symbol : symbols) {
            System.out.println(symbol);
        }
    }
}