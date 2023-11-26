package com.itextos.beacon.commonlib.modulecheck;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.dmr.ModelNode;

public class WildFlyModuleAvailabilityChecker {

	public static Map<String,Boolean> module=new HashMap<String,Boolean>();
	
	static {
		
		module.put("javax.servlet.api", false);
		module.put("org.apache.commons.logging", false);
		
		
		module.put("com.itextos.beacon.commonlib.apperrorhandler", false);
		module.put("com.itextos.beacon.commonlib.unittest", false);

	}
	
	public void doProcess() {
		  String host = "localhost"; // WildFly host
	      int port = 9990; // Management port
	      
		module.forEach((moduleName,status)->{
			

	        try (ModelControllerClient client = ModelControllerClient.Factory.create(host, port)) {
	            boolean isModuleAvailable = isModuleAvailable(client, moduleName);
	            if (isModuleAvailable) {
	            	module.put(moduleName, true);
	            } else {
	            	module.put(moduleName, false);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
            	module.put(moduleName, false);

	        }
		});
	}

    private  boolean isModuleAvailable(ModelControllerClient client, String moduleName) throws IOException {
        ModelNode operation = new ModelNode();
        operation.get("operation").set("read-resource");
        operation.get("include-runtime").set(true);
        operation.get("recursive").set(true);
        operation.get("address").setEmptyList();
        operation.get("address").add("core-service", "module");
        operation.get("address").add("module", moduleName);

        ModelNode result = executeOperation(client, operation);
        return result.get("outcome").asString().equals("success");
    }

    private  ModelNode executeOperation(ModelControllerClient client, ModelNode operation) throws IOException {
        OperationBuilder operationBuilder = new OperationBuilder(operation, false);
        ModelNode result = client.execute(operationBuilder.build());

        if ("success".equals(result.get("outcome").asString())) {
            return result.get("result");
        } else {
            throw new RuntimeException("Failed to execute operation: " + result);
        }
    }
}

