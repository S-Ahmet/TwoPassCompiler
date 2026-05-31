package ast;

import java.util.ArrayList;
import java.util.List;

public class ASTNode {

    private String name;
    private List<ASTNode> children;

    public ASTNode(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }

    public void addChild(ASTNode child) {
        children.add(child);
    }

    public String getName() {
        return name;
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    public void print(String indent) {
        System.out.println(indent + "- " + name);

        for (ASTNode child : children) {
            child.print(indent + "  ");
        }
    }
}