package si.ijs.its20;

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
	public String resolveNamespace(String namespace) {
		return namespace.replace("h:", "");
	}
	
	@Override
	public String getAttName(String att) {
		StringBuilder sb = new StringBuilder("its-");
		for (int i = 0; i < att.length(); i++) {
			char c = att.charAt(i);
			if (Character.isUpperCase(c)) {
				sb.append('-');
				sb.append(Character.toLowerCase(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	
}
