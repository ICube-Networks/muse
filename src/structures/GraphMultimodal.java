package structures;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphMultimodal {
	public int numberOfNodes;
	public int numberOfEdges;
	public int partitionSize;

	public List<Node> nodes;
	public List<List<EdgeLabeled>> outgoingEdges;
	public List<List<EdgeLabeled>> incomingEdges;
	public List<Integer> trafficData;
	public List<Integer> transitData;
	public List<List<Integer>> partitions;
	public List<List<Integer>> borderNodes;
	public Map<Supernode, List<CliqueEdge>> overlay;

	public final static int SECONDS_IN_DAY = 86400;

	public GraphMultimodal(String filepath) throws IOException {		
		loadNodes(filepath + "_nodes.txt");
		// loadEdges(filepath + "_edges.txt");
	}

	public EdgeLabeled getEdge(int tail, int head) {
		for (EdgeLabeled edge : outgoingEdges.get(tail)) { 
			if (edge.head == head) {
				return edge;
			}
		}
		return null;
	}

	public void loadOverlay(String filepath) throws IOException {
		overlay = new HashMap<>();

		FileReader file = new FileReader(filepath);
		BufferedReader reader = new BufferedReader(file);

		String sCurrentLine;
		while ((sCurrentLine = reader.readLine()) != null) {
			if (sCurrentLine.length() > 0) {

				List<String> row = Arrays.asList(sCurrentLine.split(";"));

				int cell = Integer.parseInt(row.get(0));
				int v = Integer.parseInt(row.get(1));	
				int s = Integer.parseInt(row.get(2));
				int w = Integer.parseInt(row.get(3));	
				int q = Integer.parseInt(row.get(4));

				Supernode tail = new Supernode(v, s);
				Supernode head = new Supernode(w, q);
				TTF ttf = new TTF();
				
				for (int i=5; i<row.size(); i++) {
					List<String> breakpointString = Arrays.asList(row.get(i).split(","));
					double time = Double.parseDouble(breakpointString.get(0));
					double cost = Double.parseDouble(breakpointString.get(1));
					
					ttf.addBreakpoint(time, cost);
				}				

				if (!overlay.containsKey(tail)) {
					overlay.put(tail, new ArrayList<>());
				}						
				overlay.get(tail).add(new CliqueEdge(head, ttf, cell));
			}					
		}
		reader.close();		
	}

	public void initializeTravelTimeFunctions(String filepath) throws IOException {
		FileReader file = new FileReader(filepath);
		BufferedReader reader = new BufferedReader(file);
		String sCurrentLine;
		while ((sCurrentLine = reader.readLine()) != null) {
			if (sCurrentLine.length() > 0) {

				List<String> row = Arrays.asList(sCurrentLine.split(","));

				int tail = Integer.parseInt(row.get(0));
				int head = Integer.parseInt(row.get(1));

				EdgeLabeled edge = getEdge(tail, head);
				
				double timePrevious = 0;
				double time;
				double cost;

				for (int i=2; i<row.size(); i++) {
					List<String> data = Arrays.asList(row.get(i).split(";"));

					time = Double.parseDouble(data.get(0));
					cost = Double.parseDouble(data.get(1));
					
					if (time <= 0) {
						continue;
					}
					
					edge.ttf.addBreakpoint(timePrevious, (time-timePrevious) + cost);
					edge.ttf.addBreakpoint(time, cost);
					
					timePrevious = time;
				}
			}					
		}
		reader.close();
	}

	public void splitNodesByCell() {
		partitions = new ArrayList<>();
		for (int i=0; i<partitionSize; i++) {
			partitions.add(new ArrayList<>());
		}

		for (int v=0; v<nodes.size(); v++) {
			partitions.get(nodes.get(v).cell).add(v);
		}
	}

	public void computeBorderNodes() {
		borderNodes = new ArrayList<>();
		for (int i=0; i<partitionSize; i++) {
			borderNodes.add(new ArrayList<>());
		}

		for (int v=0; v<nodes.size(); v++) {
			int vPartition = nodes.get(v).cell;
			List<EdgeLabeled> neighbors = outgoingEdges.get(v);
			for (EdgeLabeled edge : neighbors) {
				int wPartition = nodes.get(edge.head).cell;
				if (vPartition != wPartition && !borderNodes.get(vPartition).contains(v)) {
					borderNodes.get(vPartition).add(v);					
				}
			}
		}		
	}

	// NODE LABELS
	// Foot Nodes: 'f'
	// Road Nodes: 'r'
	// Bicycle Nodes: 'b'
	// Public Transit Nodes: ('s', station) ('p', platform)
	private void loadNodes(String filepath) throws IOException {

		numberOfNodes = 0;
		nodes = new ArrayList<>();

		FileReader file = new FileReader(filepath);
		BufferedReader reader = new BufferedReader(file);

		String sCurrentLine;
		while ((sCurrentLine = reader.readLine()) != null) {
			if (sCurrentLine.length() > 0) {

				List<String> row = Arrays.asList(sCurrentLine.split(","));

				double latitude = Double.parseDouble(row.get(1));
				double longitude = Double.parseDouble(row.get(2));
				char label = row.get(3).charAt(0);

				nodes.add(new Node(latitude, longitude, label));
				numberOfNodes++;
			}					
		}
		reader.close();
	}

	// EDGE LABELS
	// Foot Edges: 'f'
	// Road Edges: 'r'
	// Bicycle Edges: 'b'
	// Public Transit Edges: ('c', connection) ('t', transfer)
	// Link Edges: 'l' ('m', car_rent) ('n', car_return) ('x', bicycle_rent) ('y', bicycle_return)
	public void loadEdges(String filepath) throws IOException {

		numberOfEdges = 0;
		outgoingEdges = new ArrayList<>();
		incomingEdges = new ArrayList<>();

		for (int i=0; i<numberOfNodes; i++) {
			outgoingEdges.add(new ArrayList<>());
			incomingEdges.add(new ArrayList<>());
		}

		FileReader file = new FileReader(filepath);
		BufferedReader reader = new BufferedReader(file);

		String sCurrentLine;
		while ((sCurrentLine = reader.readLine()) != null) {
			if (sCurrentLine.length() > 0) {

				List<String> row = Arrays.asList(sCurrentLine.split(","));

				int tail = Integer.parseInt(row.get(0));
				int head = Integer.parseInt(row.get(1));
				int cost = Integer.parseInt(row.get(2)); // in [s]	
				char label = row.get(3).charAt(0);

				if (label == 'c') {
					outgoingEdges.get(tail).add(new EdgeLabeled(head, label));
					incomingEdges.get(head).add(new EdgeLabeled(tail, label));
				}
				else {
					outgoingEdges.get(tail).add(new EdgeLabeled(head, label, cost));
					incomingEdges.get(head).add(new EdgeLabeled(tail, label, cost));
				}

				numberOfEdges++;
			}					
		}
		reader.close();		
	}

	public void loadPartition(String filepath, int partitionSize) throws IOException {
		this.partitionSize = partitionSize;

		FileReader file = new FileReader(filepath);
		BufferedReader reader = new BufferedReader(file);

		int node = 0;
		String sCurrentLine;
		while ((sCurrentLine = reader.readLine()) != null) {
			if (sCurrentLine.length() > 0) {

				int cell = Integer.valueOf(sCurrentLine);
				nodes.get(node++).cell = cell;
			}					
		}
		reader.close();		
	}
}
