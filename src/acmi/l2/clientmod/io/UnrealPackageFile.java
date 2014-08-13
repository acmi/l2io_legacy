/*
 * Copyright (c) 2014 acmi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package acmi.l2.clientmod.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.stream.Collectors;

import static acmi.l2.clientmod.io.UnrealPackageConstants.*;
import static acmi.l2.clientmod.util.BufferUtil.putCompactInt;
import static acmi.l2.clientmod.util.BufferUtil.putString;
import static acmi.l2.clientmod.util.ByteUtil.compactIntToByteArray;

public class UnrealPackageFile implements Closeable {
    public static int BUFFER_SIZE = 1 << 22;

    private RandomAccessFile file;
    private final String packageName;

    private int version;
    private int licensee;
    private int flags;

    private List<NameEntry> names;
    private List<ExportEntry> exports;
    private List<ImportEntry> imports;

    private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN);

    public UnrealPackageFile(File file, boolean readOnly) throws IOException {
        this(new RandomAccessFile(file, readOnly));
    }

    public UnrealPackageFile(RandomAccessFile file) throws IOException {
        this.file = Objects.requireNonNull(file);
        String fileName = new File(file.getPath()).getName();
        this.packageName = fileName.substring(0, fileName.indexOf('.'));

        file.setPosition(0);

        if (file.readInt() != UNREAL_PACKAGE_MAGIC)
            throw new IOException("Not a L2 package file.");

        version = file.readUnsignedShort();
        licensee = file.readUnsignedShort();
        flags = file.readInt();

        readNameTable();
        readImportTable();
        readExportTable();
    }

    public String getPackageName() {
        return packageName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) throws IOException {
        file.setPosition(VERSION_OFFSET);
        file.writeShort(version);

        this.version = version;
    }

    public int getLicensee() {
        return licensee;
    }

    public void setLicensee(int licensee) throws IOException {
        file.setPosition(LICENSEE_OFFSET);
        file.writeShort(licensee);

        this.licensee = licensee;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) throws IOException {
        file.setPosition(PACKAGE_FLAGS_OFFSET);
        file.writeInt(flags);

        this.flags = flags;
    }

    public List<NameEntry> getNameTable() {
        return names;
    }

    private void readNameTable() throws IOException {
        file.setPosition(NAME_COUNT_OFFSET);
        int count = file.readInt();
        file.setPosition(getNameTableOffset());

        List<NameEntry> tmp = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
            tmp.add(new NameEntry(this, i, file.readLine(), file.readInt()));

        names = Collections.unmodifiableList(tmp);
    }

    public List<ExportEntry> getExportTable() {
        return exports;
    }

    private void readExportTable() throws IOException {
        file.setPosition(EXPORT_COUNT_OFFSET);
        int count = file.readInt();
        file.setPosition(getExportTableOffset());

        List<ExportEntry> tmp = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
            tmp.add(new ExportEntry(this, i,
                    file.readCompactInt(),
                    file.readCompactInt(),
                    file.readInt(),
                    file.readCompactInt(),
                    file.readInt(),
                    file.readCompactInt(),
                    file.readCompactInt()));

        exports = Collections.unmodifiableList(tmp);
    }

    public List<ImportEntry> getImportTable() {
        return imports;
    }

    private void readImportTable() throws IOException {
        file.setPosition(IMPORT_COUNT_OFFSET);
        int count = file.readInt();
        file.setPosition(getImportTableOffset());

        List<ImportEntry> tmp = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
            tmp.add(new ImportEntry(this, i,
                    file.readCompactInt(),
                    file.readCompactInt(),
                    file.readInt(),
                    file.readCompactInt()));

        imports = Collections.unmodifiableList(tmp);
    }

    public String getFilePath() {
        return file.getPath();
    }

    public String toString() {
        return "UnrealPackage[" + getFilePath() + "]";
    }

    public String nameReference(int index) {
        return getNameTable().get(index).getName();
    }

    public Entry objectReference(int index) {
        if (index > 0)
            return getExportTable().get(index - 1);
        else if (index < 0)
            return getImportTable().get(-index - 1);
        else
            return null;
    }

    public int nameReference(String name) {
        try {
            return getNameTable().parallelStream()
                    .filter(e -> e.getName().equalsIgnoreCase(name))
                    .findAny()
                    .get()
                    .getIndex();
        } catch (NoSuchElementException e) {
            return -1;
        }
    }

    @Deprecated
    public int objectReference(String name) {
        int ref;

        if ((ref = importReference(name)) != 0)
            return ref;

        if ((ref = exportReference(name)) != 0)
            return ref;

        return 0;
    }

    @Deprecated
    public int importReference(String name) {
        try {
            return getImportTable().stream()
                    .filter(entry -> entry.getObjectFullName().equalsIgnoreCase(name))
                    .findAny()
                    .get()
                    .getObjectReference();
        } catch (NoSuchElementException ignore) {
            return 0;
        }
    }

    @Deprecated
    public int exportReference(String name) {
        try {
            return getExportTable().stream()
                    .filter(entry -> entry.getObjectFullName().equalsIgnoreCase(name))
                    .findAny()
                    .get()
                    .getObjectReference();
        } catch (NoSuchElementException ignore) {
            return 0;
        }
    }

    public void addNameEntries(Map<String, Integer> names) throws IOException {
        List<NameEntry> nameTable = new ArrayList<>(getNameTable());
        names.forEach((k, v) -> {
            NameEntry entry = new NameEntry(null, 0, k, v);
            if (!nameTable.contains(entry))
                nameTable.add(entry);
        });

        int newNameTablePos = getDataEndOffset();
        file.setPosition(newNameTablePos);
        writeNameTable(nameTable);
        int newImportTablePos = file.getPosition();
        writeImportTable(getImportTable());
        int newExportTablePos = file.getPosition();
        writeExportTable(getExportTable());

        file.setLength(file.getPosition());

        file.setPosition(NAME_COUNT_OFFSET);
        file.writeInt(nameTable.size());
        file.setPosition(NAME_OFFSET_OFFSET);
        file.writeInt(newNameTablePos);

        file.setPosition(EXPORT_OFFSET_OFFSET);
        file.writeInt(newExportTablePos);

        file.setPosition(IMPORT_OFFSET_OFFSET);
        file.writeInt(newImportTablePos);

        readNameTable();
    }

    public void addImportEntries(Map<String, String> imports, boolean force) throws IOException {
        Map<String, Integer> namesToAdd = new HashMap<>();
        imports.forEach((k, v) -> {
            String[] namePath = k.split("\\.");
            String[] classPath = v.split("\\.");

            Arrays.stream(namePath)
                    .filter(s -> nameReference(s) == -1)
                    .forEach(s -> namesToAdd.put(s, PACKAGE_FLAGS));
            Arrays.stream(classPath)
                    .filter(s -> nameReference(s) == -1)
                    .forEach(s -> namesToAdd.put(s, PACKAGE_FLAGS));
        });
        addNameEntries(namesToAdd);

        List<ImportEntry> importTable = new ArrayList<>(getImportTable());
        for (Map.Entry<String, String> entry : imports.entrySet()) {
            String[] namePath = entry.getKey().split("\\.");
            String[] classPath = entry.getValue().split("\\.");

            int pckg = 0;
            ImportEntry importEntry;
            for (int i = 0; i < namePath.length - 1; i++) {
                importEntry = new ImportEntry(this, 0,
                        nameReference("Core"),
                        nameReference("Package"),
                        pckg,
                        nameReference(namePath[i]));
                if ((pckg = importTable.indexOf(importEntry)) == -1) {
                    importTable.add(importEntry);
                    pckg = importTable.size() - 1;
                }
                pckg = -(pckg + 1);
            }

            importEntry = new ImportEntry(this, 0,
                    nameReference(classPath[0]),
                    nameReference(classPath[1]),
                    pckg,
                    nameReference(namePath[namePath.length - 1]));
            if (force || importTable.indexOf(importEntry) == -1)
                importTable.add(importEntry);
        }

        file.setPosition(getImportTableOffset());
        writeImportTable(importTable);
        int newExportTablePos = file.getPosition();
        writeExportTable(getExportTable());
        file.setLength(file.getPosition());

        file.setPosition(EXPORT_OFFSET_OFFSET);
        file.writeInt(newExportTablePos);
        file.setPosition(IMPORT_COUNT_OFFSET);
        file.writeInt(importTable.size());

        readImportTable();
    }

    public void addExportEntry(String objectName, String objectClass, String objectSuperClass, byte[] data, int flags) throws IOException {
        Map<String, String> classes = new HashMap<>();
        if (objectClass != null && objectReference(objectClass) == 0)
            classes.put(objectClass, "Core.Class");
        if (objectSuperClass != null && objectReference(objectSuperClass) == 0)
            classes.put(objectClass, "Core.Class");
        if (!classes.isEmpty())
            addImportEntries(classes, false);

        Map<String, Integer> namesToAdd = new HashMap<>();
        String[] namePath = objectName.split("\\.");
        Arrays.stream(namePath)
                .filter(s -> nameReference(s) == -1)
                .forEach(s -> namesToAdd.put(s, PACKAGE_FLAGS));
        addNameEntries(namesToAdd);

        file.setPosition(getDataEndOffset());
        List<ExportEntry> exportTable = new ArrayList<>(getExportTable());
        int pckgInd = importReference("Core.Package");
        byte[] pckgData = compactIntToByteArray(nameReference("None"));

        int pckg = 0;
        ExportEntry exportEntry;
        for (int i = 0; i < namePath.length - 1; i++) {
            exportEntry = new ExportEntry(this, 0,
                    pckgInd,
                    0,
                    pckg,
                    nameReference(namePath[i]),
                    PACKAGE_FLAGS,
                    pckgData.length, file.getPosition());
            if ((pckg = exportTable.indexOf(exportEntry)) == -1) {
                exportTable.add(exportEntry);
                pckg = exportTable.size() - 1;
                file.write(pckgData);
            }
            pckg++;
        }

        exportEntry = new ExportEntry(this,
                0,
                objectReference(objectClass),
                objectReference(objectSuperClass),
                pckg,
                nameReference(namePath[namePath.length - 1]),
                flags,
                data.length, file.getPosition());
        if (exportTable.indexOf(exportEntry) == -1) {
            exportTable.add(exportEntry);
            file.write(data);
        }

        int nameTablePosition = file.getPosition();
        writeNameTable(getNameTable());
        int importTablePosition = file.getPosition();
        writeImportTable(getImportTable());
        int exportTablePosition = file.getPosition();
        writeExportTable(exportTable);

        file.setPosition(NAME_OFFSET_OFFSET);
        file.writeInt(nameTablePosition);
        file.setPosition(EXPORT_COUNT_OFFSET);
        file.writeInt(exportTable.size());
        file.setPosition(EXPORT_OFFSET_OFFSET);
        file.writeInt(exportTablePosition);
        file.setPosition(IMPORT_OFFSET_OFFSET);
        file.writeInt(importTablePosition);

        readNameTable();
        readImportTable();
        readExportTable();
    }

    public void renameImport(int index, String importDst) throws IOException {
        addImportEntries(
                Collections.singletonMap(importDst, getImportTable().get(index).getFullClassName()),
                true
        );

        List<ImportEntry> importTable = new ArrayList<>(getImportTable());
        importTable.set(index, importTable.remove(importTable.size() - 1));

        file.setPosition(getImportTableOffset());
        writeImportTable(importTable);
        int newExportTablePos = file.getPosition();
        writeExportTable(getExportTable());
        file.setLength(file.getPosition());

        file.setPosition(EXPORT_OFFSET_OFFSET);
        file.writeInt(newExportTablePos);
        file.setPosition(IMPORT_COUNT_OFFSET);
        file.writeInt(importTable.size());

        readImportTable();
    }

    public void changeImportClass(int index, String importDst) throws IOException {
        String[] clazz = importDst.split("\\.");
        if (clazz.length != 2)
            throw new IllegalArgumentException("Format: Package.Class");

        addNameEntries(Arrays.stream(clazz)
                .filter(s -> nameReference(s) == -1)
                .collect(Collectors.toMap(v -> v, v -> PACKAGE_FLAGS)));

        ImportEntry entry = getImportTable().get(index);
        entry.classPackage = nameReference(clazz[0]);
        entry.className = nameReference(clazz[1]);

        file.setPosition(getImportTableOffset());
        writeImportTable(getImportTable());
        int newExportTablePos = file.getPosition();
        writeExportTable(getExportTable());
        file.setLength(file.getPosition());

        file.setPosition(EXPORT_OFFSET_OFFSET);
        file.writeInt(newExportTablePos);

        readImportTable();
    }

    private void writeNameTable(List<NameEntry> nameTable) throws IOException {
        buffer.clear();
        nameTable.stream().forEach(entry -> {
            putString(buffer, entry.getName(), file.getCharset());
            buffer.putInt(entry.getFlags());
        });
        buffer.flip();
        file.write(buffer.array(), 0, buffer.limit());
    }

    private void writeImportTable(List<ImportEntry> importTable) throws IOException {
        buffer.clear();
        importTable.stream().forEach(entry -> {
            putCompactInt(buffer, entry.classPackage);
            putCompactInt(buffer, entry.className);
            buffer.putInt(entry.objectPackage);
            putCompactInt(buffer, entry.objectName);
        });
        buffer.flip();
        file.write(buffer.array(), 0, buffer.limit());
    }

    private void writeExportTable(List<ExportEntry> exportTable) throws IOException {
        buffer.clear();
        exportTable.stream().forEach(entry -> {
            putCompactInt(buffer, entry.objectClass);
            putCompactInt(buffer, entry.objectSuperClass);
            buffer.putInt(entry.objectPackage);
            putCompactInt(buffer, entry.objectName);
            buffer.putInt(entry.objectFlags);
            putCompactInt(buffer, entry.size);
            putCompactInt(buffer, entry.offset);
        });
        buffer.flip();
        file.write(buffer.array(), 0, buffer.limit());
    }

    public int getNameTableOffset() throws IOException {
        file.setPosition(NAME_OFFSET_OFFSET);
        return file.readInt();
    }

    public int getExportTableOffset() throws IOException {
        file.setPosition(EXPORT_OFFSET_OFFSET);
        return file.readInt();
    }

    public int getImportTableOffset() throws IOException {
        file.setPosition(IMPORT_OFFSET_OFFSET);
        return file.readInt();
    }

    public int getDataStartOffset() {
        return getExportTable().stream()
                .filter(entry -> entry.getSize() > 0)
                .mapToInt(ExportEntry::getOffset)
                .min()
                .orElseThrow(() -> new IllegalStateException("Data block is empty"));
    }

    public int getDataEndOffset() {
        return getExportTable().stream()
                .filter(entry -> entry.getSize() > 0)
                .mapToInt(entry -> entry.getOffset() + entry.getSize())
                .max()
                .orElseThrow(() -> new IllegalStateException("Data block is empty"));
    }

    public void close() throws IOException {
        file.close();
    }

    private static abstract class PackageEntry {
        private final UnrealPackageFile unrealPackage;
        private final int index;

        protected PackageEntry(UnrealPackageFile unrealPackage, int index) {
            this.unrealPackage = unrealPackage;
            this.index = index;
        }

        public UnrealPackageFile getUnrealPackage() {
            return unrealPackage;
        }

        public int getIndex() {
            return index;
        }
    }

    public static final class Generation extends PackageEntry {
        private final int exportCount;
        private final int importCount;

        public Generation(UnrealPackageFile unrealPackage, int index, int exportCount, int importCount) {
            super(unrealPackage, index);
            this.exportCount = exportCount;
            this.importCount = importCount;
        }

        public int getExportCount() {
            return exportCount;
        }

        public int getNameCount() {
            return importCount;
        }

        @Override
        public String toString() {
            return "Generation[" +
                    "exportCount=" + exportCount +
                    ", importCount=" + importCount +
                    ']';
        }
    }

    public static final class NameEntry extends PackageEntry {
        private final String name;
        private int flags;

        private NameEntry(UnrealPackageFile unrealPackage, int index, String name, int flags) {
            super(unrealPackage, index);
            this.name = Objects.requireNonNull(name);
            this.flags = flags;
        }

        public String getName() {
            return name;
        }

        public int getFlags() {
            return flags;
        }

        public String toString() {
            return name;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass())) {
                return false;
            }
            NameEntry nameEntry = (NameEntry) o;

            return name.equalsIgnoreCase(nameEntry.name);
        }

        public int hashCode() {
            return name.hashCode();
        }
    }

    public static abstract class Entry extends PackageEntry {
        protected final int objectPackage;
        protected final int objectName;

        protected Entry(UnrealPackageFile unrealPackage, int index, int objectPackage, int objectName) {
            super(unrealPackage, index);
            this.objectPackage = objectPackage;
            this.objectName = objectName;
        }

        public abstract int getObjectReference();

        public Entry getObjectPackage() {
            return getUnrealPackage().objectReference(objectPackage);
        }

        public NameEntry getObjectName() {
            return getUnrealPackage().getNameTable().get(objectName);
        }

        public String getObjectFullName() {
            Entry pckg = getObjectPackage();
            if (pckg == null)
                return getObjectName().getName();
            else
                return pckg.toString() + '.' + getObjectName().getName();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry entry = (Entry) o;

            return (objectName == entry.objectName) && (objectPackage == entry.objectPackage);
        }

        public int hashCode() {
            return (objectPackage << 16) + objectName;
        }

        public String toString() {
            return getObjectFullName();
        }
    }

    public static final class ExportEntry extends Entry {
        private int objectClass;
        private int objectSuperClass;
        private int objectFlags;
        private int size;
        private int offset;

        private ExportEntry(UnrealPackageFile unrealPackage, int index, int objectClass, int objectSuperClass, int objectPackage, int objectName, int objectFlags, int size, int offset) {
            super(unrealPackage, index, objectPackage, objectName);
            this.objectClass = objectClass;
            this.objectSuperClass = objectSuperClass;
            this.objectFlags = objectFlags;
            this.size = size;
            this.offset = offset;
        }

        public int getObjectReference() {
            return getIndex() + 1;
        }

        public Entry getObjectClass() {
            return getUnrealPackage().objectReference(objectClass);
        }

        public Entry getObjectSuperClass() {
            return getUnrealPackage().objectReference(objectSuperClass);
        }

        public int getObjectFlags() {
            return objectFlags;
        }

        public int getSize() {
            return size;
        }

        public int getOffset() {
            return offset;
        }

        public byte[] getObjectRawData() throws IOException {
            if (getSize() == 0)
                return new byte[0];

            byte[] raw = new byte[size];
            getUnrealPackage().file.setPosition(offset);
            getUnrealPackage().file.readFully(raw);
            return raw;
        }

        public byte[] getObjectRawDataExternally() throws IOException {
            if (getSize() == 0)
                return new byte[0];

            try (RandomAccessFile raf = new RandomAccessFile(getUnrealPackage().file.getPath(), true)) {
                byte[] data = new byte[getSize()];
                raf.setPosition(getOffset());
                raf.readFully(data);
                return data;
            }
        }

        public void setObjectRawData(byte[] data) throws IOException {
            if (data.length <= getSize()) {
                getUnrealPackage().file.setPosition(getOffset());
                getUnrealPackage().file.write(data);
                if (data.length != getSize()) {
                    size = data.length;

                    getUnrealPackage().file.setPosition(getUnrealPackage().getExportTableOffset());
                    getUnrealPackage().writeExportTable(getUnrealPackage().getExportTable());
                }
            } else {
//                //clear
//                getUnrealPackage().file.setPosition(getOffset());
//                getUnrealPackage().file.write(new byte[getSize()]);

                getUnrealPackage().file.setPosition(getUnrealPackage().getDataEndOffset());
                offset = getUnrealPackage().file.getPosition();
                size = data.length;
                getUnrealPackage().file.write(data);

                int nameTablePosition = getUnrealPackage().file.getPosition();
                getUnrealPackage().writeNameTable(getUnrealPackage().getNameTable());
                int importTablePosition = getUnrealPackage().file.getPosition();
                getUnrealPackage().writeImportTable(getUnrealPackage().getImportTable());
                int exportTablePosition = getUnrealPackage().file.getPosition();
                getUnrealPackage().writeExportTable(getUnrealPackage().getExportTable());

                getUnrealPackage().file.setPosition(NAME_OFFSET_OFFSET);
                getUnrealPackage().file.writeInt(nameTablePosition);
                getUnrealPackage().file.setPosition(EXPORT_OFFSET_OFFSET);
                getUnrealPackage().file.writeInt(exportTablePosition);
                getUnrealPackage().file.setPosition(IMPORT_OFFSET_OFFSET);
                getUnrealPackage().file.writeInt(importTablePosition);
            }
        }

        @Override
        public String getObjectFullName() {
            String str = super.getObjectFullName();
            if (getObjectPackage() == null)
                str = getUnrealPackage().getPackageName() + "." + str;
            return str;
        }

        public String getObjectInnerFullName() {
            return super.getObjectFullName();
        }
    }

    public static final class ImportEntry extends Entry {
        private int classPackage;
        private int className;

        private ImportEntry(UnrealPackageFile unrealPackage, int index, int classPackage, int className, int objectPackage, int objectName) {
            super(unrealPackage, index, objectPackage, objectName);
            this.classPackage = classPackage;
            this.className = className;
        }

        public int getObjectReference() {
            return -(getIndex() + 1);
        }

        public NameEntry getClassPackage() {
            return getUnrealPackage().getNameTable().get(classPackage);
        }

        public NameEntry getClassName() {
            return getUnrealPackage().getNameTable().get(className);
        }

        public String getFullClassName() {
            NameEntry pckg = getClassPackage();
            if (pckg == null)
                return getClassName().getName();

            return pckg.getName() + '.' + getClassName().getName();
        }
    }
}
