package si.ijs.its20;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ITSData {
	protected static Logger log = Logger.getLogger( ITSData.class.getName());
	static {
		Handler console = new ConsoleHandler();
		console.setFormatter(new SimpleFormatter());
		console.setLevel(Level.FINER);
		log.addHandler(console);
		log.setLevel(Level.FINER);
	}
	protected List<ToolRef> toolRefs;
	
	protected Granularity granularity;
	protected Double confidence;
	
	// identRef
	protected URI identRef;
	
	// source + ident
	protected String ident;
	protected String source;
	
	// classRef
	protected URI classRef;
	
	// domains
	protected List<String> domains;
	protected Map<String, String> domainMap;
	
	public static class ToolRef {
		public final String dataCategory;
		public final String tool;
		
		public ToolRef(String dc, String t) {
			dataCategory = dc;
			tool = t;
		}
		public boolean equals(Object o) {
			ToolRef tr = (ToolRef) o;
			return tr.dataCategory.equals(dataCategory) && tr.tool.equals(tool);
		}
		
		public int hashCode() {
			return dataCategory.hashCode() ^ tool.hashCode();
		}
	}
	
	public static enum NodeType {
		element, attribute
	}
	
	public static enum Granularity {
		entity, lexicalConcept, ontologyConcept;
		
		
		public String toString() {
			switch (this){
			case entity: return "entity";
			case lexicalConcept: return "lexical-concept";
			case ontologyConcept: return "ontology-concept";
			}
			return "";
		
		}
		
		public static Granularity fromString(String str) {
			if (str.equals("entity")) {
				return entity;
			} else if (str.equals("lexical-concept")) {
				return lexicalConcept;
			} else if (str.equals("ontology-concept")) {
				return ontologyConcept;
			} else {
				return null;
			}
		}
	}

	
	public ITSData() {
	}
	
	
	public void addTool(String dc, String toolRef) {
		ToolRef tool = new ToolRef(dc, toolRef);
		if (toolRefs == null) {
			toolRefs = new ArrayList<ToolRef>();
		}
		toolRefs.add(tool);
		
	}
	
	public void addDomain(String domain) {
		if (domains == null) {
			domains = new ArrayList<String>();
		}
		if (!domains.contains(domain)) {
			domains.add(domain);
		}
	}
	
	public List<String> getAttributes(List<String> lst) {
		if (toolRefs != null) {
			StringBuilder toolB = new StringBuilder("annotatorsRef=\"");
			boolean first = true;
			for (ToolRef tr : toolRefs) {
				if (first) {
					first = false;
				} else {
					toolB.append(' ');
				}
				toolB.append(tr.dataCategory);
				toolB.append('|');
				toolB.append(tr.tool);
			}
			toolB.append("\"");
			lst.add(toolB.toString());
			
		}
		
		if (classRef != null) {
			lst.add("disambigClassRef=\""+ classRef.toString()+ "\"");
		}
		if (confidence != null) {
			lst.add("disambigConfidence=\""+ confidence.toString()+ "\"");
		}
		if (granularity != null) {
			lst.add("disambigGranularity=\"" + granularity.toString()+ "\"");
		}
		if (identRef != null) {
			lst.add("disambigIdentRef=\"" + identRef.toString()+ "\"");
		} else if (ident != null) {
			lst.add("disambigIdent=\"" + ident+ "\"");
			lst.add("disambigSource=\"" + source + "\"");

		}
		
		if (domains != null) {
			StringBuilder domainsB = new StringBuilder("domains=\"");
			boolean first = true;
			for (String dom : domains) {
				if (first) {
					first = false;
				} else {
					domainsB.append(", ");
				}
				/*if (dom.indexOf(' ') > -1) { 
					domainsB.append('\'');
					domainsB.append(dom);
					domainsB.append('\'');
				} else {*/
					domainsB.append(dom);
				//}
			}
			domainsB.append('\"');
			lst.add(domainsB.toString());
		}
		return lst;
	}
	
	public String toString() {
		StringBuilder sb= new StringBuilder();
		boolean first = true;
		for (String att : getAttributes(new ArrayList<String>())) {
			if (first) {
				first = false;
			} else {
				sb.append('\t');
			}
			sb.append(att);
		}
		return sb.toString();
		
	}

	public void inherit(ITSData parentDisambig) {
		
		// disambiguation is not inherited for attributes
		/*if (classRef == null) {
			classRef = parentDisambig.classRef;
		}
		if (identRef == null) {
			identRef = parentDisambig.identRef;
		}
		if (ident == null) {
			ident = parentDisambig.ident;
		}
		if (source == null) {
			source = parentDisambig.source;
		}
		if (granularity == null) {
			granularity = parentDisambig.granularity;
		}
		if (confidence == null) {
			confidence = parentDisambig.confidence;
		}*/
		
		if (parentDisambig.toolRefs != null) {	
			Set<ToolRef> tools = new HashSet<ToolRef>();
			if (this.toolRefs != null) {
				tools.addAll(toolRefs);
			}
			tools.addAll(parentDisambig.toolRefs);
			this.toolRefs = new ArrayList<ToolRef>(tools);
		}
		
		if (domains == null) {
			if (parentDisambig.domains != null) {
				domains = new ArrayList<String>(parentDisambig.domains);
			}
		}
	}
	
	public void applyOverwrite(ITSData data) {
		log.fine("Before overwrite: " + toString());
		log.fine("Overwrite with: " + data.toString());
		if (data.classRef != null) {
			classRef = data.classRef;
		}
		if (data.identRef != null) {
			identRef = data.identRef;
		}
		if (data.ident != null) {
			ident = data.ident;
		}
		if (data.source != null) {
			source = data.source;
		}
		if (data.granularity != null) {
			granularity = data.granularity;
		}
		if (data.confidence != null) {
			confidence = data.confidence;
		}
		
		if (data.toolRefs != null) {	
			Set<ToolRef> tools = new HashSet<ToolRef>();
			if (this.toolRefs != null) {
				tools.addAll(toolRefs);
			}
			tools.addAll(data.toolRefs);
			this.toolRefs = new ArrayList<ToolRef>(tools);
		}
		
		if (data.domains != null) {
			domains = new ArrayList<String>(data.domains);
		} else {
			domains = null;
		}
		log.fine("After overwrite: " + toString());
	}
}
