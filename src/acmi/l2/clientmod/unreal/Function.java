package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.nio.ByteBuffer;

public class Function extends Struct {
    public final int iNative;
    public final int operatorPrecedence;
    public final int functionFlags;
    public final int replicationOffset;

    public Function(ByteBuffer buffer, UnrealPackageFile.ExportEntry up, PropertiesUtil propertiesUtil) {
        super(buffer, up, propertiesUtil);

        //TODO code statements

        iNative = buffer.getShort() & 0xffff;
        operatorPrecedence = buffer.get() & 0xff;
        functionFlags = buffer.getInt();
        replicationOffset = (functionFlags & 0x40) != 0 ? buffer.getShort() & 0xffff : 0;
    }
}
