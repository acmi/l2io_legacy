package acmi.l2.clientmod.unreal.Engine;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.DataOutput;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.properties.PropertiesUtil;

import java.io.IOException;

public class Font extends acmi.l2.clientmod.unreal.Core.Object {
    private CharData[] charData;
    private UnrealPackageReadOnly.Entry[] textures;

    public Font(DataInput input, UnrealPackageReadOnly.ExportEntry entry, PropertiesUtil propertiesUtil) throws IOException {
        super(input, entry, propertiesUtil);

        charData = new CharData[input.readCompactInt()];
        for (int i = 0; i < charData.length; i++)
            charData[i] = new CharData(
                    input.readInt(),
                    input.readInt(),
                    input.readInt(),
                    input.readInt(),
                    input.readCompactInt());
        textures = new UnrealPackageReadOnly.Entry[input.readCompactInt()];
        for (int i = 0; i < textures.length; i++)
            textures[i] = entry.getUnrealPackage().objectReference(input.readCompactInt());
        input.skip(9);
    }

    @Override
    public void writeTo(DataOutput output, PropertiesUtil propertiesUtil) throws IOException {
        super.writeTo(output, propertiesUtil);

        output.writeCompactInt(charData.length);
        for (CharData charDataE : charData) {
            output.writeInt(charDataE.x);
            output.writeInt(charDataE.y);
            output.writeInt(charDataE.width);
            output.writeInt(charDataE.height);
            output.writeCompactInt(charDataE.textureInd);
        }
        output.writeCompactInt(textures.length);
        for (UnrealPackageReadOnly.Entry texture : textures)
            output.writeCompactInt(texture.getObjectReference());
        output.write(new byte[9]);
    }

    public CharData[] getCharData() {
        return charData;
    }

    public UnrealPackageReadOnly.Entry[] getTextures() {
        return textures;
    }

    public static class CharData {
        private int x, y, width, height;
        private int textureInd;

        public CharData(int x, int y, int width, int height, int textureInd) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.textureInd = textureInd;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
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

        public int getTextureInd() {
            return textureInd;
        }

        public void setTextureInd(int textureInd) {
            this.textureInd = textureInd;
        }
    }
}
