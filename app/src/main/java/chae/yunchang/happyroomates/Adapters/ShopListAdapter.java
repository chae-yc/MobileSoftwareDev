package chae.yunchang.happyroomates.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import chae.yunchang.happyroomates.R;
import chae.yunchang.happyroomates.models.ToShop;

public class ShopListAdapter extends ArrayAdapter<ToShop> {
    public ShopListAdapter(Context context, List<ToShop> toToShopList) {
        super(context, 0, toToShopList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ToShop shop = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.shop_row, parent, false);
        }

        TextView mItemTextView = convertView.findViewById(R.id.shop_row_item);
        TextView mBuyTextView = convertView.findViewById(R.id.shop_row_buy);

        String item = shop.getItem();
        mItemTextView.setText(item);

        int amount = shop.getAmount();
        mBuyTextView.setText(amount + "");

        return convertView;
    }
}