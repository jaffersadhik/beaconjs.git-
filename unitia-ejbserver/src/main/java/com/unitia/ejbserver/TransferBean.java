package com.unitia.ejbserver;

import java.io.Serializable;

public class TransferBean implements Serializable,Cloneable {
	
    private static final long serialVersionUID = -7253423594039060631L;

    private String messageGson;
    
    public String getMessageGson() {
    	
    	return messageGson;
    }
    
    public void setMessageGson(String messageGson) {
    	
    	this.messageGson=messageGson;
    }
}
