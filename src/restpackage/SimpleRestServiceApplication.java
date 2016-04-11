package restpackage;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

public class SimpleRestServiceApplication {
    
    static Router routerEx;
    static Router routerIn;

public static void main(String[] args) throws Exception {
    
    String type = args[0];
    String mode = args[1];
    System.out.println("Master/Slave: " + type);
    System.out.println("AP/CP: " + mode);
    if (type.equals("slave1") || type.equals("slave2")) {
			SimpleRestService.isSlave = true;
	} else if (type.equals("master")) {
			SimpleRestService.isSlave = false;
	} else {
			System.out.println("Invalid argument: " + type);
			return;
	}
	if (mode.equals("cp")) {
			SimpleRestService.isCP = true;
	} else if (mode.equals("ap")) {
			SimpleRestService.isCP = false;
	} else {
			System.out.println("Invalid argument: " + mode);
			return;
	}
    SimpleRestService.init();
    routerEx = new Router();
    routerEx.attach("/service", SimpleRestService.class);
    routerEx.attach("/service/{request}", SimpleRestService.class);
    
    routerIn = new Router();
    routerIn.attach("/service", InternalRestService.class);
    routerIn.attach("/service/{request}", InternalRestService.class);
    
    Application appEx = new Application() {
        @Override
        public synchronized Restlet createInboundRoot() {
            routerEx.setContext(getContext());
            return routerEx;
        };
    };
    
    Application appIn = new Application() {
        @Override
        public synchronized Restlet createInboundRoot() {
            routerIn.setContext(getContext());
            return routerIn;
        };
    };
    
    Component componentEx = new Component();
    componentEx.getDefaultHost().attach("/restlet", appEx);
    
    Component componentIn = new Component();
    componentIn.getDefaultHost().attach("/restlet", appIn);

    new Server(Protocol.HTTP, 8080, componentIn).start();
    new Server(Protocol.HTTP, 8081, componentEx).start();
    
    //calling check queue method
	//if(type.equals("slave1")) SimpleRestService.checkQueue(1);
	//else if(type.equals("slave2")) SimpleRestService.checkQueue(2);
    //}
    if (SimpleRestService.isSlave) {
        int slaveNum=2;
        if(type.equals("slave1")) slaveNum=1;
	    SlaveUpdater slaveUpdater = new SlaveUpdater(slaveNum);
	    Thread t = new Thread (slaveUpdater);
        t.start();
        System.out.println("SlaveUpdater " + slaveNum +  "thread started");
    }
    }
}