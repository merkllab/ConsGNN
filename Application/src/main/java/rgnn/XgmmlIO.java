package rgnn;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import java.io.InputStream;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XgmmlIO {
    private static XgmmlIO instance;

    public Project project;
    public ResourceIO resio;
    private Map<File, SsnEventReader> ssnEventReaderMap;
    private Map<File, SsnEventWriter> ssnEventWriterMap;
    private Map<File, ExtendedGNNWriter> extendedGNNWriterMap;
    private Map<File, ColoredSsnEventReader> coloredSsnEventReaderMap;
    
    private Entity entity = new Entity();

    private XgmmlIO() {
        taxonomy = new ResourceIO.Taxonomy();
        projectManager = new ProjectManager();
        project= new Project();
        pfamWhitelist = new ResourceIO.PfamWhitelistReader();
        ssnEventReaderMap = new HashMap<>();
        ssnEventWriterMap = new HashMap<>();
        extendedGNNWriterMap = new HashMap<>();
        coloredSsnEventReaderMap = new HashMap<>();
    }

    public static XgmmlIO getInstance() {
        if(XgmmlIO.instance == null) {
            XgmmlIO.instance = new XgmmlIO();
        }
        return XgmmlIO.instance;
    }

    public Entity.SsnEntity readSsnEvent(File inputFile) throws FileNotFoundException, XMLStreamException {
        if(!ssnEventReaderMap.containsKey(inputFile)) {
            SsnEventReader ssnEventReader = new SsnEventReader(inputFile);
            ssnEventReaderMap.put(inputFile, ssnEventReader);
        }
        SsnEventReader ssnEventReader = ssnEventReaderMap.get(inputFile);
        if(ssnEventReader.hasNext()) {
            return ssnEventReader.next();
        } else {
            ssnEventReader.close();
            ssnEventReaderMap.remove(inputFile);
            return null;
        }
    }

    public Entity.ColoredSsnEntity readColoredSsnEvent(File inputFile) throws FileNotFoundException, XMLStreamException {
        if(!coloredSsnEventReaderMap.containsKey(inputFile)) {
            ColoredSsnEventReader coloredSsnEventReader = new ColoredSsnEventReader(inputFile);
            coloredSsnEventReaderMap.put(inputFile, coloredSsnEventReader);
        }
        ColoredSsnEventReader coloredSsnEventReader = coloredSsnEventReaderMap.get(inputFile);
        if(coloredSsnEventReader.colnextEntity != null){
            return coloredSsnEventReader.next();
        } else {
            coloredSsnEventReader.close();
            coloredSsnEventReaderMap.remove(inputFile);
            return null;
        }
    }

    public void writeSsnEvent(File outputFile, Entity.SsnEntity ssnEntity) throws FileNotFoundException, XMLStreamException {
        if(!ssnEventWriterMap.containsKey(outputFile)) {
            SsnEventWriter ssnEventWriter = new SsnEventWriter(outputFile);
            ssnEventWriterMap.put(outputFile, ssnEventWriter);
        }
        SsnEventWriter ssnEventWriter = ssnEventWriterMap.get(outputFile);
        if(ssnEntity != null) {
            ssnEventWriter.add(ssnEntity);
        } else {
            ssnEventWriter.close();
            ssnEventWriterMap.remove(outputFile);
        }
    }

    public void writeExtendedGnnEvent(File outputFile, Entity.XGnnEntity xGnnEntity) throws FileNotFoundException, XMLStreamException {
        if(!extendedGNNWriterMap.containsKey(outputFile)) {
            ExtendedGNNWriter extendedGNNWriter = new ExtendedGNNWriter(outputFile);
            extendedGNNWriterMap.put(outputFile, extendedGNNWriter);
        }
        ExtendedGNNWriter extendedGNNWriter = extendedGNNWriterMap.get(outputFile);
        if(xGnnEntity != null) {
            extendedGNNWriter.rgadd(xGnnEntity);
        } else {
            extendedGNNWriter.xgclose();
            extendedGNNWriterMap.remove(outputFile);
        }
    }

    public Entity.FullGnn readFullGnn(File inputFile, Integer neighborhoodSize, Integer coocurrence) throws FileNotFoundException, XMLStreamException {
        initGnnFReader(inputFile,neighborhoodSize,coocurrence);
        return gnnFparse();
    }

    public void writeFullGnn(Entity.FullGnn gnn, File outputFile) throws FileNotFoundException, XMLStreamException {
    	initGnnFullWriter(gnn);
        gfwrite(outputFile);
    }

    public List<ResourceIO.Pfam2GoEntry> getGo(String pfam) {
        return resio.getGo(pfam);
    }

    public String getCytoscapeExecutable() {
        return project.getCytoscapeExecutable();
    }

    public void setCytoscapeExecutable(String executable) {
        project.setCytoscapeExecutable(executable);
    }

    public List<Project> listProjects() {
        return project.listProjects();
    }

    public Project createProject(String name) {
        return project.createProject(name);
    }

    public void deleteProject(Project project) {
        project.deleteProject(project);
    }

    
    public class SsnEventReader {
	    private Entity.SsnEntity snextEntity;
	    private List<Entity.SsnEntity> sentityList;
	
	    public SsnEventReader(File inputFile) throws FileNotFoundException, XMLStreamException {
	    	try{
		        initXgmmlEventReader(inputFile);
		        snextEntity = sfindNextEntity();
		        sentityList = new ArrayList<>();
	    	}catch(XMLStreamException xe){
	    		xe.printStackTrace();
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}
	    }
	
	    private Entity.SsnEntity sfindNextEntity() throws XMLStreamException {
	        while(xgnextEvent != null){	
	            Entity.XgmmlEntity xgmmlEntity = xernext();
	            if (xgmmlEntity.type == EnType.BEGIN) {	
	                if (xgmmlEntity instanceof Entity.XgmmlGraph) {
	                	Entity.XgmmlGraph xgmmlGraph = (Entity.XgmmlGraph) xgmmlEntity;
	                    return entity.new Ssn(xgmmlGraph.label);
	                } else if (xgmmlEntity instanceof Entity.XgmmlEdge) {
	                	Entity.XgmmlEdge xgmmlEdge = (Entity.XgmmlEdge) xgmmlEntity;
	                	Entity.Similarity similarity = entity.new Similarity(xgmmlEdge.id, xgmmlEdge.label,xgmmlEdge.source, xgmmlEdge.target);
	                    sentityList.add(similarity);
	                } else if (xgmmlEntity instanceof Entity.XgmmlNode) {
	                	Entity.XgmmlNode xgmmlNode = (Entity.XgmmlNode) xgmmlEntity;
	                	Entity.Sequence sequence = entity.new Sequence(xgmmlNode.id, xgmmlNode.label);
	                    sentityList.add(sequence);
	                } else {
	                	Entity.XgmmlAttribute xgmmlAttribute = (Entity.XgmmlAttribute) xgmmlEntity;
	                    if (xgmmlAttribute.name.equals("ACC")) {
	                        if (sentityList.get(sentityList.size() - 1) instanceof Entity.Sequence) {
	                        	Entity.Sequence sequence = (Entity.Sequence) sentityList.get(sentityList.size() - 1);
	                            sentityList.remove(sentityList.size() - 1);
	                            Entity.RepresentativeSequence repSequence = entity.new RepresentativeSequence(sequence.id, sequence.label);
	                            sentityList.add(repSequence);
	                        }
	                    }
	                    Entity.SsnEntity ssnEntity = sentityList.get(sentityList.size() - 1);
	                    if (ssnEntity instanceof Entity.Sequence) {
	                    	Entity.Sequence sequence = (Entity.Sequence) ssnEntity;
	                        if (xgmmlAttribute.value != null)
	                            addAttributeToSequence(sequence, xgmmlAttribute);
	                    } else if (ssnEntity instanceof Entity.RepresentativeSequence) {
	                    	Entity.RepresentativeSequence repSequence = (Entity.RepresentativeSequence) ssnEntity;
	                        if (xgmmlAttribute.value != null)
	                            addAttributeToRepresentativeSequence(repSequence, xgmmlAttribute);
	                    } else if (ssnEntity instanceof Entity.Similarity) {
	                    	Entity.Similarity similarity = (Entity.Similarity) ssnEntity;
	                        if (xgmmlAttribute.value != null)
	                            addAttributeToSimilarity(similarity, xgmmlAttribute);
	                    }
	                }
	            } else {
	                if (xgmmlEntity instanceof Entity.XgmmlGraph) {
	                    return null;
	                } else if (xgmmlEntity instanceof Entity.XgmmlEdge) {
	                	Entity.SsnEntity ssnEntity = sentityList.get(sentityList.size() - 1);
	                    sentityList.remove(sentityList.size() - 1);
	                    return ssnEntity;
	                } else if (xgmmlEntity instanceof Entity.XgmmlNode) {
	                	Entity.SsnEntity ssnEntity = sentityList.get(sentityList.size() - 1);
	                    sentityList.remove(sentityList.size() - 1);
	                    return ssnEntity;
	                }
	            }
	        }
	        return null;
	    }
	
	    private void addAttributeToSimilarity(Entity.Similarity similarity, Entity.XgmmlAttribute xgmmlAttribute) {
	        switch (xgmmlAttribute.name) {
	            case "-log10(E)":
	                similarity.alignmentScore= xgmmlAttribute.value;
	                break;
	            case "%id":
	                similarity.percId= xgmmlAttribute.value;
	                break;
	            case "alignment_score":
	                similarity.alignmentScore= xgmmlAttribute.value;
	                break;
	            case "alignment_length":
	                similarity.alignmentLen= xgmmlAttribute.value;
	                break;
	        }
	    }
	
	    private void addAttributeToRepresentativeSequence(Entity.RepresentativeSequence sequence, Entity.XgmmlAttribute xgmmlAttribute) {
	        switch (xgmmlAttribute.name) {
	            case "ACC":
	                sequence.acc.add(xgmmlAttribute.value);
	                break;
	            case "Cluster Size":
	                sequence.clusterSize= xgmmlAttribute.value;
	                break;
	            case "Uniprot_ID":
	                sequence.uniprotId.add(xgmmlAttribute.value);
	                break;
	            case "STATUS":
	                sequence.status.add(xgmmlAttribute.value);
	                break;
	            case "Sequence_Length":
	                sequence.sequenceLength.add(xgmmlAttribute.value);
	                break;
	            case "Taxonomy ID":	                
	            case "Taxonomy_ID":
	                sequence.taxonomyId.add(xgmmlAttribute.value);
	                break;
	            case "GDNA":
	                sequence.gdna.add(xgmmlAttribute.value);
	                break;
	            case "Description":
	                sequence.description.add(xgmmlAttribute.value);
	                break;
	            case "Swissprot_Description":
	                sequence.swissprotDescription.add(xgmmlAttribute.value);
	                break;
	            case "Organism":
	                sequence.organism.add(xgmmlAttribute.value);
	                break;
	            case "Domain":
	                sequence.domain.add(xgmmlAttribute.value);
	                break;
	            case "GN":
	                sequence.gn.add(xgmmlAttribute.value);
	                break;
	            case "PFAM":
	                sequence.pfam.add(xgmmlAttribute.value);
	                break;
	            case "PDB":
	                sequence.pdb.add(xgmmlAttribute.value);
	                break;
	            case "IPRO":
	                sequence.ipro.add(xgmmlAttribute.value);
	                break;
	            case "GO":
	                sequence.go.add(xgmmlAttribute.value);
	                break;
	            case "GI":
	                sequence.gi.add(xgmmlAttribute.value);
	                break;
	            case "HMP_Body_Site":
	                sequence.hmpBodySite.add(xgmmlAttribute.value);
	                break;
	            case "HMP_Oxygen":
	                sequence.hmpOxygen.add(xgmmlAttribute.value);
	                break;
	            case "EFI_ID":
	                sequence.efiId.add(xgmmlAttribute.value);
	                break;
	            case "EC":
	                sequence.ec.add(xgmmlAttribute.value);
	                break;
	            case "PHYLUM":
	                sequence.phylum.add(xgmmlAttribute.value);
	                break;
	            case "CLASS":
	                sequence.clazz.add(xgmmlAttribute.value);
	                break;
	            case "ORDER":
	                sequence.order.add(xgmmlAttribute.value);
	                break;
	            case "FAMILY":
	                sequence.family.add(xgmmlAttribute.value);
	                break;
	            case "GENUS":
	                sequence.genus.add(xgmmlAttribute.value);
	                break;
	            case "SPECIES":
	                sequence.species.add(xgmmlAttribute.value);
	                break;
	            case "CAZY":
	                sequence.cazy.add(xgmmlAttribute.value);
	                break;
	
	        }
	    }
	
	    private void addAttributeToSequence(Entity.Sequence sequence, Entity.XgmmlAttribute xgmmlAttribute) {
	        switch (xgmmlAttribute.name) {
	            case "Uniprot_ID":
	                sequence.uniprotId= xgmmlAttribute.value;
	                break;
	            case "STATUS":
	                sequence.status= xgmmlAttribute.value;
	                break;
	            case "Sequence_Length":
	                sequence.sequenceLength= xgmmlAttribute.value;
	                break;
	            case "Taxonomy ID":
	            case "Taxonomy_ID":
	                sequence.taxonomyId= xgmmlAttribute.value;
	                break;
	            case "GDNA":
	                sequence.gdna= xgmmlAttribute.value;
	                break;
	            case "Description":
	                sequence.description= xgmmlAttribute.value;
	                break;
	            case "Swissprot_Description":
	                sequence.swissprotDescription= xgmmlAttribute.value;
	                break;
	            case "Organism":
	                sequence.organism= xgmmlAttribute.value;
	                break;
	            case "Domain":
	                sequence.domain= xgmmlAttribute.value;
	                break;
	            case "GN":
	                sequence.gn= xgmmlAttribute.value;
	                break;
	            case "PFAM":
	                sequence.pfam.add(xgmmlAttribute.value);
	                break;
	            case "PDB":
	                sequence.pdb.add(xgmmlAttribute.value);
	                break;
	            case "IPRO":
	                sequence.ipro.add(xgmmlAttribute.value);
	                break;
	            case "GO":
	                sequence.go.add(xgmmlAttribute.value);
	                break;
	            case "GI":
	                sequence.gi.add(xgmmlAttribute.value);
	                break;
	            case "HMP_Body_Site":
	                sequence.hmpBodySite.add(xgmmlAttribute.value);
	                break;
	            case "HMP_Oxygen":
	                sequence.hmpOxygen= xgmmlAttribute.value;
	                break;
	            case "EFI_ID":
	                sequence.efiId= xgmmlAttribute.value;
	                break;
	            case "EC":
	                sequence.ec= xgmmlAttribute.value;
	                break;
	            case "PHYLUM":
	                sequence.phylum= xgmmlAttribute.value;
	                break;
	            case "CLASS":
	                sequence.clazz= xgmmlAttribute.value;
	                break;
	            case "ORDER":
	                sequence.order= xgmmlAttribute.value;
	                break;
	            case "FAMILY":
	                sequence.family= xgmmlAttribute.value;
	                break;
	            case "GENUS":
	                sequence.genus= xgmmlAttribute.value;
	                break;
	            case "SPECIES":
	                sequence.species= xgmmlAttribute.value;
	                break;
	            case "CAZY":
	                sequence.cazy.add(xgmmlAttribute.value);
	                break;
	
	        }
	    }
	
	    public boolean hasNext() {
	        return snextEntity != null;
	    }
	
	    public Entity.SsnEntity next() throws XMLStreamException {
	        if (hasNext()) {
	        	Entity.SsnEntity returnValue = snextEntity;
	            snextEntity = sfindNextEntity();
	            return returnValue;
	        } else {
	            return null;
	        }
	    }
	
	    public void close() throws XMLStreamException {
	        xgclose();
	    }
    } 
    
    public class SsnEventWriter {
	
	    public SsnEventWriter(File outputFile) throws FileNotFoundException, XMLStreamException {
	    	initXEWriter(outputFile);
	    }
	
	    public void add(Entity.SsnEntity ssnEntity) throws XMLStreamException {
	        if(ssnEntity instanceof Entity.Ssn) {
	            writeSsn((Entity.Ssn) ssnEntity);
	        } else if(ssnEntity instanceof Entity.Similarity) {
	            writeSimilarity((Entity.Similarity) ssnEntity);
	        } else if(ssnEntity instanceof Entity.Sequence) {
	            writeSequence((Entity.Sequence) ssnEntity);
	        } else if(ssnEntity instanceof Entity.RepresentativeSequence) {
	            writeRepSequence((Entity.RepresentativeSequence) ssnEntity);
	        }
	
	    }
	
	    private void writeSsn(Entity.Ssn ssn) throws XMLStreamException {
	    	Entity.XgmmlGraph xgmmlGraph = entity.new XgmmlGraph(ssn.label,EnType.BEGIN);
	        xewadd(xgmmlGraph);
	    }
	
	    private void writeSimilarity(Entity.Similarity similarity) throws XMLStreamException {
	    	Entity.XgmmlEdge xgmmlEdge = entity.new XgmmlEdge(similarity.id, similarity.label,similarity.source, similarity.target,EnType.BEGIN);
	        xewadd(xgmmlEdge);
	
	        writeAttribute("real", "%id", similarity.percId);
	        writeAttribute("real", "alignment_score", similarity.alignmentScore);
	        writeAttribute("integer", "alignment_length", similarity.alignmentLen);
	
	        xgmmlEdge = entity.new XgmmlEdge(null,null,null,null,EnType.END);
	        xewadd(xgmmlEdge);
	    }
	
	    private void writeSequence(Entity.Sequence sequence) throws XMLStreamException {
	    	Entity.XgmmlNode xgmmlNode = entity.new XgmmlNode(sequence.id, sequence.label,EnType.BEGIN);
	        xewadd(xgmmlNode);
	
	        writeAttribute("string", "Uniprot_ID", sequence.uniprotId);
	        writeAttribute("string", "STATUS", sequence.status);
	        writeAttribute("integer", "Sequence_Length", sequence.sequenceLength);
	        writeAttribute("string", "Taxonomy_ID", sequence.taxonomyId);
	        writeAttribute("string", "GDNA", sequence.gdna);
	        writeAttribute("string", "Description", sequence.description);
	        writeAttribute("string", "Swissprot_Description", sequence.swissprotDescription);
	        writeAttribute("string", "Organism", sequence.organism);
	        writeAttribute("string", "Domain", sequence.domain);
	        writeAttribute("string", "GN", sequence.gn);
	        writeAttributeList("string", "PFAM", sequence.pfam);
	        writeAttributeList("string", "PDB", sequence.pdb);
	        writeAttributeList("string", "IPRO", sequence.ipro);
	        writeAttributeList("string", "GO", sequence.go);
	        writeAttributeList("string", "GI", sequence.gi);
	        writeAttributeList("string", "HMP_Body_Site", sequence.hmpBodySite);
	        writeAttribute("string", "HMP_Oxygen", sequence.hmpOxygen);
	        writeAttribute("string", "EFI_ID", sequence.efiId);
	        writeAttribute("string", "EC", sequence.ec);
	        writeAttribute("string", "PHYLUM", sequence.phylum);
	        writeAttribute("string", "CLASS", sequence.clazz);
	        writeAttribute("string", "ORDER", sequence.order);
	        writeAttribute("string", "FAMILY", sequence.family);
	        writeAttribute("string", "GENUS", sequence.genus);
	        writeAttribute("string", "SPECIES", sequence.species);
	        writeAttributeList("string", "CAZY", sequence.cazy);
	        xgmmlNode = entity.new XgmmlNode(null, null,EnType.END);
	        xewadd(xgmmlNode);
	    }
	
	    private void writeRepSequence(Entity.RepresentativeSequence sequence) throws XMLStreamException {
	    	Entity.XgmmlNode xgmmlNode = entity.new XgmmlNode(sequence.id, sequence.label,EnType.BEGIN);
	        xewadd(xgmmlNode);
	
	        writeAttributeList("string", "ACC", sequence.acc);
	        writeAttributeList("string", "Uniprot_ID", sequence.uniprotId);
	        writeAttributeList("string", "STATUS", sequence.status);
	        writeAttributeList("integer", "Sequence_Length", sequence.sequenceLength);
	        writeAttributeList("string", "Taxonomy_ID", sequence.taxonomyId);
	        writeAttributeList("string", "GDNA", sequence.gdna);
	        writeAttributeList("string", "Description", sequence.description);
	        writeAttributeList("string", "Swissprot_Description", sequence.swissprotDescription);
	        writeAttributeList("string", "Organism", sequence.organism);
	        writeAttributeList("string", "Domain", sequence.domain);
	        writeAttributeList("string", "GN", sequence.gn);
	        writeAttributeList("string", "PFAM", sequence.pfam);
	        writeAttributeList("string", "PDB", sequence.pdb);
	        writeAttributeList("string", "IPRO", sequence.ipro);
	        writeAttributeList("string", "GO", sequence.go);
	        writeAttributeList("string", "GI", sequence.gi);
	        writeAttributeList("string", "HMP_Body_Site", sequence.hmpBodySite);
	        writeAttributeList("string", "HMP_Oxygen", sequence.hmpOxygen);
	        writeAttributeList("string", "EFI_ID", sequence.efiId);
	        writeAttributeList("string", "EC", sequence.ec);
	        writeAttributeList("string", "PHYLUM", sequence.phylum);
	        writeAttributeList("string", "CLASS", sequence.clazz);
	        writeAttributeList("string", "ORDER", sequence.order);
	        writeAttributeList("string", "FAMILY", sequence.family);
	        writeAttributeList("string", "GENUS", sequence.genus);
	        writeAttributeList("string", "SPECIES", sequence.species);
	        writeAttributeList("string", "CAZY", sequence.cazy);
	        writeAttribute("integer", "Cluster Size", sequence.clusterSize);
	        xgmmlNode = entity.new XgmmlNode(null, null,EnType.END);
	        xewadd(xgmmlNode);  
	    }
	
	    private void writeAttribute(String type, String name, String value) throws XMLStreamException {
	        if(value != null) {
	        	Entity.XgmmlAttribute xgmmlAttribute = entity.new XgmmlAttribute(type, name, value,EnType.BEGIN);
	            xewadd(xgmmlAttribute);  
	            xgmmlAttribute = entity.new XgmmlAttribute(null, null, null,EnType.END); 
	            xewadd(xgmmlAttribute); 
	        }
	    }
	
	    private void writeAttributeList(String type, String name, List<String> values) throws XMLStreamException {
	        if(values != null && values.size() > 0) {
	        	Entity.XgmmlAttribute xgmmlAttribute = entity.new XgmmlAttribute("list", name,null,EnType.BEGIN);
	        	xewadd(xgmmlAttribute);
	            for (String value : values) {
	                writeAttribute(type, name, value);
	            }
	            xgmmlAttribute = entity.new XgmmlAttribute(null,null,null,EnType.END);
	            xewadd(xgmmlAttribute);
	        }
	    }
	
	    public void close() throws XMLStreamException {
	    	Entity.XgmmlGraph xgmmlGraph = entity.new XgmmlGraph(null,EnType.END);
	    	xewadd(xgmmlGraph);
	    	xeclose();
	    }
    }
    
    private Entity.GnnEntity genextEntity;
    private List<Entity.GnnEntity> geentityList;
    
    private int ap1=0, ap2= 0, ap3= 0;
    private int ac1=0, ac2= 0, ac3= 0;

    public void initGEReader(File inputFile) throws FileNotFoundException, XMLStreamException {
        initXgmmlEventReader(inputFile);
        genextEntity = gefindNextEntity();
        geentityList = new ArrayList<>();
    }

    public Entity.GnnEntity genext() throws XMLStreamException {
        if(genextEntity != null) {
        	Entity.GnnEntity returnValue = genextEntity;
            genextEntity = gefindNextEntity();
            return returnValue;
        } else {
            return null;
        }
    }

    private Entity.GnnEntity gefindNextEntity() throws XMLStreamException {
        while(xgnextEvent != null){	
        	Entity.XgmmlEntity xgmmlEntity= xernext();
        	EnType et= xgmmlEntity.type;
            if(et == EnType.BEGIN) {
                if(xgmmlEntity instanceof Entity.XgmmlGraph) {
                	Entity.XgmmlGraph xgmmlGraph = (Entity.XgmmlGraph)xgmmlEntity;
                    return entity.new Gnn(xgmmlGraph.label);
                } else if(xgmmlEntity instanceof Entity.XgmmlEdge) {
                	Entity.XgmmlEdge xgmmlEdge = (Entity.XgmmlEdge)xgmmlEntity;
                	Entity.Neighborhood neighborhood = entity.new Neighborhood( xgmmlEdge.label,
                            xgmmlEdge.source, xgmmlEdge.target);
                    geentityList.add(neighborhood);
                } else if(xgmmlEntity instanceof Entity.XgmmlNode) {
                	Entity.XgmmlNode xgmmlNode = (Entity.XgmmlNode)xgmmlEntity;
                    if(xgmmlNode.id.contains(":")) {
                    	Entity.Cluster cluster = entity.new Cluster(xgmmlNode.id, xgmmlNode.label);
                        geentityList.add(cluster);
                    } else {
                    	Entity.Pfam pfam = entity.new Pfam(xgmmlNode.id, xgmmlNode.label);
                        geentityList.add(pfam);
                    }
                } else {
                	Entity.XgmmlAttribute xgmmlAttribute = (Entity.XgmmlAttribute)xgmmlEntity;
                	Entity.GnnEntity gnnEntity = geentityList.get(geentityList.size()-1);
                    if(gnnEntity instanceof Entity.Cluster) {
                    	Entity.Cluster cluster = (Entity.Cluster)gnnEntity;
                        if(xgmmlAttribute.value != null)
                            addAttributeToCluster(cluster, xgmmlAttribute);
                    } else if(gnnEntity instanceof Entity.Pfam) {
                    	Entity.Pfam pfam = (Entity.Pfam)gnnEntity;
                        if(xgmmlAttribute.value != null)
                            addAttributeToPfam(pfam, xgmmlAttribute);
                    } else if(gnnEntity instanceof Entity.Neighborhood) {
                    	Entity.Neighborhood neighborhood = (Entity.Neighborhood) gnnEntity;
                        if(xgmmlAttribute.value != null)
                            addAttributeToNeighborhood(neighborhood, xgmmlAttribute);
                    }
                    else{ System.out.printf("NOT FOUND\n"); }
                }
            } else { 
                if(xgmmlEntity instanceof Entity.XgmmlGraph) {
                    return null;
                } else if(xgmmlEntity instanceof Entity.XgmmlEdge) {
                	Entity.GnnEntity gnnEntity = geentityList.get(geentityList.size()-1);
                    geentityList.remove(geentityList.size()-1);
                    return gnnEntity;
                } else if(xgmmlEntity instanceof Entity.XgmmlNode) {
                	Entity.GnnEntity gnnEntity = geentityList.get(geentityList.size()-1);
                    geentityList.remove(geentityList.size()-1);
                    return gnnEntity;
                }
            }
        }
        return null;
    }

    private void addAttributeToNeighborhood(Entity.Neighborhood neighborhood, Entity.XgmmlAttribute xgmmlAttribute) {
        switch (xgmmlAttribute.name) {
        }
    }

    private void addAttributeToPfam(Entity.Pfam pfam, Entity.XgmmlAttribute xgmmlAttribute) {
        switch (xgmmlAttribute.name) {
            case "node.size":
                pfam.nodeSize= xgmmlAttribute.value;
                break;
            case "node.shape":
                pfam.nodeShape= xgmmlAttribute.value;
                break;
            case "Pfam":
            	ap3++;
                pfam.pfam= xgmmlAttribute.value;
                break;
            case "Pfam Description":
                pfam.pfamDescription= xgmmlAttribute.value;
                break;
            case "# of Sequences in SSN Cluster with Neighbors":
            	ap1++;
                pfam.totalSsnSequences= xgmmlAttribute.value;
                break;
            case "# of Sequences in SSN Cluster":
            	ap2++;
                pfam.queriableSsnSequences= xgmmlAttribute.value;
                break;
            case "# of Queries with Pfam Neighbors":    	
                pfam.queriesWithPfamNeighbors= xgmmlAttribute.value;
                break;
            case "# of Pfam Neighbors":          	
                pfam.pfamNeighbors= xgmmlAttribute.value;
                break;
            case "node.fillColor":
                pfam.nodeFillColor= xgmmlAttribute.value;
                break;
            case "Query-Neighbor Accessions":
                pfam.queryNeighborAccessions.add(xgmmlAttribute.value);
                break;
            case "Query-Neighbor Arrangement":
                pfam.queryNeighborArrangement.add(xgmmlAttribute.value);
                break;
            case "Hub Average and Median Distances":
                pfam.hubAverageAndMedianDistances.add(xgmmlAttribute.value);
                break;
            case "Hub Co-occurrence and Ratio":
                pfam.hubCooccurrenceAndRatio.add(xgmmlAttribute.value);
                break;
            default:
            	System.out.printf("unknown Pfam attribute: <%s> v.Pfam %s L=%s\n",xgmmlAttribute.name,pfam.pfam,pfam.label);
        }
    }

    private void addAttributeToCluster(Entity.Cluster cluster, Entity.XgmmlAttribute xgmmlAttribute) {
        switch (xgmmlAttribute.name) {
            case "node.fillColor":
                cluster.nodeFillColor= xgmmlAttribute.value;
                break;
            case "Co-occurrence":
                cluster.coOccurrence= xgmmlAttribute.value;
                break;
            case "Co-occurrence Ratio":
                cluster.coOccurrenceRatio= xgmmlAttribute.value;
                break;
            case "Cluster Number":
            	ac1++;
                cluster.clusterNumber= xgmmlAttribute.value;
                break;
            case "# of Sequences in SSN Cluster with Neighbors":
            	ac2++;
                cluster.totalSsnSequences= xgmmlAttribute.value;
                break;
            case "# of Queries with Pfam Neighbors":
                cluster.queriesWithPfamNeighbors= xgmmlAttribute.value;
                break;
            case "# of Sequences in SSN Cluster":         		
            	ac3++;
                cluster.queriableSsnSequences= xgmmlAttribute.value;
                break;
            case "node.size":
                cluster.nodeSize= xgmmlAttribute.value;
                break;
            case "node.shape":
                cluster.nodeShape= xgmmlAttribute.value;
                break;
            case "Average Distance":
                cluster.averageDistance= xgmmlAttribute.value;
                break;
            case "Median Distance":
                cluster.medianDistance= xgmmlAttribute.value;
                break;
            case "# of Pfam Neighbors":	
                cluster.pfamNeighbors= xgmmlAttribute.value;
                break;
            case "Query Accessions":
                cluster.queryAccessions.add(xgmmlAttribute.value);
                break;
            case "Query-Neighbor Accessions":
                cluster.queryNeighborAccessions.add(xgmmlAttribute.value);
                break;
            case "Query-Neighbor Arrangement":
                cluster.queryNeighborArrangement.add(xgmmlAttribute.value);
                break;
            case "Pfam":
            case "Pfam Description":
            	if(xgmmlAttribute.value!=null && xgmmlAttribute.value.length() > 0){
            		System.out.printf("Cl.Attr: %s = %s\n",xgmmlAttribute.name,xgmmlAttribute.value);
            	}
            	break;
            default:
            	System.out.printf("GnnEventReader: unknown Cl. attribute: >%s< v.Cl. L=%s ID=%s\n",xgmmlAttribute.name,cluster.label,cluster.id);
        }
    }

    public void geclose() throws XMLStreamException {
    	System.out.printf("close GnnEventReader: pfam: %d %d %d\n",ap1,ap2,ap3);
    	System.out.printf("cluster: %d %d %d\n",ac1,ac2,ac3);  	
        xgclose();
    }
    
    public void initGEWriter(File outputFile) throws FileNotFoundException, XMLStreamException {	
    	initXEWriter(outputFile);
    }

    public void gewadd(Entity.GnnEntity gnnEntity) throws XMLStreamException {
        if(gnnEntity instanceof Entity.Gnn) {
            writeGnn((Entity.Gnn) gnnEntity);
        } else if(gnnEntity instanceof Entity.Cluster) {
            writeCluster((Entity.Cluster) gnnEntity);
        } else if(gnnEntity instanceof Entity.Neighborhood) {
            writeNeighborhood((Entity.Neighborhood) gnnEntity);
        } else if(gnnEntity instanceof Entity.Pfam) {
            writePfam((Entity.Pfam) gnnEntity);
        }
    }

    private void writePfam(Entity.Pfam pfam) throws XMLStreamException {
    	Entity.XgmmlNode xgmmlNode = entity.new XgmmlNode(pfam.id, pfam.label,EnType.BEGIN);
        xewadd(xgmmlNode);

        writeAttribute("string", "node.size", pfam.nodeSize);
        writeAttribute("string", "node.shape", pfam.nodeShape);
        writeAttribute("string", "Pfam", pfam.pfam);
        writeAttribute("string", "Pfam Description", pfam.pfamDescription);
        writeAttribute("integer", "# of Sequences in SSN Cluster with Neighbors", pfam.totalSsnSequences);
        writeAttribute("integer", "# of Sequences in SSN Cluster", pfam.queriableSsnSequences);
        writeAttribute("integer", "# of Queries with Pfam Neighbors", pfam.queriesWithPfamNeighbors);
        writeAttribute("integer", "# of Pfam Neighbors", pfam.pfamNeighbors);
        writeAttribute("string", "node.fillColor", pfam.nodeFillColor);
        writeAttributeList("string", "Query-Neighbor Accessions", pfam.queryNeighborAccessions);
        writeAttributeList("string", "Query-Neighbor Arrangement", pfam.queryNeighborArrangement);
        writeAttributeList("string", "Hub Average and Median Distances", pfam.hubAverageAndMedianDistances);
        writeAttributeList("string", "Hub Co-occurrence and Ratio", pfam.hubCooccurrenceAndRatio);

        xgmmlNode = entity.new XgmmlNode(null, null,EnType.END);
        xewadd(xgmmlNode);
    }

    private void writeCluster(Entity.Cluster cluster) throws XMLStreamException {
    	Entity.XgmmlNode xgmmlNode = entity.new XgmmlNode(cluster.id, cluster.label,EnType.BEGIN);
        xewadd(xgmmlNode);

        writeAttribute("string", "node.fillColor", cluster.nodeFillColor);
        writeAttribute("real", "Co-occurrence", cluster.coOccurrence);
        writeAttribute("string", "Co-occurrence Ratio", cluster.coOccurrenceRatio);
        writeAttribute("integer", "SSN Cluster Number", cluster.clusterNumber);
        writeAttribute("integer", "# of Sequences in SSN Cluster with Neighbors", cluster.totalSsnSequences);
        writeAttribute("integer", "# of Queries with Pfam Neighbors", cluster.queriesWithPfamNeighbors);
        writeAttribute("integer", "# of Sequences in SSN Cluster", cluster.queriableSsnSequences);
        writeAttribute("string", "node.size", cluster.nodeSize);
        writeAttribute("string", "node.shape", cluster.nodeShape);
        writeAttribute("real", "Average Distance", cluster.averageDistance);
        writeAttribute("real", "Median Distance", cluster.medianDistance);
        writeAttribute("integer", "Pfam Neighbors", cluster.pfamNeighbors);
        writeAttributeList("string", "Query Accessions", cluster.queryAccessions);
        writeAttributeList("string", "Query-Neighbor Accessions", cluster.queryNeighborAccessions);
        writeAttributeList("string", "Query-Neighbor Arrangement", cluster.queryNeighborArrangement);

        xgmmlNode = entity.new XgmmlNode(null, null,EnType.END);
        xewadd(xgmmlNode);
    }

    private void writeNeighborhood(Entity.Neighborhood neighborhood) throws XMLStreamException {
        Entity.XgmmlEdge xgmmlEdge = entity.new XgmmlEdge("", neighborhood.label,neighborhood.source, neighborhood.target,EnType.BEGIN);
        xewadd(xgmmlEdge);

        xgmmlEdge = entity.new XgmmlEdge("", null,null, null,EnType.END);
        xewadd(xgmmlEdge);
    }

    private void writeGnn(Entity.Gnn gnn) throws XMLStreamException {
    	Entity.XgmmlGraph xgmmlGraph = entity.new XgmmlGraph(gnn.label,EnType.BEGIN);
        xewadd(xgmmlGraph);
    }

    private void writeAttribute(String type, String name, String value) throws XMLStreamException {
        if(value != null) {
        	Entity.XgmmlAttribute xgmmlAttribute = entity.new XgmmlAttribute(type, name, value,EnType.BEGIN);
            xewadd(xgmmlAttribute);
            xgmmlAttribute = entity.new XgmmlAttribute(null, null, null,EnType.END);
            xewadd(xgmmlAttribute);
        }
    }

    private void writeAttributeList(String type, String name, List<String> values) throws XMLStreamException {
        if(values != null && values.size() > 0) {
        	Entity.XgmmlAttribute xgmmlAttribute = entity.new XgmmlAttribute("list", name,null,EnType.BEGIN);
        	xewadd(xgmmlAttribute);
            for (String value : values) {
                writeAttribute(type, name, value);
            }

            xgmmlAttribute = entity.new XgmmlAttribute(null, null,null,EnType.END);
            xewadd(xgmmlAttribute);
        }
    }

    public void gewclose() throws XMLStreamException {
        Entity.XgmmlGraph xgmmlGraph = entity.new XgmmlGraph(null,EnType.END);
        xewadd(xgmmlGraph);
        xeclose();
    }

    private Entity.FullGnn fullGnn;

    public void initGnnFReader(File gnnFile, Integer neighborhoodSize, Integer coocurrence) throws FileNotFoundException, XMLStreamException {
        initGEReader(gnnFile);
        fullGnn = entity.new FullGnn();
        fullGnn.neighborhoodSize= neighborhoodSize;
        fullGnn.coocurrence= coocurrence;
    }

	public Entity.FullGnn gnnFparse() throws XMLStreamException {
        while(genextEntity != null){	
            Entity.GnnEntity entity = genext();
            if(entity instanceof Entity.Gnn) {
                fullGnn.gnn= (Entity.Gnn) entity;
            } else if(entity instanceof Entity.Cluster) {
            	Entity.Cluster cluster = (Entity.Cluster) entity;
                fullGnn.clusterMap.put(cluster.id, cluster);
            } else if(entity instanceof Entity.Neighborhood) {
            	Entity.Neighborhood neighborhood = (Entity.Neighborhood) entity;
                fullGnn.clusterNeighborhoodMap.put(neighborhood.target,neighborhood);
                if(!fullGnn.pfamNeighborhoodMap.containsKey(neighborhood.source)) {
                    fullGnn.pfamNeighborhoodMap.put(neighborhood.source, new ArrayList<>());
                }
                fullGnn.pfamNeighborhoodMap.get(neighborhood.source).add(neighborhood);
            } else if(entity instanceof Entity.Pfam) {
            	Entity.Pfam pfam = (Entity.Pfam) entity;
                fullGnn.pfamMap.put(pfam.id, pfam);
            }
        }
        geclose();
        return fullGnn;
    }
    
    public void initGnnFullWriter(Entity.FullGnn fullGnn) {
        this.fullGnn = fullGnn;
    }

    public void gfwrite(File outputFile) throws FileNotFoundException, XMLStreamException {
        initGEWriter(outputFile);
        gewadd(fullGnn.gnn);
        for(Entity.Pfam pfam : fullGnn.pfamMap.values()) {
        	gewadd(pfam);
        }
        for(Entity.Cluster cluster : fullGnn.clusterMap.values()) {
        	gewadd(cluster);
        }
        for(Entity.Neighborhood neighborhood : fullGnn.clusterNeighborhoodMap.values()) {
            gewadd(neighborhood);
        }
        gewclose();
    }  
    
    public class ColoredSsnEventReader {
	    private Entity.ColoredSsnEntity colnextEntity;
	    private List<Entity.ColoredSsnEntity> colentityList;
	    private File inputFile;
	    private boolean isRepresentative;
	
	    public ColoredSsnEventReader(File inputFile) throws FileNotFoundException, XMLStreamException {
	        this.inputFile = inputFile;
	        colentityList = new ArrayList<>();
	        isRepresentative = checkRepresentative();	
	        isRepresentative= false;
	        initXgmmlEventReader(inputFile);
	        colnextEntity = colfindNextEntity();
	    }
	
	    private boolean checkRepresentative() throws XMLStreamException, FileNotFoundException {
	        initXgmmlEventReader(inputFile);
	        while (xgnextEvent != null){
	        	Entity.XgmmlEntity xgmmlEvent = xernext();
	            if (xgmmlEvent instanceof Entity.XgmmlAttribute) {
	            	Entity.XgmmlAttribute xgmmlAttribute = (Entity.XgmmlAttribute) xgmmlEvent;
	                if (xgmmlAttribute.name != null) {
	                    if (xgmmlAttribute.name.equals("Uniprot_ID")) {
	                        if (xgmmlAttribute.type.equals("list")) {
	                            return true;
	                        } else {
	                            return false;
	                        }
	                    }
	                }
	            }
	        }
	        return false;
	    }
	
	    private Entity.ColoredSsnEntity colfindNextEntity() throws XMLStreamException {
	    	while (xgnextEvent != null){	
	    		Entity.XgmmlEntity xgmmlEvent = xernext();
	            if(xgmmlEvent.type == EnType.BEGIN)	{
	                if (xgmmlEvent instanceof Entity.XgmmlGraph) {
	                	Entity.XgmmlGraph xgmmlGraph = (Entity.XgmmlGraph) xgmmlEvent;
	                    return entity.new ColoredSsn(xgmmlGraph.label);
	                } else if (xgmmlEvent instanceof Entity.XgmmlEdge) {
	                	Entity.XgmmlEdge xgmmlEdge = (Entity.XgmmlEdge) xgmmlEvent;
	                	Entity.ColoredSimilarity similarity = entity.new ColoredSimilarity(xgmmlEdge.id, xgmmlEdge.label, xgmmlEdge.source, xgmmlEdge.target);
	                    colentityList.add(similarity);
	                } else if (xgmmlEvent instanceof Entity.XgmmlNode) {
	                	Entity.XgmmlNode xgmmlNode = (Entity.XgmmlNode) xgmmlEvent;
	                    if (isRepresentative) {
	                    	Entity.ColoredRepresentativeSequence sequence = entity.new ColoredRepresentativeSequence(xgmmlNode.id, xgmmlNode.label);
	                        colentityList.add(sequence);
	                    } else {
	                    	Entity.ColoredSequence sequence = entity.new ColoredSequence(xgmmlNode.id, xgmmlNode.label);
	                        colentityList.add(sequence);
	                    }
	                } else {
	                	Entity.XgmmlAttribute xgmmlAttribute = (Entity.XgmmlAttribute) xgmmlEvent;
	                	Entity.ColoredSsnEntity ssnEntity = colentityList.get(colentityList.size() - 1);
	                    if (ssnEntity instanceof Entity.ColoredSequence) {
	                    	Entity.ColoredSequence sequence = (Entity.ColoredSequence) ssnEntity;
	                        if (xgmmlAttribute.value != null)
	                            addAttributeToSequence(sequence, xgmmlAttribute);
	                    } else if (ssnEntity instanceof Entity.ColoredRepresentativeSequence) {
	                    	Entity.ColoredRepresentativeSequence repSequence = (Entity.ColoredRepresentativeSequence) ssnEntity;
	                        if (xgmmlAttribute.value != null)
	                            addAttributeToRepresentativeSequence(repSequence, xgmmlAttribute);
	                    } else if (ssnEntity instanceof Entity.ColoredSimilarity) {
	                    	Entity.ColoredSimilarity similarity = (Entity.ColoredSimilarity) ssnEntity;
	                        if (xgmmlAttribute.value != null)
	                            addAttributeToSimilarity(similarity, xgmmlAttribute);
	                    }
	                }
	            } else {
	                if (xgmmlEvent instanceof Entity.XgmmlGraph) {
	                    return null;
	                } else if (xgmmlEvent instanceof Entity.XgmmlEdge) {
	                	Entity.ColoredSsnEntity ssnEntity = colentityList.get(colentityList.size() - 1);
	                    colentityList.remove(colentityList.size() - 1);
	                    return ssnEntity;
	                } else if (xgmmlEvent instanceof Entity.XgmmlNode) {
	                	Entity.ColoredSsnEntity ssnEntity = colentityList.get(colentityList.size() - 1);
	                    colentityList.remove(colentityList.size() - 1);
	                    return ssnEntity;
	                }
	            }
	        }
	        return null;
	    }
	
	    private void addAttributeToSimilarity(Entity.ColoredSimilarity similarity, Entity.XgmmlAttribute xgmmlAttribute) {
	        switch (xgmmlAttribute.name) {
	            case "-log10(E)":
	                similarity.alignmentScore= xgmmlAttribute.value;
	                break;
	            case "%id":
	                similarity.percId= xgmmlAttribute.value;
	                break;
	            case "alignment_score":
	                similarity.alignmentScore= xgmmlAttribute.value;
	                break;
	            case "alignment_length":
	                similarity.alignmentLen= xgmmlAttribute.value;
	                break;
	        }
	    }
	
	    private void addAttributeToRepresentativeSequence(Entity.ColoredRepresentativeSequence sequence, Entity.XgmmlAttribute xgmmlAttribute) {
	        switch (xgmmlAttribute.name) {
	            case "Supercluster":
	                sequence.supercluster= xgmmlAttribute.value;
	                break;
	            case "Cluster Number":
	            case "SSN Cluster Number":        	
	                sequence.supercluster= xgmmlAttribute.value;
	                break;
	            case "ACC":
	                sequence.acc.add(xgmmlAttribute.value);
	                break;
	            case "Cluster Size":
	                sequence.clusterSize= xgmmlAttribute.value;
	                break;
	            case "Uniprot_ID":
	                sequence.uniprotId.add(xgmmlAttribute.value);
	                break;
	            case "STATUS":
	                sequence.status.add(xgmmlAttribute.value);
	                break;
	            case "Cluster Sequence Count":
	            	break;  
	            case "Sequence Length":  	
	                sequence.sequenceLength.add(xgmmlAttribute.value);
	                break;
	            case "Taxonomy_ID":
	                sequence.taxonomyId.add(xgmmlAttribute.value);
	                break;
	            case "GDNA":
	                sequence.gdna.add(xgmmlAttribute.value);
	                break;
	            case "Description":
	                sequence.description.add(xgmmlAttribute.value);
	                break;
	            case "Swissprot_Description":
	                sequence.swissprotDescription.add(xgmmlAttribute.value);
	                break;
	            case "Organism":
	                sequence.organism.add(xgmmlAttribute.value);
	                break;
	            case "Domain":
	                sequence.domain.add(xgmmlAttribute.value);
	                break;
	            case "GN":
	                sequence.gn.add(xgmmlAttribute.value);
	                break;
	            case "PFAM":
	                sequence.pfam.add(xgmmlAttribute.value);
	                break;
	            case "PDB":
	                sequence.pdb.add(xgmmlAttribute.value);
	                break;
	            case "IPRO":
	                sequence.ipro.add(xgmmlAttribute.value);
	                break;
	            case "GO":
	                sequence.go.add(xgmmlAttribute.value);
	                break;
	            case "GI":
	                sequence.gi.add(xgmmlAttribute.value);
	                break;
	            case "HMP_Body_Site":
	                sequence.hmpBodySite.add(xgmmlAttribute.value);
	                break;
	            case "HMP_Oxygen":
	                sequence.hmpOxygen.add(xgmmlAttribute.value);
	                break;
	            case "EFI_ID":
	                sequence.efiId.add(xgmmlAttribute.value);
	                break;
	            case "EC":
	                sequence.ec.add(xgmmlAttribute.value);
	                break;
	            case "PHYLUM":
	                sequence.phylum.add(xgmmlAttribute.value);
	                break;
	            case "CLASS":
	                sequence.clazz.add(xgmmlAttribute.value);
	                break;
	            case "ORDER":
	                sequence.order.add(xgmmlAttribute.value);
	                break;
	            case "FAMILY":
	                sequence.family.add(xgmmlAttribute.value);
	                break;
	            case "GENUS":
	                sequence.genus.add(xgmmlAttribute.value);
	                break;
	            case "SPECIES":
	                sequence.species.add(xgmmlAttribute.value);
	                break;
	            case "CAZY":
	                sequence.cazy.add(xgmmlAttribute.value);
	                break;
	            default:
	            	System.out.printf("ColoredSsnEventReader: unknown Repr.att.: >%s< v.Seq L=%s ID=%s\n",xgmmlAttribute.name,sequence.label,sequence.id);
	        }
	    }
	
	    private void addAttributeToSequence(Entity.ColoredSequence sequence, Entity.XgmmlAttribute xgmmlAttribute) {
	        switch (xgmmlAttribute.name) {
	            case "Supercluster":
	                sequence.supercluster= xgmmlAttribute.value;
	                break;
	            case "Cluster Number":
	            case "SSN Cluster Number":       
	                sequence.supercluster= xgmmlAttribute.value;
	                break;
	            case "Uniprot_ID":
	                sequence.uniprotId= xgmmlAttribute.value;
	                break;
	            case "STATUS":
	                sequence.status= xgmmlAttribute.value;
	                break;
	            case "Cluster Sequence Count":
	            	break;	
	            case "Sequence Length":
	                sequence.sequenceLength= xgmmlAttribute.value;
	                break;
	            case "Taxonomy_ID":
	                sequence.taxonomyId= xgmmlAttribute.value;
	                break;
	            case "GDNA":
	                sequence.gdna= xgmmlAttribute.value;
	                break;
	            case "Description":
	                sequence.description= xgmmlAttribute.value;
	                break;
	            case "Swissprot_Description":
	                sequence.swissprotDescription= xgmmlAttribute.value;
	                break;
	            case "Organism":
	                sequence.organism= xgmmlAttribute.value;
	                break;
	            case "Domain":
	                sequence.domain= xgmmlAttribute.value;
	                break;
	            case "GN":
	                sequence.gn= xgmmlAttribute.value;
	                break;
	            case "PFAM":
	                sequence.pfam.add(xgmmlAttribute.value);
	                break;
	            case "PDB":
	                sequence.pdb.add(xgmmlAttribute.value);
	                break;
	            case "IPRO":
	                sequence.ipro.add(xgmmlAttribute.value);
	                break;
	            case "GO":
	                sequence.go.add(xgmmlAttribute.value);
	                break;
	            case "GI":
	                sequence.gi.add(xgmmlAttribute.value);
	                break;
	            case "HMP_Body_Site":
	                sequence.hmpBodySite.add(xgmmlAttribute.value);
	                break;
	            case "HMP_Oxygen":
	                sequence.hmpOxygen= xgmmlAttribute.value;
	                break;
	            case "EFI_ID":
	                sequence.efiId= xgmmlAttribute.value;
	                break;
	            case "EC":
	                sequence.ec= xgmmlAttribute.value;
	                break;
	            case "PHYLUM":
	                sequence.phylum= xgmmlAttribute.value;
	                break;
	            case "CLASS":
	                sequence.clazz= xgmmlAttribute.value;
	                break;
	            case "ORDER":
	                sequence.order= xgmmlAttribute.value;
	                break;
	            case "FAMILY":
	                sequence.family= xgmmlAttribute.value;
	                break;
	            case "GENUS":
	                sequence.genus= xgmmlAttribute.value;
	                break;
	            case "SPECIES":
	                sequence.species= xgmmlAttribute.value;
	                break;
	            case "CAZY":
	                sequence.cazy.add(xgmmlAttribute.value);
	                break;
	            default:
	        }
	    }
	
	    public Entity.ColoredSsnEntity next() throws XMLStreamException {
	    	if(colnextEntity != null){
	        	Entity.ColoredSsnEntity returnValue = colnextEntity;
	            colnextEntity = colfindNextEntity();
	            return returnValue;
	        } else {
	            return null;
	        }
	    }
	
	    public void close() throws XMLStreamException {
	    	xgclose();
	    }
    }  
    
    public void initXgmmlEntity(Entity.XgmmlEntity xgmmlEntity, EnType eventType) {	
        this.xgnextEvent = xgmmlEntity;
        this.eventType = eventType;
        
    }

    
    private XMLEventReader xmlEventReader;
    private Entity.XgmmlEntity xgnextEvent;
    private EnType eventType;
    private InputStream inputStream;

    public void initXgmmlEventReader(File inputFile) throws FileNotFoundException, XMLStreamException {	
    	try{
    		inputStream = new FileInputStream(inputFile);	 
	        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
	        xmlEventReader = xmlInputFactory.createXMLEventReader(inputStream);
	        xgnextEvent = xgfindNextEntity();
    	}catch(java.io.FileNotFoundException notfound){    
	        System.out.printf("File not found: %s\n",inputFile.getPath());
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }

    private Entity.XgmmlEntity xgfindNextEntity() throws XMLStreamException {	
        while(true) {
            if(xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();
                if(xmlEvent.getEventType()==XMLStreamConstants.START_ELEMENT){
                	if(xmlEvent.asStartElement().getName().getLocalPart() != null){
                		if(xmlEvent.asStartElement().getName().getLocalPart().contains("node") 
					|| xmlEvent.asStartElement().getName().getLocalPart().contains("edge")){
                		}
                	}
                }else if(xmlEvent.getEventType()==XMLStreamConstants.END_ELEMENT){
                	if(xmlEvent.asEndElement().getName().getLocalPart() != null){
                		if(xmlEvent.asEndElement().getName().getLocalPart().contains("node") 
					|| xmlEvent.asEndElement().getName().getLocalPart().contains("edge")){
                		}
                	}
                }
                switch (xmlEvent.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        return parseStartElement(xmlEvent);
                    case XMLStreamConstants.END_ELEMENT:
                        return parseEndElement(xmlEvent);
                    default:
                        break;
                }
            } else {
                return null;
            }
        }
    }

    private Entity.XgmmlEntity parseStartElement(XMLEvent xmlEvent) {	
        StartElement startElement = xmlEvent.asStartElement();

        Attribute labelAttribute = startElement.getAttributeByName(QName.valueOf("label"));
        Attribute idAttribute = startElement.getAttributeByName(QName.valueOf("id"));
        Attribute sourceAttribute = startElement.getAttributeByName(QName.valueOf("source"));
        Attribute targetAttribute = startElement.getAttributeByName(QName.valueOf("target"));
        Attribute typeAttribute = startElement.getAttributeByName(QName.valueOf("type"));
        Attribute nameAttribute = startElement.getAttributeByName(QName.valueOf("name"));
        Attribute valueAttribute = startElement.getAttributeByName(QName.valueOf("value"));

        String label = null;
        String id = null;
        String source = null;
        String target = null;
        String type = null;
        String name = null;
        String value = null;

        if(labelAttribute != null) label = labelAttribute.getValue();
        if(idAttribute != null) id = idAttribute.getValue();
        if(sourceAttribute != null) source = sourceAttribute.getValue();
        if(targetAttribute != null) target = targetAttribute.getValue();
        if(typeAttribute != null) type = typeAttribute.getValue();
        if(nameAttribute != null) name = nameAttribute.getValue();
        if(valueAttribute != null) value = valueAttribute.getValue();

        return parseTypedElement(startElement.getName().getLocalPart(),label,id,source,target,type,name,value,EnType.BEGIN);
    }

    private Entity.XgmmlEntity parseEndElement(XMLEvent element) {
        EndElement endElement = element.asEndElement();
        return parseTypedElement(endElement.getName().getLocalPart(),null,null,null,null,null,null,null,EnType.END);
    }
    
    private Entity.XgmmlEntity parseTypedElement(String elemLocalPart,String label,String id,String source,String target,String type,String name,String value,EnType en){
        switch (elemLocalPart) {
            case "graph":
            	return (entity.new XgmmlGraph(label, en));
            case "node":
            	return (entity.new XgmmlNode(id, label, en));
            case "edge":
            	return (entity.new XgmmlEdge(id, label, source, target, en));
            case "att":
                return (entity.new XgmmlAttribute(type, name, value, en));
            default:
                return null;
        }
    }

    public Entity.XgmmlEntity xernext() throws XMLStreamException {
    	if(xgnextEvent!=null){
        	Entity.XgmmlEntity returnEntity = xgnextEvent;
            xgnextEvent = xgfindNextEntity();
            return returnEntity;
        } else {
            return null;
        }
    }

    public void xgclose() throws XMLStreamException {	
        xmlEventReader.close();
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }
    private XMLEventFactory xmlEventFactory;
    private XMLEventWriter xmlEventWriter;
    private FileOutputStream fileOutputStream;

    public void initXEWriter(File outputFile) throws FileNotFoundException, XMLStreamException {
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        fileOutputStream = new FileOutputStream(outputFile);
        xmlEventWriter = xmlOutputFactory.createXMLEventWriter(fileOutputStream);
        xmlEventFactory = XMLEventFactory.newInstance();
        xmlEventWriter.add(xmlEventFactory.createStartDocument());
    }

    public void xewadd(Entity.XgmmlEntity xe) throws XMLStreamException {	
        switch (xe.type) {
            case BEGIN:
            	writeStartElement(xe);
                break;
            case END:
            	writeEndElement(xe);
                break;
        }
    }

    public void xeclose() throws XMLStreamException {
        xmlEventWriter.add(xmlEventFactory.createEndDocument());
        xmlEventWriter.close();
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            throw new XMLStreamException();
        }
    }

    private void writeStartElement(Entity.XgmmlEntity entity) throws XMLStreamException {
        if(entity instanceof Entity.XgmmlGraph) {
        	Entity.XgmmlGraph xgmmlGraph = (Entity.XgmmlGraph)entity;
            xmlEventWriter.add(xmlEventFactory.createStartElement("","", "graph"));
            xmlEventWriter.add(xmlEventFactory.createNamespace("ns0", "http://www.cs.rpi.edu/XGMML"));
            if(xgmmlGraph.label != null)
                xmlEventWriter.add(xmlEventFactory.createAttribute("label",xgmmlGraph.label));
        } else if(entity instanceof Entity.XgmmlNode) {
        	Entity.XgmmlNode xgmmlNode = (Entity.XgmmlNode)entity;
            xmlEventWriter.add(xmlEventFactory.createStartElement("","", "node"));
            if(xgmmlNode.label != null)
                xmlEventWriter.add(xmlEventFactory.createAttribute("label",xgmmlNode.label));
            if(xgmmlNode.id != null)
                xmlEventWriter.add(xmlEventFactory.createAttribute("id",xgmmlNode.id));
        } else if(entity instanceof Entity.XgmmlEdge) {
        	Entity.XgmmlEdge xgmmlEdge = (Entity.XgmmlEdge)entity;
            xmlEventWriter.add(xmlEventFactory.createStartElement("","", "edge"));
            if(xgmmlEdge.label != null)
                xmlEventWriter.add(xmlEventFactory.createAttribute("label",xgmmlEdge.label));
            if(xgmmlEdge.id != null)
                xmlEventWriter.add(xmlEventFactory.createAttribute("id",xgmmlEdge.id));
            if(xgmmlEdge.source != null)
                xmlEventWriter.add(xmlEventFactory.createAttribute("source",xgmmlEdge.source));
            if(xgmmlEdge.target != null)
                xmlEventWriter.add(xmlEventFactory.createAttribute("target",xgmmlEdge.target));
        } else if(entity instanceof Entity.XgmmlAttribute) {
        	Entity.XgmmlAttribute xgmmlAttribute = (Entity.XgmmlAttribute)entity;
            xmlEventWriter.add(xmlEventFactory.createStartElement("","", "att"));
            if(xgmmlAttribute.name != null)
                xmlEventWriter.add(xmlEventFactory.createAttribute("name",xgmmlAttribute.name));
            if(xgmmlAttribute.type != null)
                xmlEventWriter.add(xmlEventFactory.createAttribute("type",xgmmlAttribute.type));
            if(xgmmlAttribute.value != null)
                xmlEventWriter.add(xmlEventFactory.createAttribute("value",xgmmlAttribute.value));
        }
    }

    private void writeEndElement(Entity.XgmmlEntity entity) throws XMLStreamException {
        if(entity instanceof Entity.XgmmlGraph) {
            xmlEventWriter.add(xmlEventFactory.createEndElement("","", "graph"));
        } else if(entity instanceof Entity.XgmmlNode) {
            xmlEventWriter.add(xmlEventFactory.createEndElement("","","node"));
        } else if(entity instanceof Entity.XgmmlEdge) {
            xmlEventWriter.add(xmlEventFactory.createEndElement("","","edge"));
        } else if(entity instanceof Entity.XgmmlAttribute) {
            xmlEventWriter.add(xmlEventFactory.createEndElement("","","att"));
        }
    }

    public class ExtendedGNNWriter {
	
	    public ExtendedGNNWriter(File outputFile) throws FileNotFoundException, XMLStreamException {
	    	initXEWriter(outputFile);
	    }
	
	    public void rgadd(Entity.XGnnEntity gnnEntity) throws XMLStreamException { 
	        if(gnnEntity instanceof Entity.XGnn) {
	            writeXGnn((Entity.XGnn) gnnEntity);
	        } else if(gnnEntity instanceof Entity.Group) {
	            writeGroup((Entity.Group) gnnEntity);
	        } else if(gnnEntity instanceof Entity.Family) {
	            writeFamily((Entity.Family) gnnEntity);
	        } else if(gnnEntity instanceof Entity.Relationship) {
	            writeRelationship((Entity.Relationship) gnnEntity);
	        }
	    }
	
	    private void writeRelationship(Entity.Relationship relationship) throws XMLStreamException {
	    	Entity.XgmmlEdge xgmmlEdge = entity.new XgmmlEdge("", relationship.label,relationship.source, relationship.target,EnType.BEGIN);
	        xewadd(xgmmlEdge);
	
	        writeAttribute("integer", "SeqCount", relationship.seqCount);
	        writeAttribute("real", "Uniqueness", relationship.uniqueness);
	        writeAttribute("real", "Coverage", relationship.coverage);
	        xewadd(entity.new XgmmlEdge(null,null,null,null,EnType.END));
	    }
	
	    private void writeFamily(Entity.Family family) throws XMLStreamException {
	    	Entity.XgmmlNode xgmmlNode = entity.new XgmmlNode(family.id, family.label,EnType.BEGIN);
	        xewadd(xgmmlNode);
	
	        writeAttribute("string", "Pfam", family.pfam);
	        writeAttribute("string", "Description", family.description);
	        writeAttribute("integer", "SeqCount", family.seqCount);
	        writeAttributeList("string", "GO", family.go);
	        writeAttribute("real", "Uniqueness", family.uniqueness);
	        writeAttribute("string", "node.size", family.nodeSize);
	        writeAttribute("string", "node.shape", "hexagon");
	        Entity.XgmmlNode xgmmlNode2 = entity.new XgmmlNode(null, null,EnType.END);
	        xewadd(xgmmlNode2);
	    }
	
	    private void writeGroup(Entity.Group group) throws XMLStreamException {
	    	Entity.XgmmlNode xgmmlNode = entity.new XgmmlNode(group.id, group.label,EnType.BEGIN);
	    	xewadd(xgmmlNode);
	    	
	        writeAttribute("integer", "ClusterNumber", group.clusterNumber);
	        writeAttribute("integer", "SeqCount", group.seqCount);
	        writeAttribute("real", "Uniqueness", group.uniqueness);
	        writeAttribute("string", "node.fillColor", group.nodeFillColor);
	        writeAttribute("string", "node.size", group.nodeSize);
	        writeAttribute("string", "node.shape", group.nodeShape);
	        writeAttribute("string", "NodeCount", group.nodeCount);
	        writeAttributeList("string", "PhylumStat", group.phylumStat);
	        xgmmlNode = entity.new XgmmlNode(null, null,EnType.END);
	        xewadd(xgmmlNode);
	    }
	
	    private void writeXGnn(Entity.XGnn xgnn) throws XMLStreamException {
	    	Entity.XgmmlGraph xgmmlGraph = entity.new XgmmlGraph(xgnn.label,EnType.BEGIN);
	        xewadd(xgmmlGraph);
	    }
	
	    private void writeAttribute(String type, String name, String value) throws XMLStreamException {
	        if(value != null) {
	            Entity.XgmmlAttribute xgmmlAttribute = entity.new XgmmlAttribute(type, name, value,EnType.BEGIN);
	            xewadd(xgmmlAttribute);
	            xgmmlAttribute = entity.new XgmmlAttribute(null, null, null,EnType.END);
	            xewadd(xgmmlAttribute);
	        }
	    }
	
	    private void writeAttributeList(String type, String name, List<String> values) throws XMLStreamException {
	        if(values != null && values.size() > 0) {
	            Entity.XgmmlAttribute xgmmlAttribute = entity.new XgmmlAttribute("list", name,null,EnType.BEGIN);
	            xewadd(xgmmlAttribute);
	
	            for (String value : values) {
	                writeAttribute(type, name, value);
	            }
	            xgmmlAttribute = entity.new XgmmlAttribute("list", name,null,EnType.END);
	            xewadd(xgmmlAttribute);            
	        }
	    }
	
	    public void xgclose() throws XMLStreamException {
	        Entity.XgmmlGraph xgmmlGraph = entity.new XgmmlGraph(null,EnType.END);
	        xewadd(xgmmlGraph);
	        xeclose();
	    }
    }
}


