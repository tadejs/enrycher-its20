package si.ijs.its20;

import org.w3c.dom.Node;

public class XMLDialect implements Dialect {

	
	
	public XMLDialect() {
	}
	
	@Override
	public String getAttName(String att) {
		return att;
	}
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
	public String resolveNamespace(String namespace) {
		return namespace.replace("h:", "");
	}

}
