package structures;

import java.util.ArrayList;
import java.util.List;

public class Tree {
	private int vertex;
	private Tree parent = null;
    private List<Tree> children = new ArrayList<>();
    
    public Tree(int vertex) {
    	this.vertex = vertex;
    }

    public Tree(int vertex, Tree parent) {
        this.vertex = vertex;
        this.parent = parent;
    }

    public List<Tree> getChildren() {
        return children;
    }

    public void setParent(Tree parent) {
        this.parent = parent;
    }

    public void addChild(int data) {
        Tree child = new Tree(data);
        child.setParent(this);
        this.children.add(child);
    }

    public void addChild(Tree child) {
        child.setParent(this);
        this.children.add(child);
    }

    public int getVertex() {
        return this.vertex;
    }

    public boolean isRoot() {
        return (this.parent == null);
    }

    public boolean isLeaf() {
        return this.children.size() == 0;
    }

    public void removeParent() {
        this.parent = null;
    }
}
