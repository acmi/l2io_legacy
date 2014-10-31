package acmi.l2.clientmod.unreal.Engine;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.Core.Object;
import acmi.l2.clientmod.unreal.properties.PropertiesUtil;

import java.io.IOException;

public class Material extends Object {
    public Material(DataInput input, UnrealPackageReadOnly.ExportEntry entry, PropertiesUtil propertiesUtil) throws IOException {
        super(input, entry, propertiesUtil);

        readUnk(input, entry.getUnrealPackage().getVersion(), entry.getUnrealPackage().getLicensee());
    }

    private void readUnk(DataInput obj, int version, int licensee) throws IOException {
        if (licensee <= 10) {
            //???
        } else if (licensee <= 28) {
            //c0-ct0
            obj.readInt();
        } else if (licensee <= 32) {
            //???
        } else if (licensee <= 35) {
            //ct1-ct22
            obj.skip(1067);
            for (int i = 0; i < 16; i++)
                obj.readLine();
            obj.readLine();
            obj.readInt();
        } else if (licensee == 36) {
            //ct22
            obj.skip(1058);
            for (int i = 0; i < 16; i++)
                obj.readLine();
            obj.readLine();
            obj.readInt();
        } else if (licensee <= 39) {
            //Epeisodion
            if (version == 129) {
                obj.skip(92);
                int stringCount = obj.readCompactInt();
                for (int i = 0; i < stringCount; i++) {
                    obj.readLine();
                    int addStringCount = obj.readUnsignedByte();
                    for (int j = 0; j < addStringCount; j++)
                        obj.readLine();
                }
                obj.readLine();
                obj.readInt();
                return;
            }

            //ct23-Lindvior
            obj.skip(36);
            int stringCount = obj.readCompactInt();
            for (int i = 0; i < stringCount; i++) {
                obj.readLine();
                int addStringCount = obj.readUnsignedByte();
                for (int j = 0; j < addStringCount; j++)
                    obj.readLine();
            }
            obj.readLine();
            obj.readInt();
        } else {
            //Ertheia+
            obj.skip(92);
            int stringCount = obj.readCompactInt();
            for (int i = 0; i < stringCount; i++) {
                obj.readLine();
                int addStringCount = obj.readUnsignedByte();
                for (int j = 0; j < addStringCount; j++)
                    obj.readLine();
            }
            obj.readLine();
            obj.readInt();
        }
    }
}
