package si.ijs.its20;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
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
import org.xml.sax.SAXException;

import testsuitefunctions.TestSuiteFunctions;


/**
 * This class handles the metadata object model for ITS on a given document (XML or HTML).
 * It tracks the ITS data for every node while taking care of the desired inheritance behaviour. 
 * @author tadej
 *
 */
public class DOMSnapshot {

	protected Document doc;
	protected String docRoot;
	protected Map<String, ITSData> nodeItsData;
	protected List<ITSState> nodes;
	protected Map<String, String> paramMap;
	protected Dialect dialect;
	protected static Logger log = Logger.getLogger(DOMSnapshot.class.getName());
	static {
		Handler console = new ConsoleHandler();
		console.setFormatter(new SimpleFormatter());
		console.setLevel(Level.FINER);
		log.addHandler(console);
		log.setLevel(Level.FINER);
	}
	
	public DOMSnapshot(Document doc, String docRoot, Dialect dia) {
		this.doc = doc;
		this.docRoot = docRoot;
		this.dialect = dia;
		nodeItsData = new HashMap<String, ITSData>();
		nodes  = new ArrayList<ITSState>();
		paramMap = new HashMap<String, String>();

		gatherDocument(doc);
		extract();
	}

	protected void applyRuleToNode(String nodePath, ITSData dab) {
		ITSData existing = nodeItsData.get(nodePath);
		if (existing == null) {
			log.finer("Asserting: " + nodePath + " " + dab.toString());
			nodeItsData.put(nodePath, dab);
		} else {
			log.finer("Asserting: " + nodePath + " " + dab.toString() + " on top of " + existing);
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
	
		return getPath(path);
	}
	
	/*public ITSState find(Node item) {
		for (ITSState itss : nodes) {
			if (item.equals(itss.node)) {
				return itss;
			}
		}
		
		return null;
	}*/
	
	public Iterable<ITSState> iterNodes() {
		return nodes;
	}
	
	public ITSData getITSData(String nodePath) {
		return nodeItsData.get(nodePath);
	}
	
	protected void extract() {

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		
		
		for (ITSState nodeState : nodes) {
			Node node = nodeState.node;
					
			if (node.getNodeType() == Node.ELEMENT_NODE) {
			
				if (node.getNodeName().equals("its:param")) {
					
					String name = node.getAttributes().getNamedItem("name").getNodeValue();
					String value = node.getTextContent();
					paramMap.put(name, value);
					
					log.fine("Adding new parameter: " + name + "=" + value);

					
				} else if (node.getNodeName().equals("its:disambiguationRule")) {
					log.fine("Got disambiguation rule");
					// GLOBAL
					extractGlobalDisambig(xpath, nodeState);
				} else if (node.getNodeName().equals("its:domainRule")){
					log.fine("Got domain rule");
					extractGlobalDomain(xpath, nodeState);
				} else if (dialect.isRules(node)) { 
				
					String external = dialect.getExternalRules(node);
					if (external != null) {
						extractExternalRules(xpath, external);
					}
				}
				
				if (!nodeState.nodePath.contains("its:rules")) {
					// unless we're in rules, look at local annotations
					// LOCAL
					extractLocal(nodeState);
				}
				
			} 
		}
		
		// inheritance
		for (ITSState nodeState : nodes) {
			// Elements: handle inheritance 
			ITSData parentData = getParentState(nodeState);
			if (parentData != null) {
				// inherit from parent
				ITSData thisData = nodeItsData.get(nodeState.nodePath);
				if (thisData == null) {
					thisData = new ITSData();
					applyRuleToNode(nodeState.nodePath, thisData);
					//nodeItsData.put(nodeState.nodePath, thisData);
				} 
				thisData.inherit(parentData);				
			}	
						
		} 
		
		
	}

	protected void extractExternalRules(XPath xpath, String external) {
		// load extra stuff from xlink
		DocumentBuilder db = getXMLParser();
		
		try {
			Document docExtra = null;
			
			// load the standoff rules
			if (external.startsWith("http")) {
				docExtra = db.parse(external);
			} else {
				docExtra = db.parse(docRoot + external);	
			}
			
			// parse the global rules from the file
			DOMSnapshot snapExtra = new DOMSnapshot(docExtra, docRoot, new XMLDialect());
			for (ITSState itssExtra : snapExtra.nodes) {
				if (itssExtra.node.getNodeName().equals("its:disambiguationRule")) {
					extractGlobalDisambig(xpath, itssExtra);
				} else if (itssExtra.node.getNodeName().equals("its:domainRule")) {
					extractGlobalDomain(xpath, itssExtra);
				} else if (itssExtra.node.getNodeName().equals("its:param")) {
				
					String name = itssExtra.node.getAttributes().getNamedItem("name").getNodeValue();
					String value = itssExtra.node.getTextContent();
					paramMap.put(name, value);
				}
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
	}
	
	protected String expandParams(String val) {
		if (val.indexOf('$') == -1 || val == null)		 
			return val;
		
		for (Map.Entry<String, String> param : paramMap.entrySet()) {
			val = val.replace("$" + param.getKey(), "'" + param.getValue() + "'");
		}
		return val;
		
	}

	protected XPathExpression compile(XPath xpath, String selector) {
		String expanded = dialect.resolveNamespace(expandParams(selector));
		try { 
			log.fine("Got pointer " + expanded);
			return xpath.compile(expanded);
		} catch (XPathExpressionException xpe) {
			log.fine("Invalid xpath expression:" + expanded);
		}
		return null;
	}
	
	protected XPathExpression getIfExistsPointer(XPath xpath, String attName, Node node) {
		XPathExpression exp = null;
		Node att = node.getAttributes().getNamedItem(attName);
		if (att != null) {
			exp = compile(xpath, att.getNodeValue());
		}
		return exp;
	}
	
	protected void extractGlobalDisambig(XPath xpath, ITSState nodeState) {
		Node node = nodeState.node;
		
		// GLOBAL		
		// every rule has exactly one selector, and may possibly have pointers

		XPathExpression selector = getIfExistsPointer(xpath, "selector", node);
		XPathExpression classRefExp = getIfExistsPointer(xpath, "disambigClassRefPointer", node);
		XPathExpression identRefExp = getIfExistsPointer(xpath, "disambigIdentRefPointer", node);
		XPathExpression identExp = getIfExistsPointer(xpath, "disambigIdentPointer", node);
		XPathExpression sourceExp = getIfExistsPointer(xpath, "disambigSourcePointer", node);
		
		ITSData globalRule = dialect.readNode(node);
		if (globalRule == null) {
			globalRule = new ITSData();
		}
				
		// resolve the rule
		try {
			// fetch the targets
			NodeList targets = (NodeList) selector.evaluate(doc, XPathConstants.NODESET);
			
			
			if (targets.getLength() == 0) {

				log.fine("Warning: " + selector + " returned empty");
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
				thisNode.applyOverwrite(globalRule);
				if (identRefExp != null) {
					String identRef = identRefExp.evaluate(item);
					try {
						thisNode.identRef = new URI(identRef);
					} catch (URISyntaxException e) {
						log.warning("Invalid URI: " + identRef);
					}
				}
				if (classRefExp != null) {
					String classRef = classRefExp.evaluate(item);
					try {
						thisNode.classRef = new URI(classRef);
					} catch (URISyntaxException e) {
						log.warning("Invalid URI: " + classRef);
					}
				}
				if (sourceExp != null) {
					thisNode.source = sourceExp.evaluate(item);
				}
				if (identExp != null) {
					thisNode.ident = identExp.evaluate(item);
			
				}
				
				log.fine("Got rules for " + itemPath + ": " + thisNode.toString());
				
				// assert the rule
				applyRuleToNode(itemPath, thisNode);

			}
			
		} catch (XPathExpressionException e) {
			log.warning("Invalid xpath: " + selector + " - " + e.getMessage());
		}
	}

	
	protected void extractGlobalDomain(XPath xpath, ITSState nodeState) {
		Node node = nodeState.node;
		
		// GLOBAL
		ITSData globalRule = new ITSData();

		String selector = null;
		
		XPathExpression domainExp = null;
		
		Map<String, String> domainMap = null;
				
		// every rule has exactly one selector, and may possibly have pointers
		for (int i = 0; i < node.getAttributes().getLength(); i++) {
			Node att = node.getAttributes().item(i);
			
			if (att.getNodeName().equals("selector")) {
				// we have selector
				selector = expandParams(att.getNodeValue());
				//selector = cleanNamespaces(selector);
			} else if (att.getNodeName().endsWith("Pointer")) {
				// we have a pointer, figure out which one it is and compile its expression
				if (att.getNodeName().equals("domainPointer")) {
					try {
						
						domainExp = xpath.compile(expandParams(att.getNodeValue()));
					} catch (XPathExpressionException xpe) {
						log.warning("Invalid xpath for domain pointer: " + selector + " - " + xpe.getMessage());
					}
				} 
			} else if (att.getNodeName().equals("domainMapping")) {
				domainMap = new HashMap<String, String>();
				String domainMapping = att.getNodeValue();
				String[] domainMappingList = domainMapping.split("\\,");
				
				for (String mapping : domainMappingList) {

					List<String> tokens = new ArrayList<String>();
					boolean inQuote = false;
					StringBuilder sb = new StringBuilder();
					for (int j = 0; j < mapping.length(); j++) {
						char c = mapping.charAt(j);
						switch (c) {
						case '\'': 
							inQuote = !inQuote; break;
						case ' ': {
								if (inQuote) { 
									sb.append(' ');
								} else {
									if (sb.length() > 0) {
										tokens.add(sb.toString());
										sb.setLength(0);
									}
								}	
								break;
							}
						default:
							sb.append(c); break;
						}
					}
					if (sb.length() > 0) {
						tokens.add(sb.toString());
					}
					if (tokens.size() >= 2) {
						String target = tokens.get(tokens.size() - 1);
						for (int j = 0; j < tokens.size() -1; j++) {
							domainMap.put(tokens.get(j), target);
							log.fine("Domain mapping: [" + tokens.get(j) + "] -> [" + target + "]");
						}
					}
				}
				
				
			}
		}
		
		// resolve the rule
		try {
			// fetch the targets
			XPathExpression expr = xpath.compile(selector);
			NodeList targets = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			
			
			if (targets.getLength() == 0) {
				log.warning("Selector " + selector + " returned empty set");
			}
			
			for (int i = 0; i < targets.getLength(); i++) {
				Node item = targets.item(i);
				
				// find which ITSState is already associated with this target
				//ITSState itss = find(item);
				String itemPath = whoAmI(item);
				//System.out.println(itss.nodePath);
				
				// resolve the pointer values
				ITSData thisNode = new ITSData();
				thisNode.applyOverwrite(globalRule);
				if (domainExp != null) {
					String domains = domainExp.evaluate(item);
					String[] domainList=  domains.split(",");
					for (String dom : domainList) {
						if (dom.charAt(0) == '\'' && dom.charAt(dom.length() - 1) == '\'') { 
							dom = dom.substring(1, dom.length() - 2);
						}
						
						if (domainMap != null) {
							String mappedDomain = domainMap.get(dom);
							if (mappedDomain != null) { 
								thisNode.addDomain(mappedDomain);
							} else {
								thisNode.addDomain(dom);
							}
						} else {
							thisNode.addDomain(dom);
						}
					}
				}	
				// assert the rule
				applyRuleToNode(itemPath, thisNode);

			}
				
			
		} catch (XPathExpressionException e) {
			log.warning("Invalid XPath expression "  + selector + " - " + e.getMessage());
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
		
		ITSData itsd = dialect.readNode(node);
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
		for (ITSState nodeState : nodes) {
			ArrayList<String> attributes = new ArrayList<String>();
		
			ITSData disambig = nodeItsData.get(nodeState.nodePath);
			if (disambig != null) {
				disambig.getAttributes(attributes);
			}
			attributes = TestSuiteFunctions.alphabeticOrder(attributes);
			
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
	
	protected static String getPath(Collection<String> path) {
		StringBuilder sb = new StringBuilder();
		for (String string : path) {
			sb.append(string);
		}
		return sb.toString();
	}
	

	protected void gatherDocument(Document doc) {
		Stack<String> path = new Stack<String>();
		
		Node root = doc.getFirstChild();
		path.push("/" + root.getNodeName());
		
		ITSState rootState = new ITSState(root, getPath(path), null);
		nodes.add(rootState);
		
		if (root.hasAttributes()) {
			gatherAttributes(nodes, path, root, rootState);
		}
				
		gatherNodes(root, rootState, nodes, path);
	}
	

	protected void gatherNodes(Node node, ITSState parent, List<ITSState> lines, Stack<String> path) {
		Map<String, Integer> nodeCounter = new HashMap<String, Integer>();
		for (Node child = node.getFirstChild(); child != null;) {

			Node nextChild = child.getNextSibling();
			
			String nodeName = child.getNodeName();
			
			if (child.getNodeType() == Node.ELEMENT_NODE) {
			
				Integer num = nodeCounter.get(nodeName);
				if (num == null) {
					num = Integer.valueOf(1);
				} else {
					num = num + 1;
				}
				nodeCounter.put(nodeName, num);
				
				path.push("/" + nodeName + "[" + num + "]");
				
				ITSState childState = new ITSState(child, getPath(path), parent);
				lines.add(childState);
								
				if (child.hasAttributes()) {
					gatherAttributes(lines, path, child, childState);
				}
				
				gatherNodes(child, childState, lines, path);
				path.pop();
			
			}
								
			child = nextChild;
		}
		
		
	}

	protected void gatherAttributes(List<ITSState> lines, Stack<String> path,
			Node child, ITSState childState) {
		for (int i = 0; i < child.getAttributes().getLength(); i++) {
			Node att = child.getAttributes().item(i);
			
			if (att.getNodeName().startsWith("xmlns"))
				continue;
			
			path.push("/@" + att.getNodeName());
			
			lines.add(new ITSState(att, getPath(path), childState));

			path.pop();
			
		}
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
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return docBuilder;
	}
	
}
