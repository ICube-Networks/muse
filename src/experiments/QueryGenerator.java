package experiments;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import structures.GraphMultimodal;

public class QueryGenerator {
	
	private static final int NUMBER_OF_QUERIES = 100000;
	private static final String REGION = "idf";
	private static Map<Integer, Integer> queries;
	
	private static void saveQueries() throws IOException {
		 final String QUERIES_FILEPATH = String.format("./src/dataset/%s/queries/%s_queries.txt", REGION, REGION);
		 FileWriter file = new FileWriter(QUERIES_FILEPATH);
		 BufferedWriter writer = new BufferedWriter(file);
		 for (int v : queries.keySet()) {
			 writer.write(String.format("%d,%d\n", v, queries.get(v)));
		 }
		 writer.close();
	}
	
	private static int selectRandomNode(GraphMultimodal graph) {
		Random rand = new Random();
		char label = 'f';
		
		while (true) {		
			int v = rand.nextInt(graph.nodes.size());
			if (graph.nodes.get(v).label == label) {
				return v;
			}
		}
	}
	
	private static Query randomQuery(GraphMultimodal graph) {
		int v, w;
		v = selectRandomNode(graph);
		
		do {
			w = selectRandomNode(graph);
		} while (w == v);
		
		return new Query(v, w);
	}

	public static void main(String[] args) throws IOException {
		
		final String GRAPH_FILEPATH = String.format("./src/dataset/%s/graph/%s", REGION, REGION);			
		final GraphMultimodal graph = new GraphMultimodal(GRAPH_FILEPATH);
		
		queries = new HashMap<>();
		while (queries.size() < NUMBER_OF_QUERIES) {
			Query query = randomQuery(graph);
			
			if (!queries.containsKey(query.v)) {
				queries.put(query.v, query.w);
			}
		}			
		saveQueries();
	}

}

class Query {
	public int v;
	public int w;
	
	public Query(int v, int w) {
		this.v = v;
		this.w = w;
	}
}
