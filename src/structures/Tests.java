package structures;

import java.util.HashMap;
import java.util.Map;

public class Tests { 

	public static void main(String[] args) {
		Map<Supernode, TTF> mymap = new HashMap<>();
		
		mymap.put(new Supernode(5, 2), new TTF(5));
		mymap.put(new Supernode(10, 3), new TTF(10));
		
		Supernode v1 = new Supernode(5, 2);
		Supernode v2 = new Supernode(10, 3);
		Supernode v3 = new Supernode(5, 1);
		
		TTF ttf = mymap.get(v1);
		ttf = new TTF(50);
		
		System.out.println(ttf.toString());
		System.out.println(mymap.get(v1).toString());

	}

}
