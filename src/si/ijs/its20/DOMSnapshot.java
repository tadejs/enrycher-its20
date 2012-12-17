package si.ijs.its20;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import nu.validator.htmlparser.common.DoctypeExpectation;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import nu.validator.htmlparser.test.SystemErrErrorHandler;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeFilter;
import org.xml.sax.SAXException;

import testsuitefunctions.TestSuiteFunctions;

public class DOMSnapshot {

	protected Document doc;
	protected String docRoot;
	protected Map<String, ITSData> disambiguation;
	protected List<ITSState> nodes;
	protected Map<String, String> paramMap;
	protected Dialect dialect;
	
	public DOMSnapshot(Document doc, String docRoot, Dialect dia) {
		this.doc = doc;
		this.docRoot = docRoot;
		this.dialect = dia;
		disambiguation = new HashMap<String, ITSData>();
		nodes  = new ArrayList<ITSState>();
		paramMap = new HashMap<String, String>();
		
		Stack<String> path = new Stack<String>();
		
		Node root = doc.getFirstChild();
		path.push("/" + root.getNodeName());
		
		ITSState rootState = new ITSState(root, getPath(path), null);
		nodes.add(rootState);
		
		if (root.hasAttributes()) {
			gatherAttributes(nodes, path, root, rootState);
		}
		
		
		
		gatherNodes(root, rootState, nodes, path);
		extractDisambiguation();
	}
	
	protected void applyRuleToNode(String nodePath, ITSData dab) {
		ITSData existing = disambiguation.get(nodePath);
		if (existing == null) {
			disambiguation.put(nodePath, dab);
		} else {
			existing.applyOverwrite(dab);
		}
	}
	
	protected ITSState find(Node item) {
		for (ITSState itss : nodes) {
			if (item.equals(itss.node)) {
				return itss;
			}
		}
		return null;
					
	}
	
	public void extractDisambiguation() {

		XPathFactory xPathfactory = XPathFactory.newInstance();

		XPath xpath = xPathfactory.newXPath();
		
		for (ITSState nodeState : nodes) {
			Node node = nodeState.node;
		
			
			ITSState parentState = nodeState.parentState;
			if (parentState == null)
				continue;
			
			if (node.getNodeType() == Node.ELEMENT_NODE) {
			
				if (node.getNodeName().equals("its:param")) {
					String name = node.getAttributes().getNamedItem("name").getNodeValue();
					String value = node.getTextContent();
					paramMap.put(name, value);
					
				} else if (node.getNodeName().equals("its:disambiguationRule")) {
					// GLOBAL
					extractGlobalDisambig(xpath, nodeState);
				} else if (dialect.isRules(node)) { 
					String external = dialect.getExternalRules(node);
					if (external != null) {
						// load extra stuff from xlink
						DocumentBuilder db = getXMLParser();
						
						try {
							Document docExtra = db.parse(docRoot + external);
							DOMSnapshot snapExtra = new DOMSnapshot(docExtra, docRoot, new XMLDialect());
							for (ITSState itssExtra : snapExtra.nodes) {
								if (itssExtra.node.getNodeName().equals("its:disambiguationRule")) {
									extractGlobalDisambig(xpath, itssExtra);
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
						
						//doc.get
					}
				}
			} else {
				if (nodeState.nodePath.contains("its:rules")) 
					// other ITS stuff, ignore
					continue;
				// LOCAL
				extractLocalDisambig(nodeState);
			}
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

	private void extractGlobalDisambig(XPath xpath, ITSState nodeState) {
		Node node = nodeState.node;
		// GLOBAL
		ITSData globalDisambig = new ITSData();

		String selector = null;
		
		XPathExpression classRefExp = null;
		XPathExpression identRefExp = null;
		XPathExpression identExp = null;
		XPathExpression sourceExp = null;
		
		for (int i = 0; i < node.getAttributes().getLength(); i++) {
			Node att = node.getAttributes().item(i);
			if (att.getNodeName().equals("selector")) {
				selector = expandParams(att.getNodeValue());
			} else if (att.getNodeName().endsWith("Pointer")) {
				
				if (att.getNodeName().equals("disambigClassRefPointer")) {
					try {
						classRefExp = xpath.compile(att.getNodeValue());
					} catch (XPathExpressionException xpe) {
						System.err.println(att.getNodeValue());
						xpe.printStackTrace();
					}
				} else 	if (att.getNodeName().equals("disambigIdentRefPointer")) {
					try {
						identRefExp = xpath.compile(att.getNodeValue());
					} catch (XPathExpressionException xpe) {
						System.err.println(att.getNodeValue());
						xpe.printStackTrace();
					}
				} else 	if (att.getNodeName().equals("disambigSourcePointer")) {
					try {
						sourceExp = xpath.compile(att.getNodeValue());
					} catch (XPathExpressionException xpe) {
						System.err.println(att.getNodeValue());
						xpe.printStackTrace();
					}
				} else 	if (att.getNodeName().equals("disambigIdentPointer")) {
					try {
						identExp = xpath.compile(att.getNodeValue());
					} catch (XPathExpressionException xpe) {
						System.err.println(att.getNodeValue());
						xpe.printStackTrace();
					}
				}
				
			} else {
				dialect.readNode(att, globalDisambig);
			}
			
		}

		try {
			XPathExpression expr = xpath.compile(selector);
			NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			
			if (nl.getLength() == 0) {
				System.err.println("Warning: " + selector + " returned empty");
			}
			
			for (int i = 0; i < nl.getLength(); i++) {
				Node item = nl.item(i);
				if (item.getNodeType() == Node.ELEMENT_NODE) {
					ITSState itss = find(item);
					System.out.println(itss.nodePath);
					
					ITSData thisNode = new ITSData(globalDisambig);
					if (identRefExp != null) {
						String identRef = identRefExp.evaluate(item);
						try {
							thisNode.identRef = new URI(identRef);
						} catch (URISyntaxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (classRefExp != null) {
						String classRef = classRefExp.evaluate(item);
						try {
							thisNode.classRef = new URI(classRef);
						} catch (URISyntaxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (sourceExp != null) {
						thisNode.source = sourceExp.evaluate(item);
					}
					if (identExp != null) {
						thisNode.ident = identExp.evaluate(item);
				
					}
					
					applyRuleToNode(itss.nodePath, thisNode);
				}
				
			}
				
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			System.err.println(selector);
			e.printStackTrace();
		}
	}

	private void extractLocalDisambig(ITSState nodeState) {
		Node node = nodeState.node;
		ITSState parentState = nodeState.parentState;
		ITSData parentDisambig = disambiguation.get(parentState.nodePath);
		
		if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
			// Attribute: read values
			String attName = node.getNodeName();
			if (attName.contains("disambig")) {
				
				if (parentDisambig == null) {
					parentDisambig = new ITSData();
					disambiguation.put(parentState.nodePath, parentDisambig);
				}
				
				dialect.readNode(node, parentDisambig);
				
			} else if (dialect.isAnnotatorsRef(node)) {
				
				if (parentDisambig == null) {
					parentDisambig = new ITSData();
					disambiguation.put(parentState.nodePath, parentDisambig);
				}
				
				dialect.readTool(node, parentDisambig);
				
			}
			
			
			
		} else {
			// Elements: handle inheritance 

			if (parentDisambig != null) {
				// inherit from parent
				
				ITSData thisDisambig = disambiguation.get(nodeState.nodePath);
				if (thisDisambig == null) {
					disambiguation.put(nodeState.nodePath, new ITSData(parentDisambig));
				} else {
					// combine inheritance 
					thisDisambig.applyParent(parentDisambig);
				}
				
			}				
		}
	}
	
	public List<String> getLines() {

	
		List<String> lines = new ArrayList<String>();
		for (ITSState nodeState : nodes) {
			ArrayList<String> attributes = new ArrayList<String>();
		
			ITSData disambig = disambiguation.get(nodeState.nodePath);
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
	
	protected static String getPath(Stack<String> path) {
		StringBuilder sb = new StringBuilder();
		for (String string : path) {
			sb.append(string);
		}
		return sb.toString();
	}

	public void gatherNodes(Node node, ITSState parent, List<ITSState> lines, Stack<String> path) {
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

	private void gatherAttributes(List<ITSState> lines, Stack<String> path,
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
	
	public static HtmlDocumentBuilder  getHtmlParser() {
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
		HtmlDocumentBuilder b = new HtmlDocumentBuilder();
		
		b.setErrorHandler(new SystemErrErrorHandler());
		b.setDoctypeExpectation(DoctypeExpectation.HTML);
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
