package acmi.l2.clientmod.unreal.bytecode.token;

import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.bytecode.BytecodeInput;
import acmi.l2.clientmod.unreal.bytecode.BytecodeOutput;

import java.io.IOException;

public class Return extends Token {
    public static final int OPCODE = 0x04;

    private final Token value;

    public Return(UnrealPackageReadOnly unrealPackage, Token value) {
        super(unrealPackage);
        this.value = value;
    }

    public Return(UnrealPackageReadOnly unrealPackage, BytecodeInput input) throws IOException {
        super(unrealPackage, input);
        this.value = input.readToken();
    }

    @Override
    protected int getOpcode() {
        return OPCODE;
    }

    public Token getValue() {
        return value;
    }

    @Override
    public void writeTo(BytecodeOutput output) throws IOException {
        super.writeTo(output);
        value.writeTo(output);
    }

    @Override
    public String toString() {
        return String.format("return %s", value).trim() + ";";
    }
}
