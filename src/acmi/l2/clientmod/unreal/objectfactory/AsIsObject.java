package acmi.l2.clientmod.unreal.objectfactory;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.properties.PropertiesUtil;

import java.io.IOException;

public class AsIsObject extends acmi.l2.clientmod.unreal.Core.Object {
    private byte[] body;

    public AsIsObject(DataInput input, UnrealPackageReadOnly.ExportEntry entry, PropertiesUtil propertiesUtil) throws IOException {
        super(input, entry, propertiesUtil);

        body = new byte[entry.getOffset() + entry.getSize() - input.getPosition()];
        input.readFully(body);
    }

    public byte[] getBody() {
        return body;
    }
}
