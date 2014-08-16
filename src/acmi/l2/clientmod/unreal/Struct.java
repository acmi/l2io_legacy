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
package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.nio.ByteBuffer;

import static acmi.l2.clientmod.util.BufferUtil.getCompactInt;

public class Struct extends Field {
    public final int scriptText; //Object Reference.
    public final int child; //First object inside the struct. Object Reference.
    public final int friendlyName; //Name of the struct. Name Reference.
    public final int line;
    public final int textPos;
    public final int scriptSize;

    public Struct(ByteBuffer buffer, UnrealPackageFile.ExportEntry up, PropertiesUtil propertiesUtil) {
        super(buffer, up, propertiesUtil);

        scriptText = getCompactInt(buffer);
        child = getCompactInt(buffer);
        friendlyName = getCompactInt(buffer);
        getCompactInt(buffer);
        line = buffer.getInt();
        textPos = buffer.getInt();
        scriptSize = buffer.getInt();
        if (scriptSize != 0){
            //todo code statements
        }
    }

    public UnrealPackageFile.Entry getScritpText() {
        return getEntry().getUnrealPackage().objectReference(scriptText);
    }

    public UnrealPackageFile.Entry getChild() {
        return getEntry().getUnrealPackage().objectReference(child);
    }

    public String getFriendlyName() {
        return getEntry().getUnrealPackage().getNameTable().get(friendlyName).getName();
    }

    @Override
    public String toString() {
        return getEntry() + ": " + getClass().getSimpleName() + "[" + getFriendlyName() + ']';
    }
}
