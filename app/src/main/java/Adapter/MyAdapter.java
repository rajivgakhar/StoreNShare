package Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.storenshare.storenshare.R;
import com.storenshare.storenshare.ViewContentActivity;

import java.util.List;

import Model.ListItem;

public class MyAdapter extends RecyclerView.Adapter <MyAdapter.ViewHolder> {
    final String imgPath = "http://shareblood.x10host.com/storeshare/uploads/";
private Context context; //current state of the class
private List<ListItem> listItems;//create custom ListItem class

    public MyAdapter(Context context, List<ListItem> listItem) {
        this.context = context;
        listItems = listItem;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row,parent,false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
            ListItem listItem=listItems.get(position);
        Picasso.get()
                .load(imgPath+listItem.getImage())
                .placeholder(R.drawable.file_icon)
                .error(R.drawable.file)
                .resize(800, 600)
                .into(holder.contentImg);
        holder.txtFileName.setText(listItem.getImage());
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView contentImg;
        public TextView txtFileName;

        public ViewHolder(View itemView) {
            super(itemView);
            contentImg=(ImageView)itemView.findViewById(R.id.userImage);
            txtFileName=(TextView)itemView.findViewById(R.id.txtFileName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position=getAdapterPosition();
            ListItem item= listItems.get(position);
            Intent in=new Intent(context, ViewContentActivity.class);
            in.putExtra("contentId",item.getImgId());
            in.putExtra("contentUrl",imgPath+item.getImage());
            in.putExtra("contentFileName",item.getImage());
            in.putExtra("ownerId",item.getOwnerId());
            context.startActivity(in);
        }
    }

}


