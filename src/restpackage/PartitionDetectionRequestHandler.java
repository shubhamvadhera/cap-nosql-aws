package restpackage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.*;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;


public class PartitionDetectionRequestHandler {

	ArrayList<RequestPojo> rpList = null;
	String slaveNodeURL;

	public final String SUCCESSFUL_QUEUE_EXECUTION = "Successfully executed the queue.";

	public PartitionDetectionRequestHandler(String slaveUrl, String intPort, String fillerURL) {

		// initializing url for slave.

		this.slaveNodeURL = slaveUrl + intPort + fillerURL;
	}

	public boolean isInPartition() {
		boolean partitionFlag = false;

		// Logic which will check for node's partition.

		return partitionFlag;
	}

	public void generateQueue(RequestPojo requestPojo) {
		if (rpList == null)
			rpList = new ArrayList<RequestPojo>();

		rpList.add(requestPojo);
	}

	public String executeRequestQueue() {
		System.out.println("-----------------------------------------");
		System.out.println(slaveNodeURL);
		if (!rpList.isEmpty()) {

			for (RequestPojo requestPojo : rpList) {
				// execute requests.
				if (requestPojo.getRequestType().equalsIgnoreCase("post")) {
					executePost(requestPojo);
				} else if (requestPojo.getRequestType().equalsIgnoreCase("get")) {
					executeGet(requestPojo);
				} else if (requestPojo.getRequestType().equalsIgnoreCase("put")) {
					executePut(requestPojo);
				} else if (requestPojo.getRequestType().equalsIgnoreCase("delete")) {
					executeDelete(requestPojo);
				} else {
				}
			}
			System.out.println("Queue exection has been finished.");
		}else{
			System.out.println("Empty Queue");
			return "Queue is Empty.";
		}
		
		try{
			rpList.removeAll(rpList);
		}catch(Exception e){
			e.printStackTrace();
		}

		return SUCCESSFUL_QUEUE_EXECUTION;

	}

	public void executePost(RequestPojo request) {
		System.out.println("------------------------");
		System.out.println(SimpleRestService.POST_INTERNAL_URL);
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(slaveNodeURL+SimpleRestService.POST_INTERNAL_URL); // creates post
																// object for
																// slave using
																// slaveNodeURL

			StringEntity input = new StringEntity(request.getRequestKey() + ":" + request.getRequestValue());
			input.setContentType("application/json");
			postRequest.setEntity(input);

			HttpResponse response = httpClient.execute(postRequest);

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			String output;
			String ret = "";
			System.out.println("Response from master:\n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
				ret += output;
			}

			httpClient.getConnectionManager().shutdown();
			ret = "Response from master: " + ret;

			//return ret;
		} catch (Exception e) {
			System.out.println("EXCEPTION OCCURRED !");
			System.out.println(e.getMessage());
			System.out.println(e.getCause());
			//return "EXCEPTION OCCURRED: " + e.getMessage();
		}

	}

	public void executeGet(RequestPojo request) {

	}

	public void executePut(RequestPojo request) {
		
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPut putRequest = new HttpPut(slaveNodeURL + SimpleRestService.PUT_INTERNAL_URL + request.getRequestKey());
			putRequest.addHeader("Content-Type", "application/json");
            putRequest.addHeader("Accept", "application/json");
			StringEntity input = new StringEntity(request.getRequestValue());
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
			httpClient.getConnectionManager().shutdown();
			ret = "Response from master: " + ret;
			System.out.println("Puttttttttttttttttt Request");
			System.out.println(ret);
		} catch (Exception e) {
			System.out.println("EXCEPTION OCCURRED !");
			System.out.println(e.getMessage());
			System.out.println(e.getCause());
		}
		
		
	}

	public void executeDelete(RequestPojo request) {
		
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpDelete deleteRequest = new HttpDelete(slaveNodeURL + SimpleRestService.DELETE_INTERNAL_URL + request.getRequestKey());
			deleteRequest.addHeader("Content-Type", "application/json");
            deleteRequest.addHeader("Accept", "application/json");
			
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
			
			ret = "Response from master: " + ret;
			System.out.println("Delete");
			System.out.println(ret);
		} catch (Exception e) {
			System.out.println("EXCEPTION OCCURRED !");
			System.out.println(e.getMessage());
			System.out.println(e.getCause());
		}
		
	}

}
