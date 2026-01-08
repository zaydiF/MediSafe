package com.example.medisafe.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Medicine.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})  // ← ДОБАВЬ ЭТУ СТРОЧКУ
public abstract class AppDatabase extends RoomDatabase {
    public abstract MedicineDao medicineDao();

    private static volatile AppDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "medisafe_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}