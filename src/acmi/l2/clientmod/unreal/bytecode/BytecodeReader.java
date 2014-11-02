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
package acmi.l2.clientmod.unreal.bytecode;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Function;

public class BytecodeReader implements Iterator<BytecodeToken> {
    private DataInput input;
    private UnrealPackageReadOnly unrealPackage;
    private Function<Integer, NativeFunction> naviveFunctionSupplier;

    private int scriptSize;
    private int readSize;

    private boolean next_is_not_delegate;

    public BytecodeReader(DataInput input, int scriptSize, UnrealPackageReadOnly unrealPackage, Function<Integer, NativeFunction> naviveFunctionSupplier) {
        this.input = input;
        this.scriptSize = scriptSize;
        this.unrealPackage = unrealPackage;
        this.naviveFunctionSupplier = naviveFunctionSupplier;
    }

    @Override
    public boolean hasNext() {
        return readSize < scriptSize;
    }

    @Override
    public BytecodeToken next() {
        try {
            int b = readUnsignedByte();
            switch (b) {
                case EX_LocalVariable:
                case EX_InstanceVariable:
                case EX_NativeParm:
                    return readRef(e -> e.getObjectName().getName());
                case EX_DefaultVariable:
                    return readRef(e -> "Default." + e.getObjectName().getName());
                case EX_UnknownVariable:
                    return readRef(e -> next() + "." + e.getObjectName().getName());
                case EX_Return:
                    return new BytecodeToken.ReturnToken(next());
                case EX_Switch:
                    return next(); //todo
                case EX_Jump:
                    return new BytecodeToken.UncondJumpToken(readUnsignedShort());
                case EX_JumpIfNot:
                    return new BytecodeToken.JumpIfNotToken(readUnsignedShort(), next());
                case EX_Stop:
                    return new BytecodeToken("stop");
                case EX_Assert:
                    int lineNumb = readUnsignedShort();
                    return new BytecodeToken("assert (" + next() + ")");
                case EX_Case:
                    int caseInd = readUnsignedShort();
                    if (caseInd == 0xffff) {
                        return new BytecodeToken("default:");
                    } else
                        return new BytecodeToken("case " + next() + ":");
                case EX_Nothing:
                    return new BytecodeToken("");
                case EX_LabelTable:
                    String labelName;
                    int offset;
                    do {
                        labelName = readName();
                        offset = readInt();
                    } while (!labelName.equalsIgnoreCase("none"));
                    return new BytecodeToken(""); //todo
                case EX_GotoLabel:
                    return new BytecodeToken("goto " + next());
                case EX_EatString:
                    return next();
                case EX_Let:
                case EX_LetBool:
                    return new BytecodeToken(next() + " = " + next());
                case EX_DynArrayElement:
                case EX_ArrayElement:
                    String ind = String.valueOf(next());
                    String arr = String.valueOf(next());
                    return new BytecodeToken(arr + "[" + ind + "]");
                case EX_New:
                    return new BytecodeToken("new (" + next() + ", " + next() + ", " + next() + ", " + next() + ")");
                case EX_ClassContext:
                case EX_Context:
                    String res = String.valueOf(next());
                    readUnsignedShort();
                    readUnsignedByte();
                    return new BytecodeToken(res + "." + next());
                case EX_Metacast:
                    return new BytecodeToken("Class<" + readRef(e -> e.getObjectName().getName()) + ">(" + next() + ")");
                case EX_Unknown_jumpover:
                    return new BytecodeToken("");
                case EX_EndFunctionParms:
                    return new BytecodeToken(")");
                case EX_Self:
                    return new BytecodeToken("self");
                case EX_Skip:
                    readUnsignedShort();
                    return next();
                case EX_VirtualFunction:
                    return readCall(readName());
                case EX_FinalFunction:
                    return readCall(readRef(e -> e.getObjectName().getName()).toString());
                case EX_IntConst:
                    return new BytecodeToken(String.valueOf(readInt()));
                case EX_FloatConst:
                    return new BytecodeToken(String.valueOf(readFloat()));
                case EX_StringConst:
                    return new BytecodeToken(readString());
                case EX_ObjectConst:
                    return new BytecodeToken(readRef(e -> e.getObjectName().getName()).toString());
                case EX_NameConst:
                    return new BytecodeToken(readName());
                case EX_RotationConst:
                    return new BytecodeToken(String.format("rot(%d,%d,%d)", readInt(), readInt(), readInt()));
                case EX_VectorConst:
                    return new BytecodeToken(String.format("vect(%f,%f,%f)", readFloat(), readFloat(), readFloat()));
                case EX_ByteConst:
                case EX_IntConstByte:
                    return new BytecodeToken(String.valueOf(readUnsignedByte()));
                case EX_IntZero:
                    return new BytecodeToken("0");
                case EX_IntOne:
                    return new BytecodeToken("1");
                case EX_True:
                    return new BytecodeToken("True");
                case EX_False:
                    return new BytecodeToken("False");
                case EX_NoObject:
                    return new BytecodeToken("None");
                case EX_Unknown_jumpover2:
                    readUnsignedByte();
                    return next();
                case EX_BoolVariable:
                    return next();
                case EX_DynamicCast:
                    return new BytecodeToken(readRef(e -> e.getObjectName().getName()).toString() + "(" + next() + ")");
                case EX_Iterator:
                    BytecodeToken r = next();
                    readUnsignedShort();
                    return new BytecodeToken.ForeachToken(readUnsignedShort(), r);
                case EX_IteratorPop:
                    return new BytecodeToken.IteratorPopToken();
                case EX_IteratorNext:
                    return new BytecodeToken.IteratorNextToken();
                case EX_StructCmpEq:
                    readCompactInt();
                    return new BytecodeToken(next() + "==" + next());
                case EX_StructCmpNe:
                    readCompactInt();
                    return new BytecodeToken(next() + "!=" + next());
                case EX_UnicodeStringConst:
                    char ch;
                    StringBuilder sb = new StringBuilder();
                    do {
                        ch = (char) readUnsignedShort();
                        if (ch > 0)
                            sb.append(ch);
                    } while (ch != 0);
                    return new BytecodeToken(sb.toString());
                case EX_StructMember:
                    String m = readRef(e -> e.getObjectName().getName()).toString();
                    return new BytecodeToken(next() + "." + m);
                case EX_Length:
                    return new BytecodeToken(next() + ".Length");
                case EX_GlobalFunction:
                    return readCall("Global." + readName());
                case EX_RotatorToVector:
                case EX_Unknown5B:
                    next_is_not_delegate = true;
                    return next();
                case EX_ByteToInt:
                case EX_IntToByte:
                case EX_ByteToFloat:
                case EX_IntToFloat:
                    return next();
                case EX_ByteToBool:
                case EX_IntToBool:
                    return new BytecodeToken("bool(" + next() + ")");
                case EX_BoolToByte:
                    return new BytecodeToken("byte(" + next() + ")");
                case EX_Remove:
                    return new BytecodeToken(next() + ".Remove(" + next() + ", " + next() + ")");
                case EX_BoolToFloat:
                    return new BytecodeToken("float(" + next() + ")");
                case EX_DelegateCall://EX_FloatToByte
                    if (next_is_not_delegate) {
                        return new BytecodeToken("byte(" + next() + ")");
                    } else {
                        next_is_not_delegate = false;
                        readRef(e -> null);
                        return readCall(readRef(e -> e.getObjectName().getName()).toString());
                    }
                case EX_DelegateName://EX_FloatToInt
                    if (next_is_not_delegate) {
                        return new BytecodeToken("int(" + next() + ")");
                    } else {
                        next_is_not_delegate = false;
                        readRef(e -> null);
                        return readCall(readName());
                    }
                case EX_DelegateAssign://EX_FloatToBool
                    if (next_is_not_delegate) {
                        return new BytecodeToken("bool(" + next() + ")");
                    } else {
                        next_is_not_delegate = false;
                        return new BytecodeToken(next() + " = " + next());
                    }
                case EX_StringToName:
                    return new BytecodeToken(next() + ".Empty()");
                case EX_ObjectToBool:
                case EX_StringToBool:
                case EX_VectorToBool:
                case EX_RotatorToBool:
                    return new BytecodeToken("bool(" + next() + ")");
                case EX_NameToBool:
                    BytecodeToken r1 = next();
                    readUnsignedShort();
                    BytecodeToken r2 = next();
                    readUnsignedShort();
                    BytecodeToken r3 = next();
                    return new BytecodeToken("(" + r1 + " ?? " + r2 + " : " + r3 + ")");
                case EX_StringToByte:
                    return new BytecodeToken("col(" + readUnsignedByte() + ", " + readUnsignedByte() + ", " + readUnsignedByte() + ", " + readUnsignedByte() + ")");
                case EX_StringToInt:
                    return new BytecodeToken("int(" + next() + ")");
                case EX_StringToFloat:
                    return new BytecodeToken("float(" + next() + ")");
                case EX_StringToVector:
                    return new BytecodeToken("vector(" + next() + ")");
                case EX_StringToRotator:
                case EX_VectorToRotator:
                    return new BytecodeToken("rotator(" + next() + ")");
                case EX_ByteToString:
                case EX_IntToString:
                case EX_BoolToString:
                case EX_FloatToString:
                case EX_ObjectToString:
                case EX_NameToString:
                case EX_VectorToString:
                case EX_RotatorToString:
                    return new BytecodeToken("string(" + next() + ")");
                case EX_StringToName2:
                    return new BytecodeToken("name(" + next() + ")");
                default:
                    if (b >= 0x60) {
                        return readNativeCall(b);
                    }
                    throw new IllegalStateException(String.format("Unknown token: %02x", b));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int readUnsignedByte() throws IOException {
        int val = input.readUnsignedByte();
        readSize += 1;
        return val;
    }

    private int readUnsignedShort() throws IOException {
        int val = input.readUnsignedShort();
        readSize += 2;
        return val;
    }

    private int readInt() throws IOException {
        int val = input.readInt();
        readSize += 4;
        return val;
    }

    private float readFloat() throws IOException {
        float val = input.readFloat();
        readSize += 4;
        return val;
    }

    private int readCompactInt() throws IOException {
        int val = input.readCompactInt();
        readSize += 4;
        return val;
    }

    private String readString() throws IOException {
        String val = input.readLine();
        readSize += val.length() + 1;
        return val;
    }

    private String readName() throws IOException {
        return unrealPackage.nameReference(readCompactInt());
    }

    private BytecodeToken readRef(Function<UnrealPackageReadOnly.Entry, String> function) throws IOException {
        return new BytecodeToken(function.apply(unrealPackage.objectReference(readCompactInt())));
    }

    private BytecodeToken readNativeCall(int b) throws IOException {
        int nativeIndex;
        if ((b & 0xF0) == EX_ExtendedNative) {
            nativeIndex = ((b - EX_ExtendedNative) << 8) + readUnsignedByte();
        } else {
            nativeIndex = b;
        }
        if (nativeIndex < EX_FirstNative)
            throw new IllegalStateException("Invalid native index " + nativeIndex);

        NativeFunction nativeFunction = naviveFunctionSupplier.apply(nativeIndex);
        if (nativeFunction.isPreOperator() ||
                (nativeFunction.isOperator() && nativeFunction.getOperatorPrecedence() == 0)) {
            BytecodeToken p = next();
            next();   // end of parms
            if (nativeFunction.isPreOperator())
                return new BytecodeToken(nativeFunction.getName() + p);
            return new BytecodeToken(p + nativeFunction.getName());
        } else if (nativeFunction.isOperator()) {
            BytecodeToken p1 = next();
            BytecodeToken p2 = next();
            next();  // end of parms
            return new BytecodeToken(p1 + " " + nativeFunction.getName() + " " + p2);
        } else
            return readCall(nativeFunction.getName());
    }

    private BytecodeToken readCall(String functionName) throws IOException {
        BytecodeToken p;
        StringBuilder builder = new StringBuilder(functionName + "(");
        int count = 0;
        do {
            p = next();
            if (!(p instanceof BytecodeToken.DefaultValueToken)) {
                if (count > 0 && !(p instanceof BytecodeToken.EndParmsToken))
                    builder.append(", ");
                builder.append(p);
                count++;
            }
        } while (!(p instanceof BytecodeToken.EndParmsToken));
        return new BytecodeToken(builder.toString());
    }

    private static final int EX_LocalVariable = 0x00;
    private static final int EX_InstanceVariable = 0x01;
    private static final int EX_DefaultVariable = 0x02;
    private static final int EX_UnknownVariable = 0x03; // TODO : {UT2003} unknown opcode
    private static final int EX_Return = 0x04;
    private static final int EX_Switch = 0x05;
    private static final int EX_Jump = 0x06;
    private static final int EX_JumpIfNot = 0x07;
    private static final int EX_Stop = 0x08;
    private static final int EX_Assert = 0x09;
    private static final int EX_Case = 0x0A;
    private static final int EX_Nothing = 0x0B;
    private static final int EX_LabelTable = 0x0C;
    private static final int EX_GotoLabel = 0x0D;
    private static final int EX_EatString = 0x0E;
    private static final int EX_Let = 0x0F;
    private static final int EX_DynArrayElement = 0x10;
    private static final int EX_New = 0x11;
    private static final int EX_ClassContext = 0x12;
    private static final int EX_Metacast = 0x13;
    private static final int EX_LetBool = 0x14;
    private static final int EX_Unknown_jumpover = 0x15;            // ??? only seen on old packages (v61) at end of functions and in mid of code
    private static final int EX_EndFunctionParms = 0x16;
    private static final int EX_Self = 0x17;
    private static final int EX_Skip = 0x18;
    private static final int EX_Context = 0x19;
    private static final int EX_ArrayElement = 0x1A;
    private static final int EX_VirtualFunction = 0x1B;
    private static final int EX_FinalFunction = 0x1C;
    private static final int EX_IntConst = 0x1D;
    private static final int EX_FloatConst = 0x1E;
    private static final int EX_StringConst = 0x1F;
    private static final int EX_ObjectConst = 0x20;
    private static final int EX_NameConst = 0x21;
    private static final int EX_RotationConst = 0x22;
    private static final int EX_VectorConst = 0x23;
    private static final int EX_ByteConst = 0x24;
    private static final int EX_IntZero = 0x25;
    private static final int EX_IntOne = 0x26;
    private static final int EX_True = 0x27;
    private static final int EX_False = 0x28;
    private static final int EX_NativeParm = 0x29;
    private static final int EX_NoObject = 0x2A;
    private static final int EX_Unknown_jumpover2 = 0x2B;           // ??? only seen on old packages (v61)
    private static final int EX_IntConstByte = 0x2C;
    private static final int EX_BoolVariable = 0x2D;
    private static final int EX_DynamicCast = 0x2E;
    private static final int EX_Iterator = 0x2F;
    private static final int EX_IteratorPop = 0x30;
    private static final int EX_IteratorNext = 0x31;
    private static final int EX_StructCmpEq = 0x32;
    private static final int EX_StructCmpNe = 0x33;
    private static final int EX_UnicodeStringConst = 0x34;
    // =0x35
    private static final int EX_StructMember = 0x36;
    private static final int EX_Length = 0x37; // UT2003
    private static final int EX_GlobalFunction = 0x38;
    private static final int EX_RotatorToVector = 0x39; // maybe it is another thing??? some flag to modify behavior of other conversion tokens?
    private static final int EX_ByteToInt = 0x3A;
    private static final int EX_ByteToBool = 0x3B;
    private static final int EX_ByteToFloat = 0x3C;
    private static final int EX_IntToByte = 0x3D;
    private static final int EX_IntToBool = 0x3E;
    private static final int EX_IntToFloat = 0x3F;
    private static final int EX_BoolToByte = 0x40;
    private static final int EX_BoolToInt = 0x41;
    private static final int EX_Remove = 0x41; // redefined?
    private static final int EX_BoolToFloat = 0x42;
    private static final int EX_FloatToByte = 0x43;
    private static final int EX_DelegateCall = 0x43; // redefined?
    private static final int EX_FloatToInt = 0x44;
    private static final int EX_DelegateName = 0x44; // redefined?
    private static final int EX_FloatToBool = 0x45;
    private static final int EX_DelegateAssign = 0x45; // redefined?
    private static final int EX_StringToName = 0x46;                // not defined in UT source, but used in unrealscript
    private static final int EX_ObjectToBool = 0x47;
    private static final int EX_NameToBool = 0x48;
    private static final int EX_StringToByte = 0x49;
    private static final int EX_StringToInt = 0x4A;
    private static final int EX_StringToBool = 0x4B;
    private static final int EX_StringToFloat = 0x4C;
    private static final int EX_StringToVector = 0x4D;
    private static final int EX_StringToRotator = 0x4E;
    private static final int EX_VectorToBool = 0x4F;
    private static final int EX_VectorToRotator = 0x50;
    private static final int EX_RotatorToBool = 0x51;
    private static final int EX_ByteToString = 0x52;
    private static final int EX_IntToString = 0x53;
    private static final int EX_BoolToString = 0x54;
    private static final int EX_FloatToString = 0x55;
    private static final int EX_ObjectToString = 0x56;
    private static final int EX_NameToString = 0x57;
    private static final int EX_VectorToString = 0x58;
    private static final int EX_RotatorToString = 0x59;
    private static final int EX_StringToName2 = 0x5A; // a duplicated opcode found in XIII
    private static final int EX_Unknown5B = 0x5B; // unknown opcode used in Devastation, seems to be an invisible conversion

    private static final int EX_ExtendedNative = 0x60;
    private static final int EX_FirstNative = 0x70;
}
