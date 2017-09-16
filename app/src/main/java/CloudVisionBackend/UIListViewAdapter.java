package CloudVisionBackend;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mementoapp.main.mementoapp.R;

/**
 * Created by Daniel on 8/10/2017.
 */

public class UIListViewAdapter extends ArrayAdapter<UICheckBoxModel>{
    UICheckBoxModel[] modelItems = null;
    Context context;
    public UIListViewAdapter(Context context, UICheckBoxModel[] resource) {
        super(context, R.layout.listview_chkbx_row,resource);
        // TODO Auto-generated constructor stub
        this.context = context;
        this.modelItems = resource;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.listview_chkbx_row, parent, false);
        TextView name = (TextView) convertView.findViewById(R.id.textView_chkbxrow);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox_chkbxrow);
        name.setText(modelItems[position].getName());
        if(modelItems[position].getValue() == 1)
            cb.setChecked(true);
        else
            cb.setChecked(false);

        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(modelItems[position].getValue() == 0) modelItems[position].setValue(1);
                else if (modelItems[position].getValue() == 1) modelItems[position].setValue(0);
            }
        });

        return convertView;
    }
}
