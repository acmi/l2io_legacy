package acmi.l2.clientmod.unreal.objectfactory;

import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.Core.Object;

import java.io.IOException;

public interface ObjectFactory {
    Object readObject(UnrealPackageReadOnly.ExportEntry entry) throws IOException;
}
