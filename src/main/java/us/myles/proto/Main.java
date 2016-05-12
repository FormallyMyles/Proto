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
            System.out.println("Example: java -jar proto.jar Minecraft.jar output.json *");
            System.out.println("Usage java -jar proto.jar <jar> <output> <filter/*>");
            return;
        }
        String output = "output.json";
        String filter = "*";
        if(args.length >= 2){
            output = args[1];
        }
        if(args.length >= 3){
            filter = args[2];
        }
        // Note: This is more a debug thing, not actual but hey!
        JarAnalyser ja = new JarAnalyser(new File(args[0]), filter);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter fw = new FileWriter(new File(output));
        gson.toJson(ja.analyse(), fw);
        fw.close();
    }
}
