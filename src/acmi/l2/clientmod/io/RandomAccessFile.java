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
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static acmi.l2.clientmod.util.ByteUtil.compactIntToByteArray;

public class RandomAccessFile implements Closeable {
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

    public RandomAccessFile(String path, boolean readOnly, Charset charset) throws IOException {
        this(new File(path), readOnly, charset);
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

    protected int read() throws IOException {
        if (cryptVer != 0) {
            int b = file.read();
            if (b == -1)
                return -1;

            return (b ^ xorKey) & 0xff;
        } else
            return file.read();
    }

    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    public void readFully(byte b[], int off, int len) throws IOException {
        file.readFully(b, off, len);

        if (cryptVer != 0) {
            for (int i = 0; i < len; i++)
                b[off + i] ^= xorKey;
        }
    }

    public byte readByte() throws IOException {
        return (byte) readUnsignedByte();
    }

    public int readUnsignedByte() throws IOException {
        int b = read();
        if (b == -1)
            throw new EOFException();
        return b;
    }

    public short readShort() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        short i = (short) (ch1 + (ch2 << 8));

        if (cryptVer != 0)
            i ^= xorKey;

        return i;
    }

    public int readUnsignedShort() throws IOException {
        return readShort() & 0xffff;
    }

    public int readInt() throws IOException {
        int ch1 = file.read();
        int ch2 = file.read();
        int ch3 = file.read();
        int ch4 = file.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        int i = (ch1 + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));

        if (cryptVer != 0)
            i ^= xorKey;

        return i;
    }

    public int readCompactInt() throws IOException {
        int output = 0;
        boolean signed = false;
        for (int i = 0; i < 5; i++) {
            int x = readUnsignedByte();
            if (i == 0) {
                if ((x & 0x80) > 0)
                    signed = true;
                output |= (x & 0x3F);
                if ((x & 0x40) == 0)
                    break;
            } else if (i == 4) {
                output |= (x & 0x1F) << (6 + (3 * 7));
            } else {
                output |= (x & 0x7F) << (6 + ((i - 1) * 7));
                if ((x & 0x80) == 0)
                    break;
            }
        }
        if (signed)
            output *= -1;
        return output;
    }

    public long readLong() throws IOException {
        return ((((long) readUnsignedByte())) |
                (((long) readUnsignedByte()) << 8) |
                (((long) readUnsignedByte()) << 16) |
                (((long) readUnsignedByte()) << 24) |
                (((long) readUnsignedByte()) << 32) |
                (((long) readUnsignedByte()) << 40) |
                (((long) readUnsignedByte()) << 48) |
                (((long) readUnsignedByte()) << 56));
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public String readLine() throws IOException {
        int len = readCompactInt();
        if (len == 0)
            return "";

        byte[] bytes = new byte[len > 0 ? len : -2 * len];
        readFully(bytes);
        return new String(bytes, 0, bytes.length - (len > 0 ? 1 : 2), len > 0 ? charset : Charset.forName("utf-16le")).intern();
    }

    public String readUTF() throws IOException {
        int len = readInt();
        if (len == 0)
            return "";

        byte[] bytes = new byte[2 * len];
        readFully(bytes);
        return new String(bytes, Charset.forName("utf-16le"));
    }

    public void write(int b) throws IOException {
        if (cryptVer != 0)
            file.write(b ^ xorKey);
        else
            file.write(b);
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
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

    public void writeShort(int val) throws IOException {
        if (cryptVer != 0)
            val ^= xorKey;

        file.write(val & 0xFF);
        file.write((val >>> 8) & 0xFF);
    }

    public void writeInt(int val) throws IOException {
        if (cryptVer != 0)
            val ^= xorKey;

        file.write(val & 0xFF);
        file.write((val >>> 8) & 0xFF);
        file.write((val >>> 16) & 0xFF);
        file.write((val >>> 24) & 0xFF);
    }

    public void writeCompactInt(int val) throws IOException {
        write(compactIntToByteArray(val));
    }

    public void writeLong(long val) throws IOException {
        write((int) val);
        write((int) (val >> 8));
        write((int) (val >> 16));
        write((int) (val >> 24));
        write((int) (val >> 32));
        write((int) (val >> 40));
        write((int) (val >> 48));
        write((int) (val >> 56));
    }

    public void writeFloat(float val) throws IOException {
        writeInt(Float.floatToIntBits(val));
    }

    public void writeBytes(String s) throws IOException {
        byte[] strBytes = (s + '\0').getBytes(charset);
        writeCompactInt(strBytes.length);
        write(strBytes);
    }

    public void writeChars(String s) throws IOException {
        byte[] strBytes = (s + '\0').getBytes("utf-16le");
        writeCompactInt(-strBytes.length);
        write(strBytes);
    }

    public void writeLine(String s) throws IOException {
        if (charset.newEncoder().canEncode(s))
            writeBytes(s);
        else
            writeChars(s);
    }

    public void writeUTF(String s) throws IOException {
        writeInt(s.length());
        write(s.getBytes(Charset.forName("utf-16le")));
    }
}
