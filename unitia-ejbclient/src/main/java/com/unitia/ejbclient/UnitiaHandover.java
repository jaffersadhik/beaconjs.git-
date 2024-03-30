package com.unitia.ejbclient;

import java.util.Properties;

import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.smslog.ErrorLog;
import com.unitia.ejbserver.MessageTransfer;
import com.unitia.ejbserver.TransferBean;

public class UnitiaHandover {

	@EJB
	public MessageTransfer messageTransfer;
	
	public String ipport;
	
	public UnitiaHandover(String ipport) {
		
		this.ipport=ipport;
		
		init();
	}

	private void init() {

		try {
			Context context =getInitialContext();
			

			messageTransfer = (MessageTransfer) context.lookup("java:global/unitia-ejbserver-1.0.jar/MessageTransfer!com.unitia.ejbserver.MessageTransfer");
	
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	private  Properties getInitialContextProperties() {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        props.put(Context.PROVIDER_URL, "http-remoting://"+ipport); // Change to your server URL
        props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        props.put("jboss.naming.client.ejb.context", true);
        props.put(Context.SECURITY_PRINCIPAL, "john");
        props.put(Context.SECURITY_CREDENTIALS, "fcf730b6d95236ecd3c9fc2d92d7b6b2bb061514961aec041d6c7a7192f592e4");
        return props;
    }
	
	 private  Context getInitialContext() throws NamingException {
	        // Set up properties for the initial context
	        Properties props = getInitialContextProperties();
	    
	        return new InitialContext(props);
	    }
	public boolean sendtoIC(String messageGson) {
	
		try {
		TransferBean bean=new TransferBean();
		
		bean.setMessageGson(messageGson);
		
		String response=messageTransfer.send(bean);
		
		if(response.equals("ok")) {
			
			return true;
		}else {
			
			return false;
		}
		
		}catch(Exception e) {
			
			ErrorLog.log("sendtoIC : "+ErrorMessage.getStackTraceAsString(e));
			
			init();
		}
		return false;
	}
	}
