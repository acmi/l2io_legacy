package acmi.l2.clientmod.unreal.Core;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.properties.PropertiesUtil;

import java.io.IOException;
import java.util.Map;

public class DelegateProperty extends Property {
    public final int event;

    public DelegateProperty(DataInput input, UnrealPackageReadOnly.ExportEntry entry, PropertiesUtil propertiesUtil) throws IOException {
        super(input, entry, propertiesUtil);

        event = input.readCompactInt();
    }

    public UnrealPackageReadOnly.Entry getEvent() {
        return getEntry().getUnrealPackage().objectReference(event);
    }

    @Override
    public Map<String, String> getInfo() {
        Map<String, String> props = super.getInfo();
        props.put("event", getEvent().toString());
        return props;
    }
}
