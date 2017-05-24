package ke.co.debechlabs.missingpersons.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.List;

import ke.co.debechlabs.missingpersons.Config.Config;
import ke.co.debechlabs.missingpersons.MissingPersonDetailsActivity;
import ke.co.debechlabs.missingpersons.MissingPersonsListActivity;
import ke.co.debechlabs.missingpersons.MyListingsActivity;
import ke.co.debechlabs.missingpersons.R;
import ke.co.debechlabs.missingpersons.ViewFamilyMember;
import ke.co.debechlabs.missingpersons.app.AppController;
import ke.co.debechlabs.missingpersons.models.Person;

/**
 * Created by chriz on 5/24/2017.
 */

public class MissingPersonsRecyclerAdapter extends RecyclerView.Adapter<MissingPersonsRecyclerAdapter.MyViewHolder> {
    Context context;
    ImageLoader imageLoader;
    List<Person> personList;
    private MissingPersonsRecyclerAdapter adapter;

    public MissingPersonsRecyclerAdapter(Context c, List<Person> personList){
        this.context = c;
        this.personList = personList;
        imageLoader = AppController.getInstance().getImageLoader();
        this.adapter = this;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_card_layout, parent, false);
        return new MissingPersonsRecyclerAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Person person = personList.get(position);

        holder.txtMemberName.setText(person.getPersonname());
        holder.imgMemberImage.setImageUrl(Config.server_url + person.getImage_url(), this.imageLoader);
        holder.txtMemberRelationship.setText(person.getReported_date());
        if(person.getFound() == 0) {
            holder.imgOverflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu(holder.imgOverflow, person.getId());
                }
            });
        }else{
            holder.imgOverflow.setVisibility(View.GONE);
        }
    }

    private void showPopupMenu(View view, int person_id){
        PopupMenu popupMenu = new PopupMenu(context, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.my_listing_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new MissingPersonsRecyclerAdapter.MyMenuItemClickListener(person_id, view));
        popupMenu.show();
    }

    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener{
        private int person_id;
        private View v;
        public MyMenuItemClickListener(int id, View v) {
            this.person_id = id;
            this.v = v;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()){
                case R.id.action_view_missing:
                    Intent intent = new Intent(context, MissingPersonDetailsActivity.class);
                    intent.putExtra("id", person_id);
                    context.startActivity(intent);

                    break;
                case R.id.action_mark_as_completed:
                    new AlertDialog.Builder(context)
                            .setTitle("Confirmation")
                            .setMessage("Are you sure you want to mark person as found?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    final ProgressDialog pDialog = new ProgressDialog(context);
                                    pDialog.setMessage("Marking as complete...");
                                    pDialog.show();
                                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Config.server_url + "API/MissingPersons/markascomplete/" + person_id, null, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            pDialog.hide();
                                            Log.i("Response", response.toString());
                                            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                                            v.setVisibility(View.GONE);
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            pDialog.hide();
                                        }
                                    });

                                    AppController.getInstance().addToRequestQueue(jsonObjectRequest);
                                }})
                            .setNegativeButton(android.R.string.no, null).show();
                    break;
            }
            return true;
        }
    }

    @Override
    public int getItemCount() {
        return this.personList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView txtMemberName, txtMemberRelationship;
        public NetworkImageView imgMemberImage;
        public ImageView imgOverflow;

        public MyViewHolder(View itemView) {
            super(itemView);
            txtMemberName = (TextView) itemView.findViewById(R.id.member_name);
            txtMemberRelationship = (TextView) itemView.findViewById(R.id.member_relationship);
            imgMemberImage = (NetworkImageView) itemView.findViewById(R.id.member_image);
            imgOverflow = (ImageView) itemView.findViewById(R.id.overflow);
        }
    }

}
