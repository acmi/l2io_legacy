package acmi.l2.clientmod.unreal.Core;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.properties.PropertiesUtil;

import java.io.IOException;

public class TextBuffer extends Object {
    private int pos, top;
    private String text;

    public TextBuffer(DataInput input, UnrealPackageReadOnly.ExportEntry entry, PropertiesUtil propertiesUtil) throws IOException {
        super(input, entry, propertiesUtil);

        pos = input.readInt();
        top = input.readInt();
        text = input.readLine();
    }

    public TextBuffer(String text, int pos, int top) {
        this.pos = pos;
        this.top = top;
        this.text = text;
    }

    public TextBuffer(String text) {
        this(text, 0, 0);
    }

    public int getPos() {
        return pos;
    }

    public int getTop() {
        return top;
    }

    public String getText() {
        return text;
    }
}
