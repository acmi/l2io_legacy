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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static acmi.l2.clientmod.io.UnrealPackageConstants.ObjectFlag.HasStack;
import static acmi.l2.clientmod.io.UnrealPackageConstants.ObjectFlag.getFlags;
import static acmi.l2.clientmod.util.BufferUtil.*;

@SuppressWarnings("unchecked")
public class PropertiesUtil {
    private ClassHelper classHelper;

    PropertiesUtil(ClassHelper classHelper) {
        this.classHelper = classHelper;
    }

    @Deprecated
    public List<L2Property> readProperties(UnrealPackageFile.ExportEntry entry) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(entry.getObjectRawDataExternally())
                .order(ByteOrder.LITTLE_ENDIAN);

        if (getFlags(entry.getObjectFlags()).contains(HasStack)) {
            getCompactInt(buffer);
            getCompactInt(buffer);
            buffer.position(buffer.position() + 12);
            getCompactInt(buffer);
        }

        return readProperties(buffer, entry.getUnrealPackage(), entry.getObjectClass().getObjectFullName());
    }

    public List<L2Property> readProperties(ByteBuffer buffer, UnrealPackageFile up, String objClass) {
        List<L2Property> properties = new ArrayList<>();

        List<Property> classTemplate = classHelper.getStruct(objClass)
                .orElse(new ArrayList<>())
                .stream()
                .filter(field -> field instanceof Property)
                .map(field -> (Property) field)
                .collect(Collectors.toList());


        String name;
        while (!(name = up.getNameTable().get(getCompactInt(buffer)).getName()).equals("None")) {
            int info = buffer.get() & 0xff;
            Type propertyType = Type.values()[info & 0b1111];
            int sizeType = (info >> 4) & 0b111;
            boolean array = info >> 7 == 1;

            String structName = propertyType.equals(Type.STRUCT) ?
                    up.getNameTable().get(getCompactInt(buffer)).getName() : null;
            int size = readPropertySize(sizeType, buffer);
            int arrayIndex = array && !propertyType.equals(Type.BOOL) ? getCompactInt(buffer) : 0;

            byte[] objBytes = new byte[size];
            buffer.get(objBytes);

            final String n = name;
            L2Property property = properties.stream()
                    .filter(p -> p.getTemplate().getEntry().getObjectName().getName().equalsIgnoreCase((n)))
                    .findAny()
                    .orElse(null);
            if (property == null) {
                Property template = classTemplate.stream()
                        .filter(pt -> pt.getEntry().getObjectName().getName().equalsIgnoreCase((n)))
                        .findAny()
                        .orElse(null);
                if (template == null)
                    throw new IllegalStateException(objClass + ": Property template not found: " + name);

                property = new L2Property(template);
                properties.add(property);
            }

            if (structName != null &&
                    !"Vector".equals(structName) &&
                    !"Rotator".equals(structName) &&
                    !"Color".equals(structName)) {
                StructProperty structProperty = (StructProperty) property.getTemplate();
                structName = structProperty.getStructType().getObjectFullName();
            }
            UnrealPackageFile.ExportEntry arrayInner = null;
            if (propertyType.equals(Type.ARRAY)) {
                ArrayProperty arrayProperty = (ArrayProperty) property.getTemplate();
                arrayInner = (UnrealPackageFile.ExportEntry) arrayProperty.getInner();
            }

            ByteBuffer objBuffer = ByteBuffer.wrap(objBytes).order(ByteOrder.LITTLE_ENDIAN);
            property.setAt(arrayIndex, read(objBuffer, propertyType, array, arrayInner, structName, up));
        }

        return properties;
    }

    private static int readPropertySize(int sizeType, ByteBuffer buffer) {
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
                return buffer.get() & 0xff;
            case 6:
                return buffer.getShort() & 0xffff;
            case 7:
                return buffer.getInt();
            default:
                throw new IllegalArgumentException();
        }
    }

    private Object read(ByteBuffer objBuffer, Type propertyType, boolean array, UnrealPackageFile.ExportEntry arrayInner, String structName, UnrealPackageFile up) {
        switch (propertyType) {
            case BYTE:
                return objBuffer.get() & 0xff;
            case INT:
                return objBuffer.getInt();
            case BOOL:
                return array;
            case FLOAT:
                return objBuffer.getFloat();
            case OBJECT:
                return getCompactInt(objBuffer);
            case NAME:
                return getCompactInt(objBuffer);
            case ARRAY:
                int arraySize = getCompactInt(objBuffer);
                List<Object> arrayList = new ArrayList<>(arraySize);

                String a = arrayInner.getObjectClass().getObjectName().getName();
                Property f;
                try {
                    f = (Property) classHelper.loadField(arrayInner);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                array = false;
                arrayInner = null;
                structName = null;
                propertyType = Type.valueOf(a.replace("Property", "").toUpperCase());
                if (propertyType == Type.STRUCT) {
                    StructProperty structProperty = (StructProperty) f;
                    structName = structProperty.getStructType().getObjectFullName();
                }
                if (propertyType == Type.ARRAY) {
                    array = true;
                    ArrayProperty arrayProperty = (ArrayProperty) f;
                    arrayInner = (UnrealPackageFile.ExportEntry) arrayProperty.getInner();
                }

                for (int i = 0; i < arraySize; i++) {
                    arrayList.add(read(objBuffer, propertyType, array, arrayInner, structName, up));
                }
                return arrayList;
            case STRUCT:
                return readStruct(objBuffer, structName, up);
            /*case VECTOR:
                return readStruct(objBuffer, "Vector", up);
            case ROTATOR:
                return readStruct(objBuffer, "Rotator", up);*/
            case STR:
                return getString(objBuffer);
            default:
                throw new IllegalStateException("Unk type: " + propertyType);
        }
    }

    private List<L2Property> readStruct(ByteBuffer objBuffer, String structName, UnrealPackageFile up) {
        switch (structName) {
            case "Vector":
                List<Field> vectorProperties = classHelper.getStruct("Core.Object.Vector")
                        .orElseThrow(() -> new IllegalStateException("Core.Object.Vector not found"));
                L2Property x = new L2Property((Property) vectorProperties.get(0));
                x.setAt(0, objBuffer.getFloat());
                L2Property y = new L2Property((Property) vectorProperties.get(1));
                y.setAt(0, objBuffer.getFloat());
                L2Property z = new L2Property((Property) vectorProperties.get(2));
                z.setAt(0, objBuffer.getFloat());
                return Arrays.asList(x, y, z);
            case "Rotator":
                List<Field> rotatorProperties = classHelper.getStruct("Core.Object.Rotator")
                        .orElseThrow(() -> new IllegalStateException("Core.Object.Rotator not found"));
                L2Property pitch = new L2Property((Property) rotatorProperties.get(0));
                pitch.setAt(0, objBuffer.getInt());
                L2Property yaw = new L2Property((Property) rotatorProperties.get(1));
                yaw.setAt(0, objBuffer.getInt());
                L2Property roll = new L2Property((Property) rotatorProperties.get(2));
                roll.setAt(0, objBuffer.getInt());
                return Arrays.asList(pitch, yaw, roll);
            case "Color":
                List<Field> colorProperties = classHelper.getStruct("Core.Object.Color")
                        .orElseThrow(() -> new IllegalStateException("Core.Object.Color not found"));
                L2Property b = new L2Property((Property) colorProperties.get(0));
                b.setAt(0, objBuffer.get() & 0xff);
                L2Property g = new L2Property((Property) colorProperties.get(1));
                g.setAt(0, objBuffer.get() & 0xff);
                L2Property r = new L2Property((Property) colorProperties.get(2));
                r.setAt(0, objBuffer.get() & 0xff);
                L2Property a = new L2Property((Property) colorProperties.get(3));
                a.setAt(0, objBuffer.get() & 0xff);
                return Arrays.asList(b, g, r, a);
            default:
                return readProperties(objBuffer, up, structName);
        }
    }

    private ByteBuffer objBuffer = ByteBuffer.allocate(1_000_000).order(ByteOrder.LITTLE_ENDIAN);

    public void writeProperties(ByteBuffer buffer, UnrealPackageFile up, List<L2Property> list) throws Exception {
        for (L2Property property : list) {
            Property template = property.getTemplate();

            for (int i = 0; i < property.getSize(); i++) {
                Object obj = property.getAt(i);
                if (obj == null)
                    continue;

                objBuffer.clear();
                AtomicBoolean array = new AtomicBoolean(i > 0);
                AtomicReference<String> structName = new AtomicReference<>();
                AtomicReference<Type> type = new AtomicReference<>(Type.valueOf(template.getClass().getSimpleName().replace("Property", "").toUpperCase()));
                write(objBuffer, template, obj, array, structName, type, up);
                objBuffer.flip();

                int size = getPropertySize(objBuffer.limit());
                int info = (array.get() ? 1 << 7 : 0) | (size << 4) | type.get().ordinal();

                putCompactInt(buffer, up.nameReference(template.getEntry().getObjectName().getName()));
                buffer.put((byte) info);
                if (type.get() == Type.STRUCT)
                    putCompactInt(buffer, up.nameReference(structName.get()));
                switch (size) {
                    case 5:
                        buffer.put((byte) objBuffer.limit());
                        break;
                    case 6:
                        buffer.putShort((short) objBuffer.limit());
                        break;
                    case 7:
                        buffer.putInt(objBuffer.limit());
                        break;
                }
                if (i > 0)
                    buffer.put((byte) i);
                buffer.put(objBuffer);
            }
        }
        putCompactInt(buffer, up.nameReference("None"));
    }

    private void write(ByteBuffer objBuffer, Property template, Object obj, AtomicBoolean array, AtomicReference<String> structName, AtomicReference<Type> type, UnrealPackageFile up) throws Exception {
        if (template instanceof ByteProperty) {
            objBuffer.put(((Integer) obj).byteValue());
        } else if (template instanceof IntProperty) {
            objBuffer.putInt((Integer) obj);
        } else if (template instanceof BoolProperty) {
            array.set((Boolean) obj);
        } else if (template instanceof FloatProperty) {
            objBuffer.putFloat((Float) obj);
        } else if (template instanceof ObjectProperty) {
            putCompactInt(objBuffer, (Integer) obj);
        } else if (template instanceof NameProperty) {
            putCompactInt(objBuffer, (Integer) obj);
        } else if (template instanceof ArrayProperty) {
            ArrayProperty arrayProperty = (ArrayProperty) template;

            List<Object> arrayList = (List<Object>) obj;
            putCompactInt(objBuffer, arrayList.size());

            UnrealPackageFile.ExportEntry arrayInner = (UnrealPackageFile.ExportEntry) arrayProperty.getInner();
            String a = arrayInner.getObjectClass().getObjectName().getName();
            Class<? extends Property> pc = (Class<? extends Property>) Class.forName("acmi.l2.clientmod.unreal." + a);
            Property f = pc.getConstructor(ByteBuffer.class, UnrealPackageFile.ExportEntry.class)
                    .newInstance(ByteBuffer.wrap(arrayInner.getObjectRawDataExternally()).order(ByteOrder.LITTLE_ENDIAN), arrayInner);

            for (Object arrayObj : arrayList) {
                write(objBuffer, f, arrayObj, new AtomicBoolean(), new AtomicReference<>(), new AtomicReference<>(), up);
            }
        } else if (template instanceof StructProperty) {
            StructProperty structProperty = (StructProperty) template;
            structName.set(structProperty.getStructType().getObjectName().getName());
            writeStruct(objBuffer, structName.get(), up, (List<L2Property>) obj);
            /*switch (structName.get()){
                case "Vector":
                    type.set(OProperty.Type.VECTOR);
                    break;
                case "Rotator":
                    type.set(OProperty.Type.ROTATOR);
                    break;
            }*/
        } else if (template instanceof StrProperty) {
            putString(objBuffer, (String) obj);
        } else {
            throw new IllegalArgumentException(template.getClass().getSimpleName() + " serialization not implemented");
        }
    }

    private void writeStruct(ByteBuffer objBuffer, String structName, UnrealPackageFile up, List<L2Property> struct) throws Exception {
        switch (structName) {
            case "Color":
                for (int i = 0; i < 4; i++)
                    objBuffer.put(((Integer) struct.get(i).getAt(0)).byteValue());
                break;
            case "Vector":
                for (int i = 0; i < 3; i++)
                    objBuffer.putFloat((Float) struct.get(i).getAt(0));
                break;
            case "Rotator":
                for (int i = 0; i < 3; i++)
                    objBuffer.putInt((Integer) struct.get(i).getAt(0));
                break;
            default:
                writeProperties(objBuffer, up, struct);
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

    public enum Type {
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
        FIXED_ARRAY
    }
}
