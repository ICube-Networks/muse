package algorithms;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import structures.CliqueEdge;
import structures.CostToReachSuperNodeComparator;
import structures.EdgeLabeled;
import structures.GraphMultimodal;
import structures.Supernode;
import structures.Toolbox;
import structures.NFA;
import structures.Transition;

public class Muse {
	private GraphMultimodal graph;
	private NFA nfa;
	
	private Comparator<Supernode> comparator;
	private PriorityQueue<Supernode> queue;
	private Map<Supernode, Double> settled;
	private Map<Supernode, Double> costs;
	private int touchedNodes;
	
	public Muse(GraphMultimodal graph, NFA nfa) {
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
		
		queue.add(new Supernode(source, state, 0));
	}
	
	public double computeShortestPath(int source, int state, int target, double time) {
		initialize(source, state, target);
		
		int sourceCell = graph.nodes.get(source).cell;
		int targetCell = graph.nodes.get(target).cell;
		
		while (!queue.isEmpty()){
			Supernode node = queue.poll();
			touchedNodes++;
			
			int v = node.node;
			int s = node.state;
			int vCell = graph.nodes.get(v).cell;
			double vCost = node.key;
			
			if (v == target && nfa.states.get(s).isFinal) {
				return vCost;
			}
			
			Supernode vs = new Supernode(v, s);
			if (settled.containsKey(vs)) {
				continue;
			}	
    	  
			settled.put(vs, vCost);
				
			List<EdgeLabeled> graphNeighbors = graph.outgoingEdges.get(v);
			List<Transition> transitions = nfa.transitions.get(s);
			
			// explore nodes inside source and target cells + cut-edges
			for(int i=0; i<graphNeighbors.size(); i++) {
				int w = graphNeighbors.get(i).head;
				char edgeLabel = graphNeighbors.get(i).label;
				int wCell = graph.nodes.get(w).cell;
				
				if ((vCell != wCell) || vCell == sourceCell || vCell == targetCell) {
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
			
			if (!graph.overlay.containsKey(vs)) {
				continue;
			}
			
			List<CliqueEdge> overlayNeighbors = graph.overlay.get(vs);
			
			// explore overlay graph
			for(int i=0; i<overlayNeighbors.size(); i++) {
				Supernode wq = overlayNeighbors.get(i).head;
				
				// check if supernode <w,q> has not been settled yet.
				if (settled.containsKey(wq)) {
					continue;
				}
				
				double vwCost;
				try {
					vwCost = Toolbox.evaluate(overlayNeighbors.get(i).ttf, time + vCost);
				} catch (Exception e) {
					continue;
				}
				
				if (!costs.containsKey(wq) || vCost + vwCost < costs.get(wq)) {
					costs.put(wq, vCost + vwCost);						
					queue.add(new Supernode(wq.node, wq.state, vCost + vwCost));						
				}
			}
		}
		return -1;
	}
}
