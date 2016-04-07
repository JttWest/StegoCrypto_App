package cpen391_21.stegocrypto.DataTransferHistory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cpen391_21.stegocrypto.R;

public class DataTransferHistoryArrayAdapter extends ArrayAdapter<DataTransferHistoryItem> {
    private Context context ;
    private ArrayList<DataTransferHistoryItem> dataTransferHistoryArray;

    public DataTransferHistoryArrayAdapter ( Context _context, int textViewResourceId,
                                             ArrayList<DataTransferHistoryItem> _dataTransferHistoryArray) {
        super(_context, textViewResourceId,  _dataTransferHistoryArray);

        context = _context;
        dataTransferHistoryArray = _dataTransferHistoryArray;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE );
        View row = inflater.inflate ( R.layout.history_row, parent, false );
        TextView actionTV = (TextView) row.findViewById(R.id.action);
        TextView usernameTV = (TextView) row.findViewById(R.id.username);
        TextView dateTV = (TextView) row.findViewById(R.id.date);

        actionTV.setText(dataTransferHistoryArray.get(position).action);
        usernameTV.setText(dataTransferHistoryArray.get(position).username);
        dateTV.setText(dataTransferHistoryArray.get(position).date);

        int h = row.getHeight();
        ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());

        if (dataTransferHistoryArray.get(position).action.equals("TO: ")) {
            mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#880000FF"), Color.parseColor("#110000FF"), Shader.TileMode.REPEAT));
        } else {
            mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#330000AA"), Color.parseColor("#110000AA"), Shader.TileMode.REPEAT));
        }

        row.setBackground(mDrawable);

        /* Example from reference -- remove after demo
        ImageView icon = (ImageView) row.findViewById (R.id.BTicon);
        icon.setImageResource (R.drawable.bluetooth);
        icon.setVisibility (View.VISIBLE);
        TextView label = (TextView) row.findViewById( R.id.BTdeviceText);
        label.setText (theStringArray.get(position)));
        icon = (ImageView) row.findViewById (R.id.Selected);
        icon.setImageResource (R.drawable.redcross);
        icon.setVisibility (View.VISIBLE);*/

        return row;
    }

}
