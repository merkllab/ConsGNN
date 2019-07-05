package rgnn;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.ProxySelector;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.concurrent.*;
import java.util.List;
import java.util.Properties;

public class WLoader {
	
    private static final String nsMarker = "<td>Neighborhood Size</td><td>";
    private static final String coMarker = "<td>Input % Co-Occurrence</td><td>";
    private static final String endMarker = "</td>";
    
    private String uploadUri =   "http://efi.igb.illinois.edu/efi-gnt/upload.php"; 
    private String generateUri = "http://efi.igb.illinois.edu/efi-gnt/compute.php";
    private String downloadUri = "http://efi.igb.illinois.edu/efi-gnt-dev/results";
    
    private MultipartEntityBuilder builder;
    private File ssnFile;
    public String efiURL;
    private String id;
    private String key;

    private Integer neighborhoodSize;
    private Integer coocurrence;

    private void setGnnRequesterProperties(Properties addrProperties){
    	if(addrProperties.getProperty("uploadUri")!=null)
    		uploadUri = addrProperties.getProperty("uploadUri");
    	if(addrProperties.getProperty("generateUri")!=null)
    		generateUri = addrProperties.getProperty("generateUri");
    	if(addrProperties.getProperty("downloadUri")!=null)
    		downloadUri = addrProperties.getProperty("downloadUri");
    }
    public WLoader(String url,Properties addrProperties) {
        this.efiURL = url;
        setGnnRequesterProperties(addrProperties);
    }
    public WLoader(File ssnFile,Properties addrProperties) {
        this.ssnFile = ssnFile;
        this.builder = MultipartEntityBuilder.create();
        setGnnRequesterProperties(addrProperties);
    }  
    public WLoader(String url,File ssnFile,Properties addrProperties) {
    	this.ssnFile = ssnFile;
        this.efiURL = url;
        setGnnRequesterProperties(addrProperties);
    }   

    public void request(File gnnFile, File coloredFile) throws Exception {
        if(!downloadHTML()) throw new Exception();
        if(!downloadGNN(gnnFile)) throw new Exception();
        if(!downloadColored(coloredFile)) throw new Exception();
    }

    public void request(Integer neighborhoodSize, Integer coocurrence, String email, File gnnFile, File coloredFile) throws Exception {
        this.neighborhoodSize = neighborhoodSize;
        this.coocurrence = coocurrence;
        if(!upload(email)) throw new Exception("Uploading SSN failed");
        if(!generate()) throw new Exception("Generating GNN failed");
        if(!downloadGNN(gnnFile)) throw new Exception("Downloading GNN failed");
        if(!downloadColored(coloredFile)) throw new Exception("Downloading colored SSN failed");
    }
    
    public void getGNNpair(File gnnFile, File coloredFile) throws Exception {
	    if(!downloadGNN(gnnFile)) throw new Exception();
	    if(!downloadColored(coloredFile)) throw new Exception();
    }
    
    public void upload_gen(Integer neighborhoodSize, Integer coocurrence, String email, File gnnFile, File coloredFile) throws Exception {
        this.neighborhoodSize = neighborhoodSize;
        this.coocurrence = coocurrence;
        if(!upload(email)) throw new Exception("Uploading SSN failed");
    }

    public boolean downloadHTML() { 	
        try {
        	System.out.printf("downloadHTML: efiURL = %s\n",efiURL);
            List<NameValuePair> params = URLEncodedUtils.parse(new URI(efiURL), "UTF-8");
            for(NameValuePair nameValuePair : params) {
                if(nameValuePair.getName().equals("id")) {
                    id = nameValuePair.getValue();
                } else if(nameValuePair.getName().equals("key")) {
                    key = nameValuePair.getValue();
                }
            }

            SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());
            CloseableHttpClient httpclient = HttpClients.custom().setRoutePlanner(routePlanner).build();
            HttpGet httpGet = new HttpGet(efiURL);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            if(response.getStatusLine().getStatusCode() == 200) {
                InputStream inputStream = response.getEntity().getContent();
                String html = IOUtils.toString(inputStream);

                int nsBegin = html.indexOf(nsMarker);
                int nsEnd = html.indexOf(endMarker, nsBegin+nsMarker.length());
                int coBegin = html.indexOf(coMarker);
                int coEnd = html.indexOf(endMarker, coBegin+coMarker.length());
                if(nsBegin >= 0 && coBegin >= 0 && nsEnd >= 0 && coEnd >= 0) {
                    String nsString = html.substring(nsBegin+nsMarker.length(),nsEnd);
                    String coString = html.substring(coBegin+coMarker.length(), coEnd);
                    coString = coString.replace("%","");
                    neighborhoodSize = Integer.parseInt(nsString);
                    coocurrence = Integer.parseInt(coString);
                    return true;
                }
            }
            System.out.printf("downloadHTML(): statuscode!=200: %d\n",response.getStatusLine().getStatusCode());
            return  false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
 
    public String dwlStatus(Project project) { 	
        try {

            SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());
            CloseableHttpClient httpclient = HttpClients.custom().setRoutePlanner(routePlanner).build();
            String checkStr = new String("https://efi.igb.illinois.edu/efi-gnt/job_status.php?id=");
            checkStr = checkStr + this.id + "&key=" + this.key;
            HttpGet httpGet = new HttpGet(checkStr);
            int cnt= 0;  int ret= -2; int ret2= -2;   
            boolean bnew= false;   boolean brun= false;
            String html= null;
            CloseableHttpResponse response = httpclient.execute(httpGet);
            if(response.getStatusLine().getStatusCode() == 200){	
                InputStream inputStream = response.getEntity().getContent();
                html = IOUtils.toString(inputStream);	
            	ret= html.indexOf("NEW");	 
            	if(ret != -1) bnew= true;
            	ret= html.indexOf("RUNNING");
            	if(ret != -1) 
            		brun= true;
            }
            while(!(bnew && brun && ret == -1)) {   
            	try{
            	  response.close();
            	  TimeUnit.SECONDS.sleep(5);
            	  response = httpclient.execute(httpGet);
                  if(response.getStatusLine().getStatusCode() == 200){	
                    InputStream inputStream = response.getEntity().getContent();
                    html = IOUtils.toString(inputStream);	
                  	ret= html.indexOf("NEW");
                  	if(ret != -1) bnew= true;
                  	ret= html.indexOf("RUNNING");	
                	if(ret != -1) 
                		brun= true;      	  
                  }
            	  cnt++;
                  System.out.printf("dwl GNN cnt=%d\n", cnt);
            	}catch(Exception ex){
            		System.out.printf("dwlGNN ex: %s\n",ex.getMessage());
            		return null;
            	}
            }
            response.close();  
            System.out.printf("dwlStatus(): statuscode: %d, %s\n",response.getStatusLine().getStatusCode(),html);
            return html;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }   
    
    private boolean upload(String email) throws IOException, ParseException {
        SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());
        CloseableHttpClient httpclient = HttpClients.custom().setRoutePlanner(routePlanner).build();
            HttpPost httpPost = new HttpPost(uploadUri);
            HttpEntity entity = builder.addBinaryBody("file", ssnFile)    		
                    .addTextBody("email", email)
                    .addTextBody("cooccurrence", coocurrence.toString())
                    .addTextBody("neighbor_size", neighborhoodSize.toString())
                    .addTextBody("submit", "submit").build();
            httpPost.setEntity(entity);
            int cnt=1;
            CloseableHttpResponse httpResponse = null;
        	boolean waitForConnection= false;
            do{
            	if(waitForConnection) {
					try { TimeUnit.SECONDS.sleep(2);
					} catch (InterruptedException e) {	e.printStackTrace(); }  }
            	try{
            	    httpResponse = httpclient.execute(httpPost);
                    System.out.printf("%d. %d\n", cnt, httpResponse.getStatusLine().getStatusCode());
            	}catch(java.net.UnknownHostException uhe){
            		System.out.printf("Is there a network connection? %s\n",uhe.getMessage());
            		waitForConnection= true;
            	}catch(Exception ex){
            		ex.printStackTrace();
            	}
            }while(httpResponse==null || httpResponse.getStatusLine().getStatusCode() != 200 && cnt++ < 20);	
            System.out.printf("upl cnt=%d\n", cnt);
            if (cnt < 20){
                InputStream resultStream = httpResponse.getEntity().getContent();
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(resultStream));
                this.id = ((Long) jsonObject.get("id")).toString(); 
                try{
                 this.key = (String) jsonObject.get("key");  
                }catch (ClassCastException cce) {
                	System.out.printf("error: %s\n",cce.getMessage());
                }
                return true;
            } else {
                return false;
            }
    }

    public void ssn1(){
    	try{
	        SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());
	        CloseableHttpClient httpclient = HttpClients.custom().setRoutePlanner(routePlanner).build();
	        HttpGet httpGet = new HttpGet("https://efi.igb.illinois.edu/efi-est/");
	        CloseableHttpResponse response = httpclient.execute(httpGet);
	        if(response.getStatusLine().getStatusCode() == 200) {
                InputStream inputStream = response.getEntity().getContent();
                String html = IOUtils.toString(inputStream);
                System.out.printf("%s\n",html);
                
                int timeout = 1000*60*60;
                SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setSoTimeout(timeout).build();
                RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(timeout).setConnectTimeout(timeout).setSocketTimeout(timeout).build();
                CloseableHttpClient hcpost = HttpClients.custom().setRoutePlanner(routePlanner).setDefaultSocketConfig(socketConfig).setDefaultRequestConfig(requestConfig).build();
                HttpPost httpPost = new HttpPost("https://efi.igb.illinois.edu/efi-est/");   
                MultipartEntityBuilder builder1 = MultipartEntityBuilder.create();
                HttpEntity entity = builder1.addTextBody("optionBtab", "optionBtab")
                        .addTextBody("families-input", "IPR004342")
                        .addTextBody("option-b-email", "johndoe@email.edu").build();
                httpPost.setEntity(entity);
                CloseableHttpResponse repost = hcpost.execute(httpPost);
                if(repost.getStatusLine().getStatusCode() == 200){
                	System.out.printf("return OK!\n");
                }else{
                	System.out.printf("return %d\n",repost.getStatusLine().getStatusCode());
                }
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
    }
    
    private boolean generate() {
        try {
            SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());
            int timeout = 1000*60*60;
            SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setSoTimeout(timeout).build();
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(timeout).setConnectTimeout(timeout).setSocketTimeout(timeout).build();
            CloseableHttpClient httpclient = HttpClients.custom().setRoutePlanner(routePlanner).setDefaultSocketConfig(socketConfig).setDefaultRequestConfig(requestConfig).build();
            HttpPost httpPost = new HttpPost(generateUri);
            HttpEntity entity = builder.addBinaryBody("file", ssnFile)
                    .addTextBody("id", this.id)
                    .addTextBody("key", this.key).build();
            httpPost.setEntity(entity);
            CloseableHttpResponse response = httpclient.execute(httpPost);

            int cnt=0;
            while(response.getStatusLine().getStatusCode() != 200 && cnt<10) {
            	try{
            	response.close();
            	TimeUnit.SECONDS.sleep(5);
            	response = httpclient.execute(httpPost);
            	cnt++;
                System.out.printf("generate cnt=%d\n", cnt);
            	}catch(Exception ex){}
            }
            if(response.getStatusLine().getStatusCode() == 200) {
                return true;
            } else {
                return false;
            }       
        } catch (SocketTimeoutException ex) {
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
  
    private boolean downloadGNN(File gnnFile) {
    	CloseableHttpResponse response= null;
    	CloseableHttpClient httpclient= null;
    	HttpGet httpGet= null;
        try {
        	String filename = ssnFile.getName();
        	filename = filename.substring(0, filename.length()-6);
        	String uri = downloadUri + "/" + id + "/" + id + "_" + filename +  "_pfam_family_gnn_co" + coocurrence + "_ns" + neighborhoodSize + ".xgmml";
            SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());
            httpclient = HttpClients.custom().setRoutePlanner(routePlanner).build();
            httpGet = new HttpGet(uri);
            response = httpclient.execute(httpGet);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }      
        int cnt=0;
        while(response.getStatusLine().getStatusCode() != 200) {
        	try{
        	response.close();
        	TimeUnit.SECONDS.sleep(5);
        	response = httpclient.execute(httpGet);
        	cnt++;
            System.out.printf("dwl GNN cnt=%d\n", cnt);
        	}catch(Exception ex){
        		System.out.printf("dwlGNN ex: %s\n",ex.getMessage());
        	}
        }
        System.out.printf("cnt=%d\n", cnt);
        if(response.getStatusLine().getStatusCode() == 200) {
        	try{         	
            InputStream inputStream = response.getEntity().getContent();
            FileOutputStream outputStream = new FileOutputStream(gnnFile);
            IOUtils.copy(inputStream,outputStream);
            outputStream.close();
        	}catch(Exception ex){}
            return true;
        } else {
            return false;
        }

    }

    private boolean downloadColored(File coloredFile) {
        try {
        	String filename = ssnFile.getName();
        	filename = filename.substring(0, filename.length()-6);
        	String uri = downloadUri + "/" + id + "/" + id + "_" + filename +  "_ssn_cluster_gnn_co" + coocurrence + "_ns" + neighborhoodSize + ".xgmml";       
            SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());
            CloseableHttpClient httpclient = HttpClients.custom().setRoutePlanner(routePlanner).build();
            HttpGet httpGet = new HttpGet(uri);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            
            if(response.getStatusLine().getStatusCode() == 200) {
                InputStream inputStream = response.getEntity().getContent();
                FileOutputStream outputStream = new FileOutputStream(coloredFile);
                IOUtils.copy(inputStream,outputStream);
                outputStream.close();
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public Integer getNeighborhoodSize() {
        return neighborhoodSize;
    }

    public Integer getCoocurrence() {
        return coocurrence;
    }

    public class SsnRequester {}  // unused
    
}
