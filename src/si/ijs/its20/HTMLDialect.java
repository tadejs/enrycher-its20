package si.ijs.its20;

import java.net.URI;
import java.net.URISyntaxException;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
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
	public ITSData readNode(Node node) {
	
		if (!node.hasAttributes()) {
			return null;
		}
		
		ITSData disambig = null;
		
		NamedNodeMap atts = node.getAttributes();
		
		for (int i = 0; i < atts.getLength(); i++) {
			Node att = atts.item(i);
			String attName = att.getNodeName();
			if (attName.contains("its-disambig-granularity")) {
				disambig.granularity = ITSData.Granularity.fromString(node.getNodeValue());
			} else if (attName.contains("its-disambig-ident-ref")) {
				try {
					disambig = (disambig == null) ? new ITSData() : disambig; 
					disambig.identRef = new URI(node.getNodeValue());
				} catch (DOMException | URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}						
			} else if (attName.contains("its-disambig-ident")) {
				disambig = (disambig == null) ? new ITSData() : disambig;
				disambig.ident = node.getNodeValue();						
			} else if (attName.contains("its-disambig-source")) {
				disambig = (disambig == null) ? new ITSData() : disambig;
				disambig.source = node.getNodeValue();						
			} else if (attName.contains("its-disambig-confidence")) {
				disambig = (disambig == null) ? new ITSData() : disambig;
				disambig.confidence = new Double(node.getNodeValue());						
			}else if (attName.contains("its-disambig-class-ref")) {
				try {
					disambig = (disambig == null) ? new ITSData() : disambig;
					disambig.classRef = new URI(node.getNodeValue());
				} catch (DOMException | URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}						
			} else if (attName.contains("itsannotators-ref")) {
				disambig = (disambig == null) ? new ITSData() : disambig;
				
				String val  = att.getNodeValue();
				String[] vals = val.split(" ");
				for (String string : vals) {
					String[] kv = string.split("\\|");
					if (kv.length == 2) {
						disambig.addTool(kv[0], kv[1]);
					} else {
						System.err.println(string + " is not a good annotatorsRef value");
					}
				}
				
				
			}
		}
		return disambig;
			
		
	}

	@Override
	public String resolveNamespace(String namespace) {
		return namespace.replace("h:", "");
	}

	
}
