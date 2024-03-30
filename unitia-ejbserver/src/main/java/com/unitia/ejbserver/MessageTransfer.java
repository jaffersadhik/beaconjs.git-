package com.unitia.ejbserver;

import javax.ejb.Remote;

@Remote
public interface MessageTransfer {

	String send(TransferBean bean);
}
