package com.orangefrc.annotation;

import com.google.gson.Gson;

import edu.wpi.first.networktables.NetworkTableInstance;

import java.nio.file.*;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;




public class GSON {
    public static Gson gson = new Gson();
    public static void createDir() {
        Path path = Paths.get("/home/lvuser/pid/");
        try{
            Files.createDirectory(path);
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        try {
            Process process = Runtime.getRuntime().exec("chmod -R 777 /home/lvuser/pid/");
            process.waitFor();
            
        } catch (IOException e) {
            System.err.println(e);
        } catch (InterruptedException e) {
            System.err.println(e);
        }
}
    
}
