package mx.uson.cc.smed;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Jorge on 9/16/2015.
 */
public class HomeworkFragment extends Fragment {
    String desc;
    String titulo;
    public HomeworkFragment(){


    }
    @Override
    public void setArguments(Bundle b){
        desc = b.getString("Titulo");
        titulo = b.getString("Descripcion");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_homework, container, false);
        TextView titulo;
        titulo = (TextView) view.findViewById(R.id.titulo);
        titulo.setText(this.titulo);
        TextView desc;
        desc = (TextView) view.findViewById(R.id.desc);
        desc.setText(this.desc);
        System.out.println(desc.getText());
        System.out.println(this.desc);

        return view;
    }
}
