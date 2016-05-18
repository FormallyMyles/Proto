package us.myles.proto.analyser;

import javassist.*;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Opcode;
import javassist.expr.ExprEditor;
import javassist.expr.NewExpr;
import us.myles.proto.base.JarReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JarAnalyser extends JarReader {
    private List<AnalysedClass> analysedClassList = new ArrayList<AnalysedClass>();

    public JarAnalyser(File jar, String filter) throws NotFoundException, IOException {
        super(jar, filter);
    }

    protected void visit(CtClass ctClass) {
        System.out.println("Analysing " + ctClass.getName());
        AnalysedClass analysedClass = new AnalysedClass(ctClass.getName());
        // Save interfaces / superclass
        analysedClass.setSuperclass(ctClass.getClassFile().getSuperclass());
        analysedClass.getInterfaces().addAll(Arrays.asList(ctClass.getClassFile().getInterfaces()));
        // Save string constants
        for (int i = 0; i < ctClass.getClassFile().getConstPool().getSize(); i++) {
            Object value = ctClass.getClassFile().getConstPool().getLdcValue(i);
            if (value instanceof String) {
                analysedClass.getStringConstants().add((String) value);
            }
            if (value instanceof Long) {
                analysedClass.getLongConstants().add((Long) value);
            }
            if (value instanceof Double) {
                analysedClass.getDoubleConstants().add((Double) value);
            }
            if (value instanceof Float) {
                analysedClass.getFloatConstants().add((Float) value);
            }
            if (value instanceof Integer) {
                analysedClass.getIntegerConstants().add((Integer) value);
            }
        }
        // Analyse class for further constants not in constant pool
        for(CtMethod m:ctClass.getMethods()){
            CodeAttribute ca = m.getMethodInfo().getCodeAttribute();
            if(ca == null) continue; // empty method
            CodeIterator iter = ca.iterator();
            while(iter.hasNext()){
                try {
                    int index = iter.next();
                    int op = iter.byteAt(index);
                    switch(op){
                        case Opcode.BIPUSH:
                            analysedClass.getIntegerConstants().add(iter.byteAt(index + 1));
                            break;
                        case Opcode.SIPUSH:
                            analysedClass.getIntegerConstants().add(iter.s16bitAt(index + 1)); // maybe handle differently in future
                            break;
                        case Opcode.DCONST_0:
                            // 0.0
                            analysedClass.getDoubleConstants().add(0D);
                            break;
                        case Opcode.DCONST_1:
                            // 1.0
                            analysedClass.getDoubleConstants().add(1D);
                            break;
                        case Opcode.FCONST_0:
                            // 0.0f
                            analysedClass.getFloatConstants().add(0f);
                            break;
                        case Opcode.FCONST_1:
                            // 1.0f
                            analysedClass.getFloatConstants().add(1f);
                            break;
                        case Opcode.FCONST_2:
                            // 2.0f
                            analysedClass.getFloatConstants().add(2f);
                            break;
                        case Opcode.LCONST_0:
                            // 0L
                            analysedClass.getLongConstants().add(0L);
                            break;
                        case Opcode.LCONST_1:
                            // 1L
                            analysedClass.getLongConstants().add(1L);
                            break;
                    }
                } catch (BadBytecode badBytecode) {
                    badBytecode.printStackTrace();
                }
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
