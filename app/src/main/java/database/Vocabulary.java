package database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Vocabulary implements Serializable {

    @PrimaryKey()
    @NonNull
    public String eng;

    public String kor;

    @ColumnInfo(name = "add_time")
    public int addedTime;

    @ColumnInfo(name = "last_update")
    public int lastEditedTime;

    public String memo;

    public Vocabulary(String eng, String kor, int addedTime, int lastEditedTime, String memo) {
        this.eng = eng;
        this.kor = kor;
        this.addedTime = addedTime;
        this.lastEditedTime = lastEditedTime;
        this.memo = memo;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Vocabulary))
            return false;
        return this.eng.contentEquals(((Vocabulary) obj).eng);
    }
}