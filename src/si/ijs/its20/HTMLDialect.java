package si.ijs.its20;

import java.net.URI;
import java.net.URISyntaxException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public class HTMLDialect implements Dialect {

	@Override
	public String getExternalRules(Node node) {
		if (node.getNodeName().equals("link")) {
			if (node.hasAttributes()) {
				Node att = node.getAttributes().getNamedItem("rel");
				if (att != null && att.getNodeValue().equals("its-rules")) {
					return node.getAttributes().getNamedItem("href").getNodeValue();
				}
							
			}
			
		}
		
		return null;
	}

	@Override
	public boolean isRules(Node node) {
		if (node.getNodeName().equals("link")) {
			if (node.hasAttributes()) {
				Node att = node.getAttributes().getNamedItem("rel");
				if (att != null && att.getNodeValue().equals("its-rules")) {
					return true;
				}
			}
		}
		return false;
	}


	@Override
	public void readNode(Node node, ITSData disambig) {
	
		String attName = node.getNodeName();
		if (attName.contains("its-disambig-granularity")) {
			disambig.granularity = ITSData.Granularity.fromString(node.getNodeValue());
		} else if (attName.contains("its-disambig-ident-ref")) {
			try {
				disambig.identRef = new URI(node.getNodeValue());
			} catch (DOMException | URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}						
		} else if (attName.contains("its-disambig-ident")) {
			disambig.ident = node.getNodeValue();						
		} else if (attName.contains("its-disambig-source")) {
			disambig.source = node.getNodeValue();						
		} else if (attName.contains("its-disambig-confidence")) {
			disambig.confidence = new Double(node.getNodeValue());						
		}else if (attName.contains("its-disambig-class-ref")) {
			try {
				disambig.classRef = new URI(node.getNodeValue());
			} catch (DOMException | URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}						
		}
			
		
	}


	@Override
	public boolean isAnnotatorsRef(Node node) {
		return node.getNodeName().equals("its-annotators-ref");
	}

	@Override
	public void readTool(Node node, ITSData disambig) {
		String val  =node.getNodeValue();
		String[] vals = val.split(" ");
		for (String string : vals) {
			String[] kv = string.split("|");
			if (kv.length == 2) {
				disambig.addTool(kv[0], kv[1]);
			} else {
				System.err.println(string + " is not a good annotatorsRef value");
			}
		}
	}
	
}
