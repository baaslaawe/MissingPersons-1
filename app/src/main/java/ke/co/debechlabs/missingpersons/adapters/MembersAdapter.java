package ke.co.debechlabs.missingpersons.adapters;

import android.content.Context;
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

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import ke.co.debechlabs.missingpersons.Config.Config;
import ke.co.debechlabs.missingpersons.R;
import ke.co.debechlabs.missingpersons.ViewFamilyMember;
import ke.co.debechlabs.missingpersons.app.AppController;
import ke.co.debechlabs.missingpersons.models.Member;

/**
 * Created by chriz on 4/19/2017.
 */

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MyViewHolder> {
    Context context;
    List<Member> memberList;
    ImageLoader imageLoader;

    public MembersAdapter(Context mContext, List<Member> mMemberList){
        this.context = mContext;
        this.memberList = mMemberList;
        imageLoader = AppController.getInstance().getImageLoader();
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_card_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Member member = this.memberList.get(position);

        holder.txtMemberName.setText(member.getMember_name());
        holder.txtMemberRelationship.setText(member.getMember_relationship());
        holder.imgMemberImage.setImageUrl(Config.server_url + member.getMember_photo(), imageLoader);
        holder.imgOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(holder.imgOverflow, member.getId());
            }
        });
    }

    private void showPopupMenu(View view, int member_id){
        PopupMenu popupMenu = new PopupMenu(context, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.family_member_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new MyMenuItemClickListener(member_id));
//        popupMenu.show();
    }

    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener{
        private int member_id;
        public MyMenuItemClickListener(int id) {
            this.member_id = id;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()){
                case R.id.action_report_missing:

                    break;
                case R.id.action_view_member:
                    Intent intent = new Intent(context, ViewFamilyMember.class);
                    intent.putExtra("id", String.valueOf(member_id));
                    context.startActivity(intent);
                    break;
            }
            return true;
        }
    }

    @Override
    public int getItemCount() {
        return this.memberList.size();
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
