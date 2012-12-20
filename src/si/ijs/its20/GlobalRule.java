package si.ijs.its20;

import java.util.HashMap;
import java.util.Map;

public class GlobalRule {
	public final String selector;
	public final Map<String, String> pointers;
	public ITSData data;

	
	
	public GlobalRule(String selector) {
		this.selector = selector;
		this.pointers = new HashMap<String, String>();
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder(selector);
		sb.append(": ");
		for (String key : pointers.keySet()) {
			sb.append(key);
			sb.append("=\"");
			sb.append(pointers.get(key));
			sb.append("\" ");
		}
		return sb.toString();		
	}
	

}
