package mx.uson.cc.smed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_row_report_item, parent, false);
        }
        // object item based on the position
        Reporte reporte = getItem(position);
        TextView text = (TextView) convertView.findViewById(R.id.report_student_name);
        text.setText(reporte.getAcusador());
        text = (TextView) convertView.findViewById(R.id.report_date);
        SimpleDateFormat form = new SimpleDateFormat("dd-MM");
        text.setText(form.format(reporte.getFecha()));
        return convertView;
    }
}
