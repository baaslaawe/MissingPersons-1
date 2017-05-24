package ke.co.debechlabs.missingpersons.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import ke.co.debechlabs.missingpersons.Config.Config;
import ke.co.debechlabs.missingpersons.R;
import ke.co.debechlabs.missingpersons.models.Sighting;

/**
 * Created by chriz on 5/24/2017.
 */

public class SightingListAdapter extends BaseAdapter {
    Activity activity;
    List<Sighting> sightingList;
    LayoutInflater inflater;

    public SightingListAdapter(Activity activity, List<Sighting> sightingList){
        this.activity = activity;
        this.sightingList = sightingList;
    }
    @Override
    public int getCount() {
        return this.sightingList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.sightingList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.sightings_list_item, null);

        TextView txtPlaceName = (TextView) convertView.findViewById(R.id.place_name);
        TextView txtDateSeen = (TextView) convertView.findViewById(R.id.seenDate);
        TextView txtDateSubmitted = (TextView) convertView.findViewById(R.id.submittedDate);

        Sighting sighting = this.sightingList.get(position);
        txtPlaceName.setText(sighting.getLocation());
        txtDateSeen.setText("Seen on: " + Config.FormatDateString(sighting.getDateseen(), "yyyy-MM-dd", "MMMM dd, yyyy"));
        txtDateSubmitted.setText("Reported on: " + Config.FormatDateString(sighting.getDateadded(), "yyyy-MM-dd HH:mm:ss", "MMMM dd, yyyy"));

        return convertView;
    }
}
