package us.myles.proto.analyser;

import java.util.ArrayList;
import java.util.List;

public class AnalysedClass {
    private String name;
    private String superclass;
    private List<String> interfaces = new ArrayList<String>();
    private List<String> stringConstants = new ArrayList<String>();
    private List<Float> floatConstants = new ArrayList<Float>();
    private List<Double> doubleConstants = new ArrayList<Double>();
    private List<Integer> integerConstants = new ArrayList<Integer>();
    private List<Long> longConstants = new ArrayList<Long>();

    public AnalysedClass(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<String> getStringConstants() {
        return stringConstants;
    }

    public List<Float> getFloatConstants() {
        return floatConstants;
    }

    public List<Double> getDoubleConstants() {
        return doubleConstants;
    }

    public List<Integer> getIntegerConstants() {
        return integerConstants;
    }

    public List<Long> getLongConstants() {
        return longConstants;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public String getSuperclass() {
        return superclass;
    }

    public void setSuperclass(String superclass) {
        this.superclass = superclass;
    }
}
