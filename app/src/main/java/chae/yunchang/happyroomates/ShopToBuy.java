package chae.yunchang.happyroomates;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class ShopToBuy extends AppCompatActivity {

    public static String EXTRA_TOBUY_ITEM = "tobuy_id";
    public static String EXTRA_TOBUY_AMOUNT = "tobuy_amt";
    public static String EXTRA_SELECT_AMOUNT = "select_amount";

    private String TAG = "#SHOP";
    private TextView mTextView;
    private Spinner mSpinner;
    private Button mButton;
    private Button mCancel;

    private int snum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tobuy_shop);

        mTextView = findViewById(R.id.add_toshop_name);
        mSpinner = findViewById(R.id.add_toshop_amount);
        mButton = findViewById(R.id.add_toshop_saveButton);
        mCancel = findViewById(R.id.add_toshop_cancelButton);

        initViews();
    }

    private void initViews() {
        String item = getIntent().getStringExtra(EXTRA_TOBUY_ITEM);
        mTextView.setText(item);

        int amount = getIntent().getIntExtra(EXTRA_TOBUY_AMOUNT, -1);
        Log.d(TAG, "amount: "+amount);
        ArrayList<String> numbers = new ArrayList<>();
        for(int i=0; i<= amount; i++)
            numbers.add(i+"");

        mSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, numbers));

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView tmp = (TextView)view;
                snum = Integer.valueOf(tmp.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent result = new Intent();
                result.putExtra(EXTRA_SELECT_AMOUNT, snum);

                setResult(RESULT_OK, result);
                Log.d(TAG, "result: "+snum);
                finish();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mCancel.performClick();
        return true;
    }

    @Override
    public void onBackPressed() {
        mCancel.performClick();
    }


}