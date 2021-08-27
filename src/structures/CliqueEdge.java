package structures;

public class CliqueEdge {
	public Supernode head;
	public TTF ttf;
	public int cell;
	
	public CliqueEdge(Supernode head, TTF ttf, int cell) {
		this.head = head;
		this.ttf = ttf;
		this.cell = cell;
	}
}
