package acmi.l2.clientmod.unreal.bytecode.token;

import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.bytecode.BytecodeInput;

import java.io.IOException;

public class Nothing extends Token {
    public static final int OPCODE = 0x0b;

    public Nothing(UnrealPackageReadOnly unrealPackage) {
        super(unrealPackage);
    }

    public Nothing(UnrealPackageReadOnly unrealPackage, BytecodeInput input) throws IOException {
        super(unrealPackage, input);
    }

    @Override
    protected int getOpcode() {
        return OPCODE;
    }

    @Override
    public String toString() {
        return "";
    }
}
