package si.ijs.its20;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import si.ijs.its20.Dialect.OnAttribute;

public class AttributeReader {
	protected final Dialect dialect;
	protected final Map<String, OnAttribute> handlers;

	public AttributeReader(Dialect d) {
		dialect = d;
		handlers = new HashMap<String, OnAttribute>();
		initializeHandlers();
	}
	

	public Dialect getDialect() {
		return dialect;
	}
	protected void initializeHandlers() {
		 handlers.put(dialect.getAttName("disambigGranularity"), new OnAttribute() {
			@Override
			public void set(ITSData item, String nodeValue) {
				item.granularity = ITSData.Granularity.fromString(nodeValue.toLowerCase());
			}
		});
		handlers.put(dialect.getAttName("disambigIdentRef"), new OnAttribute() {
			@Override
			public void set(ITSData item, String nodeValue) {
				try {
					item.identRef = new URI(nodeValue);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});
		handlers.put(dialect.getAttName("disambigIdent"), new OnAttribute() {
			@Override
			public void set(ITSData item, String nodeValue) {
				item.ident = nodeValue;
			}
		});
		handlers.put(dialect.getAttName("disambigSource"), new OnAttribute() {
			@Override
			public void set(ITSData item, String nodeValue) {
				item.source = nodeValue;
			}
		});
		handlers.put(dialect.getAttName("disambigConfidence"), new OnAttribute() {
			@Override
			public void set(ITSData item, String nodeValue) {
				item.confidence = new Double(nodeValue);
			}
		});
		handlers.put(dialect.getAttName("disambigClassRef"), new OnAttribute() {
			@Override
			public void set(ITSData item, String nodeValue) {
				try {
					item.classRef = new URI(nodeValue);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});
		handlers.put(dialect.getAttName("annotatorsRef"), new OnAttribute() {
			@Override
			public void set(ITSData item, String nodeValue) {
				String[] vals = nodeValue.split(" ");
				for (String string : vals) {
					String[] kv = string.split("\\|");
					if (kv.length == 2) {
						item.addTool(kv[0], kv[1]);
					} else {
						System.err.println(string + " is not a good annotatorsRef value");
					}
				}
			}
		});
		handlers.put(dialect.getAttName("domainMapping"), new OnAttribute() {
			
			@Override
			public void set(ITSData item, String nodeValue) {
				item.domainMap = new HashMap<String, String>();
				String[] domainMappingList = nodeValue.split("\\,");
				
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
							item.domainMap.put(tokens.get(j).toLowerCase(), target);
							//log.fine("Domain mapping: [" + tokens.get(j) + "] -> [" + target + "]");
						}
					}
				}
			}
		});
	}
	

	public ITSData readNode(Node node) {
		if (!node.hasAttributes()) {
			return null;
		}
		NamedNodeMap atts = node.getAttributes();
		
		ITSData item = null;
		
		
		for (int i = 0; i < atts.getLength(); i++) {
			Node att = atts.item(i);
			String ns = att.getNamespaceURI();
			//System.out.println(att.getNodeName()+ " " + ns + " " + att.getLocalName() + " " + att.getPrefix() + " " + att.getBaseURI());
				
			if (ns != null && !ns.equals("http://www.w3.org/2005/11/its")) {
				// wrong namespace
				continue;
			}
				
			String localName = att.getLocalName();
			String name = localName != null ? localName : att.getNodeName(); 
			OnAttribute handler = handlers.get(name);
			if (handler != null) {
				if (item == null) {
					item = new ITSData();
				}
				handler.set(item, att.getNodeValue());
			}
		}
			
			
		return item;
	}
}