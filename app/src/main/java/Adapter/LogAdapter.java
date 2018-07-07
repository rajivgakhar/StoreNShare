package Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.storenshare.storenshare.R;
import java.util.List;
import Model.UserLog;


public class LogAdapter extends RecyclerView.Adapter <LogAdapter.ViewHolder> {
    private Context context; //current state of the class
    private List<UserLog> listItems;//create custom ListItem class

    public LogAdapter(Context context, List<UserLog> listItem) {
        this.context = context;
        listItems = listItem;
    }

    @Override
    public LogAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.log_list,parent,false);

        return new LogAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(LogAdapter.ViewHolder holder, int position) {
        UserLog listItem=listItems.get(position);

        holder.txtLogMessage.setText(listItem.getMessage()+"-"+listItem.getDate());
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView txtLogMessage;

        public ViewHolder(View itemView) {
            super(itemView);
            txtLogMessage=(TextView)itemView.findViewById(R.id.txtLog);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position=getAdapterPosition();

        }
    }

}


