package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.nio.ByteBuffer;

import static acmi.l2.clientmod.util.BufferUtil.getCompactInt;

public class Struct extends Field {
    public final int scriptText; //Object Reference.
    public final int child; //First object inside the struct. Object Reference.
    public final int friendlyName; //Name of the struct. Name Reference.
    public final int line;
    public final int textPos;
    public final int scriptSize;

    public Struct(ByteBuffer buffer, UnrealPackageFile.ExportEntry up, PropertiesUtil propertiesUtil) {
        super(buffer, up, propertiesUtil);

        scriptText = getCompactInt(buffer);
        child = getCompactInt(buffer);
        friendlyName = getCompactInt(buffer);
        getCompactInt(buffer);
        line = buffer.getInt();
        textPos = buffer.getInt();
        scriptSize = buffer.getInt();
    }

    public UnrealPackageFile.Entry getScritpText() {
        return getEntry().getUnrealPackage().objectReference(scriptText);
    }

    public UnrealPackageFile.Entry getChild() {
        return getEntry().getUnrealPackage().objectReference(child);
    }

    public String getFriendlyName() {
        return getEntry().getUnrealPackage().getNameTable().get(friendlyName).getName();
    }

    @Override
    public String toString() {
        return getEntry() + ": " + getClass().getSimpleName() + "[" + getFriendlyName() + ']';
    }
}
