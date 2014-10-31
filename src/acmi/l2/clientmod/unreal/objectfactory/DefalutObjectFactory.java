package acmi.l2.clientmod.unreal.objectfactory;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.DataInputStream;
import acmi.l2.clientmod.io.UnrealPackageFile;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.Core.Object;
import acmi.l2.clientmod.unreal.classloader.UnrealClassLoader;
import acmi.l2.clientmod.unreal.properties.PropertiesUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

public class DefalutObjectFactory implements ObjectFactory {
    private final UnrealClassLoader classLoader;

    public DefalutObjectFactory(UnrealClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public UnrealClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Object readObject(UnrealPackageReadOnly.ExportEntry entry) throws IOException {
        if (entry.getObjectClass() == null)
            return classLoader.getStructQuetly(entry.getObjectFullName())
                    .orElseThrow(() -> new IllegalArgumentException("Class can only be loaded from classpath, use " + UnrealClassLoader.class.getSimpleName()));

        java.lang.Class<? extends Object> clazz = getClass(entry.getObjectClass().getObjectFullName());
        try {
            Constructor<? extends Object> constructor = clazz.getConstructor(DataInput.class, UnrealPackageFile.ExportEntry.class, PropertiesUtil.class);
            ByteArrayInputStream bais = new ByteArrayInputStream(entry.getObjectRawDataExternally());
            DataInputStream dis = new DataInputStream(bais, entry.getOffset(), entry.getUnrealPackage().getCharset());
            return constructor.newInstance(dis, entry, classLoader.getPropertiesUtil());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private java.lang.Class<? extends Object> getClass(String clazz) {
        if (clazz.equals("Core.Object"))
            return AsIsObject.class;

        String name = getClass().getPackage().getName() + "." + clazz;
        try {
            return java.lang.Class.forName(name).asSubclass(Object.class);
        } catch (ClassNotFoundException e) {
            //System.err.println(name + " not found");
        }

        String parent = classLoader.getSuperClass(clazz);
        if (parent == null)
            parent = "Core.Object";

        return getClass(parent);
    }
}
