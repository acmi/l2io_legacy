package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static acmi.l2.clientmod.util.BufferUtil.getCompactInt;

public class ArrayProperty extends Property {
    public final int inner;

    public ArrayProperty(ByteBuffer buffer, UnrealPackageFile.ExportEntry up, PropertiesUtil propertiesUtil) {
        super(buffer, up, propertiesUtil);

        inner = getCompactInt(buffer);
    }

    public UnrealPackageFile.Entry getInner() {
        return getEntry().getUnrealPackage().objectReference(inner);
    }

    @Override
    public String toString() {
        Map<String, String> props = new HashMap<>();
        if (arrayDimension != 1)
            props.put("arrayDimension", Integer.toString(arrayDimension));
        props.put("inner", getInner().toString());
        props.put("propertyFlags", getPropertyFlags().toString());
        return getEntry() + ": " + getClass().getSimpleName() + props;
    }
}
