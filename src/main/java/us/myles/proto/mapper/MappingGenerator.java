package us.myles.proto.mapper;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.primitives.Floats;
import us.myles.proto.analyser.AnalysedClass;

import java.util.*;

public class MappingGenerator {
    private final List<AnalysedClass> oldVersion;
    private final List<AnalysedClass> newVersion;

    /* Temp variables */
    Map<String, String> savedMappings;
    BiMap<String, String> oldToNew;
    Map<String, Integer> oldChildCount;

    public MappingGenerator(List<AnalysedClass> oldVersion, List<AnalysedClass> newVersion) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    public List<MappedClass> generate() {
        // init temp vars
        oldToNew = HashBiMap.create();
        savedMappings = new HashMap<>();
        oldChildCount = new HashMap<>();
        // Order both arrays by the amount of data provided
        Collections.sort(oldVersion, new StringConstantComparator());
        Collections.sort(newVersion, new StringConstantComparator());

        for (int stage = 0; stage < 8; stage++) { // does all stages twice
            ListIterator<AnalysedClass> oldClasses = oldVersion.listIterator();
            while (oldClasses.hasNext()) {
                ListIterator<AnalysedClass> newClasses = newVersion.listIterator();
                AnalysedClass old = oldClasses.next();
                // record for child count
                if (stage == 0) {
                    if (!old.getSuperclass().equals("java.lang.Object") && !old.getName().contains("$")) {
                        if (!oldChildCount.containsKey(old.getSuperclass())) {
                            oldChildCount.put(old.getSuperclass(), 1);
                        } else {
                            oldChildCount.put(old.getSuperclass(), oldChildCount.get(old.getSuperclass()) + 1);
                        }
                    }
                }
                newLoop:
                while (newClasses.hasNext()) {
                    AnalysedClass newC = newClasses.next();
                    if (compare(old, newC, stage)) {
                        newClasses.remove();
                        oldClasses.remove();
                        oldToNew.put(old.getName(), newC.getName());
                        if (!old.getSuperclass().equals("java.lang.Object") && !old.getName().contains("$")) {
                            oldChildCount.put(old.getSuperclass(), oldChildCount.get(old.getSuperclass()) - 1);
                        }
                        break newLoop;
                    }
                }
            }
        }
        System.out.println("Failed to map " + oldVersion.size() + " old to " + newVersion.size() + " new");
        // Generate mappings
        List<MappedClass> output = new ArrayList<>();
        for (Map.Entry<String, String> entry : oldToNew.entrySet()) {
            output.add(new MappedClass(entry.getKey(), entry.getValue()));
        }
        return output;
    }

    private boolean compare(AnalysedClass old, AnalysedClass newC, int stage) {
        if (old.getName().contains("$")) {
            if (Character.isDigit(old.getName().replace(".", "/").split("\\$")[1].charAt(0))) {
                return false;
            }
        }
        // Don't use inner classes on the first 2 stages
        if(stage < 2){
            if (old.getName().contains("$")) {
                return false;
            }
        }
        // Prevent some false positives by checking parent of inner classes
        if(old.getName().contains("$")){
            if(oldToNew.containsKey(old.getName().split("\\$")[0])){
                String newP = oldToNew.get(old.getName().split("\\$")[0]);
                if(!newP.split("\\$")[0].equals(newC.getName().split("\\$")[0]))
                    return false;
            }
        }

        // Using any known inner classes, automatically decode the parent
        if (!old.getName().contains("$")) { // Doesn't allow inner classes
            if (savedMappings.containsKey(old.getName())) {
                String mapped = savedMappings.get(old.getName());
                if (mapped.equals(newC.getName())) {
                    System.out.println("Matched Inner / Parent " + old.getName() + " to " + newC.getName());
                    return true;
                }
            }
        }
        // If the super class is mapped
        if (oldToNew.containsKey(old.getSuperclass()) && !old.getName().contains("$")) {
            String newSuper = oldToNew.get(old.getSuperclass());
            // If the older super only has 1 child
            if (oldChildCount.containsKey(old.getSuperclass())) {
                if (oldChildCount.get(old.getSuperclass()) == 1) {
                    if (newSuper.equals(newC.getSuperclass())) {
                        System.out.println("Matched using 1 child " + old.getName() + " to " + newC.getName());
                        return true;
                    }
                }
            }
        }

        // Don't match inner classes to outer.
        if (old.getName().contains("$") && !newC.getName().contains("$") || !old.getName().contains("$") && newC.getName().contains("$"))
            return false;

        int threshold = 80;

        // Stage 1: Identify info based on the constants
        if (stage == 0 || stage == 4) {
            int stringMatches = 0;
            for (String s : old.getStringConstants()) {
                if (newC.getStringConstants().contains(s)) {
                    stringMatches++;
                }
            }
            int total = old.getStringConstants().size() + old.getFloatConstants().size() + old.getLongConstants().size() + old.getDoubleConstants().size() + old.getIntegerConstants().size();
            int totalNew = newC.getStringConstants().size() + newC.getFloatConstants().size() + newC.getLongConstants().size() + newC.getDoubleConstants().size() + newC.getIntegerConstants().size();
            if (within(total, totalNew)) {
                if (old.getStringConstants().size() > 1) {
                    double percentage = ((double) stringMatches / (double) old.getStringConstants().size()) * 100D;
                    if (percentage > threshold) {
                        if (old.getName().contains("$")) {
                            // Mapped inner class
                            savedMappings.put(old.getName().split("\\$")[0], newC.getName().split("\\$")[0]);
                        }
                        // Save parent classes
                        if (!old.getSuperclass().equals("java.lang.Object")) {
                            savedMappings.put(old.getSuperclass(), newC.getSuperclass());
                        }
                        if (old.getInterfaces().size() == newC.getInterfaces().size()) {
                            int x = 0;
                            for (String s : old.getInterfaces()) {
                                savedMappings.put(s, newC.getInterfaces().get(x));
                                x++;
                            }
                        }
                        System.out.println("Matched " + old.getName() + " to " + newC.getName() + " " + percentage + "% " + total + " n: " + totalNew);
                        return true;
                    }
                }
                int longMatches = 0;
                for (Long s : old.getLongConstants()) {
                    if (newC.getLongConstants().contains(s)) {
                        longMatches++;
                    }
                }
                if (old.getLongConstants().size() > 4) {
                    double percentage = ((double) longMatches / (double) old.getLongConstants().size()) * 100D;
                    if (percentage > threshold) {
                        if (old.getName().contains("$")) {
                            // Mapped inner class
                            savedMappings.put(old.getName().split("\\$")[0], newC.getName().split("\\$")[0]);
                        }
                        // Save parent classes
                        if (!old.getSuperclass().equals("java.lang.Object")) {
                            savedMappings.put(old.getSuperclass(), newC.getSuperclass());
                        }
                        if (old.getInterfaces().size() == newC.getInterfaces().size()) {
                            int x = 0;
                            for (String s : old.getInterfaces()) {
                                savedMappings.put(s, newC.getInterfaces().get(x));
                                x++;
                            }
                        }
                        System.out.println("Matched Long " + old.getName() + " to " + newC.getName() + " " + percentage + "% " + old.getLongConstants().size() + " " + total + " n: " + totalNew);
                        return true;
                    }
                }

                int doubleMatches = 0;
                for (Double s : old.getDoubleConstants()) {
                    if (newC.getDoubleConstants().contains(s)) {
                        doubleMatches++;
                    }
                }
                if (old.getDoubleConstants().size() > 8) {
                    double percentage = ((double) doubleMatches / (double) old.getDoubleConstants().size()) * 100D;
                    if (percentage > threshold) {
                        if (old.getName().contains("$")) {
                            // Mapped inner class
                            savedMappings.put(old.getName().split("\\$")[0], newC.getName().split("\\$")[0]);
                        }
                        // Save parent classes
                        if (!old.getSuperclass().equals("java.lang.Object")) {
                            savedMappings.put(old.getSuperclass(), newC.getSuperclass());
                        }
                        if (old.getInterfaces().size() == newC.getInterfaces().size()) {
                            int x = 0;
                            for (String s : old.getInterfaces()) {
                                savedMappings.put(s, newC.getInterfaces().get(x));
                                x++;
                            }
                        }
                        System.out.println("Matched Double " + old.getName() + " to " + newC.getName() + " " + percentage + "%" + " " + total + " n: " + totalNew);
                        return true;
                    }
                }

                int floatMatches = 0;
                for (Float s : old.getFloatConstants()) {
                    if (newC.getFloatConstants().contains(s)) {
                        floatMatches++;
                    }
                }
                if (old.getFloatConstants().size() > 4) {
                    double percentage = ((double) floatMatches / (double) old.getFloatConstants().size()) * 100D;
                    if (percentage > threshold) {
                        if (old.getName().contains("$")) {
                            // Mapped inner class
                            savedMappings.put(old.getName().split("\\$")[0], newC.getName().split("\\$")[0]);
                        }
                        // Save parent classes
                        if (!old.getSuperclass().equals("java.lang.Object")) {
                            savedMappings.put(old.getSuperclass(), newC.getSuperclass());
                        }
                        if (old.getInterfaces().size() == newC.getInterfaces().size()) {
                            int x = 0;
                            for (String s : old.getInterfaces()) {
                                savedMappings.put(s, newC.getInterfaces().get(x));
                                x++;
                            }
                        }
                        System.out.println("Matched Float " + old.getName() + " to " + newC.getName() + " " + percentage + "%" + " " + total + " n: " + totalNew);
                        return true;
                    }
                }

                int integerMatches = 0;
                for (Integer s : old.getIntegerConstants()) {
                    if (newC.getIntegerConstants().contains(s)) {
                        integerMatches++;
                    }
                }
                if (old.getIntegerConstants().size() > 4) {
                    double percentage = ((double) integerMatches / (double) old.getIntegerConstants().size()) * 100D;
                    if (percentage > threshold) {
                        if (old.getName().contains("$")) {
                            // Mapped inner class
                            savedMappings.put(old.getName().split("\\$")[0], newC.getName().split("\\$")[0]);
                        }
                        // Save parent classes
                        if (!old.getSuperclass().equals("java.lang.Object")) {
                            savedMappings.put(old.getSuperclass(), newC.getSuperclass());
                        }
                        if (old.getInterfaces().size() == newC.getInterfaces().size()) {
                            int x = 0;
                            for (String s : old.getInterfaces()) {
                                savedMappings.put(s, newC.getInterfaces().get(x));
                                x++;
                            }
                        }
                        System.out.println("Matched Int " + old.getName() + " to " + newC.getName() + " " + percentage + "%" + " " + total + " n: " + totalNew);
                        return true;
                    }
                }

                // Compare all constants
                int totalMatched = stringMatches + floatMatches + longMatches + doubleMatches + integerMatches;
                if (total > 10) {
                    double percentage = ((double) totalMatched / (double) total) * 100D;
                    if (percentage > threshold) {
                        if (old.getName().contains("$")) {
                            // Mapped inner class
                            savedMappings.put(old.getName().split("\\$")[0], newC.getName().split("\\$")[0]);
                        }
                        // Save parent classes
                        if (!old.getSuperclass().equals("java.lang.Object")) {
                            savedMappings.put(old.getSuperclass(), newC.getSuperclass());
                        }
                        if (old.getInterfaces().size() == newC.getInterfaces().size()) {
                            int x = 0;
                            for (String s : old.getInterfaces()) {
                                savedMappings.put(s, newC.getInterfaces().get(x));
                                x++;
                            }
                        }
                        System.out.println("Matched All " + old.getName() + " to " + newC.getName() + " " + percentage + "%" + " " + total + " n: " + totalNew);
                        return true;
                    }
                }
            }

        }
        return false;
    }

    private boolean within(int total, int totalNew) {
        double diff = Math.abs(total - totalNew);
        double fract = diff / (double) totalNew;
        return fract <= 0.30D;
    }
}
