package chae.yunchang.happyroomates;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import chae.yunchang.happyroomates.Adapters.AfterListAdapter;
import chae.yunchang.happyroomates.models.BeforeItem;
import chae.yunchang.happyroomates.models.Login;

public class AfterList extends Dialog implements View.OnClickListener{
    private String TAG = "#After";

    // VIEW
    private Context context;
    private ArrayList<BeforeItem> beforeItemList;

    private Button mSave;
    private Button mCancel;
    private String amount;

    // DB
    private Login me;

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

    public AfterList(@NonNull Context context, ArrayList<BeforeItem> list, Login me, String amount) {
        super(context);
        this.context = context;
        this.beforeItemList = list;
        this.me = me;
        this.amount = amount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afterlist);

        // FIRE-AUTH SETUP
        auth = FirebaseAuth.getInstance();
        fuser = auth.getCurrentUser();
        // FIRE-DB SETUP
        db = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        ListView mAfterView;
        AfterListAdapter mAfterAdapter;

        // VIEW
        mAfterView = findViewById(R.id.afterlistView);

        mSave = findViewById(R.id.afterList_saveButton);
        mSave.setOnClickListener(this);
        mCancel = findViewById(R.id.afterList_cancelButton);
        mCancel.setOnClickListener(this);

        Log.d(TAG, "checked size: "+beforeItemList.size());

        mAfterAdapter = new AfterListAdapter(context, beforeItemList);
        mAfterView.setAdapter(mAfterAdapter);

        TextView amount = findViewById(R.id.afterlist_amount);
        TextView myname = findViewById(R.id.afterlist_myname);
        TextView matename = findViewById(R.id.afterlist_matename);
        ImageView arrow = findViewById(R.id.afterlist_arrow);

        amount.setText(this.amount);
        myname.setText(me.getName());
        matename.setText(me.getMate_name());

        float iUsed=0;
        float iPaid=0;
        float mUsed=0;
        float mPaid=0;
        for(BeforeItem item: beforeItemList){
            iUsed += item.getMymoney();
            mUsed += item.getMatemoney();
            if(item.getPaid().equals(me.getName()))
                iPaid += item.getTotal();
            else
                mPaid += item.getTotal();
        }
        Log.d(TAG, "total used: "+(iPaid+mPaid));
        Log.d(TAG, "my used/paid: "+iUsed+"/"+iPaid);
        Log.d(TAG, "mate used/paid: "+mUsed+"/"+mPaid);
        float amountDue = iPaid-iUsed; // 낸게 쓴거 보다 많으면 양수 그럼 안내도 됨 받아야됨
        if(amountDue > 0)
            arrow.setImageResource(R.drawable.right);
        else if(amountDue < 0)
            arrow.setImageResource(R.drawable.left);
        else
            arrow.setImageResource(0);

        amount.setText(String.format(Locale.ROOT, "€%.2f", amountDue));
    }

    private CollectionReference matecol;

    private void moveList(){
        matecol = db.collection("mates");
        Task<QuerySnapshot> q1 = matecol.whereEqualTo("mate1", me.getUid()).get();
        q1.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult().size() == 0){
                    // Query2
                    Task<QuerySnapshot> q2 = matecol.whereEqualTo("mate2", me.getUid()).get();
                    q2.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.getResult().size() == 0) {
                                Log.d(TAG, "Any document dosen't find by Q2(Mate uid == my uid)");
                                return ;
                            } else {
                                // Found with Q2
                                DocumentReference matedoc = matecol.document(task.getResult().getDocuments().get(0).getId());
                                for(int i=0; i<beforeItemList.size(); i++) {
                                    String id = beforeItemList.get(i).getDocId();
                                    DocumentReference before = matedoc.collection("before").document(id);
                                    DocumentReference after = matedoc.collection("after").document(id);
                                    moveFirestoreDocument(before, after);
                                }
                            }
                        }
                    }); // q2 Complete
                    q2.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "q2 Error SELECT document", e);
                            return;
                        }
                    }); // q2 Failure
                }else{
                    // Query2 not started
                    if(task.getResult().size() == 0) {
                        Log.d(TAG, "Any document dosen't find by Q1(my uid == my uid)");
                        return ;
                    }else {
                        DocumentReference matedoc = matecol.document(task.getResult().getDocuments().get(0).getId());
                        for (int i = 0; i < beforeItemList.size(); i++) {
                            String id = beforeItemList.get(i).getDocId();
                            DocumentReference before = matedoc.collection("before").document(id);
                            DocumentReference after = matedoc.collection("after").document(id);
                            moveFirestoreDocument(before, after);
                        }
                    }
                }
            }
        });  // q1 Complete
        q1.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Any document dosen't find by Q1(My uid)");
                return;
            }
        }); // q1 Failure
    }

    // Events
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.afterList_saveButton:
                Log.d(TAG, "save button");
                moveList();
                dialogListener.onPositiveClicked();
                dismiss();
                break;
            case R.id.afterList_cancelButton:
                Log.d(TAG, "cancel button");
                dialogListener.onNegativeClicked();
                dismiss();
                break;
        }
    }

    public void setDialogListener(MyDialogListener dialogListener){
        this.dialogListener = dialogListener;
    }

    @Override
    public void onBackPressed() {
        mCancel.performClick();
    }

    public void moveFirestoreDocument(final DocumentReference fromPath, final DocumentReference toPath) {
        fromPath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        Map<String, Object> tmp = document.getData();

                        fromPath.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error deleting document", e);
                                    }
                                });

                        toPath.set(tmp)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}