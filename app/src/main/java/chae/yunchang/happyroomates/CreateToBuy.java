package chae.yunchang.happyroomates;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import chae.yunchang.happyroomates.db.AppDatabase;
import chae.yunchang.happyroomates.db.ToBuyDao;
import chae.yunchang.happyroomates.models.ToBuy;

public class CreateToBuy extends AppCompatActivity {

    private EditText mItemEditText;
    private EditText mAmountEditText;
    private Button mSaveButton;
    private Button mCancelButton;

    private ToBuyDao mToBuyDAO;

    private String account_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tobuy_create);

        mToBuyDAO = Room.databaseBuilder(this, AppDatabase.class, "db-tobuy")
                .allowMainThreadQueries()
                .build()
                .getToBuyDao();

        mItemEditText = findViewById(R.id.add_tobuy_name);
        mAmountEditText = findViewById(R.id.add_tobuy_amount);
        mSaveButton = findViewById(R.id.add_tobuy_saveButton);
        mCancelButton = findViewById(R.id.add_tobuy_cancelButton);

        mAmountEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && keyCode == KeyEvent.KEYCODE_ENTER) {
                    mSaveButton.performClick();
                    return true;
                }
                return false;
            }
        });

        account_email = getIntent().getStringExtra(UpdateToBuy.EXTRA_EMAIL);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String item = mItemEditText.getText().toString();
                int amount = Integer.valueOf(mAmountEditText.getText().toString());

                if(item.length() == 0 || amount == 0){
                    Toast.makeText(CreateToBuy.this, "Please make sure all details are correct", Toast.LENGTH_LONG).show();
                    return;
                }

                ToBuy tobuy = new ToBuy();
                tobuy.setItem(item);
                tobuy.setAmount(amount);
                tobuy.setEmail(account_email);

                try {
                    mToBuyDAO.insert(tobuy);
                    setResult(RESULT_OK);
                    finish();
                }catch(SQLiteConstraintException e) {
                    Toast.makeText(CreateToBuy.this, "A tobuy with same id already exists.", Toast.LENGTH_LONG).show();
                }

            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(getApplicationContext(), "Create cancel", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mCancelButton.performClick();
        return true;
    }

    @Override
    public void onBackPressed() {
        mCancelButton.performClick();
    }

}