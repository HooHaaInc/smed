package mx.uson.cc.smed;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jorge on 9/16/2015.
 */
public class HomeworkListAdapter extends ArrayAdapter<Tarea> {
    public HomeworkListAdapter(Context context, int textViewResourceId, ArrayList<Tarea> items) {
        super(context, textViewResourceId, items);
    }
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_row_item, parent, false);
        }
        // object item based on the position
        Tarea tarea = getItem(position);
        // get the TextView and then set the text (item name)
        TextView textViewItem = (TextView) convertView.findViewById(R.id.textViewItem);
        textViewItem.setText(tarea.getTitulo());
        return convertView;
    }


}
