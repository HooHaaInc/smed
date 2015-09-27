package mx.uson.cc.smed;

import android.app.ListFragment;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends ListFragment{
    ArrayList<Tarea> tareas;
   public MainActivityFragment() {
        tareas = new ArrayList<>();
        tareas.add(new Tarea("Titulo1","desc1"));
        tareas.add(new Tarea("Titulo2","desc2"));
        tareas.add(new Tarea("Titulo3","desc3"));

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("derp");
        HomeworkListAdapter adapter = new HomeworkListAdapter(inflater.getContext(),
                android.R.layout.simple_list_item_1,tareas);
        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
        //viejo
        //return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        HomeworkFragment  hwf = new HomeworkFragment();

        Bundle b  = new Bundle();
        b.putString("Titulo",(tareas.get(position)).getTitulo());
        b.putString("Descripcion", (tareas.get(position)).getDesc());
        hwf.setArguments(b);
        MainActivity a = (MainActivity)getActivity();
        a.changeFragments(hwf);
        // TODO: ADD THE FRAGMENT WITH THE HOMEWORK DETAILS
    }
}
