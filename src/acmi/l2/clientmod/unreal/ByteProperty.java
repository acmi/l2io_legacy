package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static acmi.l2.clientmod.util.BufferUtil.getCompactInt;

public class ByteProperty extends Property {
    public final int enumType;

    public ByteProperty(ByteBuffer buffer, UnrealPackageFile.ExportEntry up, PropertiesUtil propertiesUtil) {
        super(buffer, up, propertiesUtil);

        enumType = getCompactInt(buffer);
    }

    public UnrealPackageFile.Entry getEnumType() {
        return getEntry().getUnrealPackage().objectReference(enumType);
    }

    @Override
    public String toString() {
        Map<String, String> props = new HashMap<>();
        if (arrayDimension != 1)
            props.put("arrayDimension", Integer.toString(arrayDimension));
        if (getEnumType() != null)
            props.put("enumType", getEnumType().toString());
        props.put("propertyFlags", getPropertyFlags().toString());
        return getEntry() + ": " + getClass().getSimpleName() + props;
    }
}
