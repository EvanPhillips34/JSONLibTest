package com.orangefrc.annotation;

import com.google.gson.Gson;

import java.nio.file.*;
import java.io.IOException;




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
}
    
}
