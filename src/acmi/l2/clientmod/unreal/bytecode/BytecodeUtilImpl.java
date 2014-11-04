package acmi.l2.clientmod.unreal.bytecode;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.DataOutput;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.UnrealException;
import acmi.l2.clientmod.unreal.bytecode.token.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BytecodeUtilImpl implements BytecodeUtil {
    private static final Logger log = Logger.getLogger(BytecodeUtilImpl.class.getName());

    private static final int EX_ExtendedNative = 0x60;
    private static final int EX_FirstNative = 0x70;

    private static final Map<Integer, Constructor<? extends Token>> table1 = new HashMap<>();
    private static final Map<Integer, Constructor<? extends Token>> table2 = new HashMap<>();

    private UnrealPackageReadOnly unrealPackage;
    private NativeFunctionsSupplier nativeFunctionsSupplier;
    private Map<Integer, Constructor<? extends Token>> table = table1;

    public BytecodeUtilImpl(UnrealPackageReadOnly unrealPackage, NativeFunctionsSupplier nativeFunctionsSupplier) {
        this.unrealPackage = unrealPackage;
        this.nativeFunctionsSupplier = nativeFunctionsSupplier;
    }

    @Override
    public List<Token> readTokens(DataInput input, int scriptSize) throws UnrealException {
        List<Token> tokens = new ArrayList<>();
        try {
            BytecodeInputWrapper wrapper = new BytecodeInputWrapper(input);
            while (wrapper.getSize() < scriptSize) {
                //System.err.println(String.format("%d/%d", wrapper.getSize(), scriptSize));
                Token token = readToken(wrapper);
                //log.fine(token::toString);
                tokens.add(token);
            }
        } catch (Exception e) {
            throw new UnrealException(e);
        }
        return tokens;
    }

    private Token readToken(BytecodeInput input) throws IOException {
        int opcode = input.readUnsignedByte();
        if (opcode >= EX_ExtendedNative)
            return readNativeCall(input, opcode);
        Constructor<? extends Token> constructor = table.get(opcode);
        if (constructor == null)
            throw new IOException(String.format("Unknown token: %02x, table: %s", opcode, table == table1 ? "table1" : "table2"));
        Token token;
        try {
            if (opcode == OldOpcode.OPCODE) {
                table = table2;
                token = constructor.newInstance(unrealPackage, input);
                table = table1;
            } else {
                token = constructor.newInstance(unrealPackage, input);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("", e);
        }
        return token;
    }

    private Token readNativeCall(BytecodeInput input, int b) throws IOException {
        int nativeIndex;
        if ((b & 0xF0) == EX_ExtendedNative) {
            nativeIndex = ((b - EX_ExtendedNative) << 8) + input.readUnsignedByte();
        } else {
            nativeIndex = b;
        }
        if (nativeIndex < EX_FirstNative)
            throw new IllegalStateException("Invalid native index " + nativeIndex);

        NativeFunction nativeFunction = nativeFunctionsSupplier.apply(nativeIndex);
        log.fine(() -> String.format("Native: %03x %s preOperator:%b %d operator:%s", nativeIndex, nativeFunction.getName(), nativeFunction.isPreOperator(), nativeFunction.getOperatorPrecedence(), nativeFunction.isOperator()));
        return new NativeFunctionCall(unrealPackage, nativeFunction, input.readFunctionParams());
    }

    @Override
    public int writeTokens(DataOutput output, List<Token> tokens) throws UnrealException {
        try {
            BytecodeOutputWrapper wrapper = new BytecodeOutputWrapper(output);
            for (Token token : tokens)
                token.writeTo(wrapper);
            return wrapper.getSize();
        } catch (Exception e) {
            throw new UnrealException(e);
        }
    }

    private class BytecodeInputWrapper implements BytecodeInput {
        private DataInput input;
        private int size;

        private BytecodeInputWrapper(DataInput input) {
            this.input = input;
        }

        public int getSize() {
            return size;
        }

        @Override
        public int read() throws IOException {
            return input.read();
        }

        @Override
        public Charset getCharset() {
            return input.getCharset();
        }

        @Override
        public int getPosition() throws IOException {
            return input.getPosition();
        }

        @Override
        public int readUnsignedByte() throws IOException {
            int val = input.readUnsignedByte();
            size += 1;
            return val;
        }

        @Override
        public int readUnsignedShort() throws IOException {
            int val = input.readUnsignedShort();
            size += 2;
            return val;
        }

        @Override
        public int readInt() throws IOException {
            int val = input.readInt();
            size += 4;
            return val;
        }

        @Override
        public int readCompactInt() throws IOException {
            int val = input.readCompactInt();
            size += 4;
            return val;
        }

        @Override
        public long readLong() throws IOException {
            long val = input.readLong();
            size += 8;
            return val;
        }

        @Override
        public float readFloat() throws IOException {
            float val = input.readFloat();
            size += 4;
            return val;
        }

        @Override
        public String readLine() throws IOException {
            StringBuilder sb = new StringBuilder();
            int ch;
            do {
                ch = input.readUnsignedByte();
                sb.append((char) ch);
            } while (ch != 0);
            size += sb.length() + 1;
            return sb.substring(0, sb.length() - 1);
        }

        @Override
        public String readUTF() throws IOException {
            StringBuilder sb = new StringBuilder();
            int ch;
            do {
                ch = input.readUnsignedShort();
                sb.append((char) ch);
            } while (ch != 0);
            size += sb.length() + 1;
            return sb.substring(0, sb.length() - 1);
        }

        public Token readToken() throws IOException {
            return BytecodeUtilImpl.this.readToken(this);
        }
    }

    private class BytecodeOutputWrapper implements BytecodeOutput {
        private DataOutput output;
        private int size;

        private BytecodeOutputWrapper(DataOutput output) {
            this.output = output;
        }

        public int getSize() {
            return size;
        }

        @Override
        public void write(int b) throws IOException {
            output.write(b);
        }

        @Override
        public Charset getCharset() {
            return output.getCharset();
        }

        @Override
        public int getPosition() throws IOException {
            return output.getPosition();
        }

        @Override
        public void writeByte(int val) throws IOException {
            output.writeByte(val);
            size += 1;
        }

        @Override
        public void writeShort(int val) throws IOException {
            output.writeShort(val);
            size += 2;
        }

        @Override
        public void writeInt(int val) throws IOException {
            output.writeInt(val);
            size += 4;
        }

        @Override
        public void writeCompactInt(int val) throws IOException {
            output.writeCompactInt(val);
            size += 4;
        }

        @Override
        public void writeLong(long val) throws IOException {
            output.writeLong(val);
            size += 8;
        }

        @Override
        public void writeFloat(float val) throws IOException {
            output.writeFloat(val);
            size += 4;
        }

        @Override
        public void writeLine(String s) throws IOException {
            if (s != null)
                for (int i = 0; i < s.length(); i++)
                    writeByte(s.charAt(i));
            writeByte(0);
        }

        @Override
        public void writeUTF(String s) throws IOException {
            if (s != null)
                for (int i = 0; i < s.length(); i++)
                    writeShort(s.charAt(i));
            writeShort(0);
        }
    }

    static {
        register(LocalVariable.class, table1);     //00
        register(InstanceVariable.class, table1);  //01
        register(DefaultVariable.class, table1);   //02
        register(Return.class, table1);            //04
        register(Switch.class, table1);            //05
        register(Jump.class, table1);              //06
        register(JumpIfNot.class, table1);         //07
        register(Stop.class, table1);              //08
        register(Assert.class, table1);            //09
        register(Case.class, table1);              //0a
        register(Nothing.class, table1);           //0b
        register(LabelTable.class, table1);        //0c
        register(GotoLabel.class, table1);         //0d
        register(EatString.class, table1);         //0e
        register(Let.class, table1);               //0f
        register(DynArrayElement.class, table1);   //10
        register(New.class, table1);               //11
        register(ClassContext.class, table1);      //12
        register(Metacast.class, table1);          //13
        register(LetBool.class, table1);           //14
        register(EndFunctionParams.class, table1); //16
        register(Self.class, table1);              //17
        register(Skip.class, table1);              //18
        register(Context.class, table1);           //19
        register(ArrayElement.class, table1);      //1a
        register(VirtualFunction.class, table1);   //1b
        register(FinalFunction.class, table1);     //1c
        register(IntConst.class, table1);          //1d
        register(FloatConst.class, table1);        //1e
        register(StringConst.class, table1);       //1f
        register(ObjectConst.class, table1);       //20
        register(NameConst.class, table1);         //21
        register(RotatorConst.class, table1);      //22
        register(VectorConst.class, table1);       //23
        register(ByteConst.class, table1);         //24
        register(IntZero.class, table1);           //25
        register(IntOne.class, table1);            //26
        register(True.class, table1);              //27
        register(False.class, table1);             //28
        register(NativeParam.class, table1);       //29
        register(NoObject.class, table1);          //2a
        register(IntConstByte.class, table1);      //2c
        register(BoolVariable.class, table1);      //2d
        register(DynamicCast.class, table1);       //2e
        register(Iterator.class, table1);          //2f
        register(IteratorPop.class, table1);       //30
        register(IteratorNext.class, table1);      //31
        register(StructMember.class, table1);      //36
        register(Length.class, table1);            //37
        register(GlobalFunction.class, table1);    //38
        register(OldOpcode.class, table1);        //39
        register(ByteToInt.class, table1);         //3a
        register(ByteToFloat.class, table1);       //3c
        register(IntToByte.class, table1);         //3d
        register(IntToBool.class, table1);         //3e
        register(IntToFloat.class, table1);        //3f
        register(BoolToByte.class, table1);        //40
        register(Remove.class, table1);            //41
        register(FloatToByte.class, table1);       //43
        register(FloatToInt.class, table1);        //44
        register(StringToInt.class, table1);       //4a
        register(StringToBool.class, table1);      //4b
        register(StringToFloat.class, table1);     //4c
        register(VectorToRotator.class, table1);   //50
        register(ByteToString.class, table1);      //52
        register(IntToString.class, table1);       //53
        register(BoolToString.class, table1);      //54
        register(FloatToString.class, table1);     //55
        register(ObjectToString.class, table1);    //56
        register(NameToString.class, table1);      //57
        register(VectorToString.class, table1);    //58
        register(RotatorToString.class, table1);   //59

        table2.putAll(table1);
        register(BoolToInt.class, table2);         //41
    }

    static void register(Class<? extends Token> clazz, Map<Integer, Constructor<? extends Token>> map) {
        try {
            map.put(clazz.getDeclaredField("OPCODE").getInt(null), clazz.getConstructor(UnrealPackageReadOnly.class, BytecodeInput.class));
        } catch (ReflectiveOperationException e) {
            log.log(Level.WARNING, e, () -> String.format("Couldn't register %s opcode", clazz));
        }
    }
}
