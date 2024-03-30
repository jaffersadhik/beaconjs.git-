package com.unitia.ejbclient;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Properties;

import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

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
			

	//		messageTransfer = (MessageTransfer) context.lookup("java:global/unitia-ejbserver-1.0.jar/MessageTransfer!com.unitia.ejbserver.MessageTransfer");
			context.close();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	private static Properties getInitialContextProperties() {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        props.put(Context.PROVIDER_URL, "http-remoting://192.168.1.95:9190"); // Change to your server URL
        props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        props.put("jboss.naming.client.ejb.context", true);
        return props;
    }
	
	 private static Context getInitialContext() throws NamingException {
	        // Set up properties for the initial context
	        Properties props = getInitialContextProperties();
	    
	        return new InitialContext(props);
	    }
	public boolean send(String messageGson) {
	
		TransferBean bean=new TransferBean();
		
		bean.setMessageGson(messageGson);
		
		String response=messageTransfer.send(bean);
		
		System.out.println(response);
		
		return true;
	}
	
	
	public static void main(String args[]) {
	
		UnitiaHandover uh=new UnitiaHandover("192.168.1.95:9190");
		
		uh.send("test");
	}
}
