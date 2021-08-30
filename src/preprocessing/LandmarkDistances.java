package preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import structures.CostToReachNodeComparator;
import structures.EdgeLabeled;
import structures.GraphMultimodal;
import structures.NFA;
import structures.NodeKey;
import structures.Transition;

public class LandmarkDistances {
	private GraphMultimodal graph;
	private List<Integer> landmarks;
	private Set<Character> labels;
	public Map<Integer, List<Integer>> forwardCosts;
	public Map<Integer, List<Integer>> reverseCosts;
	
	public LandmarkDistances(GraphMultimodal graph, NFA nfa, String landmarksFilepath) throws NumberFormatException, IOException {
		this.graph = graph;
		loadLandmarks(landmarksFilepath);		
		labels = new HashSet<>();
		for (int i=0; i<nfa.numberOfStates; i++) {
			for (Transition transition : nfa.transitions.get(i)) {
				labels.add(transition.label);
			}
		}
	}
	
	public void run() {
		forwardCosts = new HashMap<>();
		reverseCosts = new HashMap<>();
		for (Integer l : landmarks) {
			forwardShortestPaths(l);
			backwardShortestPaths(l);
		}
	}
	
	private void loadLandmarks(String filepath) throws NumberFormatException, IOException {		
		String sCurrentLine;		
		FileReader file = new FileReader(filepath);
		BufferedReader reader = new BufferedReader(file);
		
		landmarks = new ArrayList<>();
		while ((sCurrentLine = reader.readLine()) != null) {
			if (sCurrentLine.length() > 0) {
				landmarks.add(Integer.parseInt(sCurrentLine));
			}
		}
		reader.close();
	}
	
	public void save(String filepath) throws IOException {
		FileWriter file = new FileWriter(filepath + "_forward.txt");
		BufferedWriter writer = new BufferedWriter(file);
		int v = 0;
		while (v < graph.numberOfNodes) {
			for (Integer l : forwardCosts.keySet()) {
				writer.write(String.format("%d,", forwardCosts.get(l).get(v)));
			}
			writer.write("\n");
			v++;
		}
		writer.close();
		
		file = new FileWriter(filepath + "_reverse.txt");
		writer = new BufferedWriter(file);
		v = 0;
		while (v < graph.numberOfNodes) {
			for (Integer l : reverseCosts.keySet()) {
				writer.write(String.format("%d,", reverseCosts.get(l).get(v)));
			}
			writer.write("\n");
			v++;
		}
		writer.close();
	}
	
	private void forwardShortestPaths(int r) {
		Comparator<NodeKey> comparator = new CostToReachNodeComparator();
		PriorityQueue<NodeKey> queue = new PriorityQueue<>(comparator);
		Set<NodeKey> settled = new HashSet<>();
		List<Integer> costs = new ArrayList<>();
		
		for (int v = 0; v < graph.numberOfNodes; v++) {
			costs.add(Integer.MAX_VALUE);
		}
		costs.set(r, 0);
		queue.add(new NodeKey(r, 0, 0));
		
		while (!queue.isEmpty()){
			NodeKey node = queue.poll();			
			if (settled.contains(node)) {
				continue;				
			}    	  
			settled.add(node);
    	  
			for (EdgeLabeled edge : graph.outgoingEdges.get(node.vertex)) {
				if (labels.contains(edge.label) &&
					costs.get(node.vertex) + (int) edge.ttf.min < costs.get(edge.head)) {
					costs.set(edge.head, costs.get(node.vertex) + (int) edge.ttf.min);
					queue.add(new NodeKey(edge.head, node.vertex, costs.get(edge.head)));
				}
			}
		}
		forwardCosts.put(r, costs);
	}
	
	private void backwardShortestPaths(int r) {
		Comparator<NodeKey> comparator = new CostToReachNodeComparator();
		PriorityQueue<NodeKey> queue = new PriorityQueue<>(comparator);
		Set<NodeKey> settled = new HashSet<>();
		List<Integer> costs = new ArrayList<>();
		
		for (int v = 0; v < graph.numberOfNodes; v++) {
			costs.add(Integer.MAX_VALUE);
		}
		costs.set(r, 0);
		queue.add(new NodeKey(r, 0, 0));
		
		while (!queue.isEmpty()){
			NodeKey node = queue.poll();			
			if (settled.contains(node)) {
				continue;				
			}    	  
			settled.add(node);
    	  
			for (EdgeLabeled edge : graph.incomingEdges.get(node.vertex)) {
				if (labels.contains(edge.label) &&
					costs.get(node.vertex) + (int) edge.ttf.min < costs.get(edge.head)) {
					costs.set(edge.head, costs.get(node.vertex) + (int) edge.ttf.min);
					queue.add(new NodeKey(edge.head, node.vertex, costs.get(edge.head)));
				}
			}
		}
		reverseCosts.put(r, costs);
	}
	
	public static void main(String[] args) throws IOException {
		String region = "idf";
		
		GraphMultimodal graph = new GraphMultimodal(String.format("./src/dataset/%s/graph/%s", region, region));
		NFA nfa = new NFA(String.format("./src/dataset/%s/nfa/", region), 1);		
		LandmarkDistances prp = new LandmarkDistances(graph, nfa, String.format("./src/dataset/%s/landmarks/%s_landmarks_32.txt", region, region));
		
		for (int l : prp.landmarks) {
			System.out.printf("%d,%f,%f\n", l, graph.nodes.get(l).latitude, graph.nodes.get(l).longitude);			
		}
		
//		String region = args[0];
//		int nfa_id = Integer.valueOf(args[1]);
//		
//		GraphMultimodal graph = new GraphMultimodal(String.format("./src/dataset/%s/graph/%s", region, region));
//		graph.initializeTravelTimeFunctions(String.format("./src/dataset/%s/graph/%s_transit_data.txt", region, region));
//		NFA nfa = new NFA(String.format("./src/dataset/%s/nfa/", region), nfa_id);
//		
//		LandmarkDistances prp = new LandmarkDistances(graph, nfa, String.format("./src/dataset/%s/landmarks/%s_landmarks_32.txt", region, region));
//		long t = System.nanoTime();
//		prp.run();
//		double time = (System.nanoTime() - t)/1000000.0;
//		System.out.printf("%s,%d,%.3f\n", region, nfa_id, time);
//		prp.save(String.format("./src/dataset/%s/landmarks/%s_%d_prep", region, region, nfa_id));
	}
}
