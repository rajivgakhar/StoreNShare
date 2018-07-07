package Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.storenshare.storenshare.MyFunction;
import com.storenshare.storenshare.R;
import com.storenshare.storenshare.ShareFileActivity;

import java.util.ArrayList;
import java.util.List;

import Model.SharedUser;


public class MyListAdapter extends BaseAdapter {

    private Activity context;
    private List<SharedUser> users=new ArrayList<>();

    public MyListAdapter(Activity con,List<SharedUser> users){
        super();
        this.context=con;
        this.users=users;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int i) {
        return users.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater=context.getLayoutInflater();
        if(view==null){
            view=inflater.inflate(R.layout.layout_list,null);
            TextView txt=(TextView)view.findViewById(R.id.txtSharedUsers);
            TextView txtPer=(TextView)view.findViewById(R.id.txtSharedUsersPer);
            TextView txtDel=(TextView)view.findViewById(R.id.txtSharedUsersId);
            txt.setText(Html.fromHtml(users.get(i).getName()));
            txtPer.setText(Html.fromHtml("<b>"+users.get(i).getPermission()+"</b>"));
            txtDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(context)
                            .setTitle("Action")
                            .setMessage("Do you really want to delete?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {

                                    MyFunction mf = new MyFunction();
                                    String url = "http://shareblood.x10host.com/storeshare/deleteSharedMember.php";
                                    mf.delSharedMember(context,url,users.get(i).getId());
                                    Intent in=new Intent(context, ShareFileActivity.class);
                                    in.putExtra("contentId",users.get(i).getData_id());
                                    context.startActivity(in);
                                    context.finish();
                                }})
                            .setNegativeButton(android.R.string.no, null).show();
                }
            });



        }
        return view;
    }
}
