package chae.yunchang.happyroomates;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import chae.yunchang.happyroomates.db.AppDatabase;
import chae.yunchang.happyroomates.db.ToBuyDao;
import chae.yunchang.happyroomates.models.ToBuy;

public class UpdateToBuy extends AppCompatActivity {
    public static String EXTRA_EMAIL = "user_email";
    public static String EXTRA_TOBUY_ID = "tobuy_id";
    private EditText mItemEditText;
    private EditText mAmountEditText;
    private Button mUpdateButton;
    private Button mCancel;

    private ToBuyDao mToBuyDao;
    private ToBuy TOBUY;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tobuy_update);

        mToBuyDao = Room.databaseBuilder(this, AppDatabase.class, "db-tobuy")
                .allowMainThreadQueries()
                .build()
                .getToBuyDao();

        mItemEditText = findViewById(R.id.update_tobuy_name);
        mAmountEditText = findViewById(R.id.update_tobuy_amount);
        mUpdateButton = findViewById(R.id.update_tobuy_saveButton);
        mCancel = findViewById(R.id.update_tobuy_cancelButton);
        mToolbar = findViewById(R.id.toolbar);

        mAmountEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    mUpdateButton.performClick();
                    return true;
                }
                return false;
            }
        });

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String item = mItemEditText.getText().toString();
                int amount = Integer.valueOf(mAmountEditText.getText().toString());

                if (item.length() == 0 || amount == 0) {
                    Toast.makeText(UpdateToBuy.this, "Please make sure all details are correct", Toast.LENGTH_SHORT).show();
                    return;
                }

                ToBuy tobuy = new ToBuy();
                tobuy.setItem(item);
                tobuy.setAmount(amount);

                try {
                    mToBuyDao.insert(tobuy);
                    setResult(RESULT_OK);
                    finish();
                } catch (SQLiteConstraintException e) {
                    Toast.makeText(UpdateToBuy.this, "A tobuy with same id already exists.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(getApplicationContext(), "Update cancel", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        String item = getIntent().getStringExtra(EXTRA_TOBUY_ID);
        String account_email = getIntent().getStringExtra(EXTRA_EMAIL);
        TOBUY = mToBuyDao.getToBuyWithItem(account_email, item);
        initViews();
    }

    private void initViews() {
        setSupportActionBar(mToolbar);
        mItemEditText.setText(TOBUY.getItem());
        mAmountEditText.setText(TOBUY.getAmount()+"");

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String item = mItemEditText.getText().toString();
                int amount = Integer.valueOf(mAmountEditText.getText().toString());

                if (item.length() == 0 || amount == 0) {
                    Toast.makeText(UpdateToBuy.this, "Please make sure all details are correct", Toast.LENGTH_LONG).show();
                    return;
                }

                //Your code goes here to update a Todo!
                TOBUY.setItem(item);
                TOBUY.setAmount(amount);

                mToBuyDao.update(TOBUY);
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_update_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete: {
                mToBuyDao.delete(TOBUY);
                setResult(RESULT_OK);
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mCancel.performClick(); return true;
    }

    @Override
    public void onBackPressed() {
        mCancel.performClick();
    }
}