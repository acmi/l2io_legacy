package acmi.l2.clientmod.unreal.Engine;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.DataOutput;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.properties.PropertiesUtil;

import java.io.IOException;

public class GFxFlash extends acmi.l2.clientmod.unreal.Core.Object {
    private Type type;
    private byte[] data;

    public GFxFlash(DataInput input, UnrealPackageReadOnly.ExportEntry entry, PropertiesUtil propertiesUtil) throws IOException {
        super(input, entry, propertiesUtil);

        type = Type.valueOf(entry.getUnrealPackage().nameReference(input.readCompactInt()).toUpperCase());
        data = input.readByteArray();
    }

    @Override
    public void writeTo(DataOutput output, PropertiesUtil propertiesUtil) throws IOException {
        super.writeTo(output, propertiesUtil);

        output.writeCompactInt(getEntry().getUnrealPackage().nameReference(type.name().toLowerCase()));
        output.writeByteArray(data);
    }

    public Type getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }

    public enum Type {
        GFX,
        TGA
    }
}
