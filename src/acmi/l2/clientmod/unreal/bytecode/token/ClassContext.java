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
package acmi.l2.clientmod.unreal.bytecode.token;

import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.bytecode.BytecodeInput;
import acmi.l2.clientmod.unreal.bytecode.BytecodeOutput;

import java.io.IOException;

public class ClassContext extends Token {
    public static final int OPCODE = 0x12;

    private final Token token1;
    private final int wSkip;
    private final int bSize;
    private final Token token2;

    public ClassContext(UnrealPackageReadOnly unrealPackage, Token token1, int wSkip, int bSize, Token token2) {
        super(unrealPackage);
        this.token1 = token1;
        this.wSkip = wSkip;
        this.bSize = bSize;
        this.token2 = token2;
    }

    public ClassContext(UnrealPackageReadOnly unrealPackage, BytecodeInput input) throws IOException {
        super(unrealPackage, input);
        this.token1 = input.readToken();
        this.wSkip = input.readUnsignedShort();
        this.bSize = input.readUnsignedByte();
        this.token2 = input.readToken();
    }

    @Override
    protected int getOpcode() {
        return OPCODE;
    }

    @Override
    public void writeTo(BytecodeOutput output) throws IOException {
        super.writeTo(output);
        token1.writeTo(output);
        output.writeShort(wSkip);
        output.writeByte(bSize);
        token2.writeTo(output);
    }

    @Override
    public String toString() {
        return String.format("%s.%s", token1, token2);
    }
}
