package com.itextos.beacon.smpp.interfaces.proxy;

import java.io.IOException;
import java.io.InputStream;

public class ProxyProtocolInputStream extends InputStream {
    private final InputStream wrappedInputStream;
    private boolean headerParsed = false;

    public ProxyProtocolInputStream(InputStream inputStream) {
        this.wrappedInputStream = inputStream;
    }

    @Override
    public int read() throws IOException {
        if (!headerParsed) {
            parseProxyHeader();
        }
        return wrappedInputStream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (!headerParsed) {
            parseProxyHeader();
        }
        return wrappedInputStream.read(b, off, len);
    }

    private void parseProxyHeader() throws IOException {
        // Read the PROXY protocol header line
        StringBuilder header = new StringBuilder();
        int c;
        while ((c = wrappedInputStream.read()) != -1) {
            header.append((char) c);
            if (c == '\n') {
                break; // End of PROXY header
            }
        }

        // Parse the PROXY header (e.g., "PROXY TCP4 192.168.1.1 192.168.1.2 12345 443\r\n")
        String proxyHeader = header.toString();
        if (proxyHeader.startsWith("PROXY")) {
            String[] parts = proxyHeader.split(" ");
            if (parts.length >= 6) {
                String clientIp = parts[2]; // Extract client IP
                System.out.println("Client IP from PROXY header: " + clientIp);
            }
        }

        headerParsed = true; // Mark header as parsed
    }
}