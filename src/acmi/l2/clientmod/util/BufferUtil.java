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
package acmi.l2.clientmod.util;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static acmi.l2.clientmod.util.ByteUtil.compactIntToByteArray;

public class BufferUtil {
    private static Charset defaultCharset;
    private static CharsetEncoder charsetEncoder;
    private static final Charset UTF16LE = Charset.forName("utf-16le");

    static {
        setDefaultCharset(Charset.forName("ISO_8859-1"));
    }

    public static Charset getDefaultCharset(){
        return defaultCharset;
    }

    public static void setDefaultCharset(Charset defaultCharset) {
        BufferUtil.defaultCharset = defaultCharset;
        charsetEncoder = defaultCharset.newEncoder();
    }

    public static int getCompactInt(ByteBuffer input) {
        int output = 0;
        boolean signed = false;
        for (int i = 0; i < 5; i++) {
            int x = input.get() & 0xff;
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

    public static void putCompactInt(ByteBuffer buffer, int v) throws BufferOverflowException, ReadOnlyBufferException {
        buffer.put(compactIntToByteArray(v));
    }

    public static String getString(ByteBuffer buffer) {
        return getString(buffer, defaultCharset);
    }

    public static String getString(ByteBuffer buffer, Charset charset) {
        int len = getCompactInt(buffer);
        if (len == 0)
            return "";

        byte[] bytes = new byte[len > 0 ? len : -2 * len];
        buffer.get(bytes);
        return new String(bytes, 0, bytes.length - (len > 0 ? 1 : 2), len > 0 ? charset : UTF16LE);
    }

    public static void putString(ByteBuffer buffer, String str) {
        putString(buffer, str, null);
    }

    public static void putString(ByteBuffer buffer, String str, Charset charset) {
        if (str == null || str.isEmpty())
            putCompactInt(buffer, 0);
        else if (charset != null)
            putBytes(buffer, str, charset);
        else if (charsetEncoder.canEncode(str))
            putBytes(buffer, str);
        else
            putChars(buffer, str);
    }

    public static void putBytes(ByteBuffer buffer, String str) {
        putBytes(buffer, str, defaultCharset);
    }

    public static void putBytes(ByteBuffer buffer, String str, Charset charset) {
        byte[] strBytes = (str + '\0').getBytes(charset);
        putCompactInt(buffer, strBytes.length);
        buffer.put(strBytes);
    }

    public static void putChars(ByteBuffer buffer, String str) {
        byte[] strBytes = (str + '\0').getBytes(UTF16LE);
        putCompactInt(buffer, -strBytes.length);
        buffer.put(strBytes);
    }

    public static String getUTF(ByteBuffer buffer){
        int len = buffer.getInt();
        if (len == 0)
            return "";
        if (len < 0)
            throw new IllegalStateException("Invalid string length: "+len);
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return new String(bytes, UTF16LE);
    }

    public static void putUTF(ByteBuffer buffer, String str){
        if (str == null || str.isEmpty()) {
            buffer.putInt(0);
            return;
        }

        byte[] bytes = str.getBytes(UTF16LE);

        buffer.putInt(bytes.length);
        buffer.put(bytes);
    }
}
