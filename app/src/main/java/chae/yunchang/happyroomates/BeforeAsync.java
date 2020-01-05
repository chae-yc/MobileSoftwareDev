package chae.yunchang.happyroomates;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import chae.yunchang.happyroomates.Adapters.BeforeRecyclerAdapter;
import chae.yunchang.happyroomates.models.BeforeItem;
import chae.yunchang.happyroomates.models.Login;

public class BeforeAsync extends AsyncTask<Void, BeforeItem, Integer> {


    // Firestore
    private FirebaseFirestore db;
    private FirebaseFirestoreSettings settings;
    private FirebaseAuth auth;
    private FirebaseUser fuser;

    private CollectionReference matecol;
    private Login me;

    private String TAG = "#BeforeAsync";

    // View
    private List<BeforeItem> beforeList;
    private BeforeRecyclerAdapter mBeforeRecyclerAdapter;
    private ImageView arrow;
    private TextView amount;
    private FloatingActionButton refresh;
    private int result;

    public BeforeAsync(List<BeforeItem> list, BeforeRecyclerAdapter adapter,
                       Login me, FloatingActionButton bt, TextView mon, ImageView img){
        this.beforeList = list;
        this.mBeforeRecyclerAdapter = adapter;
        this.me = me;
        this.refresh = bt;
        this.arrow = img;
        this.amount = mon;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
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

                                Task<QuerySnapshot> befores = matedoc.collection("before").get();
                                befores.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        for(DocumentSnapshot doc : task.getResult().getDocuments()){

                                            boolean exist = false;
                                            for(BeforeItem element: beforeList){
                                                if(element.getDocId().equals(doc.getId().toString())) {
                                                    exist = true;
                                                    break;
                                                }
                                            }
                                            if(exist)
                                                continue;

                                            Map<String, Object> map = doc.getData();
                                            BeforeItem b = new BeforeItem();

                                            b.setDocId(doc.getId().toString());
                                            b.setItems((HashMap<String, Object>) map.get("items"));
                                            b.setShop(map.get("shop").toString());
                                            b.setPaid((String) map.get("paid"));
                                            b.setTotal(Float.valueOf(String.format(Locale.ROOT, "%.2f",map.get("total_money"))));
                                            b.setMymoney(Float.valueOf(String.format(Locale.ROOT, "%.2f",map.get(me.getName()))));
                                            if(!me.getMate_name().equals(""))
                                                b.setMatemoney(Float.valueOf(String.format(Locale.ROOT, "%.2f",map.get(me.getMate_name()))));
                                            else
                                                b.setMatemoney((float)0.0);
                                            b.setBoughtDate(((Timestamp) map.get("date")).toDate());

                                            publishProgress(b);
                                        }
                                    }
                                });
                                befores.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error SELECT before documents", e);
                                    }
                                });
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
                    DocumentReference matedoc = matecol.document(task.getResult().getDocuments().get(0).getId());

                    Task<QuerySnapshot> befores = matedoc.collection("before").get();
                    befores.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {

                                boolean exist = false;
                                for(BeforeItem element: beforeList){
                                    if(element.getDocId().equals(doc.getId().toString())) {
                                        exist = true;
                                        break;
                                    }
                                }
                                if(exist)
                                    continue;

                                Map<String, Object> map = doc.getData();
                                BeforeItem b = new BeforeItem();
                                b.setShop(map.get("shop").toString());
                                b.setDocId(doc.getId().toString());
                                b.setItems((Map<String, Object>) map.get("items"));
                                b.setPaid((String) map.get("paid"));
                                b.setTotal(Float.valueOf(String.format(Locale.ROOT, "%.2f", map.get("total_money"))));
                                b.setMymoney(Float.valueOf(String.format(Locale.ROOT, "%.2f", map.get(me.getName()))));
                                if(!me.getMate_name().equals(""))
                                    b.setMatemoney(Float.valueOf(String.format(Locale.ROOT, "%.2f",map.get(me.getMate_name()))));
                                else
                                    b.setMatemoney((float)0.0);
                                b.setBoughtDate(((Timestamp) map.get("date")).toDate());

                                publishProgress(b);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error SELECT before documents", e);
                        }
                    }); // before Complete
                }
            }
        });  // q1 Complete
        q1.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Any document dosen't find by Q1(My uid)");
                return;
            }
        }); // q1 Failure*/

        return result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        beforeList.clear();
        mBeforeRecyclerAdapter.updateData(beforeList);

        // FIRE-AUTH SETUP
        auth = FirebaseAuth.getInstance();
        fuser = auth.getCurrentUser();
        // FIRE-DB SETUP
        db = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        RotateAnimation rotate = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(500);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setRepeatMode(Animation.RESTART);
        rotate.setInterpolator(new LinearInterpolator());
        refresh.setAnimation(rotate);
    }

    @Override
    protected void onProgressUpdate(BeforeItem... progress) {
        super.onProgressUpdate(progress);
        Log.d(TAG, progress[0].getPaid());
        beforeList.add(progress[0]);
        mBeforeRecyclerAdapter.updateData(beforeList);

        float iUsed=0;
        float iPaid=0;
        float mUsed=0;
        float mPaid=0;

        for(BeforeItem item: beforeList){
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

        //amount.setText(String.format(Locale.ROOT, "€%.2f", Math.abs(amountDue)));
        amount.setText(String.format(Locale.ROOT, "€%.2f", amountDue));
        result = beforeList.size();
    }

    protected void onPostExecute(Integer result){
        super.onPostExecute(result);
        refresh.clearAnimation();
        Log.d(TAG, "onPostExecute BeforeItemSize: "+beforeList.size());
    }
}
