package restpackage;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Delete;
import org.restlet.resource.ServerResource;
//import com.sun.org.apache.xml.internal.security.keys.content.KeyValue;
import org.restlet.representation.Representation;
import org.restlet.*;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import paulpackage.*;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.client.*;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.*;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;

import java.util.*;
import java.io.*;

public class SimpleRestService extends ServerResource {
	protected static final String EXT_PORT  = ":8081";
	protected static final String INT_PORT  = ":8080";
	
	protected static final String FILLER_URL = "/restlet/service";
	protected static final String POST_INTERNAL_URL  = "/postInternal";
	protected static final String PUT_INTERNAL_URL  = "/putInternal?newValue=";
	protected static final String DELETE_INTERNAL_URL  = "/deleteInternal?key=";
	protected static final String GET_SLAVE1_URL  = "?type=slave1";
	protected static final String GET_SLAVE2_URL  = "?type=slave2";
	
	protected static final String MASTER_URL = "http://172.30.0.130";
	protected static final String SLAVE_1_URL = "http://172.30.1.85";
	protected static final String SLAVE_2_URL = "http://172.30.2.21";
	
	protected static int version = 0;
	protected static boolean isSlave = false;
	protected static boolean isPartitioned = false;
	protected static boolean isCP = false;
	
	private static String currIP = "";
	
	private static final Logger logger = Logger.getLogger(SimpleRestService.class);
	
	private static ArrayList<PartitionDetectionRequestHandler> nodePartitionRequestList = new ArrayList<PartitionDetectionRequestHandler>();
	 
	public static void init() {
		nodePartitionRequestList.add(new PartitionDetectionRequestHandler(SLAVE_1_URL, INT_PORT, FILLER_URL));
		nodePartitionRequestList.add(new PartitionDetectionRequestHandler(SLAVE_2_URL, INT_PORT, FILLER_URL));
	}
	
	KeyValueStorage kvstorage = new KeyValueStorage() ;
	
	/*@Get
	public String represent() {
        return "hello, world";
    }*/
	
	@Get ("json")
	public String getSomething() throws JSONException {
		
		JSONObject json = new JSONObject();
		if (isSlave && isPartitioned) {
			if(isCP) {
				json.put ("Response", "CP Slave in partition. GET rejected !");
				json.put("IP",getCurrIp());
				return json.toString();
			}
		}
		
		String request = getQuery().getValues("key");
		String result = null;
		String value = kvstorage.fetch(request);
		result = "Response from Restlet Webservice : " + value;
 		System.out.println("Value for the key "+request+ " is "+value);
		json.put(request,value);
		json.put("IP",getCurrIp());
		json.put("Version", version);
		//JSONArray ja = new JSONArray();
		//ja.put(json);
		//System.out.println("JARRAY"+ja);
		String returnString = json.toString();
 		System.out.println("returnString "+returnString);
		return returnString;
		
	}
	
	private String getCurrIp () {
		if (!currIP.equals("")) return currIP;
		InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
                return e.getMessage();
        }
        String sip = ip.toString();
        return sip.substring(sip.indexOf('/')+1);
	}

	@Post
	public String postSomething(Representation entity) throws ResourceException, IOException, JSONException {
		JSONObject json = new JSONObject();
		
		if (isSlave && isPartitioned) {
				json.put ("Response", "Slave in partition. POST rejected !");
				return json.toString();
		}
		
		String s = null;
	    if (entity.getMediaType().isCompatible(MediaType.APPLICATION_JSON)) s = entity.getText();
	    else return "Invalid JSON format. Try again!";
	    
	    String[] data = s.split(":");
	    data[0] = data[0].replaceAll("[^a-zA-Z]","");
	    data[1] = data[1].replaceAll("[^0-9]","");
	    //data[0] = data[0].trim();
	    //data[1] = data[1].trim();
	    
	    if(isSlave) return postRedirect(data[0],data[1]);
	   	/* else{
	    	return replicatePost(data[0],data[1]);
	    }*/
	    /*System.out.println("+++++++++++++++++++++++++");
	    System.out.println("Adding requests in queue");*/
	    //Adding requests for slaves in queue.
	    /*RequestPojo request = new RequestPojo();
		request.setRequestType("post");
		request.setRequestKey(data[0]);
		request.setRequestValue(data[1]);
		nodePartitionRequestList.get(0).generateQueue(request);
		nodePartitionRequestList.get(1).generateQueue(request);*/
		postPojo(data[0],data[1]);
	   	//System.out.println("returning...." + s);
		kvstorage.store(data[0], data[1]);
		json.put (data[0],data[1]);
		json.put ("Response", "Save successful");
		json.put("IP",getCurrIp());
		//System.out.println("Saved the value in database: " + s);
		return json.toString();
	}
	
	protected static void postPojo (String k, String v) {
		RequestPojo request = new RequestPojo();
		request.setRequestType("post");
		request.setRequestKey(k);
		request.setRequestValue(v);
		nodePartitionRequestList.get(0).generateQueue(request);
		nodePartitionRequestList.get(1).generateQueue(request);
		
		version++;
	}
	
	private String postRedirect (String key, String value) {
		String url = MASTER_URL + INT_PORT + FILLER_URL + POST_INTERNAL_URL;
		System.out.println ("Slave can't process post, redirecting to master: " + url);
		
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(url);
			
			StringEntity input = new StringEntity(key + ":" + value);
			input.setContentType("application/json");
			postRequest.setEntity(input);
			
			HttpResponse response = httpClient.execute(postRequest);
			
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			String output;
			String ret = "";
			System.out.println("Response from master:\n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
				ret+=output;
			}
			httpClient.getConnectionManager().shutdown();
			//replicatePost(key,value);
			
			JSONObject json = new JSONObject();
			json.put("IP",getCurrIp());
			json.put(key,value);
			//ret = "Response from master: " + ret;
			//return ret;
			return json.toString();
		} catch (Exception e) {
			System.out.println("EXCEPTION OCCURRED !");
			System.out.println(e.getMessage());
			System.out.println(e.getCause());
			return "EXCEPTION OCCURRED: " + e.getMessage();
		}
	}
	
	
	/*private String replicatePost (String key, String value) {
		String url = SLAVE_1_URL + INT_PORT + FILLER_URL + POST_INTERNAL_URL;
		System.out.println ("Trying to update slave at: " + url);
		
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(url);
			
			StringEntity input = new StringEntity(key + ":" + value);
			input.setContentType("application/json");
			postRequest.setEntity(input);
			
			HttpResponse response = httpClient.execute(postRequest);
			
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			String output;
			String ret = "";
			System.out.println("Response from master:\n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
				ret+=output;
			}
			httpClient.getConnectionManager().shutdown();
			ret = "Response from master: " + ret;
			return ret;
		} catch (Exception e) {
			System.out.println("EXCEPTION OCCURRED !");
			System.out.println(e.getMessage());
			System.out.println(e.getCause());
			return "EXCEPTION OCCURRED: " + e.getMessage();
		}
	}*/
	
	
	@Put
	public String putSomething(Representation entity) throws IOException, JSONException {
		
		JSONObject json = new JSONObject();
		if (isSlave && isPartitioned) {
				json.put ("Response", "Slave in partition. PUT rejected !");
				json.put("IP",getCurrIp());
				return json.toString();
		}
		
		String value = entity.getText();
		System.out.println("Value "+value);
		String key = getQuery().getValues("newValue");
		System.out.println("key "+key);
		key = key.replaceAll("[^a-zA-Z]","");
	    value = value.replaceAll("[^0-9]","");
		
		if(isSlave) return putRedirect(key,value);
		
		/*RequestPojo request = new RequestPojo();
		request.setRequestType("put");
		request.setRequestKey(key);
		request.setRequestValue(value);
		nodePartitionRequestList.get(0).generateQueue(request);
		nodePartitionRequestList.get(1).generateQueue(request);*/
		putPojo(key,value);
		
		boolean status;
		status = kvstorage.update(key, value);
		json.put (key,value);
		json.put ("Response", "Update successful");
		json.put("IP",getCurrIp());
		//return json.toString();
		
		if(status) return json.toString();
		
		else return "Error: The value was not updated";	
	}
	
	protected static void putPojo (String k, String v) {
		RequestPojo request = new RequestPojo();
		request.setRequestType("put");
		request.setRequestKey(k);
		request.setRequestValue(v);
		nodePartitionRequestList.get(0).generateQueue(request);
		nodePartitionRequestList.get(1).generateQueue(request);
		
		version++;
	}
	
	
	private String putRedirect (String key, String value) {
		
		String url = MASTER_URL + INT_PORT + FILLER_URL + PUT_INTERNAL_URL + key;
		System.out.println ("Slave can't process PUT, redirecting to master: " + url);
		
			try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPut putRequest = new HttpPut(url);
			putRequest.addHeader("Content-Type", "application/json");
            putRequest.addHeader("Accept", "application/json");
            //JSONObject keyArg = new JSONObject();
            //keyArg.put(key, value);
			//StringEntity input = new StringEntity(keyArg.toString());
			StringEntity input = new StringEntity(value);
			//input.setContentType("application/json");
			putRequest.setEntity(input);
			
			HttpResponse response = httpClient.execute(putRequest);
			
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			String output;
			String ret = "";
			System.out.println("Response from master:\n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
				ret+=output;
			}
			//replicatePut(key,value);
			httpClient.getConnectionManager().shutdown();
			//ret = "Response from master: " + ret;
			JSONObject json = new JSONObject();
			json.put("IP",getCurrIp());
			json.put(key,value);
			return json.toString();
		} catch (Exception e) {
			System.out.println("EXCEPTION OCCURRED !");
			System.out.println(e.getMessage());
			System.out.println(e.getCause());
			return "EXCEPTION OCCURRED: " + e.getMessage();
		}
	}

	@Delete
	public String deleteSomething(Representation entity) throws JSONException {
		
		JSONObject json = new JSONObject();
		if (isSlave && isPartitioned) {
				json.put ("Response", "Slave in partition. DELETE rejected !");
				json.put("IP",getCurrIp());
				return json.toString();
		}
		
		String request;
		Form form = new Form(entity);
		request = getQuery().getValues("key");
        System.out.println("The request is "+request);
        
        if(isSlave) return deleteRedirect(request);
        deletePojo(request);
		/*RequestPojo requestPJ = new RequestPojo();
		requestPJ.setRequestType("delete");
		requestPJ.setRequestKey(request);
		requestPJ.setRequestValue("nv");
		nodePartitionRequestList.get(0).generateQueue(requestPJ);
		nodePartitionRequestList.get(1).generateQueue(requestPJ);*/
		
	    //System.out.println("returning...." + s);
		//kvstorage.store(data[0], data[1]);
		//json.put (data[0],data[1]);
		json.put ("Response", "Delete successful");
		json.put("IP",getCurrIp());
		System.out.println("Deleted the value in database: " + request);
		//return json.toString();
		
		        
		kvstorage.delete(request);
		return json.toString();
	}
	
	protected static void deletePojo (String k) {
		RequestPojo request = new RequestPojo();
		request.setRequestType("delete");
		request.setRequestKey(k);
		request.setRequestValue("");
		nodePartitionRequestList.get(0).generateQueue(request);
		nodePartitionRequestList.get(1).generateQueue(request);
		version++;
	}
	
	private String deleteRedirect (String key) {
		String url = MASTER_URL + INT_PORT + FILLER_URL + DELETE_INTERNAL_URL + key;
		System.out.println ("Slave can't process DELETE, redirecting to master: " + url);
		
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpDelete deleteRequest = new HttpDelete(url);
			deleteRequest.addHeader("Content-Type", "application/json");
            deleteRequest.addHeader("Accept", "application/json");
			
			//StringEntity input = new StringEntity(key);
			//input.setContentType("application/json");
			//deleteRequest.setEntity(input);
			
			HttpResponse response = httpClient.execute(deleteRequest);
			
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			String output;
			String ret = "";
			System.out.println("Response from master:\n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
				ret+=output;
			}

			httpClient.getConnectionManager().shutdown();
			//ret = "Response from master: " + ret;
			JSONObject json = new JSONObject();
			json.put("IP",getCurrIp());
			json.put("Deleted",key);
			return json.toString();
		} catch (Exception e) {
			System.out.println("EXCEPTION OCCURRED !");
			System.out.println(e.getMessage());
			System.out.println(e.getCause());
			return "EXCEPTION OCCURRED: " + e.getMessage();
		}
		
	}
	
	public static void executeQueue(int nodeNumber){
		
		if(nodeNumber == 1) {
			nodePartitionRequestList.get(0).executeRequestQueue();
		} else if(nodeNumber == 2) {
			nodePartitionRequestList.get(1).executeRequestQueue();
		}
	}
	
	public static void checkQueue(int nodeNumber){
		
		if (isPartitioned) System.out.println ("PARTITION DETECTED !");
		String url = "";
		if(nodeNumber == 1){
			url = MASTER_URL + INT_PORT + FILLER_URL + GET_SLAVE1_URL;
		}else if(nodeNumber == 2){
			url = MASTER_URL + INT_PORT + FILLER_URL + GET_SLAVE2_URL;
		}
		
		System.out.println ("Executing queue: " + url);
		
			try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpParams httpParams = httpClient.getParams(); //sv-3
			HttpConnectionParams.setConnectionTimeout(httpParams, 1000); //sv-4
			HttpConnectionParams.setSoTimeout(httpParams, 1000); //sv-5
			HttpGet getRequest = new HttpGet(url);
			HttpResponse response = httpClient.execute(getRequest);
			
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpStatus.SC_OK) isPartitioned = false;
			else {
				isPartitioned = true;
				return;
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			String output;
			String ret = "";
			System.out.println("Response from master:\n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
				ret+=output;
			}
			//replicatePut(key,value);
			httpClient.getConnectionManager().shutdown();
			ret = "Response from master: " + ret;
			
			//return ret;
		} catch (Exception e) {
			isPartitioned = true;
			System.out.println("EXCEPTION OCCURRED !");
			System.out.println(e.getMessage());
			System.out.println(e.getCause());
			//return "EXCEPTION OCCURRED: " + e.getMessage();
		}
	}
}