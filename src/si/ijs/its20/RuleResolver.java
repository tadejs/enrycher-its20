package si.ijs.its20;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


public abstract class RuleResolver {


	protected static Logger log = Logger.getLogger(DisambigRuleResolver.class.getName());
	static {
		Handler console = new ConsoleHandler();
		console.setFormatter(new SimpleFormatter());
		console.setLevel(Level.FINER);
		log.addHandler(console);
		log.setLevel(Level.FINER);
	}
	
	protected final XPath xpath;
	
	public RuleResolver(XPath xpa) {
		xpath = xpa;
	}
	
	public XPath getXPath() {
		return xpath;
	}
	
	abstract GlobalRule extractRule(AttributeReader rdr, Parameters params, Node node);
	abstract ITSData applyRule(AttributeReader rdr, Parameters params,  GlobalRule rule, Node target, ITSData data);

	protected void addPointer(NamedNodeMap attrs, GlobalRule rule, String name) {
		Node att = attrs.getNamedItem(name);
		if (att != null) {
			rule.pointers.put(name, att.getNodeValue());
		}
	}
	protected XPathExpression compile(GlobalRule rule, Parameters params, Dialect dialect, String name) {
		XPathExpression exp = null;
		if (rule.pointers.containsKey(name)) {
			String pointer = rule.pointers.get(name);
			String corrected = dialect.resolveNamespace(pointer);
			String expanded = params.expandParams(corrected);
			try {
				exp = xpath.compile(expanded);
			} catch (XPathExpressionException e) {
				log.warning("Invalid xpath: " + pointer + " - " + e.getMessage());
			}
		}
		return exp;
	}
	
}
