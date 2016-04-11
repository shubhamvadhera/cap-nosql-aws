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

public class InternalRestService extends ServerResource {
	private static final Logger logger = Logger.getLogger(SimpleRestService.class);
	
	KeyValueStorage kvstorage = new KeyValueStorage() ;
	 
	@Get ("json")
	public String getInternal() throws JSONException {
		String type = getQuery().getValues("type");
		
		if(type.equals("slave1")){
			SimpleRestService.executeQueue(1);	
		}else if (type.equals("slave2")){
			SimpleRestService.executeQueue(2);
		}
		
		JSONObject json = new JSONObject();
		json.put(type + "Internal Get: IP:",getCurrIp());
		String returnString = json.toString();
		System.out.println("returnString " + returnString);
		return returnString;
		
	}
	
	private String getCurrIp () {
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
	public String postInternal(Representation entity) throws ResourceException, IOException {
		String s = entity.getText();
	    System.out.println("returning...." + s);
		String[] data = s.split(":");
		//data[0] = data[0].replaceAll("[^a-zA-Z]","");
		kvstorage.store(data[0], data[1]);
		if(!SimpleRestService.isSlave) SimpleRestService.postPojo(data[0], data[1]);
		else SimpleRestService.version++;
		return s;
	}
	
	@Put
	public String putInternal(Representation entity) throws ResourceException, IOException {
		String value = entity.getText();
		System.out.println("Value "+value);
		String key = getQuery().getValues("newValue");
		System.out.println("key " + key);
		boolean status;
		status = kvstorage.update(key, value);
		if(status) {
			if(!SimpleRestService.isSlave) SimpleRestService.putPojo(key, value);
			else SimpleRestService.version++;
			return value+" successfully saved against the key "+key;
		}
		else return "The value was not updated";	
	}
	
	
	@Delete
	public String deleteInternal(Representation entity) throws ResourceException, IOException {
		String request;
		request = getQuery().getValues("key");
        System.out.println("The DELETE request is " + request);
		kvstorage.delete(request);
		if(!SimpleRestService.isSlave) SimpleRestService.deletePojo(request);
		else SimpleRestService.version++;
		return "success";
	}
}
