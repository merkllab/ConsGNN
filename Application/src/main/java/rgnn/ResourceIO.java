package rgnn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ResourceIO {
    private Set<String> whiteListR = new HashSet<>();
    
    private static final String TAXCAT_FILE = "categories.dmp";
    private static final String PFAM2GO_FILE = "pfam2go.txt";
    
    private InputStream inputStream;
    private Set<String> speciesSet;
    private Map<String, List<Pfam2GoEntry>> pfamMap;
    
    public ResourceIO(){
    	initResource();
    }
    
    public void initResource() {
        try {
        	File projectRootFileDir;
            projectRootFileDir = new File(System.getProperty("user.home") + "/rgnn/resources"); 	
            File wl= new File(projectRootFileDir, "whitelist.txt");   
            File ftax= new File(projectRootFileDir, TAXCAT_FILE);
            File fgo= new File(projectRootFileDir, PFAM2GO_FILE);
            this.inputStream = new FileInputStream(ftax);
            this.speciesSet = new HashSet<>();
            parseFileRes(true);	
            this.inputStream = new FileInputStream(fgo);
            this.pfamMap = new HashMap<>();    
            parseFileRes(false);
            
            FileInputStream fileInputStream = new FileInputStream(wl);
            parseFile(fileInputStream,whiteListR);
            fileInputStream.close();	
    
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public Set<String> getPfamWhitelist() {
        return getWhiteList();
    }

    public Set<String> getPfamWhitelist(File file) throws IOException {
    	Set<String> customWhitelist = initPfamWL(file); 
        return customWhitelist;
    }

    
    private void parseFileRes(boolean tax) throws IOException {	
    	
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line = bufferedReader.readLine();
        while (line != null) {
        	if(tax)
        		taxparseLine(line);
        	else 
        		goparseLine(line);
            line = bufferedReader.readLine();
        }

        bufferedReader.close();
    }
    
    private void taxparseLine(String line) {
        String[] components = line.split("\t");
        String category = components[0].trim();
        String species = components[1].trim();
        String tax = components[2].trim();
        speciesSet.add(species);
    }
    
    private void goparseLine(String line) {
        if(line.startsWith("!")) {
            ;
        } else {
            String pfam = line.substring(5,12);
            int splitIndex = line.indexOf(">");
            String goString = line.substring(splitIndex+1);
            String[] goStringParts = goString.split(";");
            String goDescription = goStringParts[0].trim().substring(3);
            String go = goStringParts[1].trim();

            Pfam2GoEntry entry = new Pfam2GoEntry(go, goDescription);

            if(pfamMap.containsKey(pfam)) {
                pfamMap.get(pfam).add(entry);
            } else {
                ArrayList<Pfam2GoEntry> list = new ArrayList<>();
                list.add(entry);
                pfamMap.put(pfam, list);
            }
        }
    }

    public boolean isSpecies(String taxonomyId) {
        return speciesSet.contains(taxonomyId);
    }
    
    public List<Pfam2GoEntry> getGo(String pfam) {
        if(pfamMap.containsKey(pfam))   return pfamMap.get(pfam);
        else return new ArrayList<>();
    }
    
    
    public class Pfam2Go {
	    private static final String PFAM2GO_FILE = "pfam2go.txt";
	
	    private InputStream inputStream;
	    private Map<String, List<Pfam2GoEntry>> pfamMap;
	
	    public Pfam2Go() {
	        try {
	            this.inputStream = Pfam2Go.class.getResourceAsStream(PFAM2GO_FILE);
	            this.pfamMap = new HashMap<>();
	            parseFile();
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    }
	
	    private void parseFile() throws IOException {
	        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	
	        String line = bufferedReader.readLine();
	        while(line != null) {
	            parseLine(line);
	            line = bufferedReader.readLine();
	        }
	
	        bufferedReader.close();
	    }

	    private void parseLine(String line) {
	        if(line.startsWith("!")) {
	        } else {
	            String pfam = line.substring(5,12);
	            int splitIndex = line.indexOf(">");
	            String goString = line.substring(splitIndex+1);
	            String[] goStringParts = goString.split(";");
	            String goDescription = goStringParts[0].trim().substring(3);
	            String go = goStringParts[1].trim();
	
	            Pfam2GoEntry entry = new Pfam2GoEntry(go, goDescription);
	
	            if(pfamMap.containsKey(pfam)) {
	                pfamMap.get(pfam).add(entry);
	            } else {
	                ArrayList<Pfam2GoEntry> list = new ArrayList<>();
	                list.add(entry);
	                pfamMap.put(pfam, list);
	            }
	        }
	    }
	
	    public List<Pfam2GoEntry> getGo(String pfam) {
	        if(pfamMap.containsKey(pfam))   
			return pfamMap.get(pfam);
	        else 
			return new ArrayList<>();
	    }
    } 
    public class Pfam2GoEntry {
	    public String go;
	    public String description;

	    public Pfam2GoEntry(String go, String description) {
	        this.go = go;
	        this.description = description;
	    }
    }
    private Set<String> whiteList;
    
    public void writePfamWhitelistToFile(Set<String> whitelist, File file) throws IOException {
        this.whiteList = whitelist;
        pfamWLwriteToFile(file);
    }

    public void pfamWLwriteToFile(File outputFile) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
        for(String entry : whiteList) {
            bufferedWriter.write(entry);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }
    public Set<String> initPfamWL(){
        InputStream filterStream = getClass().getResourceAsStream("whitelist.txt");
        try {
            parseFile(filterStream,whiteListR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return whiteListR;
    }
    

    public Set<String> initPfamWL(File inputFile) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(inputFile);
        Set<String> custList = new HashSet<>();
        parseFile(fileInputStream,custList);
        fileInputStream.close();
        return custList;
    }

    private void parseFile(InputStream inputStream,Set<String> wList) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String pfamString = bufferedReader.readLine();
        while (pfamString != null) {
        	wList.add(pfamString.trim());
            pfamString = bufferedReader.readLine();
        }
    }

    public Set<String> getWhiteList() {
        return whiteListR;
    }
    
    public class Taxonomy {
        private void parseFile() throws IOException {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line = bufferedReader.readLine();
            while (line != null) {
                parseLine(line);
                line = bufferedReader.readLine();
            }

            bufferedReader.close();
        }

        private void parseLine(String line) {
            String[] components = line.split("\t");
            String category = components[0].trim();
            String species = components[1].trim();
            String tax = components[2].trim();
            speciesSet.add(species);
        }

        public boolean isSpecies(String taxonomyId) {
            return speciesSet.contains(taxonomyId);
        }
    }
    public class PfamWhitelistReader {
        private Set<String> whiteList = new HashSet<>();

        public PfamWhitelistReader() {
            InputStream filterStream = getClass().getResourceAsStream("whitelist.txt");
            try {
                parseFile(filterStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public PfamWhitelistReader(File inputFile) throws IOException {
            FileInputStream fileInputStream = new FileInputStream(inputFile);
            parseFile(fileInputStream);
            fileInputStream.close();
        }

        private void parseFile(InputStream inputStream) throws IOException {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String pfamString = bufferedReader.readLine();
            while (pfamString != null) {
                whiteList.add(pfamString.trim());
                pfamString = bufferedReader.readLine();
            }
        }

        public Set<String> getWhiteList() {
            return whiteList;
        }

    }
}
