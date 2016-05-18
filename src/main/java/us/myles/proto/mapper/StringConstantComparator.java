package us.myles.proto.mapper;

import us.myles.proto.analyser.AnalysedClass;

public class StringConstantComparator implements java.util.Comparator<AnalysedClass> {

    @Override
    public int compare(AnalysedClass o1, AnalysedClass o2) {
        return o2.getConstants().size() - o1.getConstants().size();
    }
}
