package acmi.l2.clientmod.unreal.Fire;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.Engine.Texture;
import acmi.l2.clientmod.unreal.properties.PropertiesUtil;

import java.io.IOException;

public class FractalTexture extends Texture {
    public FractalTexture(DataInput input, UnrealPackageReadOnly.ExportEntry entry, PropertiesUtil propertiesUtil) throws IOException {
        super(input, entry, propertiesUtil);
    }
}
