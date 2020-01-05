package chae.yunchang.happyroomates;


import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import chae.yunchang.happyroomates.Adapters.BeforeRecyclerAdapter;
import chae.yunchang.happyroomates.Adapters.ToBuyRecyclerAdapter;
import chae.yunchang.happyroomates.db.AppDatabase;
import chae.yunchang.happyroomates.db.LoginDao;
import chae.yunchang.happyroomates.db.ToBuyDao;
import chae.yunchang.happyroomates.models.BeforeItem;
import chae.yunchang.happyroomates.models.Login;
import chae.yunchang.happyroomates.models.ToBuy;

public class TabActivity extends AppCompatActivity {

    // MAIN
    private ToBuyDao mToBuyDao;
    private LoginDao mLoginDao;
    private String LoginEmail;
    private String TAG = "#TAB";

    // TAB 1
    private static final int RC_CREATE_TOBUY = 1;
    private static final int RC_UPDATE_TOBUY = 2;
    private static final int RC_SHOP_TOBUY = 3;

    private RecyclerView mToBuyRecyclerView;
    private ToBuyRecyclerAdapter mToBuyRecyclerAdapter;
    private FloatingActionButton add;
    private FloatingActionButton shop;
    private int shop_pos;
    private ArrayList<Integer> toShopList;

    // TAB2
    private RecyclerView beforeRecyclerView;
    private BeforeRecyclerAdapter mBeforeRecyclerAdapter;
    private FloatingActionButton cleanAmount;
    private FloatingActionButton refresh;
    private TextView myname;
    private TextView matename;
    private ImageView arrow;
    private TextView amount;

    // TAB3
    private TextView mypg_name;
    private TextView mypg_email;
    private EditText mypg_phone;
    private EditText mypg_mate_mail;
    private TextView mypg_mate_detail;
    private Button mypg_update;
    private Button mypg_logout;
    private Button mypg_delete;
    private TextView mypg_stat;
    private Login me;

    // Firestore
    private FirebaseFirestore db;
    private FirebaseFirestoreSettings settings;
    private FirebaseAuth auth;
    private FirebaseUser fuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        MainActivity MA = (MainActivity)MainActivity._Main_Activity;
        MA.finish();

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
        mToBuyDao = Room.databaseBuilder(this, AppDatabase.class, "db-tobuy")
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build()
                .getToBuyDao();

        mLoginDao = Room.databaseBuilder(this, AppDatabase.class, "db-login")
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build()
                .getLoginDao();

        LoginEmail = fuser.getEmail();
        Log.d(TAG, LoginEmail);
        me = mLoginDao.getloginWithMail(LoginEmail);

        // TAB SETUP
        tabSetup();

        // TAB 1 SETUP
        tab1Setup();

        // TAB 2 SETUP
        tab2Setup();

        // TAB 3 SETUP
        tab3Setup();

    }

    private int listSize;
    private ArrayList<BeforeItem> beforeList;
    private BeforeAsync task;
    // TAB 2
    public void tab2Setup() {
        beforeRecyclerView = findViewById(R.id.beforeRecyclerView);
        beforeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        beforeList = new ArrayList<>();
        mBeforeRecyclerAdapter = new BeforeRecyclerAdapter(this, beforeList);

        myname = findViewById(R.id.tab2_myname);
        matename = findViewById(R.id.tab2_matename);
        arrow = findViewById(R.id.tab2_arrow);
        amount = findViewById(R.id.tab2_amount);

        refresh = findViewById(R.id.tab2_refresh_button);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tab2_refresh();
            }
        });

        cleanAmount = findViewById(R.id.tab2_finish_button);
        cleanAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<BeforeItem> list = mBeforeRecyclerAdapter.getCheckedBeforeList();
                if(list.size() <= 0){
                    Toast.makeText(getApplicationContext(), "Check the box first", Toast.LENGTH_SHORT);
                    return;
                }
                AfterList dialog = new AfterList(TabActivity.this, list, me, amount.getText().toString());
                dialog.setDialogListener(new AfterList.MyDialogListener() {
                    @Override
                    public void onPositiveClicked() {
                        tab2_refresh();
                        Toast.makeText(getApplicationContext(), "Due is updated", Toast.LENGTH_SHORT).show();
                        Log.d(TAG,"checkedList onPositiveClicked");
                    }

                    @Override
                    public void onNegativeClicked() {
                        Toast.makeText(getApplicationContext(), "Due is canceled", Toast.LENGTH_SHORT).show();
                        Log.d(TAG,"checkedList onNevativeClicked");
                    }
                });
                dialog.show();
            }
        });

        myname.setText(me.getName());
        matename.setText(me.getMate_name());

        mBeforeRecyclerAdapter.addActionCallback(new BeforeRecyclerAdapter.ActionCallback() {
            @Override
            public void onClickListener(int position) {
                BeforeDetail dialog = new BeforeDetail(TabActivity.this, beforeList.get(position), me.getName(), me.getMate_name());
                dialog.show();
            }
        });

        beforeRecyclerView.setAdapter(mBeforeRecyclerAdapter);
        tab2_refresh();
    }

    public void tab2_refresh(){
        task = new BeforeAsync(beforeList, mBeforeRecyclerAdapter, me, refresh, amount, arrow);
        try {
            listSize = task.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "listSize: "+listSize);
    }

    // TAB 1
    public void tab1Setup(){
        toShopList = new ArrayList<Integer>();
        for(int i=0 ; i<mToBuyDao.getToBuys(LoginEmail).size(); i++)
            toShopList.add(0);

        mToBuyRecyclerView = findViewById(R.id.toBuyRecyclerView);
        mToBuyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mToBuyRecyclerAdapter = new ToBuyRecyclerAdapter(this, new ArrayList<ToBuy>(), toShopList);

        add = (FloatingActionButton)findViewById(R.id.tab1_add_button);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TabActivity.this, CreateToBuy.class);
                intent.putExtra(UpdateToBuy.EXTRA_EMAIL, me.getEmail());
                startActivityForResult(intent, RC_CREATE_TOBUY);
            }
        });

        shop = (FloatingActionButton)findViewById(R.id.tab1_buy_button);
        shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(me.getMate_name().equals("")) {
                    Toast.makeText(getApplicationContext(), "Update your roomate first", Toast.LENGTH_SHORT).show();
                    return;
                }

                ShopList dialog = new ShopList(TabActivity.this, mToBuyRecyclerAdapter.getToShopList());
                dialog.setDialogListener(new ShopList.MyDialogListener() {  // MyDialogListener 를 구현
                    @Override
                    public void onPositiveClicked() {
                        toShopList.clear();
                        for(int i=0 ; i<mToBuyDao.getToBuys(LoginEmail).size(); i++)
                            toShopList.add(0);
                        loadToBuys();
                        loadShop();
                        tab2_refresh();

                        Toast.makeText(getApplicationContext(), "Shopping is updated", Toast.LENGTH_SHORT).show();
                        Log.d("MyDialogListener","onPositiveClicked");
                    }

                    @Override
                    public void onNegativeClicked() {
                        Toast.makeText(getApplicationContext(), "Shopping is canceled", Toast.LENGTH_SHORT).show();
                        Log.d("MyDialogListener","onNegativeClicked");
                    }
                });
                dialog.show();
            }
        });

        mToBuyRecyclerAdapter.addActionCallback(new ToBuyRecyclerAdapter.ActionCallback() {
            @Override
            public void onLongClickListener(ToBuy toBuy, int position) {
                shop_pos = position;
                Intent intent = new Intent(TabActivity.this, UpdateToBuy.class);
                intent.putExtra(UpdateToBuy.EXTRA_TOBUY_ID, toBuy.getItem());
                intent.putExtra(UpdateToBuy.EXTRA_EMAIL, me.getEmail());
                startActivityForResult(intent, RC_UPDATE_TOBUY);
            }

            @Override
            public void onClickListener(int position) {
                shop_pos = position;
                Intent intent = new Intent(TabActivity.this, ShopToBuy.class);
                intent.putExtra(ShopToBuy.EXTRA_TOBUY_ITEM, mToBuyRecyclerAdapter.getItemName(position));
                intent.putExtra(ShopToBuy.EXTRA_TOBUY_AMOUNT, mToBuyRecyclerAdapter.getItemAmount(position));
                startActivityForResult(intent, RC_SHOP_TOBUY);
            }
        });

        mToBuyRecyclerView.setAdapter(mToBuyRecyclerAdapter);
        loadToBuys();
    }
    private void loadToBuys() {
        Log.d(TAG, "loadToBuys");
        mToBuyRecyclerAdapter.updateData(mToBuyDao.getToBuys(LoginEmail));
    }
    private void loadShop() {
        Log.d(TAG, "LoadShop");
        mToBuyRecyclerAdapter.updateShop(toShopList);
    }

    // TAB 3
    public void tab3Setup(){
        Log.d(TAG, "MATE_ID: "+me.getMuid());

        mypg_name = findViewById(R.id.mypg_name);
        mypg_email = findViewById(R.id.mypg_email);
        mypg_phone = findViewById(R.id.mypg_phone);
        mypg_mate_mail = findViewById(R.id.mypg_mate);
        mypg_update = findViewById(R.id.mypg_update_button);
        mypg_logout = findViewById(R.id.mypg_logout_button);
        mypg_mate_detail = findViewById(R.id.mypg_mateDetail);
        mypg_stat = findViewById(R.id.mypg_stat);
        mypg_delete = findViewById(R.id.mypg_delete_button);

        mypg_name.setText(me.getName());
        mypg_email.setText(me.getEmail());
        mypg_phone.setText(me.getPhone());
        mypg_mate_mail.setText(me.getMate_mail());
        String detail = "NAME: " + me.getMate_name() + "\n";
        detail += "PHONE: " + me.getMate_phone() + "\n";
        mypg_mate_detail.setText(detail);
        // delete me
        mypg_delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                db.collection("users").document(me.getEmail())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, me.getEmail()+"successfully deleted!");
                                List<ToBuy> del = mToBuyDao.getToBuys(me.getEmail());
                                for(int i=0; i<del.size(); i++)
                                    mToBuyDao.delete(del.get(i));
                                mLoginDao.delete(me);
                                Intent intent = new Intent(TabActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error deleting document", e);
                            }
                        });
            }
        });
        // logout
        mypg_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "LOGOUT");

                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(TabActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        // update
        mypg_update.setOnClickListener(new View.OnClickListener() {
            private String mail;
            private String phone;
            private Map<String, Object> doc;

            @Override
            public void onClick(View view) {
                mail = mypg_mate_mail.getText().toString();
                phone = mypg_phone.getText().toString();
                doc = new HashMap<>();

                if(mail.equals("")){
                    me.setPhone(phone);
                    me.setMate_mail("");
                    me.setMuid("");
                    doc.put("phone", phone);
                    doc.put("mate_mail", "");
                    doc.put("mate_id","");
                    doc.put("mate_name", "");
                    doc.put("mate_phone", "");
                    mLoginDao.update(me);
                    db.collection("users").document(me.getEmail())
                            .set(doc, SetOptions.merge());

                    mypg_stat.setText("ALL Update Success");
                }
                else {
                    final DocumentReference docRef = db.collection("users").document(mail);
                    docRef.get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();

                                        if (document.exists()) {
                                            Map<String, Object> map = document.getData();

                                            doc.put("phone", phone);
                                            doc.put("mate_mail", mail);
                                            doc.put("mate_id", map.get("author_id"));
                                            doc.put("mate_name", map.get("name"));
                                            doc.put("mate_phone", map.get("phone"));

                                            me.setPhone(phone);
                                            me.setMate_mail(mail);
                                            me.setMuid(map.get("author_id").toString());
                                            me.setMate_name(map.get("name").toString());
                                            me.setMate_phone(map.get("phone").toString());

                                            String detail = "NAME: " + map.get("name") + "\n";
                                            detail += "PHONE: " + map.get("phone") + "\n";
                                            mypg_mate_detail.setText(detail);

                                            mypg_stat.setText("ALL Update Success");

                                        } else {
                                            me.setPhone(phone);
                                            me.setMuid("");
                                            me.setMate_mail("");
                                            doc.put("phone", phone);
                                            doc.put("mate_mail", "");
                                            doc.put("mate_id", "");
                                            mypg_mate_detail.setText(mail + " user not found.");
                                            mypg_stat.setText("Phone SUCCESS Mate_Email DELETE");
                                        }
                                        mLoginDao.update(me);
                                        db.collection("users").document(me.getEmail())
                                                .set(doc, SetOptions.merge());
                                    }else{
                                        mypg_mate_detail.setText(mail+" user not found.");
                                        mypg_stat.setText("Phone SUCCESS Mate_Email DELETE");
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                }

                me = mLoginDao.getloginWithMail(me.getEmail());
                Log.d(TAG, "USER updated");
                matename.setText(me.getMate_name());
            }
        });
    }

    // Main Flow
    public void tabSetup(){
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost1);
        tabHost.setup();

        TabHost.TabSpec myList = tabHost.newTabSpec("My list Tap");
        myList.setContent(R.id.content1);
        myList.setIndicator("My List");
        tabHost.addTab(myList);

        TabHost.TabSpec itemList = tabHost.newTabSpec("Item List Tap");
        itemList.setContent(R.id.content2);
        itemList.setIndicator("Item List");
        tabHost.addTab(itemList);

        TabHost.TabSpec myPage = tabHost.newTabSpec("My page Tap");
        myPage.setContent(R.id.content3);
        myPage.setIndicator("My Page");
        tabHost.addTab(myPage);
    }

    // Activity Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CREATE_TOBUY && resultCode == RESULT_OK) {
            toShopList.add(0);
            loadToBuys();
        }else if (requestCode == RC_UPDATE_TOBUY && resultCode == RESULT_OK) {
            List<ToBuy> newlist = mToBuyDao.getToBuys(LoginEmail);
            if(toShopList.size() != newlist.size())
                toShopList.remove(shop_pos);
            for(int i=0; i<newlist.size(); i++)
                if(toShopList.get(i) > newlist.get(i).getAmount())
                    toShopList.set(i, newlist.get(i).getAmount());

            loadToBuys();
            loadShop();

        }else if(requestCode == RC_SHOP_TOBUY && resultCode == RESULT_OK) {
            int snum = data.getIntExtra(ShopToBuy.EXTRA_SELECT_AMOUNT, -1);
            toShopList.set(shop_pos, snum);
            loadShop();
        }else{
            Log.d(TAG, "requestCode: "+ requestCode+" resultCode: "+resultCode);
        }
    }
}
