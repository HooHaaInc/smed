package mx.uson.cc.smed;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
        //OLDER
        materias.put(Tarea.COURSE_SPANISH + "_old",
                TextDrawable.builder()
                        .beginConfig().textColor(Color.BLACK).endConfig()
                        .buildRound("E", Color.LTGRAY));
        materias.put(Tarea.COURSE_MATH+"_old",
                TextDrawable.builder()
                        .beginConfig().textColor(Color.BLACK).endConfig()
                        .buildRound("M", Color.LTGRAY));
        materias.put(Tarea.COURSE_GEOGRAFY+"_old",
                TextDrawable.builder()
                        .beginConfig().textColor(Color.BLACK).endConfig()
                        .buildRound("G", Color.LTGRAY));
        materias.put(Tarea.COURSE_HISTORY+"_old",
                TextDrawable.builder()
                        .beginConfig().textColor(Color.BLACK).endConfig()
                        .buildRound("H", Color.LTGRAY));
        materias.put(Tarea.COURSE_NSCIENCES+"_old",
                TextDrawable.builder()
                        .beginConfig().textColor(Color.BLACK).endConfig()
                        .buildRound("C", Color.LTGRAY));
        materias.put(Tarea.COURSE_UNKNOWN,
                TextDrawable.builder()
                        .beginConfig().textColor(Color.BLACK).endConfig()
                        .buildRound("?", Color.LTGRAY));
    }

    private long now;

    public HomeworkListAdapter(Context context, int textViewResourceId, List<Tarea> items) {
        super(context, textViewResourceId, items);
        now = new Date().getTime();
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_row_item, parent, false);
        }else{
            ((TextView) convertView.findViewById(R.id.textViewItem))
                    .setTypeface(null, Typeface.BOLD);
            convertView.findViewById(R.id.dateViewItem)
                    .setVisibility(View.VISIBLE);
        }
        // object item based on the position
        Tarea tarea = getItem(position);
        // get the TextView and then set the text (item name)
        ImageView imageView = (ImageView)convertView.findViewById(R.id.imageViewItem);
        if(Tarea.isUnknown(tarea.materia))
            imageView.setImageDrawable(materias.get(Tarea.COURSE_UNKNOWN));
        else imageView.setImageDrawable(materias.get(tarea.materia));

        TextView textViewItem = (TextView) convertView.findViewById(R.id.descViewItem);
        textViewItem.setText(tarea.desc);

        textViewItem = (TextView) convertView.findViewById(R.id.textViewItem);
        textViewItem.setText(tarea.getTitulo());

        long time = (tarea.fecha.getTime() - now);
        if(time < 0){
            textViewItem.setTypeface(null, Typeface.NORMAL);
            textViewItem = (TextView) convertView.findViewById(R.id.dateViewItem);
            textViewItem.setVisibility(View.INVISIBLE);
            imageView.setImageDrawable(getOldDrawable(tarea.materia));
            return convertView;
        }
        textViewItem = (TextView) convertView.findViewById(R.id.dateViewItem);

        int showTime = (int)(time/(1000*60*60*24)) + 1; //dias restantes

        String timeLeft;
        if(showTime >= 30){
            showTime/=30;
            timeLeft = showTime+ " " + parent.getResources().getQuantityString(R.plurals.months, showTime);
        }else if(showTime >= 7){
            showTime/=7;
            timeLeft = showTime+" "+parent.getResources().getQuantityString(R.plurals.weeks, showTime);
        }else{
            timeLeft = showTime+" "+parent.getResources().getQuantityString(R.plurals.days, showTime);
        }

        textViewItem.setText(timeLeft);
        return convertView;
    }

    TextDrawable getOldDrawable(String course){
        return materias.get(course + "_old");
    }

}
