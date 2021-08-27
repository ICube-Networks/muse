package structures;

public class Node {
	public double latitude;
	public double longitude;
	public char label;
	public int cell;
	
	public Node(double latitude, double longitude, char label) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.label = label;
		this.cell = -1;
	}
	
	public Node(double latitude, double longitude, char label, int cell) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.label = label;
		this.cell = cell;
	}
}
