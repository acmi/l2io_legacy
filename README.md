l2io
====
Library for reading/modifying Lineage 2 packages.

#### Usage
```java
import acmi.l2.clientmod.io.UnrealPackageFile;
import acmi.l2.clientmod.unreal.classloader.FolderPackageLoader;
import acmi.l2.clientmod.unreal.classloader.UnrealClassLoader;
import acmi.l2.clientmod.unreal.core.TextBuffer;
import acmi.l2.clientmod.unreal.objectfactory.ObjectFactory;


String l2SystemFolder = "C:\\Lineage 2\\system";

UnrealClassLoader classLoader = new UnrealClassLoader(new FolderPackageLoader(l2SystemFolder));
ObjectFactory objectFactory = new ObjectFactory(classLoader);

String fileName = "Engine.u";
String entryName = "Actor.ScriptText";

try (UnrealPackageFile up = new UnrealPackageFile(new File(l2SystemFolder, fileName), true)) {
  TextBuffer textBuffer = up.getExportTable()
      .stream()
      .filter(e -> e.getObjectInnerFullName().equals(entryName))
      .findAny()
      .map(objectFactory)
      .map(o -> (TextBuffer) o)
      .orElseThrow(() -> new IllegalArgumentException(entryName + " not found"));

  System.out.println(textBuffer.getText());
}
```
