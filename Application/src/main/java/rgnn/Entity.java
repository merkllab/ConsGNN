package rgnn;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.*;

enum EnType{		
    BEGIN, END
}


public class Entity {

	public class XgmmlEntity {
		public EnType type;
		XgmmlEntity() { ; }
		XgmmlEntity(EnType type){ this.type=type; }
	}
	public class XgmmlGraph extends XgmmlEntity {
	    public String label;

	    public XgmmlGraph(String label,EnType type) {
	        this.label = label;
	        this.type = type;
	    }
	}
	public class XgmmlNode extends XgmmlEntity {
	    public String id;
	    public String label;
	    public XgmmlNode(String id, String label) {
	        this.id = id;
	        this.label = label;
	    }
	    public XgmmlNode(String id, String label,EnType type) {
	    	this(id, label);
	    	super.type = type;
	    }
	}
	public class XgmmlEdge extends XgmmlEntity {
	    public String id;
	    public String label;
	    public String source;
	    public String target;
	    public XgmmlEdge(String id, String label, String source, String target,EnType type) {
	        this.id = id;
	        this.label = label;
	        this.source = source;
	        this.target = target;
	        super.type = type;
	    }
	}
	public class XgmmlAttribute extends XgmmlEntity {
	    public String type;
	    public String name;
	    public String value;
	    public XgmmlAttribute(String type, String name) {
	        this.type = type;
	        this.name = name;
	    }    
	    public XgmmlAttribute(String type, String name, String value,EnType entype) {
	    	this(type,name);
	        this.value = value;
	        super.type = entype;
	    }
	}

	public interface SsnEntity {}
	public class Ssn implements SsnEntity {
	    public String label;
	    public Ssn(String label) {    this.label = label;   }
	}
	public class Similarity implements SsnEntity {
	    public String id;
	    public String label;
	    public String source;
	    public String target;
	    public String percId;
	    public String alignmentScore;
	    public String alignmentLen;
	    public Similarity(String id, String label, String source, String target) {
	        this.id = id;
	        this.label = label;
	        this.source = source;
	        this.target = target;
	    }
	}
	
	public class Sequence implements SsnEntity {
	    public String id;
	    public String label;
	
	    public String uniprotId;
	    public String status;
	    public String sequenceLength;
	    public String taxonomyId;
	    public String gdna;
	    public String description;
	    public String swissprotDescription;
	    public String organism;
	    public String domain;
	    public String gn;
	    public List<String> pfam;
	    public List<String> pdb;
	    public List<String> ipro;
	    public List<String> go;
	    public List<String> gi;
	    public List<String> hmpBodySite;
	    public String hmpOxygen;
	    public String efiId;
	    public String ec;
	    public String phylum;
	    public String clazz;
	    public String order;
	    public String family;
	    public String genus;
	    public String species;
	    public List<String> cazy;
	
	    public Sequence(String id, String label) {
	        this.id = id;
	        this.label = label;
	        this.pfam = new ArrayList<>();
	        this.pdb  = new ArrayList<>();
	        this.ipro  = new ArrayList<>();
	        this.go  = new ArrayList<>();
	        this.gi = new ArrayList<>();
	        this.hmpBodySite = new ArrayList<>();
	        this.cazy = new ArrayList<>();
	    }
	}
	public class RepresentativeSequence implements SsnEntity {
	    public String id;
	    public String label;

	    public List<String> acc;
	    public String clusterSize;

	    public List<String> uniprotId;
	    public List<String> status;
	    public List<String> sequenceLength;
	    public List<String> taxonomyId;
	    public List<String> gdna;
	    public List<String> description;
	    public List<String> swissprotDescription;
	    public List<String> organism;
	    public List<String> domain;
	    public List<String> gn;
	    public List<String> pfam;
	    public List<String> pdb;
	    public List<String> ipro;
	    public List<String> go;
	    public List<String> gi;
	    public List<String> hmpBodySite;
	    public List<String> hmpOxygen;
	    public List<String> efiId;
	    public List<String> ec;
	    public List<String> phylum;
	    public List<String> clazz;
	    public List<String> order;
	    public List<String> family;
	    public List<String> genus;
	    public List<String> species;
	    public List<String> cazy;

	    public RepresentativeSequence(String id, String label) {
	        this.id = id;
	        this.label = label;
	        this.acc = new ArrayList<>();
	        this.uniprotId = new ArrayList<>();
	        this.status = new ArrayList<>();
	        this.sequenceLength = new ArrayList<>();
	        this.taxonomyId = new ArrayList<>();
	        this.gdna = new ArrayList<>();
	        this.description = new ArrayList<>();
	        this.swissprotDescription = new ArrayList<>();
	        this.organism = new ArrayList<>();
	        this.domain = new ArrayList<>();
	        this.gn = new ArrayList<>();
	        this.pfam = new ArrayList<>();
	        this.pdb = new ArrayList<>();
	        this.ipro = new ArrayList<>();
	        this.go = new ArrayList<>();
	        this.gi = new ArrayList<>();
	        this.hmpBodySite = new ArrayList<>();
	        this.hmpOxygen = new ArrayList<>();
	        this.efiId = new ArrayList<>();
	        this.ec = new ArrayList<>();
	        this.phylum = new ArrayList<>();
	        this.clazz = new ArrayList<>();
	        this.order = new ArrayList<>();
	        this.family = new ArrayList<>();
	        this.genus = new ArrayList<>();
	        this.species = new ArrayList<>();
	        this.cazy = new ArrayList<>();
	    }

	    public void removeEntry(int i) {
	        if(acc.size() > i) acc.remove(i);
	        clusterSize = ((Integer)(Integer.parseInt(clusterSize) - 1)).toString();
	        if(uniprotId.size() > i) uniprotId.remove(i);
	        if(status.size() > i) status.remove(i);
	        if(sequenceLength.size() > i) sequenceLength.remove(i);
	        if(taxonomyId.size() > i) taxonomyId.remove(i);
	        if(gdna.size() > i) gdna.remove(i);
	        if(description.size() > i) description.remove(i);
	        if(swissprotDescription.size() > i) swissprotDescription.remove(i);
	        if(organism.size() > i) organism.remove(i);
	        if(domain.size() > i) domain.remove(i);
	        if(gn.size() > i) gn.remove(i);
	        if(pfam.size() > i)pfam.remove(i);
	        if(pdb.size() > i) pdb.remove(i);
	        if(ipro.size() > i) ipro.remove(i);
	        if(go.size() > i) go.remove(i);
	        if(gi.size() > i) gi.remove(i);
	        if(hmpBodySite.size() > i) hmpBodySite.remove(i);
	        if(hmpOxygen.size() > i) hmpOxygen.remove(i);
	        if(efiId.size() > i) efiId.remove(i);
	        if(ec.size() > i) ec.remove(i);
	        if(phylum.size() > i) phylum.remove(i);
	        if(clazz.size() > i) clazz.remove(i);
	        if(order.size() > i) order.remove(i);
	        if(family.size() > i) family.remove(i);
	        if(genus.size() > i) genus.remove(i);
	        if(species.size() > i) species.remove(i);
	        if(cazy.size() > i) cazy.remove(i);
	    }
	}

	public class GnnStatistic implements Serializable{
		public Map<String, Double> neighborhoodUniqueness;
	    public Map<String, Double> clusterUniqueness;
	    public Map<String, Double> pfamUniqueness;
	    public Integer minimumClusterSize;
	    public Integer maximumClusterSize;
	    public Integer minimumPfamSize;
	    public Integer maximumPfamSize;

	    public GnnStatistic() {
	        this.neighborhoodUniqueness = new HashMap<>();
	        this.clusterUniqueness = new HashMap<>();
	        this.pfamUniqueness = new HashMap<>();
	        this.maximumClusterSize = Integer.MIN_VALUE;
	        this.minimumClusterSize = Integer.MAX_VALUE;
	        this.maximumPfamSize = Integer.MIN_VALUE;
	        this.minimumPfamSize = Integer.MAX_VALUE;
	    }   
	}
	
	public interface GnnEntity extends Serializable {}
	public class Gnn implements GnnEntity {
	    public String label;
	    public Gnn(String label) {
	        this.label = label;
	    }
	}
	
	public class FullGnn implements Serializable {
	    private static final long serialVersionUID = 42L;

	    public Gnn gnn;
	    public Map<String, Cluster> clusterMap;
	    public Map<String, Pfam> pfamMap;
	    public Map<String, Neighborhood> clusterNeighborhoodMap;
	    public Map<String, List<Neighborhood>> pfamNeighborhoodMap;
	    public Integer neighborhoodSize;
	    public Integer coocurrence;

	    public FullGnn() {
	        this.clusterMap = new HashMap<>();
	        this.pfamMap = new HashMap<>();
	        this.clusterNeighborhoodMap = new HashMap<>();
	        this.pfamNeighborhoodMap = new HashMap<>();
	    }

	    public FullGnn(FullGnn input) {
	        this.gnn = input.gnn;
	        this.clusterMap = new HashMap<>(input.clusterMap);
	        this.pfamMap = new HashMap<>(input.pfamMap);
	        this.clusterNeighborhoodMap = new HashMap<>(input.clusterNeighborhoodMap);
	        this.pfamNeighborhoodMap = new HashMap<>(input.pfamNeighborhoodMap);
	        this.neighborhoodSize = input.neighborhoodSize;
	        this.coocurrence = input.coocurrence;
	    }

	    public int getNeighborhoodSequenceCount(Neighborhood neighborhood) {
	        Cluster cluster = clusterMap.get(neighborhood.target);
	        return Integer.parseInt(cluster.queriesWithPfamNeighbors);

	    }

	    public int getPfamSequenceCount(String pfamId) {
	        int count = 0;
	        for(Neighborhood neighborhood : pfamNeighborhoodMap.get(pfamId)) {
	            count += getNeighborhoodSequenceCount(neighborhood);
	        }
	        return count;
	    }

	    public int clusterCount() {
	        int count = 0;
	        Set<String> countedClusters = new HashSet<>();
	        for(Cluster cluster : clusterMap.values()) {
	            if(!countedClusters.contains(cluster.clusterNumber)) {
	                count ++;
	                countedClusters.add(cluster.clusterNumber);
	            }
	        }
	        return count;
	    }

	    public int getSequenceCount() {
	        int count = 0;
	        Set<String> countedClusters = new HashSet<>();
	        for(Cluster cluster : clusterMap.values()) {
	            if(!countedClusters.contains(cluster.clusterNumber)) {
	                count += Integer.parseInt(cluster.totalSsnSequences);
	                countedClusters.add(cluster.clusterNumber);
	            }
	        }
	        return count;
	    }
	}
	public class Cluster implements GnnEntity {
	    public String id;
	    public String label;
	    public String nodeFillColor;
	    public String coOccurrence;
	    public String coOccurrenceRatio;
	    public String clusterNumber;
	    public String totalSsnSequences;
	    public String queriesWithPfamNeighbors;
	    public String queriableSsnSequences;
	    public String nodeSize;
	    public String nodeShape;
	    public String averageDistance;
	    public String medianDistance;
	    public String pfamNeighbors;
	    public List<String> queryAccessions;
	    public List<String> queryNeighborArrangement;
	    public List<String> queryNeighborAccessions;

	    public Cluster(String id, String label) {
	        this.id = id;
	        this.label = label;
	        this.queryAccessions = new ArrayList<>();
	        this.queryNeighborArrangement = new ArrayList<>();
	        this.queryNeighborAccessions = new ArrayList<>();
	    }
   
	}
	public class Neighborhood implements GnnEntity {
	    public String label;
	    public String source;
	    public String target;

	    public Neighborhood(String label, String source, String target) {
	        this.label = label;
	        this.source = source;
	        this.target = target;
	    }
	}

	public class Pfam implements GnnEntity {
	    public String id;
	    public String label;
	    public String nodeSize;
	    public String nodeShape;
	    public String pfam;
	    public String pfamDescription;
	    public String totalSsnSequences;
	    public String queriableSsnSequences;
	    public String queriesWithPfamNeighbors;
	    public String pfamNeighbors;
	    public List<String> queryNeighborAccessions;
	    public List<String> queryNeighborArrangement;
	    public List<String> hubAverageAndMedianDistances;
	    public List<String> hubCooccurrenceAndRatio;
	    public String nodeFillColor;
	

	    public Pfam(String id, String label) {
	        this.id = id;
	        this.label = label;
	        this.queryNeighborAccessions = new ArrayList<>();
	        this.queryNeighborArrangement = new ArrayList<>();
	        this.hubAverageAndMedianDistances = new ArrayList<>();
	        this.hubCooccurrenceAndRatio = new ArrayList<>();
	    }
	}
		
	public class ColoredSsnAnalyzerResult implements Serializable {
	    public Map<Integer, Integer> clusterNodeCountMap;
	    public Map<Integer, Map<String, Double>> clusterPhylumPercentageMap;

	    public ColoredSsnAnalyzerResult() {
	        this.clusterNodeCountMap = new HashMap<>();
	        this.clusterPhylumPercentageMap = new HashMap<>();
	    }
	}
	
	public interface ColoredSsnEntity {	}
	
	public class ColoredSimilarity implements ColoredSsnEntity {
	    public String id;
	    public String label;
	    public String source;
	    public String target;
	    public String percId;
	    public String alignmentScore;
	    public String alignmentLen;

	    public ColoredSimilarity(String id, String label, String source, String target) {
	        this.id = id;
	        this.label = label;
	        this.source = source;
	        this.target = target;
	    }
	}
	public class ColoredSsn implements ColoredSsnEntity {
	    public String label;

	    public ColoredSsn(String label) {
	        this.label = label;
	    }
	}
	
	public class ColoredRepresentativeSequence implements ColoredSsnEntity {
	    public String id;
	    public String label;

	    public List<String> acc;
	    public String clusterSize;
	    public String supercluster;

	    public List<String> uniprotId;
	    public List<String> status;
	    public List<String> sequenceLength;
	    public List<String> taxonomyId;
	    public List<String> gdna;
	    public List<String> description;
	    public List<String> swissprotDescription;
	    public List<String> organism;
	    public List<String> domain;
	    public List<String> gn;
	    public List<String> pfam;
	    public List<String> pdb;
	    public List<String> ipro;
	    public List<String> go;
	    public List<String> gi;
	    public List<String> hmpBodySite;
	    public List<String> hmpOxygen;
	    public List<String> efiId;
	    public List<String> ec;
	    public List<String> phylum;
	    public List<String> clazz;
	    public List<String> order;
	    public List<String> family;
	    public List<String> genus;
	    public List<String> species;
	    public List<String> cazy;

	    public ColoredRepresentativeSequence(String id, String label) {
	        this.id = id;
	        this.label = label;
	        this.acc = new ArrayList<>();
	        this.uniprotId = new ArrayList<>();
	        this.status = new ArrayList<>();
	        this.sequenceLength = new ArrayList<>();
	        this.taxonomyId = new ArrayList<>();
	        this.gdna = new ArrayList<>();
	        this.description = new ArrayList<>();
	        this.swissprotDescription = new ArrayList<>();
	        this.organism = new ArrayList<>();
	        this.domain = new ArrayList<>();
	        this.gn = new ArrayList<>();
	        this.pfam = new ArrayList<>();
	        this.pdb = new ArrayList<>();
	        this.ipro = new ArrayList<>();
	        this.go = new ArrayList<>();
	        this.gi = new ArrayList<>();
	        this.hmpBodySite = new ArrayList<>();
	        this.hmpOxygen = new ArrayList<>();
	        this.efiId = new ArrayList<>();
	        this.ec = new ArrayList<>();
	        this.phylum = new ArrayList<>();
	        this.clazz = new ArrayList<>();
	        this.order = new ArrayList<>();
	        this.family = new ArrayList<>();
	        this.genus = new ArrayList<>();
	        this.species = new ArrayList<>();
	        this.cazy = new ArrayList<>();
	    }
	}
	public class ColoredSequence implements ColoredSsnEntity {
	    public String id;
	    public String label;

	    public String uniprotId;
	    public String status;
	    public String supercluster;
	    public String sequenceLength;
	    public String taxonomyId;
	    public String gdna;
	    public String description;
	    public String swissprotDescription;
	    public String organism;
	    public String domain;
	    public String gn;
	    public List<String> pfam;
	    public List<String> pdb;
	    public List<String> ipro;
	    public List<String> go;
	    public List<String> gi;
	    public List<String> hmpBodySite;
	    public String hmpOxygen;
	    public String efiId;
	    public String ec;
	    public String phylum;
	    public String clazz;
	    public String order;
	    public String family;
	    public String genus;
	    public String species;
	    public List<String> cazy;

	    public ColoredSequence(String id, String label) {
	        this.id = id;
	        this.label = label;
	        this.pfam = new ArrayList<>();
	        this.pdb  = new ArrayList<>();
	        this.ipro  = new ArrayList<>();
	        this.go  = new ArrayList<>();
	        this.gi = new ArrayList<>();
	        this.hmpBodySite = new ArrayList<>();
	        this.cazy = new ArrayList<>();
	    }
	}
	
	
	public interface XGnnEntity {	}
	
	public class XGnn implements Entity.XGnnEntity {
	    public String label;
	    public XGnn(String label) {
	        this.label = label;
	    }
	}
	
	public class Family implements XGnnEntity {
	    public String id;
	    public String label;
	    public String pfam;
	    public String description;
	    public List<String> go;
	    public String uniqueness;
	    public String seqCount;
	    public String nodeSize;
	    public String nodeTransparency;


	    public Family(String id, String label) {
	        this.id = id;
	        this.label = label;
	        this.go = new ArrayList<>();
	    }
	}
	
	public class Group implements XGnnEntity {
	    public String id;
	    public String label;
	    public String clusterNumber;
	    public String uniqueness;
	    public String nodeCount;
	    public String seqCount;
	    public String nodeFillColor;
	    public String nodeSize;
	    public String nodeShape;
	    public String nodeTransparency;
	    public List<String> phylumStat;

	    public Group(String id, String label) {
	        this.id = id;
	        this.label = label;
	        this.phylumStat = new ArrayList<>();
	    }
	}	
	public class Relationship implements XGnnEntity{
	    public String label;
	    public String source;
	    public String target;
	    public String uniqueness;
	    public String seqCount;
	    public String coverage;

	    public Relationship(String label, String source, String target) {
	        this.label = label;
	        this.source = source;
	        this.target = target;
	    }
	}
}

