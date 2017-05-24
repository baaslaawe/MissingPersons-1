package ke.co.debechlabs.missingpersons.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import ke.co.debechlabs.missingpersons.R;
import ke.co.debechlabs.missingpersons.models.Contact;

/**
 * Created by chriz on 5/24/2017.
 */

public class ContactListAdapter extends BaseAdapter {
    Activity activity;
    List<Contact> contactList;
    LayoutInflater inflater;

    public ContactListAdapter(Activity activity, List<Contact> contactList){
        this.contactList = contactList;
        this.activity = activity;
    }
    @Override
    public int getCount() {
        return contactList.size();
    }

    @Override
    public Object getItem(int position) {
        return contactList.get(position);
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
            convertView = inflater.inflate(R.layout.contact_list_item, null);
        TextView txtName = (TextView) convertView.findViewById(R.id.name);
        TextView txtContact = (TextView) convertView.findViewById(R.id.contact);

        Contact contact = contactList.get(position);

        txtName.setText(contact.getName());
        txtContact.setText(contact.getContact());
        return convertView;
    }
}
