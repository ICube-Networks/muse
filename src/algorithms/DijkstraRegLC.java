// Copyright 2021 Technology & Strategy / ICube, GNU Public License.

package algorithms;

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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import structures.CostToReachSuperNodeComparator;
import structures.EdgeLabeled;
import structures.GraphMultimodal;
import structures.Supernode;
import structures.NFA;
import structures.Toolbox;
import structures.Transition;

public class DijkstraRegLC {
	private GraphMultimodal graph;
	private NFA nfa;
	
	private Comparator<Supernode> comparator;
	private PriorityQueue<Supernode> queue;
	private Map<Supernode, Double> settled;
	private Map<Supernode, Double> costs;
	private int touchedNodes;
	
	public DijkstraRegLC(GraphMultimodal graph, NFA nfa) {
		this.graph = graph;
		this.nfa = nfa;
		
		comparator = new CostToReachSuperNodeComparator();
	}
	
	public int getTouchedNodes() {
		return touchedNodes;
	}
	
	private boolean isNodeAndStateCompatible(int v, int s) {
		char vLabel = graph.nodes.get(v).label;
		char sLabel = nfa.states.get(s).label;
		
		if ((vLabel == 's' && sLabel == 'p') || vLabel == sLabel) {
			return true;
		}
		return false;		
	}
	
	private void initialize(int source, int state, int target) {
		
		queue = new PriorityQueue<>(comparator);
		settled = new HashMap<>();
		costs = new HashMap<>();
		touchedNodes = 0;
		
		Supernode ss = new Supernode(source, state, 0);
		costs.put(ss, 0.0);
		queue.add(ss);
	}
	
	public double computeShortestPath(int source, int state, int target, int time) {
		initialize(source, state, target);
      
		while (!queue.isEmpty()){
			Supernode node = queue.poll();
			touchedNodes++;
			
			int v = node.node;
			int s = node.state;			
			double vCost = node.key;
			
			if (v == target && nfa.states.get(s).isFinal) {
				return vCost;
			}
			
			Supernode vs = new Supernode(v, s);
			if (settled.containsKey(vs)) {
				continue;				
			}
    	  
			settled.put(vs, vCost);
    	  
			List<EdgeLabeled> neighbors = graph.outgoingEdges.get(v);
			List<Transition> transitions = nfa.transitions.get(s);

			for(int i=0; i<neighbors.size(); i++) {
				int w = neighbors.get(i).head;
				char edgeLabel = neighbors.get(i).label;
				
				for(int j=0; j<transitions.size(); j++) {
					int q = transitions.get(j).to_state;
					Supernode wq = new Supernode(w, q);
					
					// consition1: neighbor vertex and to_state are compatible.
					// consition2: graph edge and NFA transition labels are the same.
					// consition3: <w,q> has not been settled yet.
					if (!isNodeAndStateCompatible(w, q) || transitions.get(j).label != edgeLabel || settled.containsKey(wq)) {
						continue;
					}
					
					double vwCost;
					try {
						vwCost = Toolbox.evaluate(graph.getEdge(v, w).ttf, time + vCost);
					} catch (Exception e) {
						continue;
					}
					
					if (!costs.containsKey(wq) || vCost + vwCost < costs.get(wq)) {
						costs.put(wq, vCost + vwCost);						
						queue.add(new Supernode(w, q, vCost + vwCost));						
					}
				}
			}   	  
		}
		return -1;
	}
}