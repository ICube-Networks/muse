// Copyright 2021 Technology & Strategy / ICube, GNU Public License.

package structures;

/**
 * The project contains the implementation of MUSE algorithm for the purpose 
 * of multimodal route planning. The project requires a dataset available at:
 * https://doi.org/10.5281/zenodo.5276749
 * 
 * MUSE has been sumbitted to the Transportation Science Journal in a paper
 * entitled "MUSE: Multimodal Separators for Efficient Route Planning in 
 * Transportation Networks".
 * 
 * This project also includes the implementation of the SDALT algorithm as
 * described in the paper. It allows for reproducing the stated performance
 * evaluation.
 *  
 * @author Amine Falek <a.falek@technologyandstrategy.com>
 */

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