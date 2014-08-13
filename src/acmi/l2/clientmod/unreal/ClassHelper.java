package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class ClassHelper implements AutoCloseable {
    private static final Logger log = Logger.getLogger(ClassHelper.class.getName());

    private final File systemFolder;

    private final Map<String, UnrealPackageFile> classPackages = new HashMap<>();
    private final Map<String, List<Field>> structCache = new HashMap<>();

    private final PropertiesUtil propertiesUtil;

    public ClassHelper(String l2SystemFolder) {
        this.systemFolder = new File(l2SystemFolder);
        this.propertiesUtil = new PropertiesUtil(this);
    }

    private UnrealPackageFile getClassPackage(String name) throws IOException {
        if (!classPackages.containsKey(name))
            classPackages.put(name, new UnrealPackageFile(new File(systemFolder, name + ".u"), true));

        return classPackages.get(name);
    }

    public PropertiesUtil getPropertiesUtil() {
        return propertiesUtil;
    }

    public Struct loadStruct(String className) throws IOException {
        String[] path = className.split("\\.", 2);
        UnrealPackageFile up = getClassPackage(path[0]);
        UnrealPackageFile.ExportEntry entry = up.getExportTable().stream()
                .filter(e -> e.getObjectFullName().equalsIgnoreCase(className))
                .findAny()
                .orElseThrow(() -> new IOException(className + " not found"));
        ByteBuffer buffer = ByteBuffer.wrap(entry.getObjectRawData()).order(ByteOrder.LITTLE_ENDIAN);
        Struct struct;
        switch (entry.getObjectClass() != null ? entry.getObjectClass().getObjectFullName() : "null") {
            case "Core.Function":
                struct = new Function(buffer, entry, propertiesUtil);
                break;
            case "Core.Struct":
                struct = new Struct(buffer, entry, propertiesUtil);
                break;
            default:
                struct = new UClass(buffer, entry, propertiesUtil);
                break;
        }
        return struct;
    }

    @Override
    public void close() {
        classPackages.values().stream().forEach(up -> {
            try {
                up.close();
            } catch (IOException e) {
                log.log(Level.WARNING, up + " close exception", e);
            }
        });
    }

    public Optional<List<Field>> getStruct(String structName) {
        if (!structCache.containsKey(structName)) {
            try {
                Struct struct = loadStruct(structName);

                List<Field> fields = new ArrayList<>();

                Struct tmp = struct;
                while (tmp != null) {
                    UnrealPackageFile.ExportEntry childEntry = (UnrealPackageFile.ExportEntry) tmp.getChild();
                    while (childEntry != null) {
                        Field field = loadField(childEntry);

                        fields.add(field);

                        childEntry = field.getNext();
                    }

                    UnrealPackageFile.Entry superStruct = tmp.getEntry().getObjectSuperClass();
                    tmp = superStruct != null ? loadStruct(superStruct.getObjectFullName()) : null;
                }

                structCache.put(structName, Collections.unmodifiableList(fields));

                if (struct instanceof UClass) {
                    ((UClass) struct).readProperties();
                }
            } catch (IOException e) {
                //log.log(Level.WARNING, "Couldn't load structure " + structName, e);
            }
        }

        return Optional.ofNullable(structCache.get(structName));
    }

    Field loadField(UnrealPackageFile.ExportEntry entry) throws IOException {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(entry.getObjectRawData()).order(ByteOrder.LITTLE_ENDIAN);

            String fieldClassName = getClass().getPackage().getName() + "." + entry.getObjectClass().getObjectName().getName();
            Class<? extends Field> fieldClass = (Class<? extends Field>) Class.forName(fieldClassName);

            return fieldClass.getConstructor(ByteBuffer.class, UnrealPackageFile.ExportEntry.class, PropertiesUtil.class)
                    .newInstance(buffer, entry, propertiesUtil);
        } catch (ReflectiveOperationException roe) {
            log.log(Level.SEVERE, "Couldn't load field " + entry, roe);
            throw new RuntimeException(roe);
        }
    }
}
