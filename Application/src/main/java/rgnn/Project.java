package rgnn;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.entity.InputStreamEntity;

public class Project {
    private String pname;
    public String projpath;
    private File projectRootFileDir;
    public String email;

    public Project(String name, File projectRootFile) {
        this.pname = name;
        this.projectRootFileDir =  projectRootFile;
        initProjManager();
    }
    public Project(){		  
        initProjManager();  	
    }
    public Project(String name){
    	this.pname = name;
    	newProject(pname);
    }

    public String getName() {
        return pname;
    }
    @Override
    public String toString() {
        return pname;
    }

    public List<File> getSsnFiles() {
        List<File> ssnFiles = new ArrayList<>();;
        for(File file : projectRootFileDir.listFiles()) {
            if(file.getName().endsWith(".xgmml") && !file.getName().endsWith(".gnn.xgmml")
                    && !file.getName().endsWith(".x.xgmml") && !file.getName().endsWith(".color.xgmml")) {
                ssnFiles.add(file);
            }
        }
        return ssnFiles;
    }

    public List<File> getFilteredFiles(File ssn) {
    	System.out.printf("getFilteredFiles not implemented!!!\n");
    	return null;
    }
    public File getFilteredFile() {		
    	File pdir= new File(projpath);
    	for(File file : pdir.listFiles()) {
    		if(file.getName().endsWith("FIL.xgmml")){	
    			return file;
    		}
    	}
    	return null;
    }

    public File getColoredSsn(File gnnFile)  {
        try {
            String fileName = gnnFile.getAbsolutePath().replace(".gnn.xgmml", ".color.xgmml");
            File coloredFile = new File(fileName);
            return coloredFile;
        } catch (Exception ex) {

        }
        return null;
    }

    public void getColoredSsnAnalyzerResult(File gnnFile)  { System.out.printf("getColoredSsnAnalyzerResult:  not implemented anymore\n"); }

    public void getStatGnn(File gnnFile)  { System.out.printf("getStatGnn:  not implemented anymore\n"); }
    
    public void getFullGnn(File gnnFile)  { System.out.printf("getFullGnn:  not implemented anymore\n"); }

    public List<File> getGNNFiles() {
        List<File> gnnFiles = new ArrayList<>();;
        for(File file : projectRootFileDir.listFiles()) {
            if(file.getName().endsWith(".gnn.xgmml")) {
                gnnFiles.add(file);
            }
        }
        return gnnFiles;
    }

    public List<File> getGNNFiles(File ssnFile) {
        List<File> ssnFiles = new ArrayList<>();
        File ssnFolder = new File(ssnFile.getAbsolutePath().replace(".xgmml", "") + "/");
        for(File file : ssnFolder.listFiles()) {
            if(file.getName().endsWith(".gnn.xgmml")) {
                ssnFiles.add(file);
            }
        }
        return ssnFiles;
    }

    public File getFilterFile(File filteredGnnFile) {
        try {
            String fileName = filteredGnnFile.getAbsolutePath().replace(".gnn.xgmml", ".txt");
            File filterFile = new File(fileName);
            return filterFile;
        } catch (Exception ex) {

        }
        return null;
    }

    public File getExtendedGnnFile(File gnnFile) {
        try {
            String fileName = gnnFile.getAbsolutePath().replace(".gnn.xgmml", ".x.xgmml");
            File filterFile = new File(fileName);
            return filterFile;
        } catch (Exception ex) {

        }
        return null;
    }

    public List<File> getFilteredGNNFiles(File gnnFile) {
        List<File> gnnFiles = new ArrayList<>();
        File gnnFolder = new File(gnnFile.getAbsolutePath().replace(".gnn.xgmml", "") + "/");
        for(File file : gnnFolder.listFiles()) {
            if(file.getName().endsWith(".gnn.xgmml")) {
                gnnFiles.add(file);
            }
        }

        return gnnFiles;
    }

    public void addSSN(File ssnFile) {
        try {
        	System.out.printf("name: %s path: %s\n",ssnFile.getName(),ssnFile.getPath());
            this.specProjProp.setProperty("ssn", ssnFile.getPath());
            specProjProp.store(new FileOutputStream(specProjFile), "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void addFilteredGNN(File filteredGnnFile, File coloredFile, Entity.FullGnn fullGnn, Entity.GnnStatistic gnnStatistic, 
    		Entity.ColoredSsnAnalyzerResult coloredSsnAnalyzerResult, File filterFile, File extendedGnn, File gnnFile) {
        try {
            String folder = gnnFile.getAbsolutePath().replace(".gnn.xgmml", "") + "/";
            LocalDateTime date = LocalDateTime.now();
            String baseFileName = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace(":", "-");

            File localGnnFile = new File(folder + baseFileName + ".gnn.xgmml");
            File localGnnStatisticFile = new File(folder + baseFileName + ".stat.res");
            File localColoredFile = new File(folder + baseFileName + ".color.xgmml");
            File localResultFile = new File(folder + baseFileName + ".fullgnn.res");
            File localFilterFile = new File(folder + baseFileName + ".txt");
            File localColoredSsnAnalyzerResultFile = new File(folder + baseFileName + ".color.stat");
            File localExtendedFile = new File(folder + baseFileName + ".x.xgmml");
            Files.copy(filteredGnnFile.toPath(), localGnnFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(coloredFile.toPath(), localColoredFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(filterFile.toPath(), localFilterFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(extendedGnn.toPath(), localExtendedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            FileOutputStream fullOutputStream = new FileOutputStream(localResultFile);
            ObjectOutputStream fullObjectStream = new ObjectOutputStream(fullOutputStream);
            fullObjectStream.writeObject(fullGnn);
            fullObjectStream.close();
            FileOutputStream coloredStatOutputStream = new FileOutputStream(localColoredSsnAnalyzerResultFile);
            ObjectOutputStream coloredStatObjectStream = new ObjectOutputStream(coloredStatOutputStream);
            coloredStatObjectStream.writeObject(coloredSsnAnalyzerResult);
            coloredStatObjectStream.close();
            FileOutputStream statOutputStream = new FileOutputStream(localGnnStatisticFile);
            ObjectOutputStream statObjectStream = new ObjectOutputStream(statOutputStream);
            statObjectStream.writeObject(gnnStatistic);
            statObjectStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addGNN(File gnnFile, File coloredFile, Entity.FullGnn fullGnn, Entity.GnnStatistic gnnStatistic, 
    		Entity.ColoredSsnAnalyzerResult coloredSsnAnalyzerResult, File extendedGnn) {
        try {
            LocalDateTime date = LocalDateTime.now();
            String baseFileName = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace(":", "-");

            File localFolder = new File(projectRootFileDir, baseFileName + "/");
            localFolder.mkdir();

            File localGnnFile = new File(projectRootFileDir, baseFileName + ".gnn.xgmml");
            File localGnnStatisticFile = new File(projectRootFileDir,  baseFileName + ".stat.res");
            File localColoredFile = new File(projectRootFileDir, baseFileName + ".color.xgmml");
            File localColoredSsnAnalyzerResultFile = new File(projectRootFileDir, baseFileName + ".color.stat");
            File localResultFile = new File(projectRootFileDir, baseFileName + ".fullgnn.res");
            File localExtendedFile = new File(projectRootFileDir, baseFileName + ".x.xgmml");

            Files.copy(gnnFile.toPath(), localGnnFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(coloredFile.toPath(), localColoredFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(extendedGnn.toPath(), localExtendedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            FileOutputStream fullOutputStream = new FileOutputStream(localResultFile);
            ObjectOutputStream fullObjectStream = new ObjectOutputStream(fullOutputStream);
            fullObjectStream.writeObject(fullGnn);
            fullObjectStream.close();
            FileOutputStream coloredStatOutputStream = new FileOutputStream(localColoredSsnAnalyzerResultFile);
            ObjectOutputStream coloredStatObjectStream = new ObjectOutputStream(coloredStatOutputStream);
            coloredStatObjectStream.writeObject(coloredSsnAnalyzerResult);
            coloredStatObjectStream.close();
            FileOutputStream statOutputStream = new FileOutputStream(localGnnStatisticFile);
            ObjectOutputStream statObjectStream = new ObjectOutputStream(statOutputStream);
            statObjectStream.writeObject(gnnStatistic);
            statObjectStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addGNN(File gnnFile, File coloredFile, Entity.FullGnn fullGnn, Entity.GnnStatistic gnnStatistic, 
    		Entity.ColoredSsnAnalyzerResult coloredSsnAnalyzerResult, File extendedGnn, File ssnFile) {
        try {
            String folder = ssnFile.getAbsolutePath().replace("FIL.xgmml", "") + "/";            
            LocalDateTime date = LocalDateTime.now();
            String baseFileName = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace(":", "-");

            File localFolder = new File(folder, baseFileName + "/");
            localFolder.mkdir();

            File localGnnFile = new File(folder + baseFileName + ".gnn.xgmml");
            File localColoredFile = new File(folder + baseFileName + ".color.xgmml");
            File localExtendedFile = new File(folder + baseFileName + ".x.xgmml");

            Files.copy(gnnFile.toPath(), localGnnFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(coloredFile.toPath(), localColoredFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(extendedGnn.toPath(), localExtendedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void saveFile(File file,String suffix,File baseFile){
        try {
        	LocalDateTime date = LocalDateTime.now();    
        	String baseFileName = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace(":", "-");
        	String folder = baseFile.getAbsolutePath().replace("FIL.xgmml", "") + "/";
        	File localFile = new File(folder + baseFileName + suffix);
        	Files.copy(file.toPath(), localFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public File getFile2Save(String suffix){
        try {
        	File localFile = new File(projpath + "rgnn" + suffix);
        	return localFile;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addFilteredSSN(File filteredSsnFile) {
    	System.out.printf("filtered: %s p: %s\n",filteredSsnFile.getName(),filteredSsnFile.getPath());	
        try {
            LocalDateTime date = LocalDateTime.now();
            String baseFileName = new String(this.pname);  
        	this.projectRootFileDir = new File(System.getProperty("user.home") + "/rgnn");
        	String genFolder = this.projectRootFileDir + "/" + projectProp.getProperty("name") + "/";          
            File filtSsnFile = new File(genFolder + baseFileName + "FIL.xgmml");		
            File localFolder = new File(genFolder + baseFileName + "/");
            localFolder.mkdir();
            Files.copy(filteredSsnFile.toPath(), filtSsnFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }   
    

    File propertiesFile;
    File addressFile;
    File projectFile;	
    File specProjFile;  
    public Properties properties;
    public Properties addrProperties;
    public Properties projectProp;  
    public Properties specProjProp;
    
    private void initProjManager(){
        this.projectRootFileDir = new File(System.getProperty("user.home") + "/rgnn");
        if(!projectRootFileDir.exists()) {
            projectRootFileDir.mkdir();
        }
        try {
            this.properties = new Properties();
            this.addrProperties = new Properties();   
            projectProp = new Properties();
            this.specProjProp= new Properties();
            this.propertiesFile = new File(projectRootFileDir, "settings.ini");
            this.addressFile = new File(projectRootFileDir, "uri.ini");     
            projectFile = new File(projectRootFileDir, "current");
            if (propertiesFile.exists()) {
                this.properties.load(new FileInputStream(propertiesFile));
            }
            if (addressFile.exists()) {
                this.addrProperties.load(new FileInputStream(addressFile));
            }   
            if (projectFile.exists()) {
                this.projectProp.load(new FileInputStream(projectFile));
                pname= projectProp.getProperty("name");
            }        
            this.email = properties.getProperty("email");
            
            projpath= projectRootFileDir + "/" + pname +"/";
            this.specProjFile= new File(projpath, "project");
            InputStream is= new FileInputStream(specProjFile);
            if (specProjFile.exists()) {
                this.specProjProp.load(is);
            }
        } catch(FileNotFoundException fne){
        	System.out.printf("file not found: %s\n",fne.getMessage());
        	fne.printStackTrace();
        }catch(NullPointerException np){
        	System.out.printf("np exception: %s\n",np.getMessage());
        	np.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void newProject(String projName){
        this.projectRootFileDir = new File(System.getProperty("user.home") + "/rgnn");
        if(!projectRootFileDir.exists()) {
            projectRootFileDir.mkdir();
        }
        projectFile = new File(projectRootFileDir, "current");
        try{
	        if (projectFile.exists()) {
	        	projectProp= new Properties();
	        	this.projectProp.setProperty("name", projName);
	        	projectProp.store(new FileOutputStream(projectFile), "");
	        	File projDir= new File(System.getProperty("user.home") + "/rgnn/" + projName);
	        	projDir.mkdir();
	        	projpath= projectRootFileDir + "/" + pname +"/";
	        	specProjFile= new File(projpath, "project");		
	        	specProjProp= new Properties();
	        	specProjProp.setProperty("neighborhoodSize", "4");
	        	specProjProp.setProperty("coocurrence", "20");
	        	specProjProp.setProperty("th", "");
	        	specProjProp.setProperty("tax", "false");
	        	specProjProp.store(new FileOutputStream(specProjFile), "");
	        }
        }catch(Exception ex){ ex.printStackTrace(); }
    }

    public String getCytoscapeExecutable() {
        return properties.getProperty("cytoscape");
    }

    public void setCytoscapeExecutable(String value) {
        try {
            this.properties.setProperty("cytoscape", value);
            properties.store(new FileOutputStream(propertiesFile), "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public List<Project> listProjects() {
        File[] projectFiles = projectRootFileDir.listFiles();
        List<Project> projectList = new ArrayList<>();

        for(File projectFile : projectFiles) {
            if(projectFile.isDirectory()) {
                projectList.add(new Project(projectFile.getName(), projectFile));   
            } 
        }
        return projectList;
    }
    public File getFile(File dir,String suffix){
        for(File file : dir.listFiles()) {
        	if(file.isDirectory()){
        		File f= getFile(new File(file.getAbsolutePath()),suffix);
        		if(f!=null) return f;
        		else continue;
        	}
            if(file.getName().endsWith(suffix)) {
                return file;
            }
        }
        return null;
    }
    public File getRGNN(){
    	File projectDir= new File(projpath);
    	return getFile(projectDir,".x.xgmml");
    }

    public Project createProject(String name) {
        File projectFile = new File(projectRootFileDir, name);
        if(!projectFile.exists()) {
            projectFile.mkdir();
            return new Project(name, projectFile);
        } else {
            return null;
        }
    }

    public void deleteProject(Project project) {
        File projectFolder = new File(projectRootFileDir, project.getName());
        if(projectFolder.exists() && projectFolder.isDirectory()) {
            deleteDir(projectFolder);
        }
    }

    private void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }

}
