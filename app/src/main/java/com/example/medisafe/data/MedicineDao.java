package com.example.medisafe.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;


@Dao
public interface MedicineDao {
    @Insert
    void insert(Medicine medicine);
    @Update
    void update(Medicine medicine);

    @Delete
    void delete(Medicine medicine);

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    LiveData<List<Medicine>> getAllMedicines();

    @Query("SELECT * FROM medicines WHERE expiryDate < :currentDate ORDER BY expiryDate ASC")
    LiveData<List<Medicine>> getExpiredMedicines(Date currentDate);

    @Query("SELECT * FROM medicines WHERE id = :id")
    Medicine getMedicineById(int id);
}