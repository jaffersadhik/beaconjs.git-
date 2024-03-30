package com.unitia.ejbserver;

import javax.ejb.Remote;

@Remote
public interface MessageTransfer {

	public String send(TransferBean bean);
}
