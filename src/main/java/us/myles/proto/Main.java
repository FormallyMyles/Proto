package us.myles.proto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javassist.NotFoundException;
import us.myles.proto.analyser.AnalysedClass;
import us.myles.proto.analyser.JarAnalyser;
import us.myles.proto.mapper.MappedClass;
import us.myles.proto.mapper.MappingGenerator;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws NotFoundException, IOException {
        if (args.length == 0) {
            System.out.println("Example: java -jar proto.jar Minecraft.jar output.json *");
            System.out.println("Usage java -jar proto.jar <jar> <output> <filter/*>");
            System.out.println("For generating a mapping between 2 files:");
            System.out.println("Usage java -jar proto.jar <input1> <input2> <output>");
            return;
        }
        String input = args[0];
        String output = "output.json";
        String filter = "*";
        if (args.length >= 2) {
            output = args[1];
        }
        if (args.length >= 3) {
            filter = args[2];
        }
        if (input.endsWith(".json")) {
            // Use mapping mode
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileReader fr1 = new FileReader(input);
            FileReader fr2 = new FileReader(output);
            List<AnalysedClass> oldV = new ArrayList<>(Arrays.asList(gson.fromJson(fr1, AnalysedClass[].class)));
            List<AnalysedClass> newV = new ArrayList<>(Arrays.asList(gson.fromJson(fr2, AnalysedClass[].class)));
            MappingGenerator mapper = new MappingGenerator(oldV, newV);

            PrintWriter pw = new PrintWriter("output.mapping");
            List<MappedClass> mapped = mapper.generate();
            Collections.sort(mapped, new Comparator<MappedClass>() {
                @Override
                public int compare(MappedClass o1, MappedClass o2) {
                    return o1.getOldName().compareTo(o2.getOldName());
                }
            });
            for (MappedClass m : mapped) {
                if (m.getOldName().contains("$")) {
                    pw.println("\tCLASS none/" + m.getNewName().replace(".", "/") + " " + m.getOldName().replace(".", "/").split("\\$")[1]);
                } else {
                    pw.println("CLASS none/" + m.getNewName().replace(".", "/") + " " + m.getOldName().replace(".", "/"));
                }
            }
            System.out.println("Done!");
            pw.close();
        } else {
            // Note: This is more a debug thing, not actual but hey!
            JarAnalyser ja = new JarAnalyser(new File(input), filter);

            Gson gson = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
            FileWriter fw = new FileWriter(new File(output));
            gson.toJson(ja.analyse(), fw);
            fw.close();
        }
    }
}
