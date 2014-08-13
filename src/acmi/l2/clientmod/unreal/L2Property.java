package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static acmi.l2.clientmod.unreal.ToT3D.toT3DString;

public class L2Property {
    private Property template;
    private Object[] value;

    public L2Property(Property template) {
        this.template = template;
        this.value = new Object[template.arrayDimension];
    }

    public String getName() {
        return template.getEntry().getObjectName().getName();
    }

    public Property getTemplate() {
        return template;
    }

    public int getSize() {
        return value.length;
    }

    public Object getAt(int index) {
        return value[index];
    }

    public void setAt(int index, Object value) {
        this.value[index] = value;
    }

    @Override
    public String toString() {
        return "["+template.getCategory()+"]"+template.getEntry().getObjectFullName() + "=" + Arrays.toString(value);
    }

    public List<String> values(ClassHelper classHelper) throws IOException {
        UnrealPackageFile up = getTemplate().getEntry().getUnrealPackage();
        List<String> result = new ArrayList<>();
        boolean array = template.arrayDimension > 1;
        if (template instanceof ByteProperty) {
            ByteProperty byteProperty = (ByteProperty) template;

            UnrealPackageFile.Entry enumType = byteProperty.getEnumType();
            Enum e = enumType != null ? (Enum) classHelper.loadField((UnrealPackageFile.ExportEntry) enumType) : null;
            for (Object aValue : value) {
                int val = (Integer) aValue;
                String strVal = enumType != null ? e.getValues().get(val) : String.valueOf(val);
                result.add(strVal);
            }
        } else if (template instanceof ObjectProperty) {
            for (Object aValue : value) {
                UnrealPackageFile.Entry entry = up.objectReference((Integer) aValue);
                result.add(toT3DString(entry));
            }
        } else if (template instanceof NameProperty) {
            for (int i = 0; i < value.length; i++) {
                result.add(Objects.toString(up.getNameTable().get((Integer) value[i])));
            }
        } else if (template instanceof ArrayProperty) {
            List<Object> list = (List<Object>) value[0];

        } else if (template instanceof StructProperty) {
            List<L2Property> list = (List<L2Property>) value[0];
        } else {
            for (int i = 0; i < value.length; i++) {
                result.add(Objects.toString(value[i]));
            }
        }
        return result;
    }

    public List<String> toString(ClassHelper classHelper) throws IOException {
        UnrealPackageFile up = getTemplate().getEntry().getUnrealPackage();
        List<String> result = new ArrayList<>();
        boolean array = template.arrayDimension > 1;
        if (template instanceof ByteProperty) {
            ByteProperty byteProperty = (ByteProperty) template;

            UnrealPackageFile.Entry enumType = byteProperty.getEnumType();
            Enum e = enumType != null ? (Enum) classHelper.loadField((UnrealPackageFile.ExportEntry) enumType) : null;
            for (int i = 0; i < value.length; i++) {
                int val = (Integer) value[i];
                String strVal = enumType != null ? e.getValues().get(val) : String.valueOf(val);
                String s = getName();
                if (array)
                    s += "(" + i + ")";
                s += "=" + strVal;
                result.add(s);
            }
        } else if (template instanceof ObjectProperty) {
            for (int i = 0; i < value.length; i++) {
                String s = getName();
                if (array)
                    s += "(" + i + ")";
                UnrealPackageFile.Entry entry = up.objectReference((Integer) value[i]);
                s += "=" + toT3DString(entry);
                result.add(s);
            }
        } else if (template instanceof NameProperty) {
            for (int i = 0; i < value.length; i++) {
                String s = getName();
                if (array)
                    s += "(" + i + ")";
                s += "=" + Objects.toString(up.getNameTable().get((Integer) value[i]));
                result.add(s);
            }
        } else if (template instanceof ArrayProperty) {
            List<Object> list = (List<Object>) value[0];

        } else if (template instanceof StructProperty) {
            List<L2Property> list = (List<L2Property>) value[0];
        } else {
            for (int i = 0; i < value.length; i++) {
                String s = getName();
                if (array)
                    s += "(" + i + ")";
                s += "=" + Objects.toString(value[i]);
                result.add(s);
            }
        }
        return result;
    }
}
