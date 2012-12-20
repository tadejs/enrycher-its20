package si.ijs.its20;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import si.ijs.its20.DOMSnapshot.ITSState;


public class ITSDocument {
	protected final Document root;
	protected final List<ITSState> nodes;
	
	public ITSDocument(Document xdoc) {
		root = xdoc;
		nodes = new ArrayList<ITSState>();
		gatherDocument(xdoc);
	}
	
	public Iterable<ITSState> iterNodes() {
		return nodes;
	}
	
	public Document getRoot() {
		return root;
	}
	
	public static String getPath(Collection<String> path) {
		StringBuilder sb = new StringBuilder();
		for (String string : path) {
			sb.append(string);
		}
		return sb.toString();
	}
	

	protected void gatherDocument(Document doc) {
		Stack<String> path = new Stack<String>();
		
		Node root = doc.getFirstChild();
		path.push("/" + root.getNodeName());
		
		ITSState rootState = new ITSState(root, getPath(path), null);
		nodes.add(rootState);
		
		if (root.hasAttributes()) {
			gatherAttributes(nodes, path, root, rootState);
		}
				
		gatherNodes(root, rootState, nodes, path);
	}
	

	protected void gatherNodes(Node node, ITSState parent, List<ITSState> lines, Stack<String> path) {
		Map<String, Integer> nodeCounter = new HashMap<String, Integer>();
		for (Node child = node.getFirstChild(); child != null;) {

			Node nextChild = child.getNextSibling();
			
			String nodeName = child.getNodeName();
			
			if (child.getNodeType() == Node.ELEMENT_NODE) {
			
				Integer num = nodeCounter.get(nodeName);
				if (num == null) {
					num = Integer.valueOf(1);
				} else {
					num = num + 1;
				}
				nodeCounter.put(nodeName, num);
				
				path.push("/" + nodeName + "[" + num + "]");
				
				ITSState childState = new ITSState(child, getPath(path), parent);
				lines.add(childState);
								
				if (child.hasAttributes()) {
					gatherAttributes(lines, path, child, childState);
				}
				
				gatherNodes(child, childState, lines, path);
				path.pop();
			
			}
								
			child = nextChild;
		}
		
		
	}

	protected void gatherAttributes(List<ITSState> lines, Stack<String> path,
			Node child, ITSState childState) {
		for (int i = 0; i < child.getAttributes().getLength(); i++) {
			Node att = child.getAttributes().item(i);
			
			if (att.getNodeName().startsWith("xmlns"))
				continue;
			
			path.push("/@" + att.getNodeName());
			
			lines.add(new ITSState(att, getPath(path), childState));

			path.pop();
			
		}
	}
	
	
}
