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

import acmi.l2.clientmod.io.*;
import acmi.l2.clientmod.unreal.UnrealException;
import acmi.l2.clientmod.unreal.core.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.Class;
import java.lang.Object;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 * Entry properties:
 * <p>
 * if (entry.getObjectClass() == null) {
 * UClass uClass = new UClass(buffer, entry, this);
 * uClass.readProperties();
 * return uClass.getProperties();
 * }
 * if (getFlags(entry.getObjectFlags()).contains(HasStack)) {
 * getCompactInt(buffer);
 * getCompactInt(buffer);
 * buffer.position(buffer.position() + 12);
 * getCompactInt(buffer);
 * }
 */
public class PropertiesUtil {
    private UnrealClassLoader unrealClassLoader;

    PropertiesUtil(UnrealClassLoader unrealClassLoader) {
        this.unrealClassLoader = unrealClassLoader;
    }

    public UnrealClassLoader getUnrealClassLoader() {
        return unrealClassLoader;
    }

    public List<L2Property> readProperties(DataInput dataInput, String objClass, UnrealPackageReadOnly up) throws UnrealException {
        List<L2Property> properties = new ArrayList<>();

        List<Property> classTemplate = unrealClassLoader.getStructProperties(objClass);

        try {
            String name;
            while (!(name = up.getNameTable().get(dataInput.readCompactInt()).getName()).equals("None")) {
                int info = dataInput.readUnsignedByte();
                int propertyType = info & 0b1111;
                int sizeType = (info >> 4) & 0b111;
                boolean array = info >> 7 == 1;

                String structName = propertyType == STRUCT ?
                        up.getNameTable().get(dataInput.readCompactInt()).getName() : null;
                int size = readPropertySize(sizeType, dataInput);
                int arrayIndex = array && propertyType != BOOL ? dataInput.readCompactInt() : 0;

                byte[] objBytes = new byte[size];
                dataInput.readFully(objBytes);

                final String n = name;
                Predicate<Property> pr = pt -> pt.getEntry().getObjectName().getName().equalsIgnoreCase(n) &&
                        propertyType == getTypeOfProperty(pt.getClass());
                L2Property property = properties.stream()
                        .filter(p -> pr.test(p.getTemplate()))
                        .findAny()
                        .orElse(null);
                if (property == null) {
                    Property template = classTemplate.stream()
                            .filter(pr::test)
                            .findAny()
                            .orElseThrow(() -> new UnrealException(objClass + ": Property template not found: " + n));

                    property = new L2Property(template, up);
                    properties.add(property);
                }

                if (structName != null &&
                        !"Vector".equals(structName) &&
                        !"Rotator".equals(structName) &&
                        !"Color".equals(structName)) {
                    StructProperty structProperty = (StructProperty) property.getTemplate();
                    structName = structProperty.getStructType().getObjectFullName();
                }
                Property arrayInner = property.getTemplate() instanceof ArrayProperty ?
                        unrealClassLoader.getProperty(((ArrayProperty) property.getTemplate()).getInner().getObjectFullName()) :
                        null;

                DataInput objBuffer = new DataInputStream(new ByteArrayInputStream(objBytes), dataInput.getCharset());
                property.putAt(arrayIndex, read(objBuffer, propertyType, array, arrayInner, structName, up));
            }
        } catch (IOException e) {
            throw new UnrealException(e);
        }

        return properties;
    }

    private static int readPropertySize(int sizeType, DataInput dataInput) throws IOException {
        switch (sizeType) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 4;
            case 3:
                return 12;
            case 4:
                return 16;
            case 5:
                return dataInput.readUnsignedByte();
            case 6:
                return dataInput.readUnsignedShort();
            case 7:
                return dataInput.readInt();
            default:
                throw new IllegalArgumentException();
        }
    }

    private Object read(DataInput objBuffer, int propertyType, boolean array, Property arrayInner, String structName, UnrealPackageReadOnly up) throws IOException {
        switch (propertyType) {
            case NONE:
                return null;
            case BYTE:
                return objBuffer.readUnsignedByte();
            case INT:
                return objBuffer.readInt();
            case BOOL:
                return array;
            case FLOAT:
                return objBuffer.readFloat();
            case OBJECT:
                return objBuffer.readCompactInt();
            case NAME:
                return objBuffer.readCompactInt();
            case ARRAY:
                int arraySize = objBuffer.readCompactInt();
                List<Object> arrayList = new ArrayList<>(arraySize);

                String a = arrayInner.getEntry().getObjectFullName();
                propertyType = getTypeOfProperty(arrayInner.getClass());
                structName = propertyType == STRUCT ? Optional
                        .ofNullable(((StructProperty) arrayInner).getStructType())
                        .orElseThrow(() -> new UnrealException(a + ": null struct ref")).getObjectFullName() : null;
                array = propertyType == ARRAY;
                arrayInner = propertyType == ARRAY ? unrealClassLoader.getProperty(((ArrayProperty) arrayInner).getInner().getObjectFullName()) : null;

                for (int i = 0; i < arraySize; i++) {
                    arrayList.add(read(objBuffer, propertyType, array, arrayInner, structName, up));
                }
                return arrayList;
            case STRUCT:
                return readStruct(objBuffer, structName, up);
            case STR:
                return objBuffer.readLine();
            default:
                throw new IllegalStateException("Unk type(" + structName + "): " + propertyType);
        }
    }

    private List<L2Property> readStruct(DataInput objBuffer, String structName, UnrealPackageReadOnly up) throws IOException {
        switch (structName) {
            case "Vector":
                return readStructBin(objBuffer, "Core.Object.Vector", up);
            case "Rotator":
                return readStructBin(objBuffer, "Core.Object.Rotator", up);
            case "Color":
                return readStructBin(objBuffer, "Core.Object.Color", up);
            default:
                return readProperties(objBuffer, structName, up);
        }
    }

    public List<L2Property> readStructBin(DataInput objBuffer, String structName, UnrealPackageReadOnly up) throws UnrealException {
        List<Property> properties = unrealClassLoader.getStructProperties(structName);

        try {
            switch (structName) {
                case "Core.Object.Vector": {
                    L2Property x = new L2Property(properties.get(0), up);
                    x.putAt(0, objBuffer.readFloat());
                    L2Property y = new L2Property(properties.get(1), up);
                    y.putAt(0, objBuffer.readFloat());
                    L2Property z = new L2Property(properties.get(2), up);
                    z.putAt(0, objBuffer.readFloat());
                    return Arrays.asList(x, y, z);
                }
                case "Core.Object.Rotator": {
                    L2Property pitch = new L2Property(properties.get(0), up);
                    pitch.putAt(0, objBuffer.readInt());
                    L2Property yaw = new L2Property(properties.get(1), up);
                    yaw.putAt(0, objBuffer.readInt());
                    L2Property roll = new L2Property(properties.get(2), up);
                    roll.putAt(0, objBuffer.readInt());
                    return Arrays.asList(pitch, yaw, roll);
                }
                case "Core.Object.Color": {
                    L2Property b = new L2Property(properties.get(0), up);
                    b.putAt(0, objBuffer.readUnsignedByte());
                    L2Property g = new L2Property(properties.get(1), up);
                    g.putAt(0, objBuffer.readUnsignedByte());
                    L2Property r = new L2Property(properties.get(2), up);
                    r.putAt(0, objBuffer.readUnsignedByte());
                    L2Property a = new L2Property(properties.get(3), up);
                    a.putAt(0, objBuffer.readUnsignedByte());
                    return Arrays.asList(b, g, r, a);
                }
                case "Fire.FireTexture.Spark": {
                    L2Property type = new L2Property(properties.get(0), up);
                    type.putAt(0, objBuffer.readUnsignedByte());
                    L2Property heat = new L2Property(properties.get(1), up);
                    heat.putAt(0, objBuffer.readUnsignedByte());
                    L2Property x = new L2Property(properties.get(2), up);
                    x.putAt(0, objBuffer.readUnsignedByte());
                    L2Property y = new L2Property(properties.get(3), up);
                    y.putAt(0, objBuffer.readUnsignedByte());
                    L2Property byteA = new L2Property(properties.get(4), up);
                    byteA.putAt(0, objBuffer.readUnsignedByte());
                    L2Property byteB = new L2Property(properties.get(5), up);
                    byteB.putAt(0, objBuffer.readUnsignedByte());
                    L2Property byteC = new L2Property(properties.get(6), up);
                    byteC.putAt(0, objBuffer.readUnsignedByte());
                    L2Property byteD = new L2Property(properties.get(7), up);
                    byteD.putAt(0, objBuffer.readUnsignedByte());
                    return Arrays.asList(type, heat, x, y, byteA, byteB, byteC, byteD);
                }
                default:
                    throw new UnsupportedOperationException("Not implemented"); //TODO
            }
        } catch (IOException e) {
            throw new UnrealException(e);
        }
    }

    public void writeProperties(DataOutput buffer, List<L2Property> list, UnrealPackageReadOnly up) throws UnrealException {
        try {
            for (L2Property property : list) {
                Property template = property.getTemplate();

                for (int i = 0; i < property.getSize(); i++) {
                    Object obj = property.getAt(i);
                    if (obj == null)
                        continue;

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    DataOutput objBuffer = new DataOutputStream(baos, buffer.getCharset());
                    write(objBuffer, template, obj, up);
                    byte[] bytes = baos.toByteArray();

                    int type = getTypeOfProperty(template.getClass());
                    int size = getPropertySize(bytes.length);
                    boolean array = (i > 0) || (type == ARRAY && ((Boolean)obj));
                    int info = (array ? 1 << 7 : 0) | (size << 4) | type;

                    buffer.writeCompactInt(up.nameReference(template.getEntry().getObjectName().getName()));
                    buffer.writeByte(info);
                    if (type == STRUCT)
                        buffer.writeCompactInt(up.nameReference(((StructProperty)template).getStructType().getObjectName().getName()));
                    switch (size) {
                        case 5:
                            buffer.writeByte(bytes.length);
                            break;
                        case 6:
                            buffer.writeShort(bytes.length);
                            break;
                        case 7:
                            buffer.writeInt(bytes.length);
                            break;
                    }
                    if (i > 0)
                        buffer.writeByte(i);
                    buffer.write(bytes);
                }
            }
            buffer.writeCompactInt(up.nameReference("None"));
        } catch (IOException e) {
            throw new UnrealException(e);
        }
    }

    private void write(DataOutput objBuffer, Property template, Object obj, UnrealPackageReadOnly up) throws IOException {
        if (template instanceof ByteProperty) {
            objBuffer.writeByte((Integer) obj);
        } else if (template instanceof IntProperty) {
            objBuffer.writeInt((Integer) obj);
        } else if (template instanceof FloatProperty) {
            objBuffer.writeFloat((Float) obj);
        } else if (template instanceof ObjectProperty) {
            objBuffer.writeCompactInt((Integer) obj);
        } else if (template instanceof NameProperty) {
            objBuffer.writeCompactInt((Integer) obj);
        } else if (template instanceof ArrayProperty) {
            ArrayProperty arrayProperty = (ArrayProperty) template;

            List<Object> arrayList = (List<Object>) obj;
            objBuffer.writeCompactInt(arrayList.size());

            UnrealPackageReadOnly.ExportEntry arrayInner = (UnrealPackageReadOnly.ExportEntry) arrayProperty.getInner();
            String a = arrayInner.getObjectClass().getObjectName().getName();
            try {
                Class<? extends Property> pc = Class.forName("acmi.l2.clientmod.unreal." + a).asSubclass(Property.class);
                Property f = pc.getConstructor(ByteBuffer.class, UnrealPackageReadOnly.ExportEntry.class, PropertiesUtil.class)
                        .newInstance(ByteBuffer.wrap(arrayInner.getObjectRawDataExternally()).order(ByteOrder.LITTLE_ENDIAN), arrayInner, this);

                for (Object arrayObj : arrayList) {
                    write(objBuffer, f, arrayObj, up);
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        } else if (template instanceof StructProperty) {
            StructProperty structProperty = (StructProperty) template;
            writeStruct(objBuffer, structProperty.getStructType().getObjectName().getName(), up, (List<L2Property>) obj);
        } else if (template instanceof StrProperty) {
            objBuffer.writeLine((String) obj);
        } else {
            throw new UnsupportedOperationException(template.getClass().getSimpleName() + " serialization not implemented");
        }
    }

    private void writeStruct(DataOutput objBuffer, String structName, UnrealPackageReadOnly up, List<L2Property> struct) throws IOException {
        switch (structName) {
            case "Color":
                writeStructBin(objBuffer, struct, "Core.Object.Color", up);
                break;
            case "Vector":
                writeStructBin(objBuffer, struct, "Core.Object.Vector", up);
                break;
            case "Rotator":
                writeStructBin(objBuffer, struct, "Core.Object.Rotator", up);
                break;
            default:
                writeProperties(objBuffer, struct, up);
        }
    }

    public void writeStructBin(DataOutput objBuffer, List<L2Property> struct, String structName, UnrealPackageReadOnly up) throws UnrealException {
        try {
            switch (structName) {
                case "Core.Object.Color":
                    for (int i = 0; i < 4; i++)
                        objBuffer.writeByte((Integer) struct.get(i).getAt(0));
                    break;
                case "Core.Object.Vector":
                    for (int i = 0; i < 3; i++)
                        objBuffer.writeFloat((Float) struct.get(i).getAt(0));
                    break;
                case "Core.Object.Rotator":
                    for (int i = 0; i < 3; i++)
                        objBuffer.writeInt((Integer) struct.get(i).getAt(0));
                    break;
                case "Fire.FireTexture.Spark":
                    for (int i = 0; i < 8; i++)
                        objBuffer.writeByte((Integer) struct.get(i).getAt(0));
                    break;
                default:
                    throw new UnsupportedOperationException("not implemented"); //TODO
            }
        } catch (IOException e) {
            throw new UnrealException(e);
        }
    }

    private static int getPropertySize(int size) {
        switch (size) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 4:
                return 2;
            case 12:
                return 3;
            case 16:
                return 4;
            default:
                if (size <= 0xff) {
                    return 5;
                } else if (size <= 0xffff) {
                    return 6;
                } else {
                    return 7;
                }
        }
    }

    /*private enum Type {
        NONE,
        BYTE,
        INT,
        BOOL,
        FLOAT,
        OBJECT,
        NAME,
        STRING,
        CLASS,
        ARRAY,
        STRUCT,
        VECTOR,
        ROTATOR,
        STR,
        MAP,
        FIXED_ARRAY;

        public static Type ofPropertyClass(Class<? extends Property> pClass) {
            if (pClass == DelegateProperty.class) {
                return Type.NONE;
            } else if (pClass == PtrProperty.class) {
                return Type.NONE;
            } else
                return Type.valueOf(pClass.getSimpleName().replace("Property", "").toUpperCase());
        }
    }*/

    public static L2Property findProperty(List<L2Property> properties, Predicate<L2Property> predicate) {
        return properties.stream()
                .filter(predicate::test)
                .findFirst()
                .orElse(null);
    }

    private static final int NONE = 0;
    private static final int BYTE = 1;
    private static final int INT = 2;
    private static final int BOOL = 3;
    private static final int FLOAT = 4;
    private static final int OBJECT = 5;
    private static final int NAME = 6;
    private static final int STRING = 7;
    private static final int CLASS = 8;
    private static final int ARRAY = 9;
    private static final int STRUCT = 0xa;
    private static final int VECTOR = 0xb;
    private static final int ROTATOR = 0xc;
    private static final int STR = 0xd;
    private static final int MAP = 0xe;
    private static final int FIXED_ARRAY = 0xf;

    private static int getTypeOfProperty(Class<? extends Property> pClass){
        Objects.requireNonNull(pClass);

        if (pClass == ByteProperty.class)
            return BYTE;
        else if (pClass == IntProperty.class)
            return INT;
        else if (pClass == BoolProperty.class)
            return BOOL;
        else if (pClass == FloatProperty.class)
            return FLOAT;
        else if (pClass == ObjectProperty.class)
            return OBJECT;
        else if (pClass == NameProperty.class)
            return NAME;
        else if (pClass == ClassProperty.class)
            return OBJECT;
        else if (pClass == ArrayProperty.class)
            return ARRAY;
        else if (pClass == StructProperty.class)
            return STRUCT;
        else if (pClass == StrProperty.class)
            return STR;
        else
            throw new UnsupportedOperationException("Type of "+pClass.getSimpleName()+" is unknown");
    }
}
