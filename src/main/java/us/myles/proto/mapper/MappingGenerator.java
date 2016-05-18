package us.myles.proto.mapper;

import us.myles.proto.analyser.AnalysedClass;

import java.util.*;

public class MappingGenerator {
    private final List<AnalysedClass> oldVersion;
    private final List<AnalysedClass> newVersion;

    public MappingGenerator(List<AnalysedClass> oldVersion, List<AnalysedClass> newVersion) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    public List<MappedClass> generate() {
        Map<String, String> oldToNew = new HashMap<>();
        Map<String, String> savedMappings = new HashMap<>();
        // Order both arrays by the amount of data provided
        Collections.sort(oldVersion, new StringConstantComparator());
        Collections.sort(newVersion, new StringConstantComparator());

        for (int stage = 0; stage < 2; stage++) {
            ListIterator<AnalysedClass> oldClasses = oldVersion.listIterator();
            while (oldClasses.hasNext()) {
                ListIterator<AnalysedClass> newClasses = newVersion.listIterator();
                AnalysedClass old = oldClasses.next();
                newLoop:
                while (newClasses.hasNext()) {
                    AnalysedClass newC = newClasses.next();
                    if (compare(oldToNew, old, newC, savedMappings, stage)) {
                        newClasses.remove();
                        oldClasses.remove();
                        oldToNew.put(old.getName(), newC.getName());
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

    private boolean compare(Map<String, String> oldToNew, AnalysedClass old, AnalysedClass newC, Map<String, String> savedMappings, int i) {
        // Don't match inner classes to outer.
        if (old.getName().contains("$") && !newC.getName().contains("$") || !old.getName().contains("$") && newC.getName().contains("$"))
            return false;

        // Stage 1: Identify info based on the constants
        if (i == 0) {
            // If it's a simple inner class match automatically deny
            if (old.getName().contains("$")) {
                if (Character.isDigit(old.getName().replace(".", "/").split("\\$")[1].charAt(0))) {
                    return false;
                }
            }
            if (old.getConstants().size() > 1) {
                int matches = 0;
                for (String s : old.getConstants()) {
                    if (newC.getConstants().contains(s)) {
                        matches++;
                    }
                }
                double percentage = ((double) matches / (double) old.getConstants().size()) * 100D;
                if (percentage > 50) {
                    if (old.getName().contains("$")) {
                        // Mapped inner class
                        savedMappings.put(old.getName().split("\\$")[0], newC.getName().split("\\$")[0]);
                    }
                    System.out.println("Matched " + old.getName() + " to " + newC.getName() + " " + percentage + "%");
                    return true;
                }
            }
            return false;
        }
        // Stage 2: Using any known inner classes, automatically decode the parent
        if (i == 1) {
            if (!old.getName().contains("$")) { // Doesn't allow inner classes
                if (savedMappings.containsKey(old.getName())) {
                    String mapped = savedMappings.get(old.getName());
                    return mapped.equals(newC.getName());
                }
            }
        }
        // Stage 3: Use inheritance outlining to get event more, :O!

        return false;
    }
}
