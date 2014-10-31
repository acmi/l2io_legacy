package acmi.l2.clientmod.unreal.Fire;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.DataOutput;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.properties.L2Property;
import acmi.l2.clientmod.unreal.properties.PropertiesUtil;

import java.io.IOException;
import java.util.List;

public class FireTexture extends FractalTexture {
    private List<L2Property>[] sparks;

    public FireTexture(DataInput input, UnrealPackageReadOnly.ExportEntry entry, PropertiesUtil propertiesUtil) throws IOException {
        super(input, entry, propertiesUtil);

        sparks = new List[input.readUnsignedByte()];
        for (int i = 0; i < sparks.length; i++)
            sparks[i] = propertiesUtil.readStructBin(input, "Fire.FireTexture.Spark", entry.getUnrealPackage());
    }

    @Override
    public void writeTo(DataOutput output, PropertiesUtil propertiesUtil) throws IOException {
        super.writeTo(output, propertiesUtil);

        output.writeCompactInt(sparks.length);
        for (List<L2Property> spark : sparks)
            propertiesUtil.writeStructBin(output, "Fire.FireTexture.Spark", getEntry().getUnrealPackage(), spark);
    }

    public List<L2Property>[] getSparks() {
        return sparks;
    }
}
