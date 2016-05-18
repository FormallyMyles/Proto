package us.myles.proto.base;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class JarReader {
    private final ClassPool pool;
    private List<String> classes;

    public JarReader(File jar, String filter) throws NotFoundException, IOException {
        this.pool = new ClassPool(true);
        this.pool.insertClassPath(jar.getAbsolutePath());

        // Index all the class files in this jar.
        JarFile jarFile = new JarFile(jar);
        Enumeration<JarEntry> iterator = jarFile.entries();

        this.classes = new ArrayList<String>();

        while (iterator.hasMoreElements()) {
            JarEntry file = iterator.nextElement();
            if (file.getName().endsWith(".class")) {
                String name = file.getName().replace("/", ".").replace(".class", "");
                if (filter.equals(".") && name.contains(".")) {
                    continue;
                }
                if (filter.equals("*") && !name.startsWith(filter))
                    continue;
                classes.add(name);
            }
        }
    }

    protected ClassPool getClassPool() {
        return this.pool;
    }

    protected void visitAll() {
        for (String name : classes) {
            try {
                CtClass ctClass = pool.get(name);
                visit(ctClass);
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void visit(CtClass ctClass);
}
