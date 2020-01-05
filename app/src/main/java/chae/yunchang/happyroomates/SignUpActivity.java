package chae.yunchang.happyroomates;


import android.arch.persistence.room.Room;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import chae.yunchang.happyroomates.db.AppDatabase;
import chae.yunchang.happyroomates.db.LoginDao;
import chae.yunchang.happyroomates.models.Login;

public class SignUpActivity extends AppCompatActivity {
    private EditText name;
    private EditText email;
    private EditText phone;
    private Button button;

    private LoginDao mLoginDAO;
    private Login login;

    private String TAG = "#FIRE";
    private FirebaseFirestore db;
    private FirebaseFirestoreSettings settings;
    private FirebaseAuth auth;
    private FirebaseUser fuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Get the DAO here!
        mLoginDAO = Room.databaseBuilder(this, AppDatabase.class, "db-login")
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build()
                .getLoginDao();

        name = findViewById(R.id.sign_in_name);
        email = findViewById(R.id.sign_in_email);
        phone = findViewById(R.id.sign_in_phone);
        button = findViewById(R.id.sign_in_button);

        // firestore
        auth = FirebaseAuth.getInstance();
        fuser = auth.getCurrentUser();

        db = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        initViews();
    }

    private void initViews() {
        String ename = fuser.getDisplayName();
        String eemail = fuser.getEmail();
        String ephone = fuser.getPhoneNumber();

        if(ename != null) name.setText(ename);
        else name.setText("");

        if(eemail != null) email.setText(eemail);
        else email.setText("");

        if(ephone != null) phone.setText(ephone);
        else phone.setText("");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uname = name.getText().toString();
                final String uemail = email.getText().toString();
                String uphone = phone.getText().toString();

                if (uname.length() == 0 || uemail.length() == 0 || uphone.length() == 0) {
                    Toast.makeText(SignUpActivity.this, "Please make sure all details are correct", Toast.LENGTH_SHORT).show();
                    return;
                }

                login = new Login();
                Map<String, Object> user = new HashMap<>();

                login.setUid(fuser.getUid());
                login.setName(uname);
                login.setEmail(uemail);
                login.setPhone(uphone);
                login.setMate_mail("");
                login.setMuid("");
                login.setMate_name("");
                login.setMate_phone("");

                user.put("author_id", fuser.getUid());
                user.put("name", uname);
                user.put("email", uemail);
                user.put("phone", uphone);
                user.put("mate_mail", "");
                user.put("mate_id", "");
                user.put("mate_name", "");
                user.put("mate_phone", "");
                //user.put("token", FirebaseInstanceId.getInstance().getToken());

                try {
                    // save room db
                    mLoginDAO.insert(login);
                    // fire store
                    db.collection("users").document(uemail)
                            .set(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "user "+uemail+" is saved successful");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error ", e);
                                }
                            });

                    // intent
                    Intent intent = new Intent(SignUpActivity.this, TabActivity.class);
                    Log.d("#Login", "Login with " + uname);
                    startActivity(intent);
                    finish();
                } catch (SQLiteConstraintException e) {
                    Toast.makeText(getApplicationContext(), "Login with same id already exists.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}