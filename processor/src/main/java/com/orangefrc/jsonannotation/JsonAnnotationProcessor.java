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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.orangefrc.annotation.GenerateJson;
import com.orangefrc.annotation.Update;
import com.orangefrc.annotation.Update.*;

@SupportedAnnotationTypes("com.orangefrc.annotation.GenerateJson")

public class JsonAnnotationProcessor extends AbstractProcessor{
        Map<Name, TypeMirror> fieldsMap = new HashMap();

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
            writer.write("public class " + className + " {\n");
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

            writer.write("public void putDashboard() {\n");

            writer.write("}");
        }
    }

    private String getTypeForDash(TypeMirror type) {
        if(type.toString() == "double" || type.toString() == "int" || type.toString() == "float") {
            return "SmartDashboard.putNumber";
        }
        else {
            return "";
        }
    }
    
} 
