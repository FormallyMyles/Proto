package us.myles.proto.analyser;

import java.util.ArrayList;
import java.util.List;

public class AnalysedClass {
    private String name;
    private String superclass;
    private List<String> interfaces = new ArrayList<String>();
    private List<String> constants = new ArrayList<String>();

    public AnalysedClass(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<String> getConstants() {
        return constants;
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
