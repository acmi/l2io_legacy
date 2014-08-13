package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.nio.ByteBuffer;

import static acmi.l2.clientmod.util.BufferUtil.getCompactInt;

public class Field extends UObject {
    public final int superField;
    public final int next;

    public Field(ByteBuffer buffer, UnrealPackageFile.ExportEntry entry, PropertiesUtil propertiesUtil) {
        super(buffer, entry, propertiesUtil);

        superField = getCompactInt(buffer);
        next = getCompactInt(buffer);
    }

    public UnrealPackageFile.ExportEntry getSuperField() {
        return (UnrealPackageFile.ExportEntry) getEntry().getUnrealPackage().objectReference(superField);
    }

    public UnrealPackageFile.ExportEntry getNext() {
        return (UnrealPackageFile.ExportEntry) getEntry().getUnrealPackage().objectReference(next);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' +
                "superField=" + getSuperField() +
                ", next=" + getNext() +
                '}';
    }
}
