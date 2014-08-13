package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.nio.ByteBuffer;
import java.util.UUID;

import static acmi.l2.clientmod.util.BufferUtil.getCompactInt;

public class UClass extends State {
    public final int classFlags;
    public final UUID classUuid;

    ByteBuffer buffer;
    PropertiesUtil propertiesUtil;

    public UClass(ByteBuffer buffer, UnrealPackageFile.ExportEntry up, PropertiesUtil propertiesUtil) {
        super(buffer, up, propertiesUtil);

        classFlags = buffer.getInt();
        byte[] uuid = new byte[16];
        buffer.get(uuid);
        classUuid = UUID.nameUUIDFromBytes(uuid);
        int dependenciesCount = getCompactInt(buffer);
        for (int i = 0; i < dependenciesCount; i++) {
            getCompactInt(buffer);
            buffer.getInt();
            buffer.getInt();
        }
        int packageImportsCount = getCompactInt(buffer);
        for (int i = 0; i < packageImportsCount; i++) {
            getCompactInt(buffer);
        }
        getCompactInt(buffer);
        getCompactInt(buffer);
        int hideCategoriesListCount = getCompactInt(buffer);
        for (int i = 0; i < hideCategoriesListCount; i++) {
            getCompactInt(buffer);
        }

        this.buffer = buffer;
        this.propertiesUtil = propertiesUtil;
    }

    public void readProperties() {
        properties = propertiesUtil.readProperties(buffer, getEntry().getUnrealPackage(), getEntry().getObjectFullName());
        buffer = null;
    }
}
