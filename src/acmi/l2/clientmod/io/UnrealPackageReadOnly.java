package acmi.l2.clientmod.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static acmi.l2.clientmod.util.CollectionsMethods.indexIf;

public interface UnrealPackageReadOnly {
    Charset getCharset();

    int getVersion();

    int getLicensee();

    int getFlags();

    List<? extends NameEntry> getNameTable();

    List<? extends ExportEntry> getExportTable();

    List<? extends ImportEntry> getImportTable();

    default int nameReference(String name) {
        return indexIf(getNameTable(), e -> e.getName().equalsIgnoreCase(name));
    }

    default String nameReference(int ref) {
        return getNameTable().get(ref).getName();
    }

    default Entry objectReference(int ref) {
        if (ref > 0)
            return getExportTable().get(ref - 1);
        else if (ref < 0)
            return getImportTable().get(-ref - 1);
        else
            return null;
    }

    public interface NameEntry {
        String getName();

        int getFlags();
    }

    public interface Entry {
        UnrealPackageReadOnly getUnrealPackage();

        int getObjectReference();

        Entry getObjectPackage();

        NameEntry getObjectName();

        default String getObjectFullName() {
            return getObjectInnerFullName();
        }

        String getObjectInnerFullName();
    }

    public interface ImportEntry extends Entry {
        NameEntry getClassPackage();

        NameEntry getClassName();
    }

    public interface ExportEntry extends Entry {
        Entry getObjectClass();

        Entry getObjectSuperClass();

        int getObjectFlags();

        int getSize();

        int getOffset();

        byte[] getObjectRawDataExternally() throws IOException;
    }

    public enum ObjectFlag {
        /**
         * Object is transactional.
         */
        Transactional(0x00000001),
        /**
         * Object is not reachable on the object graph.
         */
        Unreachable(0x00000002),
        /**
         * Object is visible outside its package.
         */
        Public(0x00000004),
        /**
         * Temporary import tag in load/save.
         */
        TagImp(0x00000008),
        /**
         * Temporary export tag in load/save.
         */
        TagExp(0x00000010),
        /**
         * Modified relative to source files.
         */
        SourceModified(0x00000020),
        /**
         * Check during garbage collection.
         */
        TagGarbage(0x00000040),
        /**
         * During load, indicates object needs loading.
         */
        NeedLoad(0x00000200),
        /**
         * A hardcoded name which should be syntaxhighlighted.
         */
        HighlightedName(0x00000400),
        /**
         * In a singular function.
         */
        InSingularFunc(0x00000800),
        /**
         * Suppressed log name.
         */
        Suppress(0x00001000),
        /**
         * Within an EndState call.
         */
        InEndState(0x00002000),
        /**
         * Don't save object.
         */
        Transient(0x00004000),
        /**
         * Data is being preloaded from file.
         */
        PreLoading(0x00008000),
        /**
         * In-file load for client.
         */
        LoadForClient(0x00010000),
        /**
         * In-file load for client.
         */
        LoadForServer(0x00020000),
        /**
         * In-file load for client.
         */
        LoadForEdit(0x00040000),
        /**
         * Keep object around for editing even if unreferenced.
         */
        Standalone(0x00080000),
        /**
         * Don't load this object for the game client.
         */
        NotForClient(0x00100000),
        /**
         * Don't load this object for the game server.
         */
        NotForServer(0x00200000),
        /**
         * Don't load this object for the editor.
         */
        NotForEdit(0x00400000),
        /**
         * Object Destroy has already been called.
         */
        Destroyed(0x00800000),
        /**
         * Object needs to be postloaded.
         */
        NeedPostLoad(0x01000000),
        /**
         * Has execution stack.
         */
        HasStack(0x02000000),
        /**
         * Native (UClass only).
         */
        Native(0x04000000),
        /**
         * Marked (for debugging).
         */
        Marked(0x08000000),
        /**
         * ShutdownAfterError called.
         */
        ErrorShutdown(0x10000000),
        /**
         * For debugging Serialize calls.
         */
        DebugPostLoad(0x20000000),
        /**
         * For debugging Serialize calls.
         */
        DebugSerialize(0x40000000),
        /**
         * For debugging Destroy calls.
         */
        DebugDestroy(0x80000000);

        private int mask;

        ObjectFlag(int mask) {
            this.mask = mask;
        }

        public int getMask() {
            return mask;
        }

        @Override
        public String toString() {
            return "RF_" + name();
        }

        public static List<ObjectFlag> getFlags(int flags) {
            return Arrays.stream(values())
                    .filter(e -> (e.getMask() & flags) != 0)
                    .collect(Collectors.toList());
        }

        public static int getFlags(ObjectFlag... flags) {
            int v = 0;
            for (ObjectFlag flag : flags)
                v |= flag.getMask();
            return v;
        }
    }

    public static final int PACKAGE_FLAGS = ObjectFlag.getFlags(
            ObjectFlag.Public,
            ObjectFlag.LoadForClient,
            ObjectFlag.LoadForServer,
            ObjectFlag.LoadForEdit);

    public enum PackageFlag {
        /**
         * Allow downloading package
         */
        AllowDownload(0x0001),
        /**
         * Purely optional for clients
         */
        ClientOptional(0x0002),
        /**
         * Only needed on the server side
         */
        ServerSideOnly(0x0004),
        /**
         * Loaded from linker with broken import links
         */
        BrokenLinks(0x0008),
        /**
         * Not trusted
         */
        Unsecure(0x0010),
        /**
         * Client needs to download this package
         */
        Need(0x8000);

        private int mask;

        PackageFlag(int mask) {
            this.mask = mask;
        }

        public int getMask() {
            return mask;
        }

        @Override
        public String toString() {
            return "PKG_" + name();
        }

        public static List<PackageFlag> getFlags(int flags) {
            return Arrays.stream(values())
                    .filter(e -> (e.getMask() & flags) != 0)
                    .collect(Collectors.toList());
        }

        public static int getFlags(PackageFlag... flags) {
            int v = 0;
            for (PackageFlag flag : flags)
                v |= flag.getMask();
            return v;
        }
    }
}
