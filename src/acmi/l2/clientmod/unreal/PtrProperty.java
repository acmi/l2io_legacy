package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PtrProperty extends Property {
    public PtrProperty(ByteBuffer buffer, UnrealPackageFile.ExportEntry up, PropertiesUtil propertiesUtil) throws IOException {
        super(buffer, up, propertiesUtil);
    }
}