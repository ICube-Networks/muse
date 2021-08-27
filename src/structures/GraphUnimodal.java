package structures;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GraphUnimodal {
	public int numberOfNodes;
	public int numberOfEdges;
	public List<List<Edge>> edges;
	public List<Integer> nodesIds;

	public GraphUnimodal(String filepath) throws IOException {
		FileReader file;
		BufferedReader reader;
		String sCurrentLine;
		
		numberOfNodes = 0;
		numberOfEdges = 0;
		edges = new ArrayList<>();
		nodesIds = new ArrayList<>();
		
		file = new FileReader(filepath + "_nodes.txt");
		reader = new BufferedReader(file);
		
		int id = 0;
		while ((sCurrentLine = reader.readLine()) != null) {
			if (sCurrentLine.length() > 0) {
				List<String> row = Arrays.asList(sCurrentLine.split(","));
				
				char label = row.get(3).charAt(0);				
				if (label == 'f') {
					nodesIds.add(id);
					edges.add(new ArrayList<>());
					numberOfNodes++;
				}
				
				id++;
			}
		}
		reader.close();

		file = new FileReader(filepath + "_edges.txt");
		reader = new BufferedReader(file);
		
		while ((sCurrentLine = reader.readLine()) != null) {
			if (sCurrentLine.length() > 0) {

				List<String> row = Arrays.asList(sCurrentLine.split(","));

				int tail = Integer.parseInt(row.get(0));
				int head = Integer.parseInt(row.get(1));
				int cost = Integer.parseInt(row.get(2)); // in [s]	
				char label = row.get(3).charAt(0);

				if (label == 'f') {
					edges.get(tail).add(new Edge(head, cost));
					numberOfEdges++;
				}
			}					
		}
		reader.close();
	}

	public Edge getEdge(int tail, int head) {
		for (Edge edge : edges.get(tail)) { 
			if (edge.head == head) {
				return edge;
			}
		}
		return null;
	}
	
	public int shortestPathCost(int v, int w) {
		return 0;
	}
}
