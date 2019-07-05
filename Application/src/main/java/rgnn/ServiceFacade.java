package rgnn;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ServiceFacade {
    private static ServiceFacade instance;

    public XgmmlIO xgmmlIO;
    private WLoader wloader;
    
    private Entity entity = new Entity();
    private ResourceIO rio = new ResourceIO();
    
    private Integer nbSize=3;
    private Integer cooc=10;
    String ssnfile;
    String tax;
    String sth;

    private ServiceFacade() {
        xgmmlIO = XgmmlIO.getInstance();
        nbSize= Integer.parseInt(xgmmlIO.project.specProjProp.getProperty("neighborhoodSize"));
        cooc= Integer.parseInt(xgmmlIO.project.specProjProp.getProperty("coocurrence"));
        ssnfile= xgmmlIO.project.specProjProp.getProperty("ssn");
        tax= xgmmlIO.project.specProjProp.getProperty("tax");
        sth= xgmmlIO.project.specProjProp.getProperty("th");
        xgmmlIO.resio= rio;
    }

    public static ServiceFacade getInstance() {
        if(ServiceFacade.instance == null) {
            ServiceFacade.instance = new ServiceFacade();
        }
        return ServiceFacade.instance;
    }

    private File getTempFilename() {
        return new File(UUID.randomUUID().toString().replace("-","") + ".tmp");
    }

    public void callCytoscape() {
        try {
        	File file= xgmmlIO.project.getRGNN();
            if(getCytoscapeExecutable() == null) {
            	System.out.printf("no cytoscape executable\n");
            	return; 
            }
            if(file == null){   
            	System.out.printf("no rgnn file available\n");
            }
            Runtime.getRuntime().exec(getCytoscapeExecutable() + " -N " + file.getAbsolutePath());
        } catch (Exception ex) { 
        	ex.printStackTrace();
        }
    }
    
    public void addSsnToProject(Project project, File inputFile) throws FileNotFoundException, XMLStreamException {
    	setAnFile(inputFile);
        if(!Thread.currentThread().isInterrupted()) {
        	project.addSSN(inputFile);
        }
    }
       
    public void addFilteredSsnToProject(Project project, File inputFile, Integer threshold, Boolean filterTaxonomy) throws FileNotFoundException, XMLStreamException {
        File TEMP_FILTERED_FILE = getTempFilename();
        File TEMP_WORKING_FILE = getTempFilename();
        File TEMP_TAXONOMY_FILE = getTempFilename();
        thFilter(inputFile, TEMP_FILTERED_FILE,threshold);
        if(filterTaxonomy) {
            taxFilter(TEMP_FILTERED_FILE, TEMP_WORKING_FILE, TEMP_TAXONOMY_FILE);
            if(!Thread.currentThread().isInterrupted()) {
                project.addFilteredSSN(TEMP_TAXONOMY_FILE);
            }
        } else {
            if(!Thread.currentThread().isInterrupted()) {
                project.addFilteredSSN(TEMP_FILTERED_FILE);
            }
        }
        try {
            if(TEMP_FILTERED_FILE.exists()) Files.delete(TEMP_FILTERED_FILE.toPath());
            if(TEMP_WORKING_FILE.exists()) Files.delete(TEMP_WORKING_FILE.toPath());
            if(TEMP_TAXONOMY_FILE.exists()) Files.delete(TEMP_TAXONOMY_FILE.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    public WLoader uploadForReqestProject(Project project, File inputFile) throws Exception {    	
        File TEMP_GNN_FILE = getTempFilename();
        File TEMP_COLORED_FILE = getTempFilename();        
        wloader = new WLoader(inputFile,xgmmlIO.project.addrProperties);
        wloader.upload_gen(this.nbSize, this.cooc, project.email, TEMP_GNN_FILE, TEMP_COLORED_FILE);
        return wloader;
    }

    public void addGnnToProject(Project project, File ssnFile, String url,boolean newWL) throws Exception{  
        File TEMP_GNN_FILE = getTempFilename();
        File TEMP_COLORED_FILE = getTempFilename();
        File TEMP_XGNN_FILE = getTempFilename();
        if(newWL)
        	wloader = new WLoader(url,ssnFile,xgmmlIO.project.addrProperties);
        wloader.request(TEMP_GNN_FILE, TEMP_COLORED_FILE);
        if(!Thread.currentThread().isInterrupted()) {
            initColSA(TEMP_COLORED_FILE);
            this.coloredSsnAnalyzerResult= colSAanalyze();	
            
            xgmmlIO.initGnnFReader(TEMP_GNN_FILE, wloader.getNeighborhoodSize(), wloader.getCoocurrence());
            this.fullGnn = xgmmlIO.gnnFparse();
            
            initNbStatistic(fullGnn);
            this.gnnStatistic = nbCalculate();
            initXGnnGenerator(this.fullGnn, this.gnnStatistic, this.coloredSsnAnalyzerResult);
            xgwriteToFile(TEMP_XGNN_FILE);    
            System.out.printf("xgnn generated\n");
            if (!Thread.currentThread().isInterrupted()) {
                project.addGNN(TEMP_GNN_FILE, TEMP_COLORED_FILE, fullGnn, gnnStatistic, coloredSsnAnalyzerResult, TEMP_XGNN_FILE, ssnFile);
            }
        }
        try {
            if(TEMP_GNN_FILE.exists()) Files.delete(TEMP_GNN_FILE.toPath());
            if(TEMP_COLORED_FILE.exists()) Files.delete(TEMP_COLORED_FILE.toPath());
            if(TEMP_XGNN_FILE.exists()) Files.delete(TEMP_XGNN_FILE.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void getGNN(Project project, WLoader wloader, File ssnFile, String url) throws Exception{
    	this.wloader= wloader;
    	this.wloader.efiURL = url;
    	addGnnToProject(project,ssnFile, url,false);	
    }
    
    public void addGnnFileCol(Project project, File ssnFile, File gnn, File col) throws Exception{
        File TEMP_XGNN_FILE = getTempFilename();
        initColSA(col);
        System.out.printf("colored analyze ...\n");   
        this.coloredSsnAnalyzerResult= colSAanalyze();	
        xgmmlIO.initGnnFReader(gnn, 4, 20);
        this.fullGnn = xgmmlIO.gnnFparse(); 
        initNbStatistic(fullGnn);
        this.gnnStatistic = nbCalculate();
        initXGnnGenerator(this.fullGnn, this.gnnStatistic, this.coloredSsnAnalyzerResult);
        xgwriteToFile(TEMP_XGNN_FILE);	    
        System.out.printf("xgnn generated\n");
        if (!Thread.currentThread().isInterrupted()) {
            project.addGNN(gnn, col, fullGnn, gnnStatistic, coloredSsnAnalyzerResult, TEMP_XGNN_FILE, ssnFile);
        }
        try {
            if(TEMP_XGNN_FILE.exists()) Files.delete(TEMP_XGNN_FILE.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private File createRGNN(File TEMP_GNN_FILE, File TEMP_COLORED_FILE, Integer neighborhoodSize, Integer coocurrence){
    	File TEMP_XGNN_FILE = getTempFilename();
        initColSA(TEMP_COLORED_FILE); 
        try{
        	coloredSsnAnalyzerResult= colSAanalyze(); 
	        xgmmlIO.initGnnFReader(TEMP_GNN_FILE, neighborhoodSize, coocurrence);
	        Entity.FullGnn fullGnn = xgmmlIO.gnnFparse();
	        initNbStatistic(fullGnn);
	        Entity.GnnStatistic gnnStatistic = nbCalculate();
	        initXGnnGenerator(fullGnn, gnnStatistic, coloredSsnAnalyzerResult);
	        File dbgF= xgmmlIO.project.getFile2Save(".dbg.xgmml");
	        xgwriteToFile(dbgF);
	        xgwriteToFile(TEMP_XGNN_FILE);
        }catch(Exception e){
        	e.printStackTrace();
        }
        return TEMP_XGNN_FILE;
    }
    
    public void dbg_createRGNN(){
    	File gnn= xgmmlIO.project.getFile(new File(xgmmlIO.project.projpath),".gnn.xgmml");
    	File color= xgmmlIO.project.getFile(new File(xgmmlIO.project.projpath),".color.xgmml");
    	File filtered= xgmmlIO.project.getFile(new File(xgmmlIO.project.projpath),"FIL.xgmml");
    	File xgnn= createRGNN(gnn,color,this.nbSize, this.cooc);
    	xgmmlIO.project.saveFile(xgnn,".x.xgmml",filtered);
    } 
    
    public void addGnnToProject(Project project, File inputFile, Integer neighborhoodSize, Integer coocurrence, String email) throws Exception {  
        File TEMP_GNN_FILE = getTempFilename();
        File TEMP_COLORED_FILE = getTempFilename();
        File TEMP_XGNN_FILE= null; 
        
        if(neighborhoodSize == null) neighborhoodSize= this.nbSize;	
        if(coocurrence == null) coocurrence= this.cooc;
        if(email == null) email= project.email;	
        wloader = new WLoader(inputFile,xgmmlIO.project.addrProperties);
        wloader.request(neighborhoodSize, coocurrence, email, TEMP_GNN_FILE, TEMP_COLORED_FILE);
        if(!Thread.currentThread().isInterrupted()) {    
        	project.saveFile(TEMP_GNN_FILE,".gnn.xgmml",inputFile);
        	project.saveFile(TEMP_COLORED_FILE,".color.xgmml",inputFile);  
        	TEMP_XGNN_FILE= createRGNN(TEMP_GNN_FILE,TEMP_COLORED_FILE,neighborhoodSize, coocurrence);
            if (!Thread.currentThread().isInterrupted()) {
            	project.saveFile(TEMP_XGNN_FILE,".x.xgmml",inputFile);
            }
        }
        try {
            if(TEMP_GNN_FILE.exists()) Files.delete(TEMP_GNN_FILE.toPath());
            if(TEMP_COLORED_FILE.exists()) Files.delete(TEMP_COLORED_FILE.toPath());
            if(TEMP_XGNN_FILE.exists()) Files.delete(TEMP_XGNN_FILE.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 
        

    public void addFilteredGnnToProject(Project project, File gnnFile, File filterFile) throws IOException, XMLStreamException {
        File TEMP_FILTER_FILE = getTempFilename();
        File TEMP_XGNN_FILE = getTempFilename();
        File TEMP_GNN_FILE = getTempFilename();
        File TEMP_COLORED_FILE = getTempFilename();
        File TEMP_FILTERED_FILE = getTempFilename();
        File TEMP_WORKING_FILE = getTempFilename();
        File TEMP_TAXONOMY_FILE = getTempFilename();

        if(filterFile != null) {
            initPfamFilter(filterFile);
        } else {
            initPfamFilter();
        }
        initColSA(project.getColoredSsn(gnnFile));      
        colSAanalyze();
        xgmmlIO.initGnnFReader(gnnFile, nbSize, cooc);	
        fullGnn= xgmmlIO.gnnFparse();
        
        Entity.FullGnn filteredGnn = pfFilter(fullGnn);
        initNbStatistic(filteredGnn);  
        Entity.GnnStatistic gnnStatistic = nbCalculate();
        xgmmlIO.writeFullGnn(filteredGnn,TEMP_GNN_FILE);
        writeFilter(TEMP_FILTER_FILE);
        initXGnnGenerator(filteredGnn, gnnStatistic, coloredSsnAnalyzerResult);
        xgwriteToFile(TEMP_XGNN_FILE);
        if(!Thread.currentThread().isInterrupted()) {
            project.addFilteredGNN(TEMP_GNN_FILE, project.getColoredSsn(gnnFile), filteredGnn, gnnStatistic,
                    coloredSsnAnalyzerResult, TEMP_FILTER_FILE, TEMP_XGNN_FILE, gnnFile);
        }
        try {
            if(TEMP_FILTER_FILE.exists()) Files.delete(TEMP_FILTER_FILE.toPath());
            if(TEMP_XGNN_FILE.exists()) Files.delete(TEMP_XGNN_FILE.toPath());
            if(TEMP_GNN_FILE.exists()) Files.delete(TEMP_GNN_FILE.toPath());
            if(TEMP_COLORED_FILE.exists()) Files.delete(TEMP_COLORED_FILE.toPath());
            if(TEMP_FILTERED_FILE.exists()) Files.delete(TEMP_FILTERED_FILE.toPath());
            if(TEMP_WORKING_FILE.exists()) Files.delete(TEMP_WORKING_FILE.toPath());
            if(TEMP_TAXONOMY_FILE.exists()) Files.delete(TEMP_TAXONOMY_FILE.toPath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Project> listProjects() {
        return xgmmlIO.listProjects();
    }

    public Project createProject(String name) {
        return xgmmlIO.createProject(name);
    }

    public void deleteProject(Project project) {
        xgmmlIO.deleteProject(project);
    }

    public List<ResourceIO.Pfam2GoEntry> serv_getGo(String pfam) {
        return xgmmlIO.getGo(pfam);
    }

    public String getCytoscapeExecutable() {
        return xgmmlIO.getCytoscapeExecutable();
    }

    public void setCytoscapeExecutable(String executable) {
        xgmmlIO.setCytoscapeExecutable(executable);
    }

    public static  <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return ( o2.getValue() ).compareTo( o1.getValue() );
            }
        } );
        
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
    private Map<String, Integer> old_nodeThresholdMap;
    private File old_safile;
    public void setAnFile(File inputFile){
        xgmmlIO = XgmmlIO.getInstance();
        old_safile = inputFile;
        this.old_nodeThresholdMap = new HashMap<>();
    }

    public void sanAnalyze(boolean taxonomyFiltered,File safile) throws XMLStreamException, FileNotFoundException {	
    	Map<String, Integer>nodeThresholdMap = new HashMap<>();
    	Entity.SsnEntity entity= null;
    	if(safile==null){
    		safile= old_safile;
    		nodeThresholdMap= old_nodeThresholdMap;
    	}
    	try{
	    	initSAR();
	        this.ssnTaxonomyFiltered = taxonomyFiltered;	  
	        entity = xgmmlIO.readSsnEvent(safile);
    	}catch(Exception ex)
    		{ System.out.printf("sanAnalyze: %s\n", ex.getMessage()); System.exit(0); }
        while(entity != null) {
            if(entity instanceof Entity.Ssn) {
            	Entity.Ssn ssn = (Entity.Ssn)entity;
                ssnsetName(ssn.label);
            } else if(entity instanceof Entity.Similarity) {
            	Entity.Similarity similarity = (Entity.Similarity)entity;
                Double alignmentScore = Double.parseDouble(similarity.alignmentScore);
                ssnsetSimilarityCount(ssnSimilarityCount+1);

                if(ssnThresholdEdgeCountMap.containsKey(alignmentScore.intValue())) {
                    Integer currentCount = ssnThresholdEdgeCountMap.get(alignmentScore.intValue());
                    ssnThresholdEdgeCountMap.put(alignmentScore.intValue(), currentCount+1);
                } else {
                    ssnThresholdEdgeCountMap.put(alignmentScore.intValue(),1);
                }

                String source = similarity.source;
                String target = similarity.target;
                if(nodeThresholdMap.containsKey(source)) {
                    if(nodeThresholdMap.get(source) < alignmentScore.intValue()) {
                        nodeThresholdMap.put(source, alignmentScore.intValue());
                    }
                } else {
                    nodeThresholdMap.put(source, alignmentScore.intValue());
                }
                if(nodeThresholdMap.containsKey(target)) {
                    if(nodeThresholdMap.get(target) < alignmentScore.intValue()) {
                        nodeThresholdMap.put(target, alignmentScore.intValue());
                    }
                } else {
                    nodeThresholdMap.put(target, alignmentScore.intValue());
                }

                if(ssnMinThreshold > alignmentScore.intValue())
                    ssnMinThreshold= alignmentScore.intValue();
                if(ssnMaxThreshold < alignmentScore.intValue())
                    ssnMaxThreshold= alignmentScore.intValue();
            } else if(entity instanceof Entity.Sequence) {
                snnIsRepresentative= false;
                ssnSequenceCount= ssnSequenceCount+1;
                ssnTotalSequenceCount= ssnTotalSequenceCount+1;
            } else if(entity instanceof Entity.RepresentativeSequence) {
            	Entity.RepresentativeSequence sequence = (Entity.RepresentativeSequence)entity;
                Integer clusterSize = Integer.parseInt(sequence.clusterSize);
                snnIsRepresentative= true;
                ssnSequenceCount= ssnSequenceCount+1;
                ssnTotalSequenceCount= ssnTotalSequenceCount + clusterSize;
            }
            entity = xgmmlIO.readSsnEvent(safile);
        }

        for(int th = ssnMinThreshold; th <= ssnMaxThreshold; th++) {
            if (!ssnThresholdEdgeCountMap.containsKey(th)) {
                ssnThresholdEdgeCountMap.put(th,0);
            }
        }

        int totalEdges = 0;
        for(int th = ssnMaxThreshold; th >= ssnMinThreshold; th--) {
            if(ssnThresholdEdgeCountMap.containsKey(th)) {
                totalEdges += ssnThresholdEdgeCountMap.get(th);
            }
            ssnSeMap.put(th, totalEdges);
        }

        for(int th = ssnMinThreshold; th <= ssnMaxThreshold; th++) {
            int nodeCount = 0;
            for(String node : nodeThresholdMap.keySet()) {
                if(nodeThresholdMap.get(node) >= th) {
                    nodeCount++;
                }
            }
            ssnNnMap.put(th, nodeCount);
        }

        return;
    }
    public boolean snnIsRepresentative; 
    public int ssnSimilarityCount;
    public int ssnSequenceCount;
    public int ssnTotalSequenceCount;
    public int ssnMaxThreshold=0;
    public int ssnMinThreshold=Integer.MAX_VALUE;
    public boolean ssnTaxonomyFiltered;
    public String ssnName;
    public Map<Integer, Integer> ssnThresholdEdgeCountMap;
    public Map<Integer, Integer> ssnSeMap;
    public Map<Integer, Integer> ssnNnMap;

    public void initSAR(){ 
        ssnMaxThreshold = 0;
        ssnMinThreshold = Integer.MAX_VALUE;
        ssnThresholdEdgeCountMap = new HashMap<>();
        ssnSeMap = new HashMap<>();
        ssnNnMap = new HashMap<>();
    }
    public void ssnsetSimilarityCount(int similarityCount) {
        this.ssnSimilarityCount = similarityCount;
    }
    public void setSequenceCount(int sequenceCount) {
        this.ssnSequenceCount = sequenceCount;
    }
    public void setTotalSequenceCount(int totalSequenceCount) {
        this.ssnTotalSequenceCount = totalSequenceCount;
    }
    public void setMaxThreshold(int maxThreshold) {
        this.ssnMaxThreshold = maxThreshold;
    }
    public void setMinThreshold(int minThreshold) {
        this.ssnMinThreshold = minThreshold;
    }
    public void ssnsetName(String name) {
        this.ssnName = name;
    }
    public void ssnsetTaxonomyFiltered(boolean taxonomyFiltered) {
        this.ssnTaxonomyFiltered = taxonomyFiltered;
    } 
     
    public Integer smGetThreshold() {
    	int lastSE= 0;
    	int lastNN= 0;
    	try{
           lastSE= ssnSeMap.get(ssnMinThreshold);
           lastNN= ssnNnMap.get(ssnMinThreshold);
    	}catch(NullPointerException nex){
    		System.out.printf("ex in getThreshold: %s\n",nex.getMessage());
    		nex.printStackTrace();
    		return null;
    	}
        for(int i = ssnMinThreshold+1; i <= ssnMaxThreshold; i++) {       	
            int currentSE = ssnSeMap.get(i);
            int currrentNN = ssnNnMap.get(i);

            double percSE = ((double)(lastSE-currentSE))/((double) ssnSimilarityCount);
            double percNN = ((double)(lastNN-currrentNN))/((double) ssnSequenceCount);

            if(percNN > percSE) return i;

            lastSE = currentSE;
            lastNN = currrentNN;

        }
        return null;
    }
    

    public double nsv(Integer threshold) {
    	try{
	    	double se= ssnSeMap.get(threshold);
	    	double nn= ssnNnMap.get(threshold);
	    	return( se / nn );
    	}catch(Exception ex){
    		System.out.printf("ex in ApeltsinThreshold:nsv: %s\n",ex.getMessage());
    	}
    	return (double)0;
    }

    private Set<String> whiteList;

    public void initPfamFilter(){  
    	rio= new ResourceIO();
        whiteList = rio.getPfamWhitelist();
    }

    public void initPfamFilter(File filter) throws IOException {
    	rio= new ResourceIO();
        whiteList = rio.getPfamWhitelist(filter);
    }

    public void writeFilter(File outputFile) throws IOException {
        rio.writePfamWhitelistToFile(whiteList, outputFile);
    }

    public int getWhiteListSize() {
        return whiteList.size();
    }

    public Entity.FullGnn pfFilter(Entity.FullGnn input) {
    	Entity.FullGnn result = entity.new FullGnn(input);

        for (String pfamId : input.pfamMap.keySet()) {
        	Entity.Pfam pfam = input.pfamMap.get(pfamId);
            if(!whiteList.contains(pfam.pfam)) {
                result.pfamMap.remove(pfamId);
                List<Entity.Neighborhood> neighborhoodList =  input.pfamNeighborhoodMap.get(pfamId);
                for(Entity.Neighborhood neighborhood: neighborhoodList) {
                    String clusterId = neighborhood.target;
                    result.clusterMap.remove(clusterId);
                    result.clusterNeighborhoodMap.remove(clusterId);
                }
                result.pfamNeighborhoodMap.remove(pfamId);
            }
        }

        return  result;
    }
        
    private File colSAfile;

    private void initColSA(File inputFile) {
        xgmmlIO = XgmmlIO.getInstance();
        colSAfile = inputFile;
    }

    private Entity.ColoredSsnAnalyzerResult colSAanalyze() throws XMLStreamException, FileNotFoundException {    	
    	Entity.ColoredSsnAnalyzerResult coloredSsnAnalyzerResult = entity.new ColoredSsnAnalyzerResult();
    	initColSAR();
        Entity.ColoredSsnEntity csEntity = xgmmlIO.readColoredSsnEvent(colSAfile);
        while (csEntity != null) {
            if (csEntity instanceof Entity.ColoredSequence) {
            	Entity.ColoredSequence coloredSequence = (Entity.ColoredSequence) csEntity;
                if (coloredSequence.supercluster != null) {               	
                    Integer supercluster = Integer.parseInt(((Entity.ColoredSequence) csEntity).supercluster);
                    if (coloredSsnAnalyzerResult.clusterNodeCountMap.containsKey(supercluster)) {
                        Integer count = coloredSsnAnalyzerResult.clusterNodeCountMap.get(supercluster);
                        count++;
                        coloredSsnAnalyzerResult.clusterNodeCountMap.put(supercluster, count);
                    } else {
                        coloredSsnAnalyzerResult.clusterNodeCountMap.put(supercluster, 1);
                    }
                }

                if (coloredSequence.phylum!=null && coloredSequence.supercluster!=null) {                	
                    String phylum = coloredSequence.phylum;
                    Integer supercluster = Integer.parseInt(((Entity.ColoredSequence) csEntity).supercluster);
                    if (coloredSsnAnalyzerResult.clusterPhylumPercentageMap.containsKey(supercluster)) {
                        Map<String, Double> phylumStatMap = coloredSsnAnalyzerResult.clusterPhylumPercentageMap.get(supercluster);
                        if (phylumStatMap.containsKey(phylum)) {
                            Double count = phylumStatMap.get(phylum);
                            count++;
                            phylumStatMap.put(phylum, count);
                        } else {
                            phylumStatMap.put(phylum, 1.0);
                        }
                    } else {
                        Map<String, Double> phylumStatMap = new HashMap<>();
                        phylumStatMap.put(phylum, 1.0);
                        coloredSsnAnalyzerResult.clusterPhylumPercentageMap.put(supercluster, phylumStatMap);
                    }

                }

            } else if (csEntity instanceof Entity.ColoredRepresentativeSequence) {
            	Entity.ColoredRepresentativeSequence coloredRepresentativeSequence = (Entity.ColoredRepresentativeSequence) csEntity;
                
                if (coloredRepresentativeSequence.supercluster!=null && !coloredRepresentativeSequence.supercluster.isEmpty()) {
                    Integer supercluster = Integer.parseInt(((Entity.ColoredRepresentativeSequence) csEntity).supercluster);
                    if (coloredSsnAnalyzerResult.clusterNodeCountMap.containsKey(supercluster)) {
                        Integer count = coloredSsnAnalyzerResult.clusterNodeCountMap.get(supercluster);
                        count++;
                        coloredSsnAnalyzerResult.clusterNodeCountMap.put(supercluster, count);
                    } else {
                        coloredSsnAnalyzerResult.clusterNodeCountMap.put(supercluster, 1);
                    }
                }
                
                

                if (!coloredRepresentativeSequence.phylum.isEmpty() &&
                        !coloredRepresentativeSequence.supercluster.isEmpty()) {
                    for (String phylum : coloredRepresentativeSequence.phylum) {
                        Integer supercluster = Integer.parseInt(((Entity.ColoredRepresentativeSequence) csEntity).supercluster);
                        if (coloredSsnAnalyzerResult.clusterPhylumPercentageMap.containsKey(supercluster)) {
                            Map<String, Double> phylumStatMap = coloredSsnAnalyzerResult.clusterPhylumPercentageMap.get(supercluster);
                            if (phylumStatMap.containsKey(phylum)) {
                                Double count = phylumStatMap.get(phylum);
                                count++;
                                phylumStatMap.put(phylum, count);
                            } else {
                                phylumStatMap.put(phylum, 1.0);
                            }
                        } else {
                            Map<String, Double> phylumStatMap = new HashMap<>();
                            phylumStatMap.put(phylum, 1.0);
                            coloredSsnAnalyzerResult.clusterPhylumPercentageMap.put(supercluster, phylumStatMap);
                        }
                    }
                }
            }
            csEntity = xgmmlIO.readColoredSsnEvent(colSAfile);
        }

        for(Integer cluster : coloredSsnAnalyzerResult.clusterPhylumPercentageMap.keySet()) {
            Map<String, Double> phylumMap = coloredSsnAnalyzerResult.clusterPhylumPercentageMap.get(cluster);
            int count = 0;
            for (String phylum : phylumMap.keySet()) {
                count += phylumMap.get(phylum);
            }
            for (String phylum : phylumMap.keySet()) {
                phylumMap.replace(phylum, phylumMap.get(phylum) / count);
            }
        }

        return coloredSsnAnalyzerResult;
    }

    private Map<Integer, Integer> clusterNodeCountMap;
    private Map<Integer, Map<String, Double>> clusterPhylumPercentageMap;

    public void initColSAR(){	
        this.clusterNodeCountMap = new HashMap<>();
        this.clusterPhylumPercentageMap = new HashMap<>();
    }

    public Map<Integer, Integer> getClusterNodeCountMap() {
        return clusterNodeCountMap;
    }

    public Map<Integer, Map<String, Double>> getClusterPhylumPercentageMap() {
        return clusterPhylumPercentageMap;
    }


    public void thFilter(File inputFile, File outputFile,Integer threshold) throws XMLStreamException, FileNotFoundException {
    	File inputSsn = inputFile;
    	File outputSsn = outputFile;
        Entity.SsnEntity entity = xgmmlIO.readSsnEvent(inputSsn);
        while(entity != null) {
            if(entity instanceof Entity.Similarity) {
            	Entity.Similarity similarity = (Entity.Similarity)entity;
                Double score = Double.parseDouble(similarity.alignmentScore);
                if(score >= threshold) {
                    xgmmlIO.writeSsnEvent(outputSsn, entity);
                }
            } else {
                xgmmlIO.writeSsnEvent(outputSsn, entity);
            }
            entity = xgmmlIO.readSsnEvent(inputSsn);
        }
        xgmmlIO.writeSsnEvent(outputSsn, null);
    }

    private File inputSsnTax;
    private File outputSsnTax;
    private File workingSsnTax;
    private Set<String> removedNodesSet;


    public void taxFilter(File inputFile, File workingFile, File outputFile) throws FileNotFoundException, XMLStreamException {
        xgmmlIO = XgmmlIO.getInstance();
        inputSsnTax = inputFile;
        outputSsnTax = outputFile;
        workingSsnTax = workingFile;
        removedNodesSet = new HashSet<>();
        filterSpecies();
        removeEdges();
        System.out.printf("taxonomy filter applied\n");
    }

    private void removeEdges() throws XMLStreamException, FileNotFoundException {
    	Entity.SsnEntity esEntity = xgmmlIO.readSsnEvent(workingSsnTax);
        while (esEntity != null) {
            if(esEntity instanceof Entity.Similarity) {
            	Entity.Similarity similarity = (Entity.Similarity)esEntity;
                if(!removedNodesSet.contains(similarity.source) && !removedNodesSet.contains(similarity.target)) {
                    xgmmlIO.writeSsnEvent(outputSsnTax, esEntity);
                }
            } else {
                xgmmlIO.writeSsnEvent(outputSsnTax, esEntity);
            }
            esEntity = xgmmlIO.readSsnEvent(workingSsnTax);
        }
        xgmmlIO.writeSsnEvent(outputSsnTax, null);

    }

    private void filterSpecies() throws XMLStreamException, FileNotFoundException {
    	Entity.SsnEntity esEntity = xgmmlIO.readSsnEvent(inputSsnTax);
        while(esEntity != null) {
            if(esEntity instanceof Entity.Sequence) {
            	Entity.Sequence sequence = (Entity.Sequence)esEntity;
                if(rio.isSpecies(sequence.taxonomyId)) {		
                    xgmmlIO.writeSsnEvent(workingSsnTax, esEntity);
                } else {
                    removedNodesSet.add(sequence.id);
                }
            } else if(esEntity instanceof Entity.RepresentativeSequence) {
            	Entity.RepresentativeSequence representativeSequence = (Entity.RepresentativeSequence)esEntity;
                List<Integer> toBeRemoved = new ArrayList<>();
                for(int i = 0; i < representativeSequence.taxonomyId.size(); i++) {
                    if(!rio.isSpecies(representativeSequence.taxonomyId.get(i))) {
                        toBeRemoved.add(i);
                    }
                }
                for(Integer i : toBeRemoved) {
                    representativeSequence.removeEntry(i);
                }
                if(representativeSequence.taxonomyId.size() > 0) {
                    xgmmlIO.writeSsnEvent(workingSsnTax, esEntity);
                } else {
                    removedNodesSet.add(representativeSequence.id);
                }
            } else if(esEntity instanceof Entity.Similarity) {
                xgmmlIO.writeSsnEvent(workingSsnTax, esEntity);
            } else if(esEntity instanceof Entity.Ssn ){
                xgmmlIO.writeSsnEvent(workingSsnTax, esEntity);
            }
            esEntity = xgmmlIO.readSsnEvent(inputSsnTax);
        }
        xgmmlIO.writeSsnEvent(workingSsnTax, null);
    }
    
    private Entity.FullGnn fullGnn;

    public void initNbStatistic(Entity.FullGnn fullGnn) {	
        this.fullGnn = fullGnn;
    }

    public Entity.GnnStatistic nbCalculate() {
        try {
            Entity.GnnStatistic gsResult = entity.new GnnStatistic();
            for (Entity.Neighborhood neighborhood : fullGnn.clusterNeighborhoodMap.values()) {
            	Entity.Pfam pfam = fullGnn.pfamMap.get(neighborhood.source);
            	Entity.Cluster cluster = fullGnn.clusterMap.get(neighborhood.target);
                Integer N = fullGnn.getSequenceCount();
                Integer n = Integer.parseInt(cluster.totalSsnSequences);
                Integer K = fullGnn.getPfamSequenceCount(pfam.id);
                Integer k = fullGnn.getNeighborhoodSequenceCount(neighborhood);
                Double uniqueness = -1 * (logBinom(K, k) + logBinom(N - K, n - k) - logBinom(N, n));
                gsResult.neighborhoodUniqueness.put(neighborhood.label, uniqueness);

                if (n > gsResult.maximumClusterSize) {
                    gsResult.maximumClusterSize= n;
                }
                if (n < gsResult.minimumClusterSize) {
                    gsResult.minimumClusterSize= n;
                }

                if (K > gsResult.maximumPfamSize) {
                    gsResult.maximumPfamSize= K;
                }
                if (K < gsResult.minimumPfamSize) {
                    gsResult.minimumPfamSize= K;
                }

                if (gsResult.clusterUniqueness.containsKey(cluster.clusterNumber)) {
                    Double newUniqueness = gsResult.clusterUniqueness.get(cluster.clusterNumber) + uniqueness;
                    gsResult.clusterUniqueness.put(cluster.clusterNumber, newUniqueness);
                } else {
                    gsResult.clusterUniqueness.put(cluster.clusterNumber, uniqueness);
                }

                if (gsResult.pfamUniqueness.containsKey(pfam.pfam)) {
                    Double newUniqueness = gsResult.pfamUniqueness.get(pfam.pfam) + uniqueness;
                    gsResult.pfamUniqueness.put(pfam.pfam, newUniqueness);
                } else {
                    gsResult.pfamUniqueness.put(pfam.pfam, uniqueness);
                }

            }
            return gsResult;
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static double logBinom(double n, double k) {
        double sum = 0;
        for(int i = 1; i <=k; i++) {
            sum += Math.log10((n+1-i)/i);
        }
        return sum;
    }


    private static final double MAXIMUM_CLUSTER_SIZE = 150;
    private static final double MINIMUM_CLUSTER_SIZE = 30;
    private static final double MAXIMUM_PFAM_SIZE = 150;
    private static final double MINIMUM_PFAM_SIZE = 30;
    private static final double MAXIMUM_GROUP_TRANSPARENCY = 255;
    private static final double MINIMUM_GROUP_TRANSPARENCY = 30;
    private static final double MAXIMUM_FAMILY_TRANSPARENCY = 255;
    private static final double MINIMUM_FAMILY_TRANSPARENCY = 30;

    private Entity.ColoredSsnAnalyzerResult coloredSsnAnalyzerResult;
    private Entity.GnnStatistic gnnStatistic;
    private Set<Integer> writtenClusters;
    private File outputFile;
    private Double minimumGroupUniqueness = Double.MAX_VALUE;
    private Double maximumGroupUniqueness = Double.MIN_VALUE;
    private Double minimumFamilyUniqueness = Double.MAX_VALUE;
    private Double maximumFamilyUniqueness = Double.MIN_VALUE;
    private NumberFormat uniquenessDF = DecimalFormat.getNumberInstance(Locale.US);

    public void initXGnnGenerator(Entity.FullGnn fullGnn, Entity.GnnStatistic gnnStatistic, Entity.ColoredSsnAnalyzerResult coloredSsnAnalyzerResult) {
        writtenClusters = new HashSet<>();
        uniquenessDF.setMaximumFractionDigits(2);
        uniquenessDF.setGroupingUsed(false);
        this.coloredSsnAnalyzerResult = coloredSsnAnalyzerResult;
        this.fullGnn = fullGnn;
        this.gnnStatistic = gnnStatistic;
        for(Double uniqueness : gnnStatistic.clusterUniqueness.values()) {
            if(uniqueness < minimumGroupUniqueness)
                minimumGroupUniqueness = uniqueness;
            if(uniqueness > maximumGroupUniqueness)
                maximumGroupUniqueness = uniqueness;
        }
        for(Double unqiqueness : gnnStatistic.pfamUniqueness.values()) {
            if(unqiqueness < minimumFamilyUniqueness)
                minimumFamilyUniqueness = unqiqueness;
            if(unqiqueness > maximumFamilyUniqueness)
                maximumFamilyUniqueness = unqiqueness;
        }
    }

    public void xgwriteToFile(File outputFile) throws FileNotFoundException, XMLStreamException {
        this.outputFile = outputFile;
        writeXGnn(fullGnn.gnn);
        for(Entity.Cluster cluster : fullGnn.clusterMap.values()) {
            writeGroup(cluster);
        }
        for(Entity.Pfam pfam : fullGnn.pfamMap.values()) {
            writeFamily(pfam);
        }
        for(List<Entity.Neighborhood> neighborhoodList: fullGnn.pfamNeighborhoodMap.values()) {
            for(Entity.Neighborhood neighborhood : neighborhoodList) {
                writeRelationship(neighborhood);
            }
        }
        xgmmlIO.writeExtendedGnnEvent(outputFile, null);
    }

    private void writeRelationship(Entity.Neighborhood neighborhood) throws XMLStreamException, FileNotFoundException {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        numberFormat.setMaximumFractionDigits(2);

        Entity.Pfam pfam = fullGnn.pfamMap.get(neighborhood.source);
        Entity.Cluster cluster = fullGnn.clusterMap.get(neighborhood.target);
        Entity.Relationship relationship = entity.new Relationship(pfam.pfam + "-" + cluster.clusterNumber, pfam.pfam, cluster.clusterNumber);
        relationship.seqCount= Integer.toString(fullGnn.getNeighborhoodSequenceCount(neighborhood));
        relationship.uniqueness= uniquenessDF.format(gnnStatistic.neighborhoodUniqueness.get(neighborhood.label));
        Double coverage = ((double)fullGnn.getNeighborhoodSequenceCount(neighborhood)) / Double.parseDouble(cluster.totalSsnSequences);
        relationship.coverage= uniquenessDF.format(coverage);
        xgmmlIO.writeExtendedGnnEvent(outputFile, relationship);
    }

    private void writeFamily(Entity.Pfam pfam) throws XMLStreamException, FileNotFoundException {
    	Entity.Family family = entity.new Family(pfam.pfam, pfam.pfam);
        family.seqCount= Integer.toString(fullGnn.getPfamSequenceCount(pfam.id));
        family.description= pfam.pfamDescription;
        family.pfam= pfam.pfam;
        family.go= xg_getGo(pfam.pfam);
        Double uniqueness = gnnStatistic.pfamUniqueness.get(pfam.pfam);
        family.uniqueness= uniquenessDF.format(uniqueness);
        family.nodeSize= mapFamilyNodeSize((double)fullGnn.getPfamSequenceCount(pfam.id)).toString();
        family.nodeTransparency= mapFamilyTransparency(gnnStatistic.pfamUniqueness.get(pfam.pfam)).toString();
        xgmmlIO.writeExtendedGnnEvent(outputFile, family);
    }

    private void writeGroup(Entity.Cluster cluster) throws XMLStreamException, FileNotFoundException {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        numberFormat.setMaximumFractionDigits(2);

        Integer clusterNumber = Integer.parseInt(cluster.clusterNumber);
        if(!writtenClusters.contains(clusterNumber)) {
        	Entity.Group group = entity.new Group(cluster.clusterNumber, cluster.clusterNumber);
            group.clusterNumber= cluster.clusterNumber;
            if(coloredSsnAnalyzerResult == null || coloredSsnAnalyzerResult.clusterNodeCountMap == null || clusterNumber == null){
            	System.out.printf("cNum NULL\n");  
            }
            Object o1= coloredSsnAnalyzerResult.clusterNodeCountMap.get(clusterNumber);
            if(o1 != null){
                group.nodeCount= o1.toString();
            }else{
            	System.out.printf("Error: clNum not found: %d sz=%d\n",clusterNumber,coloredSsnAnalyzerResult.clusterNodeCountMap.size());
            	
            }
            
            group.seqCount= cluster.totalSsnSequences;
            group.uniqueness= uniquenessDF.format(gnnStatistic.clusterUniqueness.get(cluster.clusterNumber));
            group.nodeFillColor= cluster.nodeFillColor;
            group.nodeShape= cluster.nodeShape;
            group.nodeSize= mapGroupNodeSize(Double.parseDouble(cluster.totalSsnSequences)).toString();
            group.nodeTransparency= mapGroupTransparency(gnnStatistic.clusterUniqueness.get(cluster.clusterNumber)).toString();

            Map<String, Double> unsortedPhylumStats = coloredSsnAnalyzerResult.clusterPhylumPercentageMap.get(clusterNumber);
            if(unsortedPhylumStats != null) {
                Map<String, Double> phylumStats = ServiceFacade.sortByValue(unsortedPhylumStats);
                for (String phylum : phylumStats.keySet()) {
                    Double value = phylumStats.get(phylum);
                    if(value != null) group.phylumStat.add(phylum + ": " + numberFormat.format(value));
                }
            }

            xgmmlIO.writeExtendedGnnEvent(outputFile, group);
            writtenClusters.add(Integer.parseInt(cluster.clusterNumber));

        }
    }

    private Double mapGroupTransparency(Double uniqueness) {
        double perc = uniqueness/(double)(maximumGroupUniqueness);
        return MINIMUM_GROUP_TRANSPARENCY + perc*(MAXIMUM_GROUP_TRANSPARENCY-MINIMUM_GROUP_TRANSPARENCY);
    }

    private Double mapFamilyTransparency(Double uniqueness) {
        double perc = uniqueness/(double)(maximumFamilyUniqueness);
        return MINIMUM_FAMILY_TRANSPARENCY + perc*(MAXIMUM_FAMILY_TRANSPARENCY-MINIMUM_FAMILY_TRANSPARENCY);
    }

    private Double mapGroupNodeSize(Double ssnClusterSize) {
        double perc = ssnClusterSize/(double)(gnnStatistic.maximumClusterSize);
        return MINIMUM_CLUSTER_SIZE + (MAXIMUM_CLUSTER_SIZE-MINIMUM_CLUSTER_SIZE)*perc;
    }

    private Double mapFamilyNodeSize(Double familySize) {
        double perc = familySize/(double)(gnnStatistic.maximumPfamSize);
        return MINIMUM_PFAM_SIZE + (MAXIMUM_PFAM_SIZE-MINIMUM_PFAM_SIZE)*perc;
    }

    private void writeXGnn(Entity.Gnn gnn) throws XMLStreamException, FileNotFoundException {
    	Entity.XGnn xGnn = entity.new XGnn(gnn.label);
        xgmmlIO.writeExtendedGnnEvent(outputFile, xGnn);
    }

    private List<String> xg_getGo(String pfam) {
        List<String> returnValue = new ArrayList<>();
        List<ResourceIO.Pfam2GoEntry> goresult = xgmmlIO.getGo(pfam);
        for(ResourceIO.Pfam2GoEntry xgEntry : goresult) {
            String go = xgEntry.go;
            String description = xgEntry.description;
            returnValue.add(go + ": " + description);
        }
        return returnValue;
    }
    
}

