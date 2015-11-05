package mx.uson.cc.smed.util;

/**
 * Created by nancio on 1/11/15.
 */
public class Group {
    private String name;
    private String shift;
    private String teacher;
    private int id;

    public Group(int id, String n, String s, String t){
        name = n;
        shift = s;
        teacher = t;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getShift() {
        return shift;
    }

    public String getTeacher() {
        return teacher;
    }

    public int getId(){ return id; }

    @Override
    public String toString() {
        return teacher + " " + name + " " + shift;
    }
}
