package com.itextos.beacon.smpp.interfaces.proxy;


import org.jboss.netty.buffer.ChannelBuffer;

import com.cloudhopper.commons.util.HexUtil;
import com.cloudhopper.commons.util.StringUtil;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.NotEnoughDataInBufferException;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import com.cloudhopper.smpp.util.ChannelBufferUtil;
import com.cloudhopper.smpp.util.PduUtil;

public abstract class ProxyBaseBind extends BaseBind<PduResponse> {

    private String systemId;
    private String password;
    private String systemType;
    private byte interfaceVersion;
    private Address addressRange;
    private String clientIp; // New field to store the client IP

    public ProxyBaseBind(int commandId, String name) {
        super(commandId, name);
    }

    public void setSystemId(String value) {
        this.systemId = value;
    }

    public String getSystemId() {
        return this.systemId;
    }

    public void setPassword(String value) {
        this.password = value;
    }

    public String getPassword() {
        return this.password;
    }

    public void setSystemType(String value) {
        this.systemType = value;
    }

    public String getSystemType() {
        return this.systemType;
    }

    public void setInterfaceVersion(byte value) {
        this.interfaceVersion = value;
    }

    public byte getInterfaceVersion() {
        return this.interfaceVersion;
    }

    public Address getAddressRange() {
        return this.addressRange;
    }

    public void setAddressRange(Address value) {
        this.addressRange = value;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    @Override
    public void readBody(ChannelBuffer buffer) throws UnrecoverablePduException, RecoverablePduException {
        // Preprocess the buffer to remove the PROXY header and extract the client IP
        preprocessProxyHeader(buffer);

        // Read the remaining data (SMPP PDU)
        this.systemId = ChannelBufferUtil.readNullTerminatedString(buffer);
        this.password = ChannelBufferUtil.readNullTerminatedString(buffer);
        this.systemType = ChannelBufferUtil.readNullTerminatedString(buffer);

        // At this point, we should have at least 3 bytes left
        if (buffer.readableBytes() < 3) {
            throw new NotEnoughDataInBufferException("After parsing systemId, password, and systemType", buffer.readableBytes(), 3);
        }

        this.interfaceVersion = buffer.readByte();
        this.addressRange = ChannelBufferUtil.readAddress(buffer);
    }

    private void preprocessProxyHeader(ChannelBuffer buffer) throws UnrecoverablePduException {
        // Read the PROXY protocol header line (ends with \r\n)
        int endOfHeader = buffer.indexOf(buffer.readerIndex(), buffer.writerIndex(), (byte) '\n');
        if (endOfHeader == -1) {
            throw new UnrecoverablePduException("Invalid or incomplete PROXY protocol header");
        }

        // Extract the header
        String header = buffer.toString(buffer.readerIndex(), endOfHeader - buffer.readerIndex() + 1, "UTF-8");
        buffer.readerIndex(endOfHeader + 1); // Move the reader index past the header

        // Parse the PROXY header (e.g., "PROXY TCP4 192.168.1.1 192.168.1.2 12345 443\r\n")
        String[] parts = header.split(" ");
        if (parts.length >= 6 && parts[0].equals("PROXY")) {
            this.clientIp = parts[2]; // Extract client IP
            System.out.println("Client IP from PROXY header: " + this.clientIp);
        } else {
            throw new UnrecoverablePduException("Invalid PROXY protocol header");
        }
    }

    @Override
    public int calculateByteSizeOfBody() {
        int bodyLength = 0;
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.systemId);
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.password);
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.systemType);
        bodyLength += 1; // interface version
        bodyLength += PduUtil.calculateByteSizeOfAddress(this.addressRange);
        return bodyLength;
    }

    @Override
    public void writeBody(ChannelBuffer buffer) throws UnrecoverablePduException, RecoverablePduException {
        ChannelBufferUtil.writeNullTerminatedString(buffer, this.systemId);
        ChannelBufferUtil.writeNullTerminatedString(buffer, this.password);
        ChannelBufferUtil.writeNullTerminatedString(buffer, this.systemType);
        buffer.writeByte(this.interfaceVersion);
        ChannelBufferUtil.writeAddress(buffer, this.addressRange);
    }

    @Override
    public void appendBodyToString(StringBuilder buffer) {
        buffer.append("systemId [");
        buffer.append(StringUtil.toStringWithNullAsEmpty(this.systemId));
        buffer.append("] password [");
        buffer.append(StringUtil.toStringWithNullAsEmpty(this.password));
        buffer.append("] systemType [");
        buffer.append(StringUtil.toStringWithNullAsEmpty(this.systemType));
        buffer.append("] interfaceVersion [0x");
        buffer.append(HexUtil.toHexString(this.interfaceVersion));
        buffer.append("] addressRange (");
        if (this.addressRange == null) {
            buffer.append(SmppConstants.EMPTY_ADDRESS.toString());
        } else {
            buffer.append(StringUtil.toStringWithNullAsEmpty(this.addressRange));
        }
        buffer.append(")");
        buffer.append("] clientIp [");
        buffer.append(StringUtil.toStringWithNullAsEmpty(this.clientIp));
        buffer.append("]");
    }
}
