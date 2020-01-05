package chae.yunchang.happyroomates.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import chae.yunchang.happyroomates.R;
import chae.yunchang.happyroomates.models.BeforeItem;

public class BeforeRecyclerAdapter extends RecyclerView.Adapter<BeforeRecyclerAdapter.ViewHolder> {

    public interface ActionCallback {
        void onClickListener(int position);
    }

    private String TAG = "#BeforeAdaptor";

    private ActionCallback mActionCallbacks;
    private Context context;
    private List<BeforeItem> beforeList;
    private ArrayList<Boolean> checked;

    public BeforeRecyclerAdapter(Context context, List<BeforeItem> beforeList) {
        this.context = context;
        this.beforeList = beforeList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.before_row, parent, false);
        Log.d(TAG, "itmeCount: "+getItemCount());
        checked = new ArrayList<>();
        for(int i=0; i<getItemCount(); i++)
            checked.add(false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(position);
    }

    @Override
    public int getItemCount() {
        return beforeList.size();
    }

    public ArrayList<BeforeItem> getCheckedBeforeList(){
        ArrayList<BeforeItem> newlist = new ArrayList<>();
        for(int i=0; i<beforeList.size(); i++)
            if(checked.get(i))
                newlist.add(beforeList.get(i));

        return newlist;
    }
    public void updateData(List<BeforeItem> beforelist) {
        this.beforeList = beforelist;
        notifyDataSetChanged();
    }

    //View Holder
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mDateTextView;
        private TextView mAmountTextView;
        private CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            mDateTextView = itemView.findViewById(R.id.before_row_date);
            mAmountTextView = itemView.findViewById(R.id.before_row_amount);
            checkBox = itemView.findViewById(R.id.checkBox);
            checkBox.setChecked(false);
        }
        private int p;
        void bindData(int position) {
            p = position;
            BeforeItem before = beforeList.get(position);

            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            String strDate = df.format(before.getBoughtDate());

            mDateTextView.setText(strDate);

            float cost = before.getTotal();
            mAmountTextView.setText(cost+"");


            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checked.set(p, isChecked);
                }
            });
            Log.d(TAG, "bind position: "+position);
            checkBox.setChecked(checked.get(position));
        }

        @Override
        public void onClick(View v) {
            if (mActionCallbacks != null) {
                mActionCallbacks.onClickListener(getAdapterPosition());
            }
        }
    }

    public void addActionCallback(ActionCallback actionCallbacks) {
        mActionCallbacks = actionCallbacks;
    }
}
