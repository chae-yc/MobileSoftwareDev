package chae.yunchang.happyroomates.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "login")
public class Login {


    private String name;
    @PrimaryKey
    @NonNull
    private String email;
    private String phone;
    private String uid;
    private String muid;
    private String mate_name;
    private String mate_mail;
    private String mate_phone;

    public String getMate_name() {
        return mate_name;
    }

    public void setMate_name(String mate_name) {
        this.mate_name = mate_name;
    }

    public String getMate_phone() {
        return mate_phone;
    }

    public void setMate_phone(String mate_phone) {
        this.mate_phone = mate_phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMuid() {
        return muid;
    }

    public void setMuid(String muid) {
        this.muid = muid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getMate_mail() {
        return mate_mail;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setMate_mail(String mate_mail) {
        this.mate_mail = mate_mail;
    }
}
