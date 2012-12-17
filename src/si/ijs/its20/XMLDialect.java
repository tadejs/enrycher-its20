package si.ijs.its20;

import java.net.URI;
import java.net.URISyntaxException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

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
	public void readNode(Node node, ITSData disambig) {
	
		String attName = node.getNodeName();
		if (attName.contains("disambigGranularity")) {
			disambig.granularity = ITSData.Granularity.fromString(node.getNodeValue());
		} else if (attName.contains("disambigIdentRef")) {
			try {
				disambig.identRef = new URI(node.getNodeValue());
			} catch (DOMException | URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}						
		} else if (attName.contains("disambigIdent")) {
			disambig.ident = node.getNodeValue();						
		} else if (attName.contains("disambigSource")) {
			disambig.source = node.getNodeValue();						
		} else if (attName.contains("disambigConfidence")) {
			disambig.confidence = new Double(node.getNodeValue());						
		}else if (attName.contains("disambigClassRef")) {
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
		return node.getNodeName().equals("its:annotatorsRef");
	}

	@Override
	public void readTool(Node node, ITSData disambig) {
		String val  =node.getNodeValue();
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
