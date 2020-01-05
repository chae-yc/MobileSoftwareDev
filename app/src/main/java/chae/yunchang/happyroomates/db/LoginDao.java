package chae.yunchang.happyroomates.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import chae.yunchang.happyroomates.models.Login;


@Dao
public interface LoginDao {
    @Insert
    public void insert(Login... Logins);

    @Update
    public void update(Login... Logins);

    @Delete
    public void delete(Login... Logins);

    @Query("SELECT * FROM login")
    public List<Login> getlogin();

    @Query("SELECT * FROM login WHERE email = :mail")
    public Login getloginWithMail(String mail);
}