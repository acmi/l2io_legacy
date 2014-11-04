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

public class JumpIfNot extends Token {
    public static final int OPCODE = 0x07;

    private final int targetOffset;
    private final Token condition;

    public JumpIfNot(UnrealPackageReadOnly unrealPackage, int targetOffset, Token condition) {
        super(unrealPackage);
        this.targetOffset = targetOffset;
        this.condition = condition;
    }

    public JumpIfNot(UnrealPackageReadOnly unrealPackage, BytecodeInput input) throws IOException {
        super(unrealPackage, input);
        targetOffset = input.readUnsignedShort();
        condition = input.readToken();
    }

    @Override
    protected int getOpcode() {
        return OPCODE;
    }

    @Override
    public void writeTo(BytecodeOutput output) throws IOException {
        super.writeTo(output);
        output.writeShort(targetOffset);
        condition.writeTo(output);
    }

    @Override
    public String toString() {
        return String.format("if (!%s) goto %04x", condition, targetOffset);
    }
}
