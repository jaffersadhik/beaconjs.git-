package com.itextos.beacon.smpp.smsdecode;

import com.itextos.beacon.commonlib.constants.MessageClass;

public class SMSBodyContent {

    private String           mMessage             = null;

    private String           mUdh                 = null;

    private int              mDestPort            = 0;

    private String           mUdhi                = null;
    
    private boolean          mIsHexMsg            = false;

    MessageClass     mMsgclass            = null;

	public String getmMessage() {
		return mMessage;
	}

	public void setmMessage(String mMessage) {
		this.mMessage = mMessage;
	}

	public String getmUdh() {
		return mUdh;
	}

	public void setmUdh(String mUdh) {
		this.mUdh = mUdh;
	}

	public int getmDestPort() {
		return mDestPort;
	}

	public void setmDestPort(int mDestPort) {
		this.mDestPort = mDestPort;
	}

	public String getmUdhi() {
		return mUdhi;
	}

	public void setmUdhi(String mUdhi) {
		this.mUdhi = mUdhi;
	}

	public boolean ismIsHexMsg() {
		return mIsHexMsg;
	}

	public void setmIsHexMsg(boolean mIsHexMsg) {
		this.mIsHexMsg = mIsHexMsg;
	}

	public MessageClass getmMsgclass() {
		return mMsgclass;
	}

	public void setmMsgclass(MessageClass mMsgclass) {
		this.mMsgclass = mMsgclass;
	}

    
    
}
