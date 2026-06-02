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

    public String toTreeString() {
        StringBuilder builder = new StringBuilder();
        appendTree(builder, "");
        return builder.toString();
    }

    private void appendTree(StringBuilder builder, String indent) {
        builder.append(indent).append("- ").append(name).append("\n");

        for (ASTNode child : children) {
            child.appendTree(builder, indent + "  ");
        }
    }
}
