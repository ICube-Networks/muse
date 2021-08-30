package algorithms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import structures.NFA;
import structures.Supernode;
import structures.Toolbox;
import structures.Transition;
import structures.CostToReachSuperNodeComparator;
import structures.EdgeLabeled;
import structures.GraphMultimodal;

public class Sdalt {
	private GraphMultimodal graph;
	private NFA nfa;
	private int numberOfLandmarks;
	private List<Integer> landmarks;
	private Map<Integer, List<Integer>> forwardCosts;
	private Map<Integer, List<Integer>> reverseCosts;

	private Comparator<Supernode> comparator;
	private PriorityQueue<Supernode> queue;
	private Map<Supernode, Double> settled;
	private Map<Supernode, Double> costs;
	private int touchedNodes;
	
	public Sdalt(GraphMultimodal graph, NFA nfa, int numberOfLandmarks) throws NumberFormatException, IOException {
		this.graph = graph;
		this.nfa = nfa;
		this.numberOfLandmarks = numberOfLandmarks;
		comparator = new CostToReachSuperNodeComparator();
	}
	
	public int getTouchedNodes() {
		return touchedNodes;
	}
	
	private double computeHeuristic(int v, int t) {
		double bestBound = 0;
		double bound;
		double dist_lv;
		double dist_lt;
		double dist_vl;
		double dist_tl;		
		for (Integer l : landmarks) {
			dist_lv = forwardCosts.get(l).get(v);
			dist_lt = forwardCosts.get(l).get(t);
			dist_vl = reverseCosts.get(l).get(v);
			dist_tl = reverseCosts.get(l).get(t);
			bound = Math.max(dist_vl - dist_tl, dist_lt - dist_lv);
			bestBound = Math.max(bestBound, bound);
		}
		return bestBound;
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
	
	public double computeShortestPath(int source, int state, int target, double time) {
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
						queue.add(new Supernode(w, q, vCost + vwCost, computeHeuristic(v, target)));
					}
				}
			}   	  
		}
		return -1;
	}
	
	private void loadLandmarks(String filepath) throws NumberFormatException, IOException {		
		String sCurrentLine;		
		FileReader file = new FileReader(filepath + String.format("_landmarks_%d.txt", numberOfLandmarks));
		BufferedReader reader = new BufferedReader(file);
		
		landmarks = new ArrayList<>();
		forwardCosts = new HashMap<>();
		reverseCosts = new HashMap<>();
		while ((sCurrentLine = reader.readLine()) != null) {
			if (sCurrentLine.length() > 0) {
				int landmark = Integer.parseInt(sCurrentLine);
				landmarks.add(landmark);
				forwardCosts.put(landmark, new ArrayList<>());
				reverseCosts.put(landmark, new ArrayList<>());
			}
		}
		reader.close();
	}
	
	public void loadPreprocessing(String filepath) throws NumberFormatException, IOException  {
		loadLandmarks(filepath);
		
		String sCurrentLine;
		FileReader file = new FileReader(filepath + String.format("_%d_prep_forward.txt", nfa.nfa_id));
		BufferedReader reader = new BufferedReader(file);		
		while ((sCurrentLine = reader.readLine()) != null) {
			if (sCurrentLine.length() > 0) {
				List<String> row = Arrays.asList(sCurrentLine.split(","));
				for (int i=0; i<landmarks.size(); i++) {
					forwardCosts.get(landmarks.get(i)).add(Integer.parseInt(row.get(i)));
				}
			}
		}
		reader.close();
		
		file = new FileReader(filepath + String.format("_%d_prep_reverse.txt", nfa.nfa_id));
		reader = new BufferedReader(file);
		while ((sCurrentLine = reader.readLine()) != null) {
			if (sCurrentLine.length() > 0) {
				List<String> row = Arrays.asList(sCurrentLine.split(","));
				for (int i=0; i<landmarks.size(); i++) {
					reverseCosts.get(landmarks.get(i)).add(Integer.parseInt(row.get(i)));
				}
			}
		}
		reader.close();
	}
	
	public static void main(String[] args) throws IOException {
		GraphMultimodal graph = new GraphMultimodal("D:/java/muse/src/dataset/idf/graph/idf");
		graph.initializeTravelTimeFunctions("D:/java/muse/src/dataset/idf/graph/idf_transit_data.txt");
		NFA nfa = new NFA("D:/java/muse/src/dataset/idf/nfa/", 1);
		
		DijkstraRegLC dijkstra = new DijkstraRegLC(graph, nfa);
		
		Sdalt sdalt = new Sdalt(graph, nfa, 4);		
		sdalt.loadPreprocessing("D:/java/muse/src/dataset/idf/sdalt/idf");
		
		double cost1 = sdalt.computeShortestPath(0, 0, 500000, 36000);
		double cost2 = dijkstra.computeShortestPath(0, 0, 500000, 36000);
		System.out.printf("%.1f %d\n", cost1, sdalt.getTouchedNodes());
		System.out.printf("%.1f %d", cost2, dijkstra.getTouchedNodes());
	}
}
