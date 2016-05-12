package us.myles.proto.analyser;

import javassist.CtClass;
import javassist.NotFoundException;
import us.myles.proto.base.JarReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JarAnalyser extends JarReader {
    private List<AnalysedClass> analysedClassList = new ArrayList<AnalysedClass>();

    public JarAnalyser(File jar) throws NotFoundException, IOException {
        super(jar);
    }

    protected void visit(CtClass ctClass) {
//        System.out.println("Analysing " + ctClass.getName());
        AnalysedClass analysedClass = new AnalysedClass(ctClass.getName());
        // Save interfaces / superclass
        analysedClass.setSuperclass(ctClass.getClassFile().getSuperclass());
        analysedClass.getInterfaces().addAll(Arrays.asList(ctClass.getClassFile().getInterfaces()));
        // Save string constants
        for (int i = 0; i < ctClass.getClassFile().getConstPool().getSize(); i++) {
            Object value = ctClass.getClassFile().getConstPool().getLdcValue(i);
            if (value instanceof String) {
                analysedClass.getConstants().add((String) value);
            }
        }

        analysedClassList.add(analysedClass);
    }

    public List<AnalysedClass> analyse() {
        // clear
        analysedClassList.clear();
        // visit
        visitAll();
        return analysedClassList;
    }
}
