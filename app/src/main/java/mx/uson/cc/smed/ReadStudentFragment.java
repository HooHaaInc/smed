package mx.uson.cc.smed;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import mx.uson.cc.smed.util.ResourcesMan;
import mx.uson.cc.smed.util.Student;

/**
 * Created by Hans on 03/03/2016.
 */
public class ReadStudentFragment extends Fragment {
    private String nombre;
    private String tutor;
    private String noTutor;
    private String correoT;

    @Override
    public void setArguments(Bundle b){
        Student ejtudiante = (Student)b.getSerializable("estudiante");
        nombre = ejtudiante.getName() + " " + ejtudiante.getLastName1() + (ejtudiante.getLastName2() != null ? ejtudiante.getLastName2() : "");
        tutor = ejtudiante.getTutor();
        noTutor = ejtudiante.getNumero();
        correoT = ejtudiante.getCorreoT();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        View view = inflater.inflate(R.layout.student_details, container, false);

        TextView nameText = (TextView)view.findViewById(R.id.user_name);
        nameText.setText(nombre);
        TextView tutor = (TextView)view.findViewById(R.id.parent_name);
        tutor.setText(this.tutor);
        TextView mail = (TextView)view.findViewById(R.id.parent_mail);
        mail.setText(correoT);
        TextView telefono = (TextView)view.findViewById(R.id.parent_phone);
        telefono.setText(noTutor);
        Log.d("1", nameText.getText().toString());
        Log.d("2", tutor.getText().toString());
        Log.d("3", mail.getText().toString());
        Log.d("4", telefono.getText().toString());

        return view;
    }
}
