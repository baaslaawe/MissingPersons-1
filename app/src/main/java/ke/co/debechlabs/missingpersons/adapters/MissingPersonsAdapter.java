package ke.co.debechlabs.missingpersons.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import ke.co.debechlabs.missingpersons.MissingPersonsListActivity;
import ke.co.debechlabs.missingpersons.R;
import ke.co.debechlabs.missingpersons.app.ImageLoader;

/**
 * Created by chriz on 4/11/2017.
 */

public class MissingPersonsAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    ImageLoader imageLoader;

    public MissingPersonsAdapter(Activity a, ArrayList<HashMap<String, String>> d){
        activity = a;
        data = d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (convertView == null)
            v = inflater.inflate(R.layout.missing_person_list_item, null);

        TextView txtMissingName = (TextView) v.findViewById(R.id.lost_person_name);
        TextView txtMissingDate = (TextView) v.findViewById(R.id.date_reported);
        ImageView imgPerson = (ImageView) v.findViewById(R.id.list_image);

        HashMap<String, String> person = new HashMap<String, String>();
        person = data.get(position);

        txtMissingName.setText(person.get(MissingPersonsListActivity.KEY_NAME));
        txtMissingDate.setText(person.get(MissingPersonsListActivity.KEY_DATE));
        imageLoader.DisplayImage(person.get(MissingPersonsListActivity.KEY_THUMB), imgPerson);
        return v;
    }
}
