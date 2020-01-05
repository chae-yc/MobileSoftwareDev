package chae.yunchang.happyroomates.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import chae.yunchang.happyroomates.models.ToBuy;

@Dao
public interface ToBuyDao {
    @Insert
    public void insert(ToBuy... toBuys);

    @Delete
    public void delete(ToBuy... toBuy);

    @Update
    public void update(ToBuy... toBuy);

    @Query("SELECT * FROM tobuy WHERE email = :email")
    public List<ToBuy> getToBuys(String email);

    @Query("SELECT * FROM tobuy WHERE email = :email AND item = :item")
    public ToBuy getToBuyWithItem(String email, String item);


}