/*
 * Copyright (c) 2014 acmi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package acmi.l2.clientmod.crypt.rsa;

import acmi.l2.clientmod.io.DataInputStream;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Objects;
import java.util.zip.InflaterInputStream;

public final class L2Ver41xInputStream extends InputStream implements L2Ver41x {
    private InputStream stream;
    private boolean closed;

    private int size;
    private int got;

    public L2Ver41xInputStream(InputStream input, BigInteger modulus, BigInteger exponent) throws IOException {
        RSAInputStream rsaInputStream = new RSAInputStream(Objects.requireNonNull(input, "stream"), Objects.requireNonNull(modulus, "modulus"), Objects.requireNonNull(exponent, "exponent"));

        DataInputStream dataInputStream = new DataInputStream(rsaInputStream, null);
        size = dataInputStream.readInt();

        stream = new InflaterInputStream(rsaInputStream);
    }

    @Override
    public int read() throws IOException {
        if (closed)
            throw new IOException("Stream closed");

        int b = stream.read();
        if (got < size) got++;

        return b;
    }

    @Override
    public int available() throws IOException {
        if (closed)
            throw new IOException("Stream closed");

        return size - got;
    }

    @Override
    public void close() throws IOException {
        if (closed)
            return;

        closed = true;
    }

    private static class RSAInputStream extends InputStream {
        private InputStream input;

        private Cipher cipher;

        private byte[] readBuffer = new byte[128];
        private ByteBuffer dataBuffer = ByteBuffer.allocate(124);

        {
            dataBuffer.position(dataBuffer.limit());
        }

        private boolean closed;

        public RSAInputStream(InputStream input, BigInteger modulus, BigInteger exponent) {
            this.input = input;

            try {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(modulus, exponent);
                cipher = Cipher.getInstance("RSA/ECB/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, keyFactory.generatePrivate(keySpec));
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int read() throws IOException {
            if (closed)
                throw new IOException("Stream closed");

            if (dataBuffer.position() == dataBuffer.limit()) {
                int remain = readBuffer.length;
                while (remain > 0) {
                    int r = input.read(readBuffer, readBuffer.length - remain, remain);
                    if (r < 0)
                        return r;
                    remain -= r;
                }

                try {
                    cipher.doFinal(readBuffer, 0, 128, readBuffer);
                } catch (GeneralSecurityException | IndexOutOfBoundsException e) {
                    throw new RuntimeException(e);
                }

                int size = readBuffer[3] & 0xff;
                if (size > 124)
                    throw new IOException("block data size too large");

                dataBuffer.clear();
                dataBuffer.put(readBuffer, 128 - size - ((124 - size) % 4), size);
                dataBuffer.flip();
            }

            return dataBuffer.get() & 0xff;
        }

        @Override
        public void close() throws IOException {
            if (closed)
                return;

            closed = true;
            input.close();
        }
    }
}