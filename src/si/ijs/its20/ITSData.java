package si.ijs.its20;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ITSData {
	
	
	protected List<ToolRef> toolRefs;
	
	protected Granularity granularity;
	protected Double confidence;
	// identRef
	protected URI identRef;
	
	// source + ident
	protected String ident;
	protected String source;
	
	//
	protected URI classRef;
	
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
		granularity = Granularity.entity;
	}
	
	public ITSData(ITSData other) {
		this.granularity = other.granularity;
		this.identRef = other.identRef;
		this.ident = other.ident;
		this.source = other.source;
		this.classRef = other.classRef;
		this.confidence = other.confidence;
		if (other.toolRefs != null) {
			this.toolRefs = new ArrayList<ToolRef>(other.toolRefs);
		}
		 
	}
	
	public void addTool(String dc, String toolRef) {
		ToolRef tool = new ToolRef(dc, toolRef);
		if (toolRefs == null) {
			toolRefs = new ArrayList<ToolRef>();
		}
		toolRefs.add(tool);
		
	}
	
	public static ITSData fromIdentRef(URI iref) {
		ITSData d = new ITSData();
		d.identRef = iref;
		return d;
	}
	
	public static ITSData fromSourceIdent(String source, String ident) {
		ITSData d = new ITSData();
		d.source = source;
		d.ident = ident;
		return d;
	}
	
	public static ITSData fromClassRef(URI cref) {
		ITSData d = new ITSData();
		d.classRef = cref;
		return d;
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
		lst.add("disambigGranularity=\"" + granularity.toString()+ "\"");
		if (identRef != null) {
			lst.add("disambigIdentRef=\"" + identRef.toString()+ "\"");
		} else if (ident != null) {
			lst.add("disambigIdent=\"" + ident+ "\"");
			lst.add("disambigSource=\"" + source + "\"");

		}

	
		
		return lst;
		
	}

	public void applyParent(ITSData parentDisambig) {
		
		if (classRef == null) {
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
		}
		
		if (parentDisambig.toolRefs != null) {	
			Set<ToolRef> tools = new HashSet<ToolRef>();
			if (this.toolRefs != null) {
				tools.addAll(toolRefs);
			}
			tools.addAll(parentDisambig.toolRefs);
			this.toolRefs = new ArrayList<ToolRef>(tools);
		}
		
	}
	
	public void applyOverwrite(ITSData parentDisambig) {
		
		if (parentDisambig.classRef != null) {
			classRef = parentDisambig.classRef;
		}
		if (parentDisambig.identRef != null) {
			identRef = parentDisambig.identRef;
		}
		if (parentDisambig.ident != null) {
			ident = parentDisambig.ident;
		}
		if (parentDisambig.source != null) {
			source = parentDisambig.source;
		}
		if (parentDisambig.granularity != null) {
			granularity = parentDisambig.granularity;
		}
		if (parentDisambig.confidence != null) {
			confidence = parentDisambig.confidence;
		}
		
		if (parentDisambig.toolRefs != null) {	
			Set<ToolRef> tools = new HashSet<ToolRef>();
			if (this.toolRefs != null) {
				tools.addAll(toolRefs);
			}
			tools.addAll(parentDisambig.toolRefs);
			this.toolRefs = new ArrayList<ToolRef>(tools);
		}
	}
}
