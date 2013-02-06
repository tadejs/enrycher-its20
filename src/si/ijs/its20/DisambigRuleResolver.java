package si.ijs.its20;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DisambigRuleResolver extends RuleResolver {
	
	
	
	public DisambigRuleResolver(XPath xpa) {
		super(xpa);
	}

	
	@Override
	public GlobalRule extractRule(AttributeReader reader, Parameters params, Node node) {
		NamedNodeMap attrs = node.getAttributes();
		GlobalRule rule = new GlobalRule(params.expandParams(attrs.getNamedItem("selector").getNodeValue()));
		addPointer(attrs, rule, "tanClassRefPointer");
		addPointer(attrs, rule, "tanIdentRefPointer");
		addPointer(attrs, rule, "tanIdentPointer");
		addPointer(attrs, rule, "tanSourcePointer");
		
		//ITSData globalRule = dialect.readNode(node);
		rule.data = reader.readNode(node);
		
		return rule;
	}

	@Override
	public ITSData applyRule(AttributeReader reader, Parameters params, GlobalRule rule, Node item, ITSData data) {
		try {
			Dialect dialect = reader.getDialect();
			XPathExpression identRefExp = compile(rule, params, dialect, "tanIdentRefPointer");
			XPathExpression classRefExp = compile(rule,  params, dialect,"tanClassRefPointer");
			XPathExpression identExp = compile(rule,  params, dialect,"tanIdentPointer");
			XPathExpression sourceExp = compile( rule,  params, dialect,"tanSourcePointer");
			
			
			if (identRefExp != null) {
				String identRef = identRefExp.evaluate(item);
				try {
					data.identRef = new URI(identRef);
				} catch (URISyntaxException e) {
					log.warning("Invalid URI: " + identRef);
				}
			}
			if (classRefExp != null) {
				String classRef = classRefExp.evaluate(item);
				try {
					data.classRef = new URI(classRef);
				} catch (URISyntaxException e) {
					log.warning("Invalid URI: " + classRef);
				}
			}
			if (sourceExp != null) {
				data.source = sourceExp.evaluate(item);
			}
			if (identExp != null) {
				data.ident = identExp.evaluate(item);
			}
		

		} catch (XPathExpressionException e) {
			log.warning("Invalid xpath: " + rule.selector + " - " + e.getMessage());
		}
		
		return data;
	}

}
