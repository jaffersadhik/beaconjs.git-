package com.unitia.ejbinterface;

import javax.ejb.Remote;

@Remote
public interface MessageTransfer {

	public String send(TransferBean bean);
}
