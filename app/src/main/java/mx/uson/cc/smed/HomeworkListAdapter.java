package mx.uson.cc.smed;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import mx.uson.cc.smed.textdrawable.TextDrawable;

/**
 * Created by Jorge on 9/16/2015.
 */
public class HomeworkListAdapter extends ArrayAdapter<Tarea> {

    private static HashMap<String, TextDrawable> materias;

    static{
        materias = new HashMap<>();

        materias.put(Tarea.COURSE_SPANISH,
                TextDrawable.builder().buildRound("E", Tarea.COLOR_SPANISH));
        materias.put(Tarea.COURSE_MATH,
                TextDrawable.builder().buildRound("M", Tarea.COLOR_MATH));
        materias.put(Tarea.COURSE_GEOGRAFY,
                TextDrawable.builder().buildRound("G", Tarea.COLOR_GEOGRAFY));
        materias.put(Tarea.COURSE_HISTORY,
                TextDrawable.builder().buildRound("H", Tarea.COLOR_HISTORY));
        materias.put(Tarea.COURSE_NSCIENCES,
                TextDrawable.builder().buildRound("C", Tarea.COLOR_NSCIENCES));
    }

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
        ImageView imageView = (ImageView)convertView.findViewById(R.id.imageViewItem);
        imageView.setImageDrawable(materias.get(tarea.materia));
        TextView textViewItem = (TextView) convertView.findViewById(R.id.textViewItem);
        textViewItem.setText(tarea.getTitulo());
        textViewItem = (TextView) convertView.findViewById(R.id.descViewItem);
        textViewItem.setText(tarea.desc);
        textViewItem = (TextView) convertView.findViewById(R.id.dateViewItem);
        textViewItem.setText(tarea.fecha.toString());
        return convertView;
    }


}
