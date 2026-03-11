package com.orangefrc.jsonannotation;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.orangefrc.annotation.GenerateJson;



@SupportedAnnotationTypes("com.orangefrc.annotation.GenerateJson")
@SupportedSourceVersion(SourceVersion.RELEASE_17)

public class JsonAnnotationProcessor extends AbstractProcessor{
        Map<Name, TypeMirror> fieldsMap = new HashMap<>();
        String jsonClassName;
        String classn;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
 
        for(Element element : roundEnv.getElementsAnnotatedWith(GenerateJson.class)) {
            if(element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                String className = typeElement.getSimpleName().toString();
                String generatedClassName = className + "Json";
                classn = generatedClassName + "NT4";

                String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
                List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());
                try {
                    generateClass(packageName, generatedClassName, fields);
                    generateNT4Table(packageName, generatedClassName);
                    fieldsMap.clear();
                }
                catch (IOException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate class");
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                }

            }
        }
        return true;
    }
    private void generateClass(String packageName, String className, List<VariableElement> fields) throws IOException{
        Filer filer = processingEnv.getFiler();
        JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + className);
        try (Writer writer = sourceFile.openWriter()) {
            writer.write("package " + packageName + ";\n\n");
            writer.write("import edu.wpi.first.networktables.*;\nimport com.orangefrc.annotation.GSON;\nimport java.io.*;\nimport java.util.Arrays;\nimport java.util.stream.*;\nimport java.nio.file.*;\n");
            writer.write("import java.util.Map;\n import javax.lang.model.element.Name;\n import java.util.HashMap;\nimport " + packageName + "." + classn + ";\nimport java.lang.StackTraceElement;\n");

            writer.write("public class " + className + " {\n");
            for(VariableElement field : fields) {
                writer.write("private " + field.asType().toString() + " update" + field.getSimpleName() + ";" + "\n");
            }
            writer.write("private boolean hasUpdate = false;\n");

            writer.write("public " + className + "() {\n");
            writer.write("init();\n");
            writer.write("}\n");
            writer.write("public class JSON {\n");
            for(VariableElement field : fields) {
                writer.write("private " + field.asType().toString() + " " + field.getSimpleName() + ";" + "\n");

                writer.write("public " + field.asType().toString() + " " + "get" + field.getSimpleName() + "() " + " {return " + field.getSimpleName()  + ";}\n");
                fieldsMap.put(field.getSimpleName(), field.asType());
                
            }
            writer.write("\npublic JSON" + " (");
            boolean first = true;
            for(Map.Entry<Name,TypeMirror> entry : fieldsMap.entrySet()) {
                if(first) {
                    writer.write("");
                    first = false;
                }
                else {
                    writer.write(", ");
                }
                writer.write(entry.getValue().toString() +" " + entry.getKey());
            }
            
            writer.write(") {\n");
            for(Map.Entry<Name,TypeMirror> entry : fieldsMap.entrySet()) {
                writer.write("this." + entry.getKey() + " = " + entry.getKey() + ";\n");
            }

            writer.write("}\n");

            writer.write("}\n");
            
            writer.write("String filePath = \"/home/lvuser/pid/" + className + ".json\";\n");
            writer.write("Path path = Paths.get(filePath);");

            // for(Name key: fieldsMap.keySet()) {
            //     if(fieldsMap.get(key).getKind() == TypeKind.DOUBLE) {
            //         writer.write("DoublePublisher " + key.toString() + "Pub = " + classn + ".doubleMap.get(\"" + key.toString() + "\");\n");
            //     }

            //     if(fieldsMap.get(key).getKind() == TypeKind.INT) {
            //         writer.write("IntegerPublisher " + key.toString() + "Pub = " + classn + ".intMap.get(\"" + key.toString() + "\");\n");
            //     }
            // }
            writer.write("JSON json;\n");
            writer.write("public void init() {\n");
            writer.write(classn + ".init();\n");

            writer.write("try{\n");
                        writer.write("      if(!Files.exists(path)) {\n" + //
                                "            Files.createFile(path);\n" + //
                                classn + ".table.getStringTopic(\"Status\").publish().set(\"Creating file for " + className +"!\");\n}\n" +
                                "" + //
                                "      }\n");


            writer.write("catch (IOException e) {\r\n" +"NT4Publisher.createError" + //
                                "          .set(e.getMessage() + \"\\n" + //
                                "\"\r\n" + //
                                "              + Arrays.stream(e.getStackTrace())\r\n" + //
                                "                  .map(StackTraceElement::toString)\r\n" + //
                                "                  .collect(Collectors.joining(System.lineSeparator() + \"\\tat\")));\r\n" + //
                                "    }\r\n" + //
                                "    catch (UnsupportedOperationException e) {\r\n" + //
                                "      " + "NT4Publisher.createError\r\n" + //
                                "          .set(e.getMessage() + \"\\n" + //
                                "\"\r\n" + //
                                "              + Arrays.stream(e.getStackTrace())\r\n" + //
                                "                  .map(StackTraceElement::toString)\r\n" + //
                                "                  .collect(Collectors.joining(System.lineSeparator() + \"\\tat\")));\r\n" + //
                                "    }    catch (SecurityException e) {\r\n" + //
                                       "NT4Publisher.createError" +
                                "          .set(e.getMessage() + \"\\n" + //
                                "\"\r\n" + //
                                "              + Arrays.stream(e.getStackTrace())\r\n" + //
                                "                  .map(StackTraceElement::toString)\r\n" + //
                                "                  .collect(Collectors.joining(System.lineSeparator() + \"\\tat\")));\r\n" + //
                                "    }\n");
            //writer.write(classn + ".table.getStringTopic(\"FileCreateError\").publish().set(e.getCause() + \"\\n\" + Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining(System.lineSeparator() + \"\\tat\")));\r\n" + "}\n");


            writer.write("try(Reader reader = new FileReader(filePath)) {\n");
            writer.write("if(GSON.gson.fromJson(reader, JSON.class) != null) {\n");
            writer.write("json = GSON.gson.fromJson(reader, JSON.class);\n");
            writer.write("NT4Publisher.readError.set(\"Reading file for " + className + "!\");\n");
            writer.write("}\n");
            writer.write("else {\n");
            writer.write("NT4Publisher.status." + "set(\"Making new class file for " + className +"!\");\n");

            writer.write("json = new JSON(");
            first = true;
            for(Map.Entry<Name,TypeMirror> entry : fieldsMap.entrySet()) {
                if(first) {
                    writer.write("");
                    first = false;
                }
                else {
                    writer.write(", ");
                }
                if(entry.getValue().getKind() == TypeKind.DOUBLE) {
                    writer.write("0.0d");
                }
                else if(entry.getValue().getKind() == TypeKind.INT) {
                    writer.write("0");
                }
            }
            writer.write(");\n}\n}");
            
            writer.write("catch(Exception e) {\n" + "NT4Publisher.readError." + "set(e.getCause() + \"\\n\" + Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining(System.lineSeparator() + \"\\tat\")));\r\n}\n");
            
            writer.write("try(Writer writer = new FileWriter(filePath)) {\n" + //
                                "    GSON.gson.toJson(json, writer);\n" + //
                                "    writer.close();\n" +
                                            "NT4Publisher.status" + ".set(\"Writing file for " + className + "!\");\n"
+ 
                                "}\n" + //
                                "catch(Exception e) {\n" + //
                                "NT4Publisher.writeError" + ".set(e.getCause() + \"\\n\" + Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining(System.lineSeparator() + \"\\tat\")));\n" + //
                                "}\n");

            writer.write("}\n");

            writer.write("public void putVals(");
            first = true;
            for(Map.Entry<Name,TypeMirror> entry : fieldsMap.entrySet()) {
                if(first) {
                    writer.write("");
                    first = false;
                }
                else {
                    writer.write(", ");
                }
                writer.write(entry.getValue().toString() +" " + entry.getKey());
            }
            
            writer.write(") {\n");

            writer.write("");

            for(Name key: fieldsMap.keySet()) {
                if(fieldsMap.get(key).getKind() == TypeKind.DOUBLE) {
                    writer.write(classn + ".doubleMap.get(\"" + key.toString() + "\").set(" + key.toString() + ");\n");
                }
                if(fieldsMap.get(key).getKind() == TypeKind.INT) {
                    writer.write(classn + ".intMap.get(\"" + key.toString() + "\").set(" + key.toString() + ");\n");
                }
            }
            
            writer.write("}\n");

            writer.write("public void updateVals() {\n");
            writer.write("try {\n");
            first = true;
            writer.write("if(");
            
            for(Map.Entry<Name,TypeMirror> entry : fieldsMap.entrySet()) {
                if(first) {
                    writer.write("");
                    first = false;
                }
                else {
                    writer.write(" || ");
                }
                if(entry.getValue().getKind() == TypeKind.DOUBLE) {
                    writer.write("update" + entry.getKey() +" != " + classn + ".dubSubMap.get(\"" + entry.getKey() + "\").getAsDouble()");
                }
                if(entry.getValue().getKind() == TypeKind.INT) {
                    writer.write("update" + entry.getKey() +" != (int) " + classn + ".intSubMap.get(\"" + entry.getKey() + ".get()");
                }
            }
            writer.write(") {\n");
            writer.write("hasUpdate = true;\n");
            for(Map.Entry<Name,TypeMirror> entry : fieldsMap.entrySet()) {
                if(entry.getValue().getKind() == TypeKind.DOUBLE) {
                    writer.write("update" + entry.getKey() +" = " + classn + ".dubSubMap.get(\"" + entry.getKey() + "\").getAsDouble();\n");
                }
                if(entry.getValue().getKind() == TypeKind.INT) {
                    writer.write("update" + entry.getKey() +" = (int) " + classn + ".intSubMap.get(\"" + entry.getKey() + ".get();\n");

                }
            }
            writer.write("json = new JSON(");
            first = true;
            for(Map.Entry<Name,TypeMirror> entry : fieldsMap.entrySet()) {
                if(first) {
                    writer.write("");
                    first = false;
                }
                else {
                    writer.write(", ");
                }
                writer.write("update" + entry.getKey());
            }
            writer.write(");\n");
            writer.write("try(Writer writer = new FileWriter(filePath)) {\n");
            writer.write("GSON.gson.toJson(json, writer);\n");
            writer.write("writer.close();\n");
            writer.write("}\n");
            writer.write("catch(Exception e) {\n" + "NT4Publisher.writeError.set" + "(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining(System.lineSeparator() + \"\\tat\")));\n" + "}\n");
            writer.write("}\n");
            writer.write("else {\n hasUpdate = false;\n}\n");
            writer.write("}\n");
            writer.write("catch(NullPointerException e) {\n" + "NT4Publisher.updateError" +".set(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining(System.lineSeparator() + \"\\tat\")));\n}\n}\n");

            
            for(Map.Entry<Name,TypeMirror> entry : fieldsMap.entrySet()) {
                writer.write("public " + entry.getValue().toString() +" get" + entry.getKey() + "() {\n");
                writer.write("return update" + entry.getKey() + ";\n}\n");
            }
            writer.write("public boolean hasUpdated() {\nreturn hasUpdate;\n}\n");


            writer.write("}");
        }
    }



   private void generateNT4Table(String packageName, String className) throws IOException {
    Filer filer = processingEnv.getFiler();
    String classn = className + "NT4";
    JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + classn);
    try(Writer writer = sourceFile.openWriter()) {
        writer.write("package " + packageName + ";\n\n");
        writer.write("import edu.wpi.first.networktables.*;\n");
        writer.write("import java.util.Map;\n import javax.lang.model.element.Name;\n import java.util.HashMap;\n");
        writer.write("public class " + classn + " {\n");
        writer.write("public static Map<String,DoublePublisher> doubleMap = new HashMap<String,DoublePublisher>();\n");
        writer.write("public static Map<String,IntegerPublisher> intMap = new HashMap<String,IntegerPublisher>();\n");

        writer.write("public static NetworkTableInstance inst = NetworkTableInstance.getDefault();\n");
        writer.write("public static NetworkTable table = inst.getTable(\"Tuning\");\n");

        writer.write("  public static Map<String, DoubleSubscriber> dubSubMap = new HashMap<String, DoubleSubscriber>();\n" + //
                        "  public static Map<String, IntegerSubscriber> intSubMap = new HashMap<String, IntegerSubscriber>();");

        writer.write("public static void init() {\n");
        for(Map.Entry<Name, TypeMirror> entry : fieldsMap.entrySet()) {
            if(entry.getValue().getKind() == TypeKind.DOUBLE) {
                writer.write("DoublePublisher " + entry.getKey() + " = table.getDoubleTopic(\"" + className + "/" + entry.getKey() + "\").publish();\n");
                writer.write("doubleMap.put(\"" + entry.getKey() + "\", " + entry.getKey() + ");\n");
                writer.write("DoubleSubscriber " + entry.getKey() + "Sub = " + entry.getKey() + ".getTopic().subscribe(0.0);\n");
                writer.write("dubSubMap.put(\"" + entry.getKey() + "Sub\"," + entry.getKey() + "Sub);\n");
            }
            if(entry.getValue().getKind() == TypeKind.INT) {
                writer.write("IntegerPublisher " + entry.getKey() + " = table.getIntegerTopic(\"" + className + "/" + entry.getKey() + "\").publish();\n");
                writer.write("intMap.put(\"" + entry.getKey() + "\", " + entry.getKey() + ");\n");
                writer.write("IntegerSubscriber " + entry.getKey() + "Sub = " + entry.getKey() + ".getTopic().subscribe(0);\n");
                writer.write("intSubMap.put(\"" + entry.getKey() + "Sub\"," + entry.getKey() + "Sub);\n");
            }
            
        }

        
        writer.write("}\n");
        writer.write("}");
    }
   }
   
    
} 
