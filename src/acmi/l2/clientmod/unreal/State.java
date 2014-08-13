package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.nio.ByteBuffer;

public class State extends Struct {
    public final long probeMask;
    public final long ignoreMask;
    public final int labelTableOffset;
    public final int stateFlags;

    public State(ByteBuffer buffer, UnrealPackageFile.ExportEntry up, PropertiesUtil propertiesUtil) {
        super(buffer, up, propertiesUtil);

        probeMask = buffer.getLong();
        ignoreMask = buffer.getLong();
        labelTableOffset = buffer.getShort() & 0xffff;
        stateFlags = buffer.getInt();
    }

    public enum STATE {
        /**
         * State should be user-selectable in UnrealEd.
         */
        Editable(0x00000001),
        /**
         * State is automatic (the default state).
         */
        Auto(0x00000002),
        /**
         * State executes on client side.
         */
        Simulated(0x00000004);

        private int mask;

        STATE(int mask) {
            this.mask = mask;
        }

        @Override
        public String toString() {
            return "STATE_" + name();
        }
    }
}
