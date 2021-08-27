package structures;

public class Transition {
	public int to_state;
	public char label;
	
	public Transition(int to_state, char label) {
		this.to_state = to_state;
		this.label = label;
	}
}
