package mx.uson.cc.smed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener{

SwipeRefreshLayout swipeLayout;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        getActivity().setTitle(R.string.homeworks);



        View v = inflater.inflate(R.layout.fragment_main, container,false);



        swipeLayout = (SwipeRefreshLayout)v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return v;
    }

    /**
     * le dice a la activity que debe cambiar/actualizar el fragment compañero
     * (HomeworkFragment), le manda un bundle con la posicion de la tarea
     * en ResourcesMan y el fragment compañero
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Bundle b  = new Bundle();
        b.putSerializable("frag", HomeworkFragment.class);
        b.putInt("position", position);

        try {
            ((MainActivity)getActivity()).changeFragments(b);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        }
        // TODO: ADD THE FRAGMENT WITH THE HOMEWORK DETAILS
    }


    @Override
    public void onRefresh() {
        Log.v("",":3");
        new MainActivity.GetHomework(((MainActivity) getActivity())).execute();
        swipeLayout.setRefreshing(false);

    }
}
