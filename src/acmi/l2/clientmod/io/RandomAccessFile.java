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
package acmi.l2.clientmod.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;

public class RandomAccessFile implements DataInput, DataOutput, Closeable {
    private static Charset defaultCharset;

    static {
        try {
            defaultCharset = Charset.forName(System.getProperty(RandomAccessFile.class.getName() + ".charset", "EUC-KR"));
        } catch (UnsupportedCharsetException e) {
            defaultCharset = Charset.defaultCharset();
        }
    }

    protected final java.io.RandomAccessFile file;
    private final String path;

    private final int cryptVer;
    protected final int xorKey;
    protected final int startOffset;

    private Charset charset;

    public RandomAccessFile(File f, boolean readOnly, Charset charset) throws IOException {
        file = new java.io.RandomAccessFile(f, readOnly ? "r" : "rw");
        path = f.getPath();

        String l2CryptHeader;
        if (file.length() >= 28 && (l2CryptHeader = getCryptHeader()).startsWith("Lineage2Ver")) {
            startOffset = 28;
            cryptVer = Integer.parseInt(l2CryptHeader.substring(11));
            switch (cryptVer) {
                case 111:
                    xorKey = 0xACACACAC;
                    break;
                case 121:
                    int xb = getCryptKey(f.getName());
                    xorKey = xb | (xb << 8) | (xb << 16) | (xb << 24);
                    break;
                default:
                    throw new IOException("Crypt " + cryptVer + " is not supported.");
            }
        } else {
            startOffset = 0;
            cryptVer = 0;
            xorKey = 0;
        }

        setCharset(charset);
    }

    public RandomAccessFile(File f, boolean readOnly) throws IOException {
        this(f, readOnly, defaultCharset);
    }

    public RandomAccessFile(String path, boolean readOnly, Charset charset) throws IOException {
        this(new File(path), readOnly, charset);
    }

    public RandomAccessFile(String path, boolean readOnly) throws IOException {
        this(new File(path), readOnly, defaultCharset);
    }

    private String getCryptHeader() throws IOException {
        byte[] l2CryptHeaderBytes = new byte[28];
        file.readFully(l2CryptHeaderBytes);
        return new String(l2CryptHeaderBytes, "UTF-16LE");
    }

    private static int getCryptKey(String filename) {
        filename = filename.toLowerCase();
        int ind = 0;
        for (int i = 0; i < filename.length(); i++)
            ind += filename.charAt(i);
        return ind & 0xff;
    }

    public String getPath() {
        return path;
    }

    public int getCryptVersion() {
        return cryptVer;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public void setPosition(int pos) throws IOException {
        file.seek(pos + startOffset);
    }

    public int getPosition() throws IOException {
        return (int) file.getFilePointer() - startOffset;
    }

    public void setLength(int newLength) throws IOException {
        file.setLength(newLength + startOffset);
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    public int read() throws IOException {
        if (cryptVer != 0) {
            int b = file.read();
            if (b == -1)
                return -1;

            return (b ^ xorKey) & 0xff;
        } else
            return file.read();
    }

    @Override
    public void readFully(byte b[], int off, int len) throws IOException {
        file.readFully(b, off, len);

        if (cryptVer != 0) {
            for (int i = 0; i < len; i++)
                b[off + i] ^= xorKey;
        }
    }

    public void write(int b) throws IOException {
        if (cryptVer != 0)
            file.write(b ^ xorKey);
        else
            file.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if ((off | len | (b.length - (len + off)) | (off + len)) < 0)
            throw new IndexOutOfBoundsException();

        if (cryptVer != 0) {
            byte[] toWrite = Arrays.copyOfRange(b, off, off + len);
            for (int i = 0; i < toWrite.length; i++)
                toWrite[i] ^= xorKey;
            file.write(toWrite);
        } else {
            file.write(b, off, len);
        }
    }
}
