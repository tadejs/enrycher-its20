package si.ijs.its20;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DomainRuleResolver extends RuleResolver {

	public DomainRuleResolver(XPath xpa) {
		super(xpa);
	}

	@Override
	public GlobalRule extractRule(AttributeReader reader, Parameters params, Node node) {
		NamedNodeMap attrs = node.getAttributes();
		GlobalRule rule = new GlobalRule(params.expandParams(attrs.getNamedItem("selector").getNodeValue()));
		addPointer(attrs, rule, "domainPointer");
				
		//ITSData globalRule = dialect.readNode(node);
		rule.data = reader.readNode(node);
		
		return rule;
	}

	@Override
	public ITSData applyRule(AttributeReader reader, Parameters params, GlobalRule rule, Node item,
			ITSData data) {
	try {
			
			XPathExpression domainExp = compile(rule,  params, reader.getDialect(), "domainPointer");
			
			if (domainExp != null) {
				NodeList domainsNodes = (NodeList) domainExp.evaluate(item, XPathConstants.NODESET);
				for (int i = 0; i < domainsNodes.getLength(); i++) {
					Node domainNode = domainsNodes.item(i);
					String domains = domainNode.getNodeValue();
					String[] domainList=  domains.split(",");
					for (String dom : domainList) {
						if (dom.charAt(0) == '\'' && dom.charAt(dom.length() - 1) == '\'') { 
							dom = dom.substring(1, dom.length() - 2);
						}
						dom = dom.trim();
						
						if (rule.data != null && rule.data.domainMap != null) {
							String mappedDomain = rule.data.domainMap.get(dom);
							if (mappedDomain != null) { 
								if (!mappedDomain.equals("")) {
									data.addDomain(mappedDomain);
								}
							} else {
								data.addDomain(dom);
							}
						} else {
							data.addDomain(dom);
						}
					}
				}
			}	
		

		} catch (XPathExpressionException e) {
			log.warning("Invalid xpath: " + rule.selector + " - " + e.getMessage());
		}
		
		return data;
	}

}
