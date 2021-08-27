package structures;

public class EdgeLabeled {
	public int head;
	public char label;
	public TTF ttf;
	
	public EdgeLabeled(int head, char label) {
		this.head = head;
		this.label = label;
		ttf = new TTF();
	}
	
	public EdgeLabeled(int head, char label, int travelTime) {
		this.head = head;
		this.label = label;
		ttf = new TTF(travelTime);
	}
}