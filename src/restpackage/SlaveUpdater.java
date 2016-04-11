package restpackage;
public class SlaveUpdater implements Runnable {
    
    final int slaveNum;
    
    public SlaveUpdater (int slaveNum) {
        this.slaveNum = slaveNum;
    }
    
    public void run () {
   	    while(true) {
   	        SimpleRestService.checkQueue(slaveNum);
   	        try {
   	            Thread.sleep(5000);
   	        } catch (Exception ex) {}
   	    }
    }
}