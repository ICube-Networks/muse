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
