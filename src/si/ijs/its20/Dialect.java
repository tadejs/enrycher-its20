package si.ijs.its20;

import org.w3c.dom.Node;

public interface Dialect {
	boolean isRules(Node node);
	String getExternalRules(Node node);
	
	String resolveNamespace(String namespace);
	
	String getAttName(String att);
	interface OnAttribute {
		void set(ITSData item, String nodeValue);
	}
}
