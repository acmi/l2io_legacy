package acmi.l2.clientmod.unreal.classloader;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.DataInputStream;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.Core.Field;
import acmi.l2.clientmod.unreal.Core.Function;
import acmi.l2.clientmod.unreal.Core.State;
import acmi.l2.clientmod.unreal.Core.Struct;
import acmi.l2.clientmod.unreal.properties.PropertiesUtil;
import acmi.l2.clientmod.unreal.properties.PropertiesUtilImpl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnrealClassLoaderImpl implements UnrealClassLoader {
    private final File systemFolder;
    private final Charset charset;
    private final PropertiesUtil propertiesUtil;

    private final Map<String, Struct> structCache = new HashMap<>();
    private final Map<String, List<Field>> structFieldsCache = new HashMap<>();

    private final Map<Integer, Function> nativeFunctions = new HashMap<>();

    public UnrealClassLoaderImpl(String l2SystemFolder) {
        this.systemFolder = new File(l2SystemFolder);
        this.propertiesUtil = new PropertiesUtilImpl(this);
        this.charset = Charset.forName(System.getProperty(String.format("%s.charset", UnrealClassLoader.class.getName()), "EUC-KR"));
    }

    @Override
    public PropertiesUtil getPropertiesUtil() {
        return propertiesUtil;
    }

    private UnrealPackageReadOnly.ExportEntry getExportEntry(String name) {
        return null;
    }

    @Override
    public Struct getStruct(String structName) throws ClassLoadException {
        if (!structCache.containsKey(structName)) {
            Struct struct = null;

            structCache.put(structName, struct);
        }
        return structCache.get(structName);
    }

    @Override
    public List<Field> getStructFields(String structName) throws ClassLoadException {
        if (!structFieldsCache.containsKey(structName)) {
            List<Field> structFields = null;

            structFieldsCache.put(structName, structFields);
        }
        return structFieldsCache.get(structName);
    }

    private void load(String name) {
        List<Struct> list = new ArrayList<>();


    }


    private Struct loadStruct(String name) throws IOException {
        UnrealPackageReadOnly.ExportEntry entry = getExportEntry(name);
        DataInput buffer = new DataInputStream(new ByteArrayInputStream(entry.getObjectRawDataExternally()), entry.getOffset(), charset);
        Struct struct;
        switch (entry.getObjectClass() != null ? entry.getObjectClass().getObjectFullName() : "null") {
            case "Core.Function":
                struct = new Function(buffer, entry, propertiesUtil);
                if (Function.Flag.getFlags(((Function) struct).functionFlags).contains(Function.Flag.NATIVE))
                    nativeFunctions.put(((Function) struct).nativeIndex, (Function) struct);
                break;
            case "Core.Struct":
                struct = new Struct(buffer, entry, propertiesUtil);
                break;
            case "Core.State":
                struct = new State(buffer, entry, propertiesUtil);
                break;
            default:
                struct = new acmi.l2.clientmod.unreal.Core.Class(buffer, entry, propertiesUtil);
                break;
        }
        return struct;
    }

    @Override
    public Function getNativeFunction(int index) throws ClassLoadException {
        return nativeFunctions.get(index);
    }
}
