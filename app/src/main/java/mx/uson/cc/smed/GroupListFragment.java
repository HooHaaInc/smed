package mx.uson.cc.smed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import mx.uson.cc.smed.util.Reporte;
import mx.uson.cc.smed.util.ResourcesMan;
import mx.uson.cc.smed.util.Student;

/**
 * Created by Hans on 03/03/2016.
 */
public class GroupListFragment extends ListFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        StudentListAdapter adapter = new StudentListAdapter(inflater.getContext(),R.layout.list_item_single_line_with_avatar,ResourcesMan.getEstudiantes());
        setListAdapter(adapter);
        getActivity().setTitle(R.string.students);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Bundle b  = new Bundle();
        ArrayList<Student> s = ResourcesMan.getEstudiantes();
        b.putSerializable("frag", ReadStudentFragment.class);
        b.putSerializable("estudiante", s.get(position));
        try {
            ((MainActivity)getActivity()).changeFragments(b, null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        }
    }
}
