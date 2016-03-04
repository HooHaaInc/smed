package mx.uson.cc.smed.util;

import java.io.Serializable;

/**
 * Created by nancio on 4/11/15.
 */
public class Student implements Serializable{
    private String name, lastName1, lastName2;
    private int id;

    private String tutor = "";
    private String noTutor = "";
    private String correoT = "";

    public Student(int id, String n, String l1, String l2,String tutor,String numTutor,String correoTu){
        this.id = id;
        name = n;
        lastName1 = l1;
        lastName2 = l2;
        this.tutor = tutor;
        noTutor = numTutor;
        correoT = correoTu;
    }

    public String getName() {
        return name;
    }

    public String getLastName1() {
        return lastName1;
    }

    public String getLastName2() {
        return lastName2;
    }

    public int getId() {
        return id;
    }

    public String getInitials(){
        return ""+name.charAt(0) + lastName1.charAt(0);
    }

     public String getTutor(){
        return tutor;
    }

    public String getNumero(){
        return noTutor;
    }

    public String getCorreoT(){
        return correoT;
    }

    @Override
    public String toString() {
        return name + " " + lastName1 + (lastName2 != null && lastName2.length() > 0
                ?" "+lastName2 : "");
    }
}
