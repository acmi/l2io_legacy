package acmi.l2.clientmod.unreal.bytecode.token;

import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.bytecode.BytecodeInput;
import acmi.l2.clientmod.unreal.bytecode.BytecodeOutput;

import java.io.IOException;

public class InstanceVariable extends Token {
    public static final int OPCODE = 0x01;

    private int objRef;

    public InstanceVariable(UnrealPackageReadOnly unrealPackage, int objRef) {
        super(unrealPackage);
        this.objRef = objRef;
    }

    public InstanceVariable(UnrealPackageReadOnly unrealPackage, BytecodeInput input) throws IOException {
        super(unrealPackage, input);
        this.objRef = input.readCompactInt();
    }

    @Override
    protected int getOpcode() {
        return OPCODE;
    }

    public UnrealPackageReadOnly.Entry getVariable() {
        return unrealPackage.objectReference(objRef);
    }

    @Override
    public void writeTo(BytecodeOutput output) throws IOException {
        super.writeTo(output);
        output.writeCompactInt(objRef);
    }

    @Override
    public String toString() {
        return getVariable().getObjectName().getName();
    }
}
