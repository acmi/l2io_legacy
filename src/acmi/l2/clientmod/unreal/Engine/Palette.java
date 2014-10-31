package acmi.l2.clientmod.unreal.Engine;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.DataOutput;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.properties.PropertiesUtil;

import java.io.IOException;

public class Palette extends acmi.l2.clientmod.unreal.Core.Object {
    private int[] colors;

    public Palette(DataInput input, UnrealPackageReadOnly.ExportEntry entry, PropertiesUtil propertiesUtil) throws IOException {
        super(input, entry, propertiesUtil);

        colors = new int[input.readCompactInt()];
        for (int i = 0; i < colors.length; i++)
            colors[i] = input.readInt();
    }

    @Override
    public void writeTo(DataOutput output, PropertiesUtil propertiesUtil) throws IOException {
        super.writeTo(output, propertiesUtil);

        output.writeCompactInt(colors.length);
        for (int color : colors)
            output.writeInt(color);
    }

    public int[] getColors() {
        return colors;
    }
}
