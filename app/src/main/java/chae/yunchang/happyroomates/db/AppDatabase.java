package chae.yunchang.happyroomates.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import chae.yunchang.happyroomates.models.Login;
import chae.yunchang.happyroomates.models.ToBuy;

@Database(entities = {Login.class, ToBuy.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LoginDao getLoginDao();
    public abstract ToBuyDao getToBuyDao();
}