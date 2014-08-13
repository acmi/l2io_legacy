package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.nio.ByteBuffer;

import static acmi.l2.clientmod.util.BufferUtil.getString;

public class Const extends Field {
    public final String constant;

    public Const(ByteBuffer buffer, UnrealPackageFile.ExportEntry up, PropertiesUtil propertiesUtil) {
        super(buffer, up, propertiesUtil);

        constant = getString(buffer);
    }

    @Override
    public String toString() {
        return getEntry() + ": Const[" + constant + ']';
    }
}
