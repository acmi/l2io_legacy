package acmi.l2.clientmod.unreal;

import acmi.l2.clientmod.io.UnrealPackageFile;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static acmi.l2.clientmod.util.BufferUtil.getCompactInt;

public class Enum extends Field {
    public final List<Integer> values;

    public Enum(ByteBuffer buffer, UnrealPackageFile.ExportEntry up, PropertiesUtil propertiesUtil) {
        super(buffer, up, propertiesUtil);

        int size = getCompactInt(buffer);
        values = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            values.add(getCompactInt(buffer));
    }

    public List<String> getValues() {
        return values.stream()
                .map(i -> getEntry().getUnrealPackage().getNameTable().get(i).getName())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return getEntry() + ": Enum" + getValues();
    }
}
