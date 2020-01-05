package chae.yunchang.happyroomates.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "tobuy")
public class ToBuy {
    @PrimaryKey
    @NonNull
    private String item;

    private int amount;
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @NonNull
    public String getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }

    public void setItem(@NonNull String item) {
        this.item = item;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
