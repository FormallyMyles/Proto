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
        // Order both arrays by the amount of data provided
        Collections.sort(oldVersion, new StringConstantComparator());
        Collections.sort(newVersion, new StringConstantComparator());

        ListIterator<AnalysedClass> oldClasses = oldVersion.listIterator();
        while (oldClasses.hasNext()) {
            ListIterator<AnalysedClass> newClasses = newVersion.listIterator();
            AnalysedClass old = oldClasses.next();

            newLoop:
            while (newClasses.hasNext()) {
                AnalysedClass newC = newClasses.next();
                if (compare(oldToNew, old, newC)) {
                    newClasses.remove();
                    oldClasses.remove();
                    oldToNew.put(old.getName(), newC.getName());
                    break newLoop;
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

    private boolean compare(Map<String, String> oldToNew, AnalysedClass old, AnalysedClass newC) {
        if(old.getConstants().size() > 1){
            int matches = 0;
            for(String s:old.getConstants()){
                if(newC.getConstants().contains(s)){
                    matches++;
                }
            }
            double percentage = ((double)matches/(double)old.getConstants().size()) * 100D;
            if(percentage > 50){
                System.out.println("Matched " + old.getName() + " to " + newC.getName() + " " + percentage + "%");
                return true;
            }
        }
        return false;
    }
}
