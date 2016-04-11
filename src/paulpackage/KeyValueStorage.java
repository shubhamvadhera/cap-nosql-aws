package paulpackage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import javax.xml.bind.DatatypeConverter;

public class KeyValueStorage {
    public static SM sm;
    public KeyValueStorage() {
        sm = new SMFactory().getInstance();
    }

    public static class OID implements SM.OID {
        private byte[] key;
        private int intKey;
        private int keyType = 0;

        /**
         *  Constructor for the OID object
         *
         *@param  key  Description of Parameter
         *@since
         */
        public OID(byte[] key) {
          this.key = key;
        }

        public OID(int key) {
            this.key = Util.generateGUID() ;
        }

        /**
         *  Gets the key attribute of the OID object
         *
         *@return    The key value
         *@since
         */
         
        public String getKey() {
            return Util.toHexString(this.key) ;
        }


        /**
         *  Description of the Method
         *
         *@return    Description of the Returned Value
         *@since
         */
        public byte[] toBytes() {

            return this.key ;

        }
    }

    public void store(String key, String value) {
		try {

        	Record newRecord = new Record(value.length());
            newRecord.setBytes(value.getBytes());
            SM.OID oid = (SM.OID) sm.store(newRecord);
            String hexObjectId = DatatypeConverter.printHexBinary(oid.toBytes());
            writeToLookUpFile(key, hexObjectId);
            System.out.println("Value stored successfully");
        } catch(Exception e) {
        	System.out.println("Unable to Store File");
        	System.out.println( e ) ;
            e.printStackTrace() ;
        }
    }
    
    public String fetch(String key) {
		try {
    		SM.Record fetch = null;
    		String hexObjectId = readFromLookUpFile(key);
    		if(!hexObjectId.equals("")) {
    			byte[] recId = DatatypeConverter.parseHexBinary(hexObjectId);
        		OID oid = new OID(recId);
        		fetch = sm.fetch(oid);
        		String decoded = new String(fetch.getBytes(0, 0), "UTF-8");
        		decoded = decoded.replace("\n}", "");
			    System.out.println(decoded);
        	    System.out.println("Fetch Successful!");
        	    return decoded;
    		}
    		else {
        		System.out.println("Unable to fetch record");
        		return "No Value found for this key";
    		}
    		
		}
		catch(Exception e) {
			System.out.println(e);
            e.printStackTrace();
            return "Unable to fetch record";
    	}
    }

    public boolean update(String key, String value) {
		boolean updateStatus = false;
		try {
			SM.Record fetch = null;
			OID oid = null;
			String hexObjectId = readFromLookUpFile(key);
			System.out.println("Record fetched is " + hexObjectId);

			if(!hexObjectId.equals("")) {
				byte[] recId = DatatypeConverter.parseHexBinary(hexObjectId);
				oid = new OID(recId);
				Record updateRecord = new Record(value.length());
				updateRecord.setBytes(value.getBytes());
				SM.OID newoid = (SM.OID) sm.update(oid, updateRecord);
				String hexUpdatedObjectId = DatatypeConverter.printHexBinary(newoid.toBytes());
				updateLookUpFile(key, hexUpdatedObjectId);
				System.out.println("Successfully updated record");
				updateStatus = true;
			}
			else {
				System.out.println("Unable to fetch record");
				updateStatus = false;
			}
			}
			catch(Exception e) {
				updateStatus = false;
				System.out.println("Unable to update record");
				System.out.println( e );
				e.printStackTrace();
			}
			return updateStatus;
    }
    
    public void delete(String key) {
		try {
			SM.Record fetch = null;
			OID oid = null;
			String hexObjectId = readFromLookUpFile(key);
			System.out.println("Record fetched is " + hexObjectId);
			if(!hexObjectId.equals("")) {
				byte[] recId = DatatypeConverter.parseHexBinary(hexObjectId);
				oid = new OID(recId);
				sm.delete(oid);
				deleteFromLookUpFile(key);
			}
			else System.out.println("Unable to delete record");
			}
			catch(Exception e) {
    			System.out.println("Unable to delete record");
            	System.out.println( e );
            	e.printStackTrace();
    		}
    }

    public void deleteFromLookUpFile(String key) throws IOException {
		String fileLine = null;
		File file = new File("lookup.txt");
		File tempFile = new File("lookup.temp");
		
		if(!tempFile.exists()) tempFile.createNewFile();
	    
	    FileWriter fw = new FileWriter(tempFile.getAbsoluteFile());
	    BufferedWriter bufwrite = new BufferedWriter(fw);
	    FileReader fr = new FileReader(file);
	    BufferedReader br = new BufferedReader(fr);
    	while((fileLine = br.readLine())!=null) {
    		StringTokenizer str = new StringTokenizer(fileLine, " ");
    		String keystored = str.nextToken();
    		if(!keystored.equals(key)) {
    			bufwrite.write(fileLine);
    			bufwrite.write("\n");
    		}
    	}
    	bufwrite.close();
    	br.close();
    	file.delete();
    	tempFile.renameTo(file);    		
    }
 
    public void updateLookUpFile(String key, String newObjectId) throws IOException {
		String fileLine = null;
		File file = new File("lookup.txt");
		File tempFile = new File("lookup.temp");
		int count = 0;
		if(!tempFile.exists()) {
			tempFile.createNewFile();
		}
	    FileWriter fw = new FileWriter(tempFile.getAbsoluteFile());
	    BufferedWriter bufwrite = new BufferedWriter(fw);
	    FileReader fr = new FileReader(file);
	    BufferedReader br = new BufferedReader(fr);
    	while((fileLine = br.readLine())!=null) {
			if(count != 0) {
				bufwrite.write("\n");			
			}
    		StringTokenizer str = new StringTokenizer(fileLine, " ");
    		String keystored = str.nextToken();
    		if(keystored.equals(key)) {
    			String newData = fileLine.replaceFirst(".*", key + " " + newObjectId);
    			bufwrite.write(newData);
    		}
    		else {
    			bufwrite.write(fileLine);
    		}
    		count++;
    	}
    	bufwrite.close();
    	br.close();
    	file.delete();
    	tempFile.renameTo(file);
    }

    public String readFromLookUpFile(String key) throws IOException {
    	File directory = new File(".");
		File in = new File("lookup.txt");
    	FileInputStream fInputStream = new FileInputStream(in);
    	BufferedReader br = new BufferedReader(new InputStreamReader(fInputStream));
    	
    	String line = null;
    	while((line = br.readLine())!=null) {
    		StringTokenizer str = new StringTokenizer(line, " ");
    		String keystored = str.nextToken();
    		if(keystored.equals(key)) {
        		return str.nextToken();    				
    		}
    	}
    	return "";
    }

    public void writeToLookUpFile(String key, String objectId) throws IOException{
    	try {
    		String fileLine = null;
			File file = new File("lookup.txt");
        	if(!file.exists()) {
        		System.out.println("Creating  a new file");
			file.createNewFile();
        	} else {
        		System.out.println("File exists");
			}
            FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
            BufferedWriter bufwrite = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bufwrite);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            if(br.readLine()!=null) {
            		pw.println("");
                br.close();
            }
			pw.print(key);
			pw.print(" ");
			pw.print(objectId);
			System.out.println("Successfully written to file");            			
			pw.close();

    	} catch(IOException io) {
    		System.out.println("IO Exception");
    		io.printStackTrace();
    	}
    }

}