package structures;

import java.util.Objects;

public class Supernode {

    public final int node;
    public final int state;
    public double key;
    public double heuristic;

    public Supernode(int node, int state) {
        this.node = node;
        this.state = state;
        heuristic = 0;
    }
    
    public Supernode(int node, int state, double key) {
        this.node = node;
        this.state = state;
        this.key = key;
        heuristic = 0;
    }
    
    public Supernode(int node, int state, double key, double heuristic) {
        this.node = node;
        this.state = state;
        this.key = key;
        this.heuristic = heuristic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Supernode)) return false;
        Supernode key = (Supernode) o;
        return node == key.node && state == key.state;
    }

    @Override
    public int hashCode() {
    	return Objects.hash(node, state);
    }
    
    @Override
    public String toString() {
    	return String.format("<%d,%d>", node, state);
    }
}