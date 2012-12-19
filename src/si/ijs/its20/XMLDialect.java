package si.ijs.its20;

import java.net.URI;
import java.net.URISyntaxException;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import sun.util.logging.resources.logging;

public class XMLDialect implements Dialect {

	@Override
	public String getExternalRules(Node node) {
		if (node.getNodeName().equals("its:rules")) {
			Node xlink = node.getAttributes().getNamedItem("xlink:href");
			if (xlink != null) {
				return xlink.getNodeValue();
			} else {
				return null;
			}
		}
		return null;
	}

	@Override
	public boolean isRules(Node node) {
		return node.getNodeName().equals("its:rules");
	}

	@Override
	public ITSData readNode(Node node) {
		if (!node.hasAttributes()) {
			return null;
		}
		NamedNodeMap atts = node.getAttributes();
		
		ITSData disambig = null;
		
		for (int i = 0; i < atts.getLength(); i++) {
			Node att = atts.item(i);
			String attName = att.getNodeName();
			if (attName.contains("disambigGranularity")) {
				disambig = (disambig == null) ? new ITSData() : disambig;
				disambig.granularity = ITSData.Granularity.fromString(att.getNodeValue());
			} else if (attName.contains("disambigIdentRef")) {
				try {
					disambig = (disambig == null) ? new ITSData() : disambig;
					disambig.identRef = new URI(att.getNodeValue());
				} catch (DOMException | URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}						
			} else if (attName.contains("disambigIdent")) {
				disambig = (disambig == null) ? new ITSData() : disambig;
				disambig.ident = att.getNodeValue();						
			} else if (attName.contains("disambigSource")) {
				disambig = (disambig == null) ? new ITSData() : disambig;
				disambig.source = att.getNodeValue();						
			} else if (attName.contains("disambigConfidence")) {
				disambig = (disambig == null) ? new ITSData() : disambig;
				disambig.confidence = new Double(att.getNodeValue());						
			}else if (attName.contains("disambigClassRef")) {
				try {
					disambig = (disambig == null) ? new ITSData() : disambig;
					disambig.classRef = new URI(att.getNodeValue());
				} catch (DOMException | URISyntaxException e) {
					System.err.println(e.getMessage());
				}						
			} else if (attName.contains("annotatorsRef")) {
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
