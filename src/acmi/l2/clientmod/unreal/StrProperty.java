package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.nio.ByteBuffer;

public class StrProperty extends Property {
    public StrProperty(ByteBuffer buffer, UnrealPackageFile.ExportEntry up, PropertiesUtil propertiesUtil) {
        super(buffer, up, propertiesUtil);
    }
}