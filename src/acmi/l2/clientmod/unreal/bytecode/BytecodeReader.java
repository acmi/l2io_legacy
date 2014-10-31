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
import acmi.l2.clientmod.io.UnrealPackageFile;
import acmi.l2.clientmod.unreal.classloader.UnrealClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static acmi.l2.clientmod.unreal.bytecode.BytecodeToken.*;

public class BytecodeReader implements Iterator<BytecodeToken> {
    private DataInput input;
    private int scriptSize;
    private int read;
    private UnrealPackageFile unrealPackage;
    private UnrealClassLoader classLoader;

    public BytecodeReader(DataInput input, int scriptSize, UnrealPackageFile unrealPackage, UnrealClassLoader classLoader) {
        this.input = input;
        this.scriptSize = scriptSize;
        this.unrealPackage = unrealPackage;
        this.classLoader = classLoader;
    }

    @Override
    public boolean hasNext() {
        return read < scriptSize;
    }

    public BytecodeToken next() {
        try {
            int b = readUnsignedByte();
            switch (b) {
                case EX_RotatorToVector:
                    return next();
                case EX_IntToString:
                    return new BytecodeToken("string(" + next() + ")");
                case EX_LocalVariable:
                case EX_InstanceVariable:
                case EX_NativeParm:
                    return readRef(e -> e.getObjectName().getName());
//            case EX_DefaultVariable:
//                return readRef(e -> "Default." + e.getObjectName().getName());
                case EX_Return:
                    return new ReturnToken(next());
//            case EX_Assert:
//                _reader.ReadInt16();
//                _reader.ReadByte();
//                return WrapNextBytecode(c => new BytecodeToken("assert(" + c + ")"));
//
//            case EX_Switch:
//                byte b1 = _reader.ReadByte();
//                BytecodeToken switchExpr = ReadNext();
//                return new SwitchToken(switchExpr.ToString(), switchExpr);
//
//
//            case EX_Case:
//            {
//                short offset = _reader.ReadInt16();
//                if (offset == -1) return new DefaultToken();
//                BytecodeToken caseExpr = ReadNext();
//                return new CaseToken(caseExpr.ToString());
//            }
//
                case EX_Jump: {
                    return new UncondJumpToken(readUnsignedShort());
                }
                case EX_JumpIfNot: {
                    return new JumpIfNotToken(readUnsignedShort(), next());
                }

//            case EX_LabelTable:
//            {
//                var token = new LabelTableToken();
//                while (true)
//                {
//                    string labelName = ReadName();
//                    if (labelName == "None") break;
//                    int offset = _reader.ReadInt32();
//                    token.AddLabel(labelName, offset);
//                }
//                return token;
//            }
//
//            case EX_GotoLabel:
//                return WrapNextBytecode(op => Token("goto " + op));
//
                case EX_Self:
                    return new BytecodeToken("self");
                case EX_Skip:
                    readUnsignedShort();
                    return next();
//            case EX_EatReturnValue:
//                _reader.ReadInt32();
//                return ReadNext();
                case EX_Nothing:
                    return new NothingToken();
//            case EX_Stop:
//                _reader.ReadInt16();
//                return new NothingToken();//
                case EX_IntZero:
                    return new BytecodeToken("0");
                case EX_IntOne:
                    return new BytecodeToken("1");
                case EX_True:
                    return new BytecodeToken("true");
                case EX_False:
                    return new BytecodeToken("false");
                case EX_NoObject:
                    return new BytecodeToken("None");
                case EX_Let:
                case EX_LetBool:
                    BytecodeToken lhs = next();
                    BytecodeToken rhs = next();
                    return new BytecodeToken(lhs + " = " + rhs);
                case EX_IntConst:
                    return new BytecodeToken(String.valueOf(readInt()));
                case EX_FloatConst:
                    return new BytecodeToken(String.valueOf(readFloat()));
                case EX_StringConst: {
                    String s = readLine();
                    return new BytecodeToken("\"" + s.replaceAll("\n", "\\n").replaceAll("\t", "\\t") + "\"");
                }
                case EX_FloatToInt:
                    return new BytecodeToken("int(" + next() + ")");
                case EX_FloatToString:
                    return next();
                case EX_ByteConst:
                case EX_IntConstByte:
                    return new BytecodeToken(String.valueOf(readUnsignedByte()));
//
//            case EX_ObjectConst:
//            {
//                int objectIndex = _reader.ReadInt32();
//                var item = _package.ResolveClassItem(objectIndex);
//                if (item == null) return ErrToken("Unresolved class item " + objectIndex);
//                return Token(item.ClassName + "'" + item.ObjectName + "'");
//            }
//
//            case EX_NameConst:
//                return Token("'" + _package.Names[(int) _reader.ReadInt64()].Name + "'");
//
                case EX_EndFunctionParms:
                    return new EndParmsToken(")");
//            case EX_ClassContext:
                case EX_Context: {
                    BytecodeToken context = next();
                    int exprSize = readUnsignedShort();
                    int bSize = readUnsignedByte();
                    BytecodeToken value = next();
                    return new BytecodeToken(context + "." + value);
                }

//            case EX_InterfaceContext:
//                return ReadNext();
//
//            case EX_FinalFunction:
//            {
//                int functionIndex = _reader.ReadInt32();
//                var item = _package.ResolveClassItem(functionIndex);
//                if (item == null) return ErrToken("Unresolved function item " + item);
//                string functionName = item.ObjectName;
//                return ReadCall(functionName);
//            }
//
//            case EX_PrimitiveCast:
//            {
//                var prefix = _reader.ReadByte();
//                var v = ReadNext();
//                return v;
//            }
//
                case EX_VirtualFunction:
                    return readCall(readName());

//            case EX_GlobalFunction:
//                return ReadCall("Global." + ReadName());
//
                case EX_BoolVariable:
                case EX_ByteToInt:
                case EX_IntToByte:
                    return next();

//            case EX_DynamicCast:
//            {
//                int typeIndex = _reader.ReadInt32();
//                var item = _package.ResolveClassItem(typeIndex);
//                return WrapNextBytecode(op => Token(item.ObjectName + "(" + op + ")"));
//            }
//
//            case EX_Metacast:
//            {
//                int typeIndex = _reader.ReadInt32();
//                var item = _package.ResolveClassItem(typeIndex);
//                if (item == null) return ErrToken("Unresolved class item " + typeIndex);
//                return WrapNextBytecode(op => Token("Class<" + item.ObjectName + ">(" + op + ")"));
//            }
//
                case EX_StructMember: {
                    String field = unrealPackage.objectReference(readCompactInt()).getObjectName().getName();
                    BytecodeToken from = next();
                    return new BytecodeToken(from + "." + field);
                }

//            case EX_ArrayElement:
//            case EX_DynArrayElement:
//            {
//                var index = ReadNext();
//                if (IsInvalid(index)) return index;
//                var array = ReadNext();
//                if (IsInvalid(array)) return array;
//                return Token(array + "[" + index + "]");
//            }
//            case EX_DynArrayLength:
//                return wrapNextBytecode(op -> new BytecodeToken(op + ".Length"));
//            case EX_StructCmpEq:
//                return CompareStructs("==");
//
//            case EX_StructCmpNe:
//                return CompareStructs("!=");
//
//            case EX_EndOfScript:
//                return new EndOfScriptToken();
//
//            case EX_EmptyParmValue:
//            case EX_GoW_DefaultValue:
//                return new DefaultValueToken("");
//            case EX_DefaultParmValue:
//            {
//                var size = _reader.ReadInt16();
//                var offset = _reader.BaseStream.Position;
//                var defaultValueExpr = ReadNext();
//                _reader.BaseStream.Position = offset + size;
//                return new DefaultParamValueToken(defaultValueExpr.ToString());
//            }
//
//            case EX_LocalOutVariable:
//                int valueIndex = _reader.ReadInt32();
//                var packageItem = _package.ResolveClassItem(valueIndex);
//                if (packageItem == null) return ErrToken("Unresolved package item " + packageItem);
//                return Token(packageItem.ObjectName);
//
//            case EX_Iterator:
//                BytecodeToken expr = next();
//                int loopEnd = input.readUnsignedShort();
//                return new ForeachToken(loopEnd, expr);
//
//            case EX_IteratorPop:
//                return new IteratorPopToken();
//
//            case EX_IteratorNext:
//                return new IteratorNextToken();
//
//            case EX_New:
//                var outer = ReadNext();
//                if (IsInvalid(outer)) return outer;
//                var name = ReadNext();
//                if (IsInvalid(name)) return name;
//                var flags = ReadNext();
//                if (IsInvalid(flags)) return flags;
//                var cls = ReadNext();
//                if (IsInvalid(cls)) return cls;
//                return Token("new(" + JoinTokens(outer, name, flags, cls) + ")");
//
//            case EX_VectorConst:
//                return new BytecodeToken("vect(" + input.readFloat() + "," + input.readFloat() + "," + input.readFloat() + ")");
//            case EX_RotationConst:
//                return new BytecodeToken("rot(" + input.readInt() + "," + input.readInt() + "," + input.readInt() + ")");
//            case EX_InterfaceCast:
//            {
//                var interfaceName = ReadRef();
//                return WrapNextBytecode(op => Token(interfaceName.ObjectName + "(" + op + ")"));
//            }
//
//            case EX_Conditional:
//            {
//                var condition = ReadNext();
//                if (IsInvalid(condition)) return condition;
//                var trueSize = _reader.ReadInt16();
//                var pos = _reader.BaseStream.Position;
//                var truePart = ReadNext();
//                if (IsInvalid(truePart)) return WrapErrToken(condition + " ? " + truePart, truePart);
//                if (_reader.BaseStream.Position != pos + trueSize)
//                    return ErrToken("conditional true part size mismatch");
//                var falseSize = _reader.ReadInt16();
//                pos = _reader.BaseStream.Position;
//                var falsePart = ReadNext();
//                if (IsInvalid(truePart)) return WrapErrToken(condition + " ? " + truePart + " : " + falsePart, falsePart);
//                Debug.Assert(_reader.BaseStream.Position == pos + falseSize);
//                return Token(condition + " ? " + truePart + " : " + falsePart);
//            }
//
//            case EX_DynArrayFind:
//                return ReadDynArray1ArgMethod("Find");
//
//            case EX_DynArrayFindStruct:
//                return ReadDynArray2ArgMethod("Find", true);
//
//            case EX_DynArrayRemove:
//                return ReadDynArray2ArgMethod("Remove", false);
//
//            case EX_DynArrayInsert:
//                return ReadDynArray2ArgMethod("Insert", false);
//
//            case EX_DynArrayAddItem:
//                return ReadDynArray1ArgMethod("AddItem");
//
//            case EX_DynArrayRemoveItem:
//                return ReadDynArray1ArgMethod("RemoveItem");
//
//            case EX_DynArrayInsertItem:
//                return ReadDynArray2ArgMethod("InsertItem", true);

//            case EX_DynArrayIterator:
//            {
//                var array = ReadNext();
//                if (IsInvalid(array)) return array;
//                var iteratorVar = ReadNext();
//                if (IsInvalid(iteratorVar)) return iteratorVar;
//                _reader.ReadInt16();
//                var endOffset = _reader.ReadInt16();
//                return new ForeachToken(endOffset, array, iteratorVar);
//            }
//
//            case EX_DelegateProperty:
//            case EX_InstanceDelegate:
//                return Token(ReadName());
//
//            case EX_DelegateFunction:
//            {
//                var receiver = ReadNext();
//                if (IsInvalid(receiver)) return receiver;
//                var methodName = ReadName();
//                if (receiver.ToString().StartsWith("__") && receiver.ToString().EndsWith("__Delegate"))
//                {
//                    return ReadCall(methodName);
//                }
//                return ReadCall(receiver + "." + methodName);
//            }
//
//            case EX_EqualEqual_DelDel:
//            case EX_EqualEqual_DelFunc:
//                return CompareDelegates("==");
//
//            case EX_NotEqual_DelDel:
//            case EX_NotEqual_DelFunc:
//                return CompareDelegates("!=");
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

    private BytecodeToken CompareDelegates(String op) throws IOException {
        BytecodeToken operand1 = next();
        BytecodeToken operand2 = next();
        next();  // close paren
        return new BytecodeToken(operand1 + " " + op + " " + operand2);
    }

    private BytecodeToken readRef(Function<UnrealPackageFile.Entry, String> function) throws IOException {
        return new BytecodeToken(function.apply(unrealPackage.objectReference(readCompactInt())));
    }

    private BytecodeToken wrapNextBytecode(Function<BytecodeToken, BytecodeToken> function) throws IOException {
        return function.apply(next());
    }

    private BytecodeToken ReadDynArray2ArgMethod(String methodName, boolean skip2Bytes) throws IOException {
        BytecodeToken array = next();
        if (skip2Bytes)
            input.readUnsignedShort();
        BytecodeToken index = next();
        BytecodeToken count = next();
        return new BytecodeToken(array + "." + methodName + "(" + index + ", " + count + ")");
    }

    private BytecodeToken readCall(String functionName) throws IOException {
        BytecodeToken p;
        StringBuilder builder = new StringBuilder(functionName + "(");
        int count = 0;
        do {
            p = next();
            if (!(p instanceof DefaultValueToken)) {
                if (count > 0 && !(p instanceof EndParmsToken))
                    builder.append(", ");
                builder.append(p);
                count++;
            }
        } while (!(p instanceof EndParmsToken));
        return new BytecodeToken(builder.toString());
    }

    private String readName() throws IOException {
        return unrealPackage.nameReference(readCompactInt());
    }

    private BytecodeToken readNativeCall(int b) throws IOException {
        int nativeIndex;
        if ((b & 0xF0) == 0x60) {
            nativeIndex = ((b - 0x60) << 8) + readUnsignedByte();
        } else {
            nativeIndex = b;
        }
        if (nativeIndex < EX_FirstNative)
            throw new IllegalStateException("Invalid native index " + nativeIndex);

        acmi.l2.clientmod.unreal.Core.Function function = classLoader.getNativeFunctionQuetly(nativeIndex)
                .orElseThrow(() -> new IllegalStateException("Native function " + nativeIndex + " not found"));
        List<acmi.l2.clientmod.unreal.Core.Function.Flag> flags = acmi.l2.clientmod.unreal.Core.Function.Flag.getFlags(function.functionFlags);
        //System.out.println(function.toString() + flags.toString());
        if (flags.contains(acmi.l2.clientmod.unreal.Core.Function.Flag.PRE_OPERATOR) ||
                (flags.contains(acmi.l2.clientmod.unreal.Core.Function.Flag.OPERATOR) && function.operatorPrecedence == 0)) {
            BytecodeToken p = next();
            next();   // end of parms
            if (flags.contains(acmi.l2.clientmod.unreal.Core.Function.Flag.PRE_OPERATOR))
                return new BytecodeToken(function.getFriendlyName() + p);
            return new BytecodeToken(p + function.getFriendlyName());
        } else if (flags.contains(acmi.l2.clientmod.unreal.Core.Function.Flag.OPERATOR)) {
            BytecodeToken p1 = next();
            BytecodeToken p2 = next();
            next();  // end of parms
            return new BytecodeToken(p1 + " " + function.getFriendlyName() + " " + p2);
        } else
            return readCall(function.getFriendlyName());
    }

    private int readUnsignedByte() throws IOException {
        int val = input.readUnsignedByte();
        read += 1;
        return val;
    }

    private int readUnsignedShort() throws IOException {
        int val = input.readUnsignedShort();
        read += 2;
        return val;
    }

    private int readInt() throws IOException {
        int val = input.readInt();
        read += 4;
        return val;
    }

    private float readFloat() throws IOException {
        float val = input.readFloat();
        read += 4;
        return val;
    }

    private int readCompactInt() throws IOException {
        int val = input.readCompactInt();
        read += 4;
        return val;
    }

    private String readLine() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = readUnsignedByte()) != 0)
            baos.write(b);
        return new String(baos.toByteArray(), input.getCharset());
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
