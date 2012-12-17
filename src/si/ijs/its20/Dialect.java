package si.ijs.its20;

import org.w3c.dom.Node;

public interface Dialect {
	boolean isRules(Node node);
	String getExternalRules(Node node);

	void readNode(Node node, ITSData disambig);
	boolean isAnnotatorsRef(Node node);
	void readTool(Node node, ITSData disambig);
}
