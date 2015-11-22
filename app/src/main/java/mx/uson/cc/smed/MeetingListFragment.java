package mx.uson.cc.smed;
import android.support.v4.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;

import mx.uson.cc.smed.util.Reporte;
import mx.uson.cc.smed.util.ResourcesMan;

/**
 * Created by Jorge on 10/3/2015.
 */
public class MeetingListFragment extends ListFragment {
    public MeetingListFragment(){


    }
    public void setArguments(Bundle B){


    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        MeetingListAdapter adapter = new MeetingListAdapter(inflater.getContext(),R.layout.list_view_row_report_item,
                ResourcesMan.getJuntas());
        setListAdapter(adapter);
        getActivity().setTitle(R.string.meetings);
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ReadMeetingFragment  rrf = new ReadMeetingFragment();
        Bundle b  = new Bundle();
        b.putSerializable("frag",ReadMeetingFragment.class);
        b.putInt("Position", position);
        MainActivity a = (MainActivity)getActivity();
        try {
            ((MainActivity)getActivity()).changeFragments(b);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        }

        // TODO: ADD THE FRAGMENT WITH THE HOMEWORK DETAILS
    }


}
