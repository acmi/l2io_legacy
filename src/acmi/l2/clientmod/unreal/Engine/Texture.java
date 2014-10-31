package acmi.l2.clientmod.unreal.Engine;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.DataOutput;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.properties.PropertiesUtil;

import java.io.IOException;

import static acmi.l2.clientmod.io.ByteUtil.compactIntToByteArray;

public class Texture extends BitmapMaterial {
    private final MipMapData[] mipMaps;

    public Texture(DataInput input, UnrealPackageReadOnly.ExportEntry entry, PropertiesUtil propertiesUtil) throws IOException {
        super(input, entry, propertiesUtil);

        mipMaps = new MipMapData[input.readCompactInt()];
        for (int i = 0; i < mipMaps.length; i++) {
            input.readInt();
            mipMaps[i] = new MipMapData(
                    input.readByteArray(),
                    input.readInt(),
                    input.readInt(),
                    input.readUnsignedByte(),
                    input.readUnsignedByte());
        }
    }

    @Override
    public void writeTo(DataOutput output, PropertiesUtil propertiesUtil) throws IOException {
        super.writeTo(output, propertiesUtil);

        output.writeCompactInt(mipMaps.length);
        for (MipMapData mipMapData : mipMaps) {
            byte[] len = compactIntToByteArray(mipMapData.data.length);
            output.writeInt(output.getPosition() + 4 + len.length + mipMapData.data.length);
            output.write(len);
            output.write(mipMapData.data);
            output.writeInt(mipMapData.width);
            output.writeInt(mipMapData.height);
            output.writeByte(mipMapData.widthBits);
            output.writeByte(mipMapData.heightBits);

        }
    }

    public MipMapData[] getMipMaps() {
        return mipMaps;
    }

    public static class MipMapData {
        private byte[] data;
        private int width, height;
        private int widthBits, heightBits;

        public MipMapData() {
        }

        public MipMapData(byte[] data, int width, int height, int widthBits, int heightBits) {
            this.data = data;
            this.width = width;
            this.height = height;
            this.widthBits = widthBits;
            this.heightBits = heightBits;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getWidthBits() {
            return widthBits;
        }

        public void setWidthBits(int widthBits) {
            this.widthBits = widthBits;
        }

        public int getHeightBits() {
            return heightBits;
        }

        public void setHeightBits(int heightBits) {
            this.heightBits = heightBits;
        }
    }
}
