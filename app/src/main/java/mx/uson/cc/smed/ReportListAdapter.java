package mx.uson.cc.smed;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import mx.uson.cc.smed.textdrawable.TextDrawable;
import mx.uson.cc.smed.util.Reporte;

/**
 * Created by Jorge on 10/3/2015.
 */
public class ReportListAdapter extends ArrayAdapter<Reporte> {

    public ReportListAdapter(Context context, int textViewResourceId, List<Reporte> items) {
        super(context, textViewResourceId, items);
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_two_line_with_avatar, parent, false);
        }

        Reporte reporte = getItem(position);
        String[] split = reporte.getAcusador().split(" ");
        String initials = ""+ split[0].charAt(0) + (split.length > 1 ?
                split[1].charAt(0) : "");
        ((ImageView)convertView.findViewById(R.id.icon))
                .setImageDrawable(TextDrawable.builder().buildRound(initials, Color.LTGRAY));
        TextView text = (TextView) convertView.findViewById(R.id.title);
        SimpleDateFormat form = new SimpleDateFormat("dd-MM");
        text.setText(form.format(reporte.getFecha()));
        text = (TextView) convertView.findViewById(R.id.desc);
        text.setText(reporte.getDescripcion());
        return convertView;
    }
}
