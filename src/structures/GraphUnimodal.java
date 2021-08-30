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
