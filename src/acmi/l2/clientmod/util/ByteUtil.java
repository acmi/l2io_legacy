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

public class ByteUtil {
    public static byte[] compactIntToByteArray(int v) {
        boolean negative = v < 0;
        v = Math.abs(v);
        int[] bytes = new int[]{
                (v) & 0b00111111,
                (v >> 6) & 0b01111111,
                (v >> 6 + 7) & 0b01111111,
                (v >> 6 + 7 + 7) & 0b01111111,
                (v >> 6 + 7 + 7 + 7) & 0b01111111
        };

        if (negative) bytes[0] |= 0b10000000;

        int size = 5;
        for (int i=4; i>0; i--) {
            if (bytes[i] != 0)
                break;
            size--;
        }
        byte[] res = new byte[size];

        for (int i = 0; i < size; i++) {
            if (i != size - 1)
                bytes[i] |= i == 0 ? 0b01000000 : 0b10000000;
            res[i] = (byte)bytes[i];
        }
        return res;
    }

    public static boolean isAscii(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) > 0x7f)
                return false;
        }
        return true;
    }
}
