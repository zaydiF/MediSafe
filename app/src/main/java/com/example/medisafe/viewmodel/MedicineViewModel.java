package com.example.medisafe.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.medisafe.data.AppDatabase;
import com.example.medisafe.data.MedicineDao;
import com.example.medisafe.data.Medicine;

import java.util.Date;
import java.util.List;

public class MedicineViewModel extends AndroidViewModel {
    private final MedicineDao medicineDao;
    private final LiveData<List<Medicine>> allMedicines;

    public MedicineViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getDatabase(application);
        medicineDao = database.medicineDao();
        allMedicines = medicineDao.getAllMedicines();
    }

    public LiveData<List<Medicine>> getAllMedicines() {
        return allMedicines;
    }

    public void insert(Medicine medicine) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            medicineDao.insert(medicine);
        });
    }

    public void update(Medicine medicine) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            medicineDao.update(medicine);
        });
    }

    public void delete(Medicine medicine) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            medicineDao.delete(medicine);
        });
    }

    public LiveData<List<Medicine>> getExpiredMedicines() {
        return medicineDao.getExpiredMedicines(new Date());
    }
}