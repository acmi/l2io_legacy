package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static acmi.l2.clientmod.util.BufferUtil.getCompactInt;

public class ClassProperty extends ObjectProperty {
    public final int clazz;

    public ClassProperty(ByteBuffer buffer, UnrealPackageFile.ExportEntry up, PropertiesUtil propertiesUtil) {
        super(buffer, up, propertiesUtil);

        clazz = getCompactInt(buffer);
    }

    public UnrealPackageFile.Entry getClazz() {
        return getEntry().getUnrealPackage().objectReference(clazz);
    }

    @Override
    public String toString() {
        Map<String, String> props = new HashMap<>();
        if (arrayDimension != 1)
            props.put("arrayDimension", Integer.toString(arrayDimension));
        props.put("objectType", getObjectType().toString());
        props.put("class", getClazz().toString());
        props.put("propertyFlags", getPropertyFlags().toString());
        return getEntry() + ": " + getClass().getSimpleName() + props;
    }
}
