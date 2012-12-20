package si.ijs.its20;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.htmlcleaner.HtmlCleaner;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * This class handles the metadata object model for ITS on a given document (XML or HTML).
 * It tracks the ITS data for every node while taking care of the desired inheritance behaviour. 
 * @author tadej
 *
 */
public class DOMSnapshot {

	protected String docRoot;
	protected Map<String, ITSData> nodeItsData;
	protected Parameters parameters;
	protected ITSDocument doc;
	protected AttributeReader attrReader;
	protected Map<String, RuleResolver> resolvers;

	
	
	protected static Logger log = Logger.getLogger(DOMSnapshot.class.getName());
	static {
		Handler console = new ConsoleHandler();
		console.setFormatter(new SimpleFormatter());
		console.setLevel(Level.WARNING);
		log.addHandler(console);
		log.setLevel(Level.WARNING);
	}
	
	public DOMSnapshot(Document xdoc, String docRoot, Dialect dia) {
		this.docRoot = docRoot;
		attrReader = new AttributeReader(dia);
		nodeItsData = new HashMap<String, ITSData>();
		  
		parameters = new Parameters();
		doc = new ITSDocument(xdoc);
		
		resolvers = new HashMap<String, RuleResolver>();
		XPath xpath = XPathFactory.newInstance().newXPath();
		resolvers.put("its:disambiguationRule", new DisambigRuleResolver(xpath));
		resolvers.put("its:domainRule", new DomainRuleResolver(xpath));
		
		extract();
	}
	

	protected void applyRuleToNode(String nodePath, ITSData dab) {
		ITSData existing = nodeItsData.get(nodePath);
		if (existing == null) {
			//log.finer("Asserting: " + nodePath + " " + dab.toString());
			nodeItsData.put(nodePath, dab);
		} else {
			//log.finer("Asserting: " + nodePath + " " + dab.toString() + " on top of " + existing);
			existing.applyOverwrite(dab);
		}
	}
	
	protected static int getSiblingIndex(Node item) { 
		int i = 1;
		for (Node sib = item.getPreviousSibling(); sib != null;) {
			
			if (sib.getNodeName().equals(item.getNodeName())) {
				i++;
			}
			
			sib = sib.getPreviousSibling();
		}
		return i;
		
	}
	public static String whoAmI(Node item) {
		LinkedList<String> path = new LinkedList<String>();
		
		
		while (item != null) {
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				if (item.getParentNode().getNodeType() == Node.DOCUMENT_NODE) {
					// root node
					path.addFirst("/" + item.getNodeName());
				} else {
					path.addFirst("/" + item.getNodeName() + "[" + getSiblingIndex(item) + "]");
				}
				
				item = item.getParentNode();
				
			} else if (item.getNodeType() == Node.ATTRIBUTE_NODE) {
				path.addFirst( "/@" + item.getNodeName());
				
				Attr att = (Attr) item;
				item = att.getOwnerElement();
			} else {
				item = item.getParentNode();
			}
			
				
		}
	
		return ITSDocument.getPath(path);
	}
	
	
	public Iterable<ITSState> iterNodes() {
		return doc.iterNodes();
	}
	
	public ITSData getITSData(String nodePath) {
		return nodeItsData.get(nodePath);
	}
	
	protected void extract() {

		
		// parse the global rules from the file
		for (ITSState nodeState : doc.iterNodes()) {
			Node node = nodeState.node;
			RuleResolver resolver = resolvers.get(node.getNodeName()); 
			if (resolver != null) {
				GlobalRule rule = resolver.extractRule(attrReader, parameters, nodeState.node);
				applyRule(resolver, rule);
			}
			
			if (node.getNodeName().equals("its:param")) {
				String name = node.getAttributes().getNamedItem("name").getNodeValue();
				String value = node.getTextContent();
				parameters.add(name, value);
			} else if (attrReader.dialect.isRules(node)) { 
				// we have external link (xlink:href or link href)
				String external = attrReader.dialect.getExternalRules(node);
				if (external != null) {
					extractExternalRules(external);
				}
			} else if (node.getNodeName().equals("script")) {
				// we have embedded
				if (node.hasAttributes()) {
					if (node.getAttributes().getNamedItem("type").getNodeValue().equals("application/its+xml")) {
						extractEmbeddedRules(node.getTextContent());
					}
				}
			}
			if (!nodeState.nodePath.contains("its:rules")) {
				// unless we're in rules, look at local annotations
				// LOCAL
				extractLocal(nodeState);
			}
		}
		
		// process inheritance
		for (ITSState nodeState : doc.iterNodes()) {
			// Elements: handle inheritance 
			ITSData parentData = getParentState(nodeState);
			if (parentData != null) {
				// inherit from parent
				ITSData thisData = nodeItsData.get(nodeState.nodePath);
				if (thisData == null) {
					thisData = new ITSData();
					applyRuleToNode(nodeState.nodePath, thisData);
				} 
				thisData.inherit(parentData);				
			}	
						
		} 
		
		
	}

	protected void extractExternalRules(String external) {
		// load extra stuff from xlink
		DocumentBuilder db = getXMLParser();

		Document docExtra = null;
		try {
			
			// load the standoff rules
			if (external.startsWith("http")) {
				docExtra = db.parse(external);
			} else {
				docExtra = db.parse(docRoot + external);	
			}
			
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (docExtra != null) {
			processStandoffMarkup(docExtra);
		}

	}

	protected void extractEmbeddedRules(String rulesXml) {
		// load extra stuff from xlink
		DocumentBuilder db = getXMLParser();

		Document docExtra = null;
		try {
			InputSource is = new InputSource(new StringReader(rulesXml));
			docExtra = db.parse(is);
			
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (docExtra != null) {
			processStandoffMarkup(docExtra);
		}

	}


	
	private void processStandoffMarkup(Document docExtra) {
		// parse the global rules from the file
		DOMSnapshot snapExtra = new DOMSnapshot(docExtra, docRoot, new XMLDialect());
		for (ITSState itssExtra : snapExtra.iterNodes()) {
			RuleResolver resolver = resolvers.get(itssExtra.node.getNodeName()); 
			if (resolver != null) {
				GlobalRule rule = resolver.extractRule(snapExtra.attrReader, snapExtra.parameters, itssExtra.node);
				applyRule(resolver, rule);
			}
			
			if (itssExtra.node.getNodeName().equals("its:param")) {
				String name = itssExtra.node.getAttributes().getNamedItem("name").getNodeValue();
				String value = itssExtra.node.getTextContent();
				parameters.add(name, value);
			}
			
		}
	}
	
	private void applyRule(RuleResolver resolver,
			GlobalRule rule) {
		// fetch the targets
		XPathExpression selectorExp = null;
		try {
			selectorExp = resolver.getXPath().compile(parameters.expandParams(attrReader.dialect.resolveNamespace(rule.selector)));
		} catch (XPathExpressionException e) {
			log.warning("Invalid xpath: " + rule.selector + " - " + e.getMessage());
		}
		
		NodeList targets;
		try {
			targets = (NodeList) selectorExp.evaluate(doc.getRoot(), XPathConstants.NODESET);
			
			
			
			if (targets.getLength() == 0) {
				log.fine("Warning: " + rule.selector + " returned empty");
			}
			
			for (int i = 0; i < targets.getLength(); i++) {
				Node item = targets.item(i);
				
				// find which ITSState is already associated with this target
				String itemPath = whoAmI(item);
								
				// resolve the pointer values
				ITSData thisNode = getITSData(itemPath);
				if (thisNode == null) {
					thisNode = new ITSData();
				}
				if (rule.data != null) {
					thisNode.applyOverwrite(rule.data);
				}
				
				resolver.applyRule(attrReader, parameters, rule, item, thisNode);
				
				
				//log.fine("Got rules for " + itemPath + ": " + thisNode.toString());
				
				// assert the rule
				applyRuleToNode(itemPath, thisNode);

			}
		} catch (XPathExpressionException e1) {
			log.warning("Invalid xpath: " + rule.selector + " - " + e1.getMessage());
		}
	}

		
	protected void extractLocal(ITSState nodeState) {
		Node node = nodeState.node;

		
		
		// Attribute: read values
		/*String attName = node.getNodeName();
		if (attName.contains("disambig")) {

			log.fine("Got local disambig rule");
			ITSData parentDisambig = getOrCreateParentState(nodeState);
			dialect.readNode(node, parentDisambig);
			
		} else if (dialect.isAnnotatorsRef(node)) {
			
			log.fine("Got local annotatorsRef rule");
			ITSData parentDisambig = getOrCreateParentState(nodeState);
			dialect.readTool(node, parentDisambig);
			
		} */
		
		ITSData itsd = attrReader.readNode(node);
		if (itsd != null) {
			applyRuleToNode(nodeState.nodePath, itsd);
		}
	
	}

	protected ITSData getParentState(ITSState state) {
		if (state.parentState == null) {
			return null;
		}
		return nodeItsData.get(state.parentState.nodePath);
	}
	
	protected ITSData getOrCreateParentState(ITSState state) {
		if (state.parentState == null) {
			return null;
		}
		ITSData parentDisambig = getParentState(state);
		if (parentDisambig == null) {
			parentDisambig = new ITSData();
			nodeItsData.put(state.parentState.nodePath, parentDisambig);
		}
		return parentDisambig;
	}

	
	public List<String> getLines() {

	
		List<String> lines = new ArrayList<String>();
		for (ITSState nodeState : doc.iterNodes()) {
			ArrayList<String> attributes = new ArrayList<String>();
		
			ITSData disambig = nodeItsData.get(nodeState.nodePath);
			if (disambig != null) {
				disambig.getAttributes(attributes);
			}
		
			String line = null;
			if (attributes.isEmpty()) {
				line = nodeState.nodePath; 
			} else {
				line = nodeState.nodePath + "\t" + join(attributes, "\t"); 
			}
			lines.add(line);
			
		}
		
		
		return lines;
		
	}
	
	public static class ITSState {
		public String nodePath;
		public Node node;
		public ITSState parentState;
		
		public ITSState(Node node, String nodePath, ITSState parent) {
			this.node = node;
			this.nodePath = nodePath;
			this.parentState = parent;
		}
		
		
	}
	
	
	protected static String join(Collection<String> path, String separator) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String string : path) {
			if (first) {
				first = false;
			} else {
				sb.append(separator);
			}
			sb.append(string);
		}
		return sb.toString();
	}
	

	public static HtmlCleaner  getHtmlParser() {
		/*Tidy tidy = new Tidy();
		tidy.setTidyMark(false);
		tidy.setDocType("omit");
		tidy.setDropProprietaryAttributes(false);
		tidy.setLiteralAttribs(true);
		//tidy.set
		//tidy.setSmartIndent(true);
		//tidy.setIndentAttributes(true);
		//tidy.setIndentContent(true);
		//tidy.setDropEmptyParas(false);
		//tidy.setDropProprietaryAttributes(false);
		//tidy.setMakeClean(false);
		tidy.setTrimEmptyElements(false);
		tidy.setWraplen(128);
		tidy.setInputEncoding("UTF-8");
		tidy.setOutputEncoding("UTF-8");
		tidy.setNumEntities(false);
		tidy.setRawOut(true);
		
			//tidy.setPrintBodyOnly(true);
		
		return tidy;*/
		
		/*HtmlDocumentBuilder b = new HtmlDocumentBuilder();
		
		b.setErrorHandler(new SystemErrErrorHandler());
		b.setDoctypeExpectation(DoctypeExpectation.HTML);*/
		HtmlCleaner b = new HtmlCleaner();
		
		return b;
	}
	
	public static DocumentBuilder getXMLParser() {
		// parse XML document
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilderFactory.setNamespaceAware(true);
			docBuilder = docBuilderFactory.newDocumentBuilder();
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return docBuilder;
	}
	
}
