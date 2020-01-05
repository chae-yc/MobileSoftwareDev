package chae.yunchang.happyroomates;

import android.app.Dialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import chae.yunchang.happyroomates.Adapters.ShopListAdapter;
import chae.yunchang.happyroomates.db.AppDatabase;
import chae.yunchang.happyroomates.db.LoginDao;
import chae.yunchang.happyroomates.db.ToBuyDao;
import chae.yunchang.happyroomates.models.Login;
import chae.yunchang.happyroomates.models.ToShop;
import chae.yunchang.happyroomates.models.ToBuy;

public class ShopList extends Dialog implements View.OnClickListener, AdapterView.OnItemSelectedListener{
    private String TAG = "#SHOP";

    // VIEW
    private Context context;

    private List<ToShop> toShopList;
    private Button mCancel;

    private EditText edit_shop;
    private EditText edit_total;
    private EditText edit_my;
    private EditText edit_mate;

    // DB
    private Login me;
    private ToBuyDao mToBuyDao;

    // Firestore
    private FirebaseFirestore db;
    private FirebaseFirestoreSettings settings;
    private FirebaseAuth auth;
    private FirebaseUser fuser;

    public interface MyDialogListener {
        void onPositiveClicked();
        void onNegativeClicked();
    }

    private MyDialogListener dialogListener;

    public ShopList(@NonNull Context context, ArrayList<ToShop> toShopList) {
        super(context);
        this.context = context;
        this.toShopList = toShopList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoplist);

        // FIRE-AUTH SETUP
        auth = FirebaseAuth.getInstance();
        fuser = auth.getCurrentUser();
        // FIRE-DB SETUP
        db = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        // ROOM-DB SETUP
        String LoginEmail = fuser.getEmail();
        mToBuyDao = Room.databaseBuilder(context, AppDatabase.class, "db-tobuy")
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build()
                .getToBuyDao();
        LoginDao mLoginDao = Room.databaseBuilder(context, AppDatabase.class, "db-login")
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build()
                .getLoginDao();

        me = mLoginDao.getloginWithMail(LoginEmail);

        // VIEW
        ListView mShopView = findViewById(R.id.shoplist_detail);

        Button mSave = findViewById(R.id.shopList_saveButton);
        mSave.setOnClickListener(this);
        mCancel = findViewById(R.id.shopList_cancelButton);
        mCancel.setOnClickListener(this);

        ShopListAdapter mShopListAdapter = new ShopListAdapter(context, toShopList);
        mShopView.setAdapter(mShopListAdapter);

        edit_shop = findViewById(R.id.shoplist_shop);
        edit_my = findViewById(R.id.shoplist_mymoney);
        edit_mate = findViewById(R.id.shoplist_matemoney);
        edit_mate.setHint(me.getMate_name()+"'ll pay.");

        Spinner spin_ratio = findViewById(R.id.shoplist_spinner);
        spin_ratio.setOnItemSelectedListener(this);

        edit_total = findViewById(R.id.shoplist_total);
    }

    // METHOD
    private void deleteToBuys(){
        for(int i = 0; i< toShopList.size(); i++) {
            ToBuy buy = mToBuyDao.getToBuyWithItem(me.getEmail(), toShopList.get(i).getItem());
            int change = buy.getAmount() - toShopList.get(i).getAmount();
            if(change == 0)
                mToBuyDao.delete(buy);
            else{
                buy.setAmount(change);
                mToBuyDao.update(buy);
            }
        }
    }

    private String uid;
    private String mate_id;
    private Map<String, Object> bought;
    private float mtotal;
    private float mymoney;
    private float matemoney;

    CollectionReference boughtcol;

    private void insertToFireStore(){
        uid = me.getUid();
        mate_id = me.getMuid();

        bought = new HashMap<>();
        // setup itemlist
        Date today = Calendar.getInstance().getTime();
        Map<String, Object> itemlist = new HashMap<>();
        for(int i=0; i<toShopList.size(); i++)
            itemlist.put(toShopList.get(i).getItem(), toShopList.get(i).getAmount());

        // setup bought
        bought.put("shop", edit_shop.getText().toString());
        bought.put("paid", me.getName());
        bought.put("items", itemlist);
        bought.put("date", today);
        bought.put("total_money", mtotal);
        bought.put(me.getName(), mymoney);
        if(!me.getMate_name().equals(""))
            bought.put(me.getMate_name(), matemoney);

        Log.d(TAG, "Check Items"+bought);

        // get collection
        boughtcol = db.collection("mates");

        // Query1
        boughtcol.whereEqualTo("mate1", uid).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().size() == 0){
                            // Query2
                            boughtcol.whereEqualTo("mate2", uid).get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.getResult().size() == 0) {
                                                Map<String, Object> data = new HashMap<>();
                                                data.put("mate1", uid);
                                                data.put("mate2", mate_id);
                                                boughtcol.add(data)
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                            @Override
                                                            public void onSuccess(DocumentReference documentReference) {
                                                                Log.d(TAG, "new partner created: " + documentReference.getId());
                                                            }
                                                        })
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                                CollectionReference ref = boughtcol.document(task.getResult().getId()).collection("before");
                                                                ref.add(bought)
                                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                            @Override
                                                                            public void onSuccess(DocumentReference documentReference) {
                                                                                Log.d(TAG, "new partner's before created: " + documentReference.getId());
                                                                            }
                                                                        })
                                                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                //Log.d(TAG, "new partner's new before created: " + task.getResult().getId() + task.getResult());
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                Log.w(TAG, "new partner, no before adding document", e);
                                                                            }
                                                                        });
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "no partner adding document", e);
                                                    }
                                                });
                                            } else {
                                                DocumentReference dref = boughtcol.document(task.getResult().getDocuments().get(0).getId());
                                                CollectionReference cref = dref.collection("before");
                                                cref.add(bought)
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                Log.d(TAG, "q2 new before created: " + task.getResult().getId() + task.getResult());
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w(TAG, "q2 Error adding document", e);
                                                            }
                                                        });
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "q2 Error SELECT document", e);
                                }
                            });
                        }else{
                            DocumentReference dref = boughtcol.document(task.getResult().getDocuments().get(0).getId());
                            CollectionReference cref = dref.collection("before");
                            cref.add(bought)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            Log.d(TAG, "q1 new before created: " + task.getResult().getId() + task.getResult());
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "q1 Error adding document", e);
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "q1 Error SELECT document", e);
            }
        });

        Log.d(TAG, "END FIRE");
    }
    private boolean validateValues(){
        if(toShopList.size() == 0){
            Toast.makeText(context,"Please make the shopping list first", Toast.LENGTH_LONG).show();
            Log.d(TAG, "No shopping list");
            return false;
        }

        // TEXT VALIDATE
        String t = edit_total.getText().toString();
        String m = edit_my.getText().toString();
        String ma = edit_mate.getText().toString();
        String sp = edit_shop.getText().toString();

        if(t.length() == 0 || m.length() == 0 || sp.length() == 0){
            Toast.makeText(context, "Please make sure all details are correct", Toast.LENGTH_LONG).show();
            return false;
        }

        // NUM VALIDATE
        mtotal = Float.valueOf(t);
        mymoney = Float.valueOf(m);
        if(!ma.equals(""))
            matemoney = Float.valueOf(ma);
        else
            matemoney = 0;

        if(mtotal < mymoney + matemoney){
            Toast.makeText(context, "Money can't be over the total", Toast.LENGTH_LONG).show();
            return false;
        }

        if(me.getMate_name().equals("") && matemoney > 0) {
            Toast.makeText(context, "You don't have roomate yet", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    // Events
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.shopList_saveButton:
                Log.d(TAG, "save button shoplist size: "+ toShopList.size());
                if(validateValues()) {      // if validate true (VALUES ARE OKAY)
                    deleteToBuys();
                    insertToFireStore();
                    dialogListener.onPositiveClicked();
                }
                dismiss();
                break;
            case R.id.shopList_cancelButton:
                Log.d(TAG, "cancel button");
                dialogListener.onNegativeClicked();
                dismiss();
                break;
        }
    }

    // Spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(edit_total.getText().toString().equals("Custom"))
            return;
        if(edit_total.getText().toString().equals(""))
            return;
        TextView tv = (TextView) view;
        String str = tv.getText().toString();

        float total = Float.valueOf(edit_total.getText().toString());
        if(str.equals("1:1")){
            edit_my.setText(String.format(Locale.ROOT, "%.2f",total/2));
            edit_mate.setText(String.format(Locale.ROOT, "%.2f",total/2));
        }else if(str.equals("2:1")){
            edit_my.setText(String.format(Locale.ROOT, "%.2f",(total*2)/3));
            edit_mate.setText(String.format(Locale.ROOT, "%.2f",total/3));
        }else if(str.equals("1:2")){
            edit_my.setText(String.format(Locale.ROOT, "%.2f",total/3));
            edit_mate.setText(String.format(Locale.ROOT, "%.2f",(total*2)/3));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {  }


    public void setDialogListener(MyDialogListener dialogListener){
        this.dialogListener = dialogListener;
    }

    @Override
    public void onBackPressed() {
        mCancel.performClick();
    }
}