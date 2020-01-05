package chae.yunchang.happyroomates.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import chae.yunchang.happyroomates.R;
import chae.yunchang.happyroomates.models.BeforeItem;
import chae.yunchang.happyroomates.models.ToShop;

public class AfterListAdapter extends ArrayAdapter<BeforeItem> {

    private String TAG = "#SLIST";

    public AfterListAdapter(Context context, List<BeforeItem> beforeItemList) {
        super(context, 0, beforeItemList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BeforeItem before = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.after_row, parent, false);
        }

        TextView date = (TextView) convertView.findViewById(R.id.after_row_date);
        TextView shop = (TextView) convertView.findViewById(R.id.after_row_shop);
        TextView cost = (TextView) convertView.findViewById(R.id.after_row_cost);

        DateFormat df = new SimpleDateFormat("MM/dd/yy");
        String strDate = df.format(before.getBoughtDate());

        date.setText(strDate);
        shop.setText(before.getShop());
        cost.setText(before.getTotal() + "");

        return convertView;
    }
}