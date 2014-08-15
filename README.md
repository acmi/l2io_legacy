l2io
====
Library for reading/modifying Lineage 2 packages.

#### Usage
```java
import acmi.l2.clientmod.io.UnrealPackageFile;
import acmi.l2.clientmod.util.BufferUtil;


String fileName = "Engine.u";
boolean readOnly = true;

String entryName = "Actor.ScriptText";

try (UnrealPackageFile up = new UnrealPackageFile(new File(fileName), readOnly)) {
  UnrealPackageFile.ExportEntry entry = up.getExportTable()
      .stream()
      .filter(e -> e.getObjectInnerFullName().equals(entryName))
      .findAny()
      .orElseThrow(() -> new IllegalArgumentException(entryName + " not found"));

  ByteBuffer buffer = ByteBuffer.wrap(entry.getObjectRawData());
  buffer.position(9);
  String scriptText = BufferUtil.getString(buffer, Charset.forName("EUC-KR"));

  System.out.println(scriptText);
}
```