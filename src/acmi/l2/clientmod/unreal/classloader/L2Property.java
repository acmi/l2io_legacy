/*
 * Copyright (c) 2015 acmi
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
package acmi.l2.clientmod.unreal.classloader;

import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.core.*;
import acmi.l2.clientmod.unreal.core.Enum;

import java.lang.Object;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface L2Property {
    Property getTemplate();

    default String getName(){
        return getTemplate().getEntry().getObjectName().getName();
    }

    int getSize();

    Object getAt(int index);

    void putAt(int index, Object value);

    default String toString(UnrealPackageReadOnly up, UnrealClassLoader classLoader) {
        return getName() + "=" + (getSize() == 1 ? toString(getAt(0), getTemplate(), up, classLoader) : IntStream.range(0, getSize())
                .mapToObj(this::getAt)
                .map(o -> toString(o, getTemplate(), up, classLoader))
                .collect(Collectors.joining(",", "(", ")")));
    }

    static String toString(Object val, Property template, UnrealPackageReadOnly up, UnrealClassLoader classLoader){
        if (val == null)
            return "null";

        if (template instanceof ByteProperty){
            ByteProperty byteProperty = (ByteProperty) template;
            if (byteProperty.getEnumType() == null) {
                return val.toString();
            } else {
                Enum en = (Enum) classLoader.loadField(classLoader.getExportEntry(byteProperty.getEnumType().getObjectFullName(), e -> e.getObjectClass().getObjectFullName().equals("Core.Enum"))
                        .orElseThrow(() -> new IllegalStateException(String.format("Enum %s not found", byteProperty.getEnumType()))));
                return en.getValues().get((Integer) val);
            }
        }else if (template instanceof IntProperty ||
                template instanceof BoolProperty ||
                template instanceof FloatProperty) {
            return val.toString();
        }else if (template instanceof ObjectProperty){
            UnrealPackageReadOnly.Entry ref = up.objectReference((Integer) val);
            if (ref == null) {
                return "null";
            } else {
                String cl;
                if (ref instanceof UnrealPackageReadOnly.ImportEntry)
                    cl = ((UnrealPackageReadOnly.ImportEntry) ref).getClassName().getName();
                else if (((UnrealPackageReadOnly.ExportEntry) ref).getObjectClass() != null)
                    cl = ((UnrealPackageReadOnly.ExportEntry) ref).getObjectClass().getObjectName().getName();
                else
                    cl = "Class";

                return cl+"'"+ref+"'";
            }
        }else if (template instanceof NameProperty){
            return "'"+up.nameReference((Integer) val)+"'";
        }else if (template instanceof ArrayProperty){
            throw new UnsupportedOperationException("array");
        }else if (template instanceof StructProperty){
            return ((List<L2Property>)val).stream()
                    .map(Objects::toString)
                    .collect(Collectors.joining(",", "(", ")"));
        }else if (template instanceof StrProperty){
            return "\""+val.toString()+"\"";
        }else {
            throw new UnsupportedOperationException("wtf");
        }
    }
}
