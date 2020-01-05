package chae.yunchang.happyroomates;

import android.app.Activity;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Map;

import chae.yunchang.happyroomates.db.AppDatabase;
import chae.yunchang.happyroomates.db.LoginDao;
import chae.yunchang.happyroomates.models.Login;

public class MainActivity extends AppCompatActivity {

    public static Activity _Main_Activity;

    private static final String TAG = "#LOGIN";

    private static final int RC_SIGN_IN = 1000;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;

    private LoginDao mLoginDAO;

    private FirebaseFirestore db;
    private Map<String, Object> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _Main_Activity = MainActivity.this;

        mLoginDAO = Room.databaseBuilder(this, AppDatabase.class, "db-login")
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build()
                .getLoginDao();

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed:" + connectionResult);
                        Toast.makeText(MainActivity.this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton Google_Login = findViewById(R.id.Google_Login);
        Google_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        // Firestore to read
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                //구글 로그인 성공해서 firebase에 인증
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                //구글 로그인 실패
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "인증 실패", Toast.LENGTH_SHORT).show();
                        }else{
                            DocumentReference docRef = db.collection("users").document(user.getEmail());
                            docRef.get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                if (document.exists()) {
                                                    map = document.getData();
                                                    // find with email
                                                    Login login_user = mLoginDAO.getloginWithMail(user.getEmail());
                                                    // non exist create new
                                                    if(login_user == null) {
                                                        Login new_user = new Login();
                                                        new_user.setUid(user.getUid());
                                                        new_user.setName(user.getDisplayName());
                                                        new_user.setEmail(user.getEmail());
                                                        new_user.setPhone((String) map.get("phone"));
                                                        new_user.setMate_mail((String) map.get("mate_mail"));
                                                        new_user.setMuid((String) map.get("mate_id"));
                                                        new_user.setMate_name((String) map.get("mate_name"));
                                                        new_user.setMate_phone((String) map.get("mate_phone"));

                                                        mLoginDAO.insert(new_user);
                                                        Login a = mLoginDAO.getloginWithMail(new_user.getEmail());
                                                        Log.d(TAG, "Create new user: "+a.getEmail());
                                                    }
                                                    Intent intent = new Intent(MainActivity.this, TabActivity.class);
                                                    startActivity(intent);

                                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                                }else{
                                                    Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                                                    startActivity(intent);
                                                    Log.d(TAG, "Sign UP Activity");
                                                }
                                            }else {
                                                Log.d(TAG, "get failed with ", task.getException());
                                            }
                                        }
                                    });

                        }
                    }
                });
    }
}
