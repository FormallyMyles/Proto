package us.myles.proto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javassist.NotFoundException;
import us.myles.proto.analyser.JarAnalyser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws NotFoundException, IOException {
        if(args.length == 0){
            System.out.println("Usage java -jar proto.jar Minecraft.jar");
            return;
        }
        // Note: This is more a debug thing, not actual but hey!
        JarAnalyser ja = new JarAnalyser(new File(args[0]));

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter fw = new FileWriter(new File("output.json"));
        gson.toJson(ja.analyse(), fw);
        fw.close();
    }
}
