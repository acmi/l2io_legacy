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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface UnrealPackageConstants {
    public static final int UNREAL_PACKAGE_MAGIC = 0x9E2A83C1;

    public static final int VERSION_OFFSET = 4;
    public static final int LICENSEE_OFFSET = 6;
    public static final int PACKAGE_FLAGS_OFFSET = 8;
    public static final int NAME_COUNT_OFFSET = 12;
    public static final int NAME_OFFSET_OFFSET = 16;
    public static final int EXPORT_COUNT_OFFSET = 20;
    public static final int EXPORT_OFFSET_OFFSET = 24;
    public static final int IMPORT_COUNT_OFFSET = 28;
    public static final int IMPORT_OFFSET_OFFSET = 32;
    public static final int GUID_OFFSET = 36;
    public static final int GENERATIONS_OFFSET = 52;

    public static final int PACKAGE_FLAGS = ObjectFlag.getFlags(
            ObjectFlag.Public,
            ObjectFlag.LoadForClient,
            ObjectFlag.LoadForServer,
            ObjectFlag.LoadForEdit);

    public enum PackageFlag{
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
            return "PKG_"+name();
        }

        public static List<PackageFlag> getFlags(int flags){
            return Arrays.stream(values())
                    .filter(e -> (e.getMask() & flags) != 0)
                    .collect(Collectors.toList());
        }

        public static int getFlags(PackageFlag ... flags){
            int v = 0;
            for (PackageFlag flag : flags)
                v |= flag.getMask();
            return v;
        }
    }

    public enum ObjectFlag{
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
            return "RF_"+name();
        }

        public static List<ObjectFlag> getFlags(int flags){
            return Arrays.stream(values())
                    .filter(e -> (e.getMask() & flags) != 0)
                    .collect(Collectors.toList());
        }

        public static int getFlags(ObjectFlag ... flags){
            int v = 0;
            for (ObjectFlag flag : flags)
                v |= flag.getMask();
            return v;
        }
    }
}
