package ke.co.debechlabs.missingpersons.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import ke.co.debechlabs.missingpersons.R;
import ke.co.debechlabs.missingpersons.models.Relationship;

/**
 * Created by chriz on 4/19/2017.
 */

public class SpinnerAdapter extends BaseAdapter {
    Activity activity;
    List<Relationship> relationshipList;
    private LayoutInflater layoutInflater;

    public SpinnerAdapter(Activity a, List<Relationship> relationships){
        this.activity = a;
        this.relationshipList = relationships;
        this.layoutInflater =(LayoutInflater)a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return relationshipList.size();
    }

    @Override
    public Object getItem(int position) {
        return relationshipList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder spinnerHolder;

        if(convertView == null) {
            spinnerHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.spinner_layout, parent, false);
            spinnerHolder.spinnerMainText = (TextView) convertView.findViewById(R.id.mainText);
            spinnerHolder.spinnerID  = (TextView) convertView.findViewById(R.id.contentID);
            convertView.setTag(spinnerHolder);
        }else{
            spinnerHolder = (ViewHolder)convertView.getTag();
        }

        Relationship relationship = relationshipList.get(position);

        spinnerHolder.spinnerID.setText(String.valueOf(relationship.get_id()));
        spinnerHolder.spinnerMainText.setText(relationship.get_relationship());
        return convertView;
    }

    class ViewHolder{
        TextView spinnerMainText;
        TextView spinnerID;
    }
}
