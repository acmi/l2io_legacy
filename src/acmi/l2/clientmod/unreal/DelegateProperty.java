package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.nio.ByteBuffer;
import java.util.Map;

import static acmi.l2.clientmod.util.BufferUtil.getCompactInt;

public class DelegateProperty extends Property {
    public final int event;

    public DelegateProperty(ByteBuffer buffer, UnrealPackageFile.ExportEntry up, PropertiesUtil propertiesUtil) {
        super(buffer, up, propertiesUtil);

        event = getCompactInt(buffer);
    }

    public UnrealPackageFile.Entry getEvent() {
        return getEntry().getUnrealPackage().objectReference(event);
    }

    @Override
    public Map<String, String> getInfo() {
        Map<String, String> props = super.getInfo();
        props.put("event", getEvent().toString());
        return props;
    }
}
