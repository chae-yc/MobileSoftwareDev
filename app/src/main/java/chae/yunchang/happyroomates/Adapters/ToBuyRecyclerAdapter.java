package chae.yunchang.happyroomates.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import chae.yunchang.happyroomates.R;
import chae.yunchang.happyroomates.models.ToShop;
import chae.yunchang.happyroomates.models.ToBuy;

public class ToBuyRecyclerAdapter extends RecyclerView.Adapter<ToBuyRecyclerAdapter.ViewHolder> {

    //Interface for callbacks
    public interface ActionCallback {
        void onLongClickListener(ToBuy toBuy, int position);
        void onClickListener(int position);
    }

    private Context context;
    private List<ToBuy> toBuyList;
    private List<Integer> toShopList;
    private ActionCallback mActionCallbacks;

    public ToBuyRecyclerAdapter(Context context, List<ToBuy> toBuyList, List<Integer> shop) {
        this.context = context;
        this.toBuyList = toBuyList;
        this.toShopList = shop;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tobuy_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(position);
    }

    @Override
    public int getItemCount() {
        return toBuyList.size();
    }

    public String getItemName(int position) { return toBuyList.get(position).getItem(); }
    public int getItemAmount(int position)  { return toBuyList.get(position).getAmount(); }

    public void updateShop(List<Integer> toShop){
        this.toShopList = toShop;
        notifyDataSetChanged();
    }
    public void updateData(List<ToBuy> toBuys) {
        this.toBuyList = toBuys;
        notifyDataSetChanged();
    }

    public ArrayList<ToShop> getToShopList() {
        ArrayList<ToShop> shoplist = new ArrayList<>();
        int c = 0;
        for(int i=0; i<toShopList.size(); i++) {
            if(toShopList.get(i) > 0) {
                ToShop item = new ToShop();
                item.setItem(toBuyList.get(i).getItem());

                item.setAmount(toShopList.get(i));
                shoplist.add(item);
            }
        }
        return shoplist;
    }

    //View Holder
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
        private TextView mItemTextView;
        private TextView mBuyTextView;
        private TextView mAmountTextView;

        ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);

            mItemTextView = itemView.findViewById(R.id.tobuy_row_item);
            mBuyTextView = itemView.findViewById(R.id.tobuy_row_buy);
            mAmountTextView = itemView.findViewById(R.id.tobuy_row_amount);
        }

        void bindData(int position) {
            ToBuy toBuy = toBuyList.get(position);

            String item = toBuy.getItem();
            mItemTextView.setText(item);

            int shop = toShopList.get(position);
            if (shop > 0)
                mBuyTextView.setText(shop+"");
            else if(shop == 0)
                mBuyTextView.setText("");


            int amount = toBuy.getAmount();
            mAmountTextView.setText(amount+"");
        }

        @Override
        public boolean onLongClick(View v) {
            if (mActionCallbacks != null) {
                mActionCallbacks.onLongClickListener(toBuyList.get(getAdapterPosition()), getAdapterPosition());
            }
            return true;
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
