package structures;

public class State {
	public boolean isInitial;
	public boolean isFinal;
	public char label;
	
	public State(boolean isInitial, boolean isFinal, char label) {
		this.isInitial = isInitial;
		this.isFinal = isFinal;
		this.label = label;
	}
}
