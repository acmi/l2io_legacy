package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.nio.ByteBuffer;
import java.util.List;

public class UObject {
    private UnrealPackageFile.ExportEntry entry;
    protected List<L2Property> properties;

    public UObject(ByteBuffer buffer, UnrealPackageFile.ExportEntry entry, PropertiesUtil propertiesUtil) {
        this.entry = entry;

        if (!(this instanceof UClass))
            properties = propertiesUtil.readProperties(buffer, entry.getUnrealPackage(), entry.getObjectClass().getObjectFullName());
    }

    public UnrealPackageFile.ExportEntry getEntry() {
        return entry;
    }

    public List<L2Property> getProperties() {
        return properties;
    }
}
