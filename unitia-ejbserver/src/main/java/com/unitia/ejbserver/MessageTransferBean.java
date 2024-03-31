package com.unitia.ejbserver;

import javax.ejb.Stateless;

import com.unitia.ejbinterface.MessageTransfer;
import com.unitia.ejbinterface.TransferBean;

@Stateless(name="MessageTransfer",mappedName="java:global/MessageTransfer")
public class MessageTransferBean implements MessageTransfer {


	@Override
	public String send(TransferBean bean) {

		System.out.println(bean.getMessageGson());
		
		return"ok";
	}
}
