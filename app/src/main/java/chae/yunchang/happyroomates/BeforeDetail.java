package chae.yunchang.happyroomates;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import chae.yunchang.happyroomates.Adapters.ShopListAdapter;
import chae.yunchang.happyroomates.models.BeforeItem;
import chae.yunchang.happyroomates.models.ToShop;

public class BeforeDetail extends Dialog {
    private String TAG = "#BEFORE_DETAIL";

    // VIEW
    private Context context;
    private BeforeItem before;
    private String str_myName;
    private String str_mateName;

    public BeforeDetail(@NonNull Context context, BeforeItem before, String myName, String mateName) {
        super(context);
        this.context = context;
        this.before = before;
        this.str_myName = myName;
        this.str_mateName = mateName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_before_detail);

        ArrayList<ToShop> toShopList = new ArrayList<>();

        Map<String, Object> map = before.getItems();
        for ( String key : map.keySet() ) {
            ToShop shop = new ToShop();
            shop.setItem(key);
            shop.setAmount(Integer.valueOf(String.valueOf(map.get(key))));
            toShopList.add(shop);
        }

        // VIEW
        TextView title = findViewById(R.id.beforelist_toolbartitle);
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String strDate = df.format(before.getBoughtDate());
        title.setText(strDate);

        ListView mShopView = findViewById(R.id.beforelist_detail);
        Log.d(TAG, "beforelist size: "+toShopList.size());

        ShopListAdapter mShopListAdapter = new ShopListAdapter(context, toShopList);
        mShopView.setAdapter(mShopListAdapter);

        TextView shop = findViewById(R.id.beforelist_shop);
        TextView total = findViewById(R.id.beforelist_total);
        TextView paid = findViewById(R.id.beforelist_paid);
        TextView myName = findViewById(R.id.beforelist_myname);
        TextView myMoney = findViewById(R.id.beforelist_mymoney);
        TextView mateName = findViewById(R.id.beforelist_matename);
        TextView mateMoney = findViewById(R.id.beforelist_matemoney);

        paid.setText(before.getPaid());
        shop.setText(before.getShop());
        total.setText(String.valueOf(before.getTotal()));
        myName.setText(str_myName);
        myMoney.setText(String.valueOf(before.getMymoney()));
        mateName.setText(str_mateName);
        mateMoney.setText(String.valueOf(before.getMatemoney()));

    }
}