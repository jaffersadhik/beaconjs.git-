package com.unitia.ejbinterface;

import java.io.Serializable;

public class TransferBean implements Serializable,Cloneable {
	
    public static final long serialVersionUID = -7253423594039060631L;

    public String messageGson;
    
    public String getMessageGson() {
    	
    	return messageGson;
    }
    
    public void setMessageGson(String messageGson) {
    	
    	this.messageGson=messageGson;
    }
}
