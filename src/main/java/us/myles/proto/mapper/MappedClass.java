package us.myles.proto.mapper;

public class MappedClass {
    private String oldName;
    private String newName;

    public MappedClass(String oldName, String newName) {
        this.oldName = oldName;
        this.newName = newName;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }
}
