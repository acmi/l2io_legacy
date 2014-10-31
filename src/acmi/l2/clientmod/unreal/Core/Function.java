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
package acmi.l2.clientmod.unreal.Core;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.properties.PropertiesUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Function extends Struct {
    public final int nativeIndex;
    public final int operatorPrecedence;
    public final int functionFlags;
    public final int replicationOffset;

    public Function(DataInput input, UnrealPackageReadOnly.ExportEntry entry, PropertiesUtil propertiesUtil) throws IOException {
        super(input, entry, propertiesUtil);

        nativeIndex = input.readUnsignedShort();
        operatorPrecedence = input.readUnsignedByte();
        functionFlags = input.readInt();
        replicationOffset = (functionFlags & 0x40) != 0 ? input.readUnsignedShort() : 0;
    }

    public enum Flag {
        FINAL(1),
        DEFINED(2),
        LATENT(8),
        PRE_OPERATOR(0x10),
        NET(0x40),
        NETRELIABLE(0x80),
        SIMULATED(0x100),
        EXEC(0x200),
        NATIVE(0x400),
        EVENT(0x800),
        OPERATOR(0x1000),
        STATIC(0x2000),
        PROTECTED(0x80000),
        DELEGATE(0x100000);

        private int mask;

        Flag(int mask) {
            this.mask = mask;
        }

        public int getMask() {
            return mask;
        }

        @Override
        public String toString() {
            return "FF_" + name();
        }

        public static List<Flag> getFlags(int flags) {
            return Arrays.stream(values())
                    .filter(e -> (e.getMask() & flags) != 0)
                    .collect(Collectors.toList());
        }

        public static int getFlags(Flag... flags) {
            int v = 0;
            for (Flag flag : flags)
                v |= flag.getMask();
            return v;
        }
    }
}