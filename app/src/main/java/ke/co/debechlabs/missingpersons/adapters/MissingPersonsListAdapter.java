package ke.co.debechlabs.missingpersons.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.List;

import ke.co.debechlabs.missingpersons.Config.Config;
import ke.co.debechlabs.missingpersons.MissingPersonsListActivity;
import ke.co.debechlabs.missingpersons.R;
import ke.co.debechlabs.missingpersons.app.AppController;
import ke.co.debechlabs.missingpersons.models.Person;

/**
 * Created by chriz on 4/16/2017.
 */

public class MissingPersonsListAdapter extends BaseAdapter implements Filterable{

    Activity activity;
    List<Person> persons;
    LayoutInflater inflater;
    ImageLoader imageLoader;
    List<Person> filteredList;
    private MissingPersonFilter missingPersonFilter;

    public MissingPersonsListAdapter(Activity activity, List<Person> personList){
        this.activity = activity;
        this.persons = personList;
        this.filteredList = personList;
        imageLoader= AppController.getInstance().getImageLoader();

        getFilter();
    }
    @Override
    public int getCount() {
        return this.filteredList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.filteredList.get(position);
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
            convertView = inflater.inflate(R.layout.missing_person_list_item, null);

        TextView txtMissingName = (TextView) convertView.findViewById(R.id.lost_person_name);
        TextView txtMissingDate = (TextView) convertView.findViewById(R.id.date_reported);
        TextView txtPersonID = (TextView) convertView.findViewById(R.id.personID);
        NetworkImageView imgPerson = (NetworkImageView) convertView.findViewById(R.id.list_image);

        Person person = filteredList.get(position);

        String reported_date = Config.FormatDateString(person.getReported_date(), "yyyy-MM-dd HH:mm:ss", "MMMM dd, yyyy");;

        txtMissingName.setText(person.getPersonname());
        txtMissingDate.setText("Reported On: " + reported_date);
        txtPersonID.setText(String.valueOf(person.getId()));
        imgPerson.setImageUrl(person.getImage_url(), imageLoader);

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (missingPersonFilter == null){
            missingPersonFilter = new MissingPersonFilter();
        }

        return missingPersonFilter;
    }

    private class MissingPersonFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                List<Person> tempList = new ArrayList<Person>();
                for (Person person : persons){
                    if (person.getPersonname().toLowerCase().contains(constraint.toString().toLowerCase())){
                        tempList.add(person);
                    }

                    filterResults.count = tempList.size();
                    filterResults.values = tempList;
                }
            }else{
                filterResults.count = persons.size();
                filterResults.values = persons;
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<Person>) results.values;
            notifyDataSetChanged();
        }
    }
}
