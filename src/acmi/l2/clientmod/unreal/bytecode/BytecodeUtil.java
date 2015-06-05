package acmi.l2.clientmod.unreal.bytecode;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.DataOutput;
import acmi.l2.clientmod.unreal.UnrealException;
import acmi.l2.clientmod.unreal.bytecode.token.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BytecodeUtil {
    private static final int EX_ExtendedNative = 0x60;
    private static final int EX_FirstNative = 0x70;

    private static final Map<Integer, Method> mainTokenTable = new HashMap<>();
    private static final Map<Integer, Method> conversionTokenTable = new HashMap<>();

    private int noneInd;
    private Map<Integer, Method> table = mainTokenTable;

    public BytecodeUtil(int noneInd) {
        this.noneInd = noneInd;
    }

    public int getNoneInd() {
        return noneInd;
    }

    public BytecodeInput createBytecodeInput(DataInput input) {
        return new BytecodeInputWrapper(input, noneInd, this::readToken);
    }

    public BytecodeOutput createBytecodeOutput(DataOutput output) {
        return new BytecodeOutputWrapper(output, noneInd);
    }

    public List<Token> readTokens(DataInput input, int scriptSize) throws IOException {
        BytecodeInput wrapper = createBytecodeInput(input);

        List<Token> tokens = new ArrayList<>();
        while (wrapper.getSize() < scriptSize) {
            tokens.add(readToken(wrapper));
        }

        return tokens;
    }

    private Token readToken(BytecodeInput input) throws IOException {
        int opcode = input.readUnsignedByte();
        Method constructorMethod = table.get(opcode);
        boolean main = table == mainTokenTable;
        table = mainTokenTable;

        if (opcode >= EX_ExtendedNative && constructorMethod == null)
            return readNativeCall(input, opcode);

        if (constructorMethod == null)
            throw new IOException(String.format("Unknown token: %02x, table: %s", opcode, main ? "Main" : "Conversion"));

        if (opcode == ConversionTable.OPCODE)
            table = conversionTokenTable;

        try {
            return (Token) constructorMethod.invoke(null, input);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof IOException)
                throw (IOException) targetException;
            throw new UnrealException("Read token error", targetException);
        }
    }

    private Token readNativeCall(BytecodeInput input, int b) throws IOException {
        int nativeIndex = (b & 0xF0) == EX_ExtendedNative ?
                ((b - EX_ExtendedNative) << 8) + input.readUnsignedByte() : b;

        if (nativeIndex < EX_FirstNative)
            throw new UnrealException("Invalid native index: " + nativeIndex);

        return NativeFunctionCall.readFrom(input, nativeIndex);
    }

    public int writeTokens(DataOutput output, Token... tokens) throws IOException {
        BytecodeOutputWrapper wrapper = new BytecodeOutputWrapper(output, noneInd);
        for (Token token : tokens)
            wrapper.writeToken(token);
        return wrapper.getSize();
    }

    public int writeTokens(DataOutput output, Iterable<Token> tokens) throws IOException {
        BytecodeOutputWrapper wrapper = new BytecodeOutputWrapper(output, noneInd);
        for (Token token : tokens)
            wrapper.writeToken(token);
        return wrapper.getSize();
    }

    static {
        register(LocalVariable.class, mainTokenTable);     //00
        register(InstanceVariable.class, mainTokenTable);  //01
        register(DefaultVariable.class, mainTokenTable);   //02

        register(Return.class, mainTokenTable);            //04
        register(Switch.class, mainTokenTable);            //05
        register(Jump.class, mainTokenTable);              //06
        register(JumpIfNot.class, mainTokenTable);         //07
        register(Stop.class, mainTokenTable);              //08
        register(Assert.class, mainTokenTable);            //09
        register(Case.class, mainTokenTable);              //0a
        register(Nothing.class, mainTokenTable);           //0b
        register(LabelTable.class, mainTokenTable);        //0c
        register(GotoLabel.class, mainTokenTable);         //0d
        register(EatString.class, mainTokenTable);         //0e
        register(Let.class, mainTokenTable);               //0f
        register(DynArrayElement.class, mainTokenTable);   //10
        register(New.class, mainTokenTable);               //11
        register(ClassContext.class, mainTokenTable);      //12
        register(Metacast.class, mainTokenTable);          //13
        register(LetBool.class, mainTokenTable);           //14

        register(EndFunctionParams.class, mainTokenTable); //16
        register(Self.class, mainTokenTable);              //17
        register(Skip.class, mainTokenTable);              //18
        register(Context.class, mainTokenTable);           //19
        register(ArrayElement.class, mainTokenTable);      //1a
        register(VirtualFunction.class, mainTokenTable);   //1b
        register(FinalFunction.class, mainTokenTable);     //1c
        register(IntConst.class, mainTokenTable);          //1d
        register(FloatConst.class, mainTokenTable);        //1e
        register(StringConst.class, mainTokenTable);       //1f
        register(ObjectConst.class, mainTokenTable);       //20
        register(NameConst.class, mainTokenTable);         //21
        register(RotatorConst.class, mainTokenTable);      //22
        register(VectorConst.class, mainTokenTable);       //23
        register(ByteConst.class, mainTokenTable);         //24
        register(IntZero.class, mainTokenTable);           //25
        register(IntOne.class, mainTokenTable);            //26
        register(True.class, mainTokenTable);              //27
        register(False.class, mainTokenTable);             //28
        register(NativeParam.class, mainTokenTable);       //29
        register(NoObject.class, mainTokenTable);          //2a

        register(IntConstByte.class, mainTokenTable);      //2c
        register(BoolVariable.class, mainTokenTable);      //2d
        register(DynamicCast.class, mainTokenTable);       //2e
        register(Iterator.class, mainTokenTable);          //2f
        register(IteratorPop.class, mainTokenTable);       //30
        register(IteratorNext.class, mainTokenTable);      //31
        register(StructCmpEq.class, mainTokenTable);       //32
        register(StructCmpNe.class, mainTokenTable);       //33

        register(StructMember.class, mainTokenTable);      //36
        register(Length.class, mainTokenTable);            //37
        register(GlobalFunction.class, mainTokenTable);    //38
        register(ConversionTable.class, mainTokenTable);   //39
        register(Insert.class, mainTokenTable);            //40
        register(Remove.class, mainTokenTable);            //41
        register(DelegateName.class, mainTokenTable);      //44

        conversionTokenTable.putAll(mainTokenTable);
        register(ByteToInt.class, conversionTokenTable);         //3a
        register(ByteToBool.class, conversionTokenTable);        //3b
        register(ByteToFloat.class, conversionTokenTable);       //3c
        register(IntToByte.class, conversionTokenTable);         //3d
        register(IntToBool.class, conversionTokenTable);         //3e
        register(IntToFloat.class, conversionTokenTable);        //3f
        register(BoolToByte.class, conversionTokenTable);        //40
        register(BoolToInt.class, conversionTokenTable);         //41
        register(BoolToFloat.class, conversionTokenTable);       //42
        register(FloatToByte.class, conversionTokenTable);       //43
        register(FloatToInt.class, conversionTokenTable);        //44
        register(FloatToBool.class, conversionTokenTable);       //45

        register(StringToInt.class, conversionTokenTable);       //4a
        register(StringToBool.class, conversionTokenTable);      //4b
        register(StringToFloat.class, conversionTokenTable);     //4c
        register(StringToVector.class, conversionTokenTable);    //4d
        register(StringToRotator.class, conversionTokenTable);   //4e
        register(VectorToBool.class, conversionTokenTable);      //4f
        register(VectorToRotator.class, conversionTokenTable);   //50
        register(RotatorToBool.class, conversionTokenTable);     //51
        register(ByteToString.class, conversionTokenTable);      //52
        register(IntToString.class, conversionTokenTable);       //53
        register(BoolToString.class, conversionTokenTable);      //54
        register(FloatToString.class, conversionTokenTable);     //55
        register(ObjectToString.class, conversionTokenTable);    //56
        register(NameToString.class, conversionTokenTable);      //57
        register(VectorToString.class, conversionTokenTable);    //58
        register(RotatorToString.class, conversionTokenTable);   //59

        register(ByteToINT64.class, conversionTokenTable);       //5a
        register(IntToINT64.class, conversionTokenTable);        //5b
        register(BoolToINT64.class, conversionTokenTable);       //5c
        register(FloatToINT64.class, conversionTokenTable);      //5d
        register(StringToINT64.class, conversionTokenTable);     //5e
        register(INT64ToByte.class, conversionTokenTable);       //5f
        register(INT64ToInt.class, conversionTokenTable);        //60
        register(INT64ToBool.class, conversionTokenTable);       //61
        register(INT64ToFloat.class, conversionTokenTable);      //62
        register(INT64ToString.class, conversionTokenTable);     //63
    }

    static void register(Class<? extends Token> clazz, Map<Integer, Method> map) {
        try {
            map.put(clazz.getDeclaredField("OPCODE").getInt(null), clazz.getDeclaredMethod("readFrom", BytecodeInput.class));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(String.format("Couldn't register %s opcode", clazz), e);
        }
    }
}
