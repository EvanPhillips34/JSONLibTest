package com.orangefrc.jsonannotation;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.orangefrc.annotation.GSON;
import com.orangefrc.annotation.GenerateJson;
import com.orangefrc.annotation.NT4Publisher;
import com.orangefrc.annotation.NT4Publisher.*;
import com.orangefrc.annotation.Update.*;
import edu.wpi.first.networktables.*;



@SupportedAnnotationTypes("com.orangefrc.annotation.GenerateJson")

public class JsonAnnotationProcessor extends AbstractProcessor{
        Map<Name, TypeMirror> fieldsMap = new HashMap<>();
        NT4Publisher publisher;
        String jsonClassName;

        GSON typegson = new GSON();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        
        for(Element element : roundEnv.getElementsAnnotatedWith(GenerateJson.class)) {
            if(element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                String className = typeElement.getSimpleName().toString();
                String generatedClassName = className + "Json";
                String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
                List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());
                try {
                    generateClass(packageName, generatedClassName, fields);
                    generateNT4Table(packageName, generatedClassName);

                }
                catch (IOException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate class");
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
            writer.write("import edu.wpi.first.networktables.*;\nimport com.orangefrc.annotation.GSON;\nimport java.io.*;\nimport java.util.Arrays;\nimport java.util.stream.*;\nimport java.nio.file.*;");
            writer.write("import java.util.Map;\n import javax.lang.model.element.Name;\n import java.util.HashMap;\nimport frc.robot.subsystems.shooter.ShooterTunerJsonNT4;\nimport java.lang.StackTraceElement;\n");

            writer.write("public class " + className + " {\n");
            for(VariableElement field : fields) {
                writer.write("private " + field.asType().toString() + " update" + field.getSimpleName() + ";" + "\n");
            }
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
            String classn = className + "NT4";

            writer.write("}\n");

            writer.write("}\n");
            
            writer.write("String filePath = \"/home/lvuser/pid/" + className + ".json\";\n");
            writer.write("Path path = Paths.get(filePath);");

            for(Name key: fieldsMap.keySet()) {
                if(fieldsMap.get(key).getKind() == TypeKind.DOUBLE) {
                    writer.write("DoublePublisher " + key.toString() + "Pub = " + classn + ".doubleMap.get(\"" + key.toString() + "\");\n");
                }

                if(fieldsMap.get(key).getKind() == TypeKind.INT) {
                    writer.write("IntegerPublisher " + key.toString() + "Pub = " + classn + ".intMap.get(\"" + key.toString() + "\");\n");
                }
            }
            writer.write("JSON json;\n");
            writer.write("public void init() {\n");
            writer.write("try{\n");
            writer.write("Files.createFile(path);\n}\n");

            writer.write("catch(IOException e) {\n");
            writer.write(classn + ".table.getStringTopic(\"Error\").publish().set(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining(System.lineSeparator() + \"\\tat\")));\r\n" + "}\n");


            writer.write("try(Reader reader = new FileReader(filePath)) {\n");
            writer.write("json = GSON.gson.fromJson(reader, JSON.class);\n");
            writer.write("}\n");

            
            writer.write("catch(IOException e) {\n" + classn + ".table.getStringTopic(\"Error\").publish().set(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining(System.lineSeparator() + \"\\tat\")));\r\n");
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
            writer.write(");\n}\n");

            writer.write("try(Writer writer = new FileWriter(filePath)) {\n" + //
                                "    GSON.gson.toJson(json, writer);\n" + //
                                "    writer.close();\n" + //
                                "}\n" + //
                                "catch(IOException e) {\n" + //
                                "ShooterTunerJsonNT4.table.getStringTopic(\"Error\").publish().set(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining(System.lineSeparator() + \"\\tat\")));\n" + //
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
            writer.write(classn + ".init();\n");

            for(Name key: fieldsMap.keySet()) {
                if(fieldsMap.get(key).getKind() == TypeKind.DOUBLE) {
                    writer.write(key.toString() + "Pub.set(" + key.toString() + ");\n");
                }
                if(fieldsMap.get(key).getKind() == TypeKind.INT) {
                    writer.write(key.toString() + "Pub.set(" + key.toString() + ");\n");
                }
            }
            
            writer.write("}\n");
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

        writer.write("public static void init() {\n");
        for(Map.Entry<Name, TypeMirror> entry : fieldsMap.entrySet()) {
            if(entry.getValue().getKind() == TypeKind.DOUBLE) {
                writer.write("DoublePublisher " + entry.getKey() + " = table.getDoubleTopic(\"" + entry.getKey() + "\").publish();\n");
                writer.write("doubleMap.put(\"" + entry.getKey() + "\", " + entry.getKey() + ");\n");
            }
            if(entry.getValue().getKind() == TypeKind.INT) {
                writer.write("IntegerPublisher " + entry.getKey() + " = table.getIntegerTopic(\"" + entry.getKey() + "\").publish();\n");
                writer.write("intMap.put(\"" + entry.getKey() + "\", " + entry.getKey() + ");\n");
            }
            
        }
        writer.write("}\n");
        writer.write("}");
    }
   }
   
    
} 
