package org.jload.response;

import java.io.IOException;
import java.io.OutputStream;

/*
Get the BytesReceived
*/
public class ByteProcess extends OutputStream {
    private long byteCount = 0;

    @Override
    public void write(int b) throws IOException {
        byteCount++;
    }

    @Override
    public void write(byte[] b) throws IOException {
        byteCount += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byteCount += len;
    }

    public long getByteCount() {
        return byteCount;
    }
}

