package rgnn;

import java.io.File;

public class ui {
    private static Project findProjectByName(String name) {
        ServiceFacade serviceFacade = ServiceFacade.getInstance();
        for(Project project : serviceFacade.listProjects()) {
            if(project.getName().equals(name))
                return project;
        }
        return null;
    }
	public static void main(String[] args) {
        if(args[0].compareTo("new")==0){
        	if(args.length < 3) {System.err.println("3 args required: new <ssnFilename> <newProjectName>");  }
        	else{
        		String fileName = new String(args[1]);
        		String projName= new String(args[2]);
        		Project p1= new Project(projName);
        		File inputFile= new File(fileName);
        		p1.addSSN(inputFile);
        		System.exit(0);
        	}
        }
        
        ServiceFacade serviceFacade = ServiceFacade.getInstance();
        try {
            if(args.length < 1 || args[0].isEmpty() || args[0].compareTo("help")==0){
            	System.out.printf("no args provided, available:\n");
            	System.out.printf("ssn filename\n");
            	System.out.printf("filter <filename>\n");
            	System.out.printf("gnn <filename>\n");
            	System.out.printf("fg <filename> = filter gnn\n");
            	System.exit(0);
            }
    
            if(args[0].compareTo("filter")==0) {	 	
                Project project = findProjectByName(serviceFacade.xgmmlIO.project.getName()); 
                String tax= serviceFacade.tax;
                String th= serviceFacade.sth;
                if(tax == null || tax.compareTo("")==0) {
                	tax = new String("true");
                }
                String fileName;
                if(args.length > 1){
                	fileName = new String(args[1]);
                }else{
                	fileName= serviceFacade.ssnfile;
                	if(fileName==null) System.out.printf("no ssn file in project file\n");
                }
                File ssnf = new File(fileName);
            	serviceFacade.sanAnalyze(new Boolean(tax),ssnf); 
                if(th == null || th.compareTo("")==0) { 
                    th = serviceFacade.smGetThreshold().toString();        
                }
                serviceFacade.addFilteredSsnToProject(project,new File(fileName),new Integer(th), new Boolean(tax));      
            } else if(args[0].compareTo("gengnn")==0) {		   
                Project project = findProjectByName(serviceFacade.xgmmlIO.project.getName());
                File filteredSSN = project.getFilteredFile();
                if(filteredSSN == null) System.out.printf("main: filtered ssn file empty\n");
                System.out.printf("upload follows...\n");
                WLoader wl= serviceFacade.uploadForReqestProject(project, filteredSSN);	
                System.out.printf("upload finished\n");
            } else if(args[0].compareTo("dwlgnn")==0) {      
            	if(args.length < 2) {
            		System.err.println("Download uri missing!"); 
            		System.exit(1); 
            	}
            	Project project = findProjectByName(serviceFacade.xgmmlIO.project.getName());
            	File filteredSSN = project.getFilteredFile();
            	serviceFacade.addGnnToProject(project,filteredSSN, new String(args[1]),true);
            } else if(args[0].compareTo("request")==0) {
                Project project = findProjectByName(serviceFacade.xgmmlIO.project.getName());
                File filteredSSN = project.getFilteredFile();
                if(filteredSSN == null) System.out.printf("main: filtered ssn file empty\n");
                serviceFacade.addGnnToProject(project,filteredSSN,null, null, null);  
            } else if(args[0].compareTo("gnn")==0) { 
            	if(args.length < 3) {
            		System.err.println("3 args: gnn colFile gnnFile");
            	}
            	Project project = findProjectByName(serviceFacade.xgmmlIO.project.getName());
            	File coloredFile = new File(args[1]);
            	File gnnFile = new File(args[2]);         	
                File filteredSSN = project.getFilteredFile();
            	serviceFacade.addGnnFileCol(project,filteredSSN,gnnFile,coloredFile);
            } else if(args[0].compareTo("dbg")==0) {	
                serviceFacade.dbg_createRGNN();
            } else if(args[0].compareTo("cyto")==0){
            	serviceFacade.callCytoscape();
            } else if(args[0].compareTo("html")==0){
            	WLoader wl= new WLoader("unused",serviceFacade.xgmmlIO.project.addrProperties);
            	wl.ssn1();
            }
             else {
        		System.err.println("Unknown args given!"); 
        		System.exit(1);
            }
        }
        catch( Exception exp ) {
        	exp.printStackTrace();
            System.out.println(exp.getMessage() );
        }
        System.exit(0);
	}
	
}
