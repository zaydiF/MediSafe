package com.example.medisafe;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medisafe.data.Medicine;
import com.example.medisafe.viewmodel.MedicineViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MedicineAdapter adapter;
    private MedicineViewModel medicineViewModel;
    private List<Medicine> medicineList = new ArrayList<>();

    private TextView textTotalMedicines;
    private TextView textExpiredMedicines;
    private LinearLayout layoutEmptyState;
    private TextView textSort;

    // Переменная для хранения текущей сортировки
    private int currentSortType = 0; // 0-по названию А-Я, 1-по названию Я-А, 2-по сроку годности, 3-по дате добавления

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // УСТАНАВЛИВАЕМ ЦВЕТ ВЕРХНЕЙ ПАНЕЛИ СТАТУСА (ГДЕ ВРЕМЯ)
        setStatusBarColor();

        // Код для изменения цвета экшнбара
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF1976D2")));
        }

        initViews();
        setupRecyclerView();
        setupViewModel();
        setupFab();
    }

    // МЕТОД ДЛЯ ИЗМЕНЕНИЯ ЦВЕТА ВЕРХНЕЙ ПАНЕЛИ СТАТУСА
    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.primary_blue));

            // светлый текст на темном фоне (белый текст на синем)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            }
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewMedicines);
        textTotalMedicines = findViewById(R.id.textTotalMedicines);
        textExpiredMedicines = findViewById(R.id.textExpiredMedicines);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        textSort = findViewById(R.id.textSort);

        textSort.setOnClickListener(v -> showSortDialog());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicineAdapter(medicineList, this::onMedicineClick);
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        medicineViewModel = new ViewModelProvider(this).get(MedicineViewModel.class);

        medicineViewModel.getAllMedicines().observe(this, medicines -> {
            medicineList.clear();
            medicineList.addAll(medicines);
            applySorting(); // Применяем текущую сортировку
            updateStatistics(medicines);
            updateEmptyState(medicines.isEmpty());
        });
    }

    private void setupFab() {
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddMedicineActivity.class);
            startActivity(intent);
        });
    }

    private void updateStatistics(List<Medicine> medicines) {
        int total = medicines.size();
        int expired = 0;
        int expiringSoon = 0;

        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        Date weekLater = calendar.getTime();

        for (Medicine medicine : medicines) {
            if (medicine.getExpiryDate() != null) {
                if (medicine.getExpiryDate().before(currentDate)) {
                    expired++;
                } else if (medicine.getExpiryDate().before(weekLater)) {
                    expiringSoon++;
                }
            }
        }

        textTotalMedicines.setText(String.valueOf(total));

        if (expired > 0) {
            textExpiredMedicines.setText(expired + "");
            textExpiredMedicines.setTextColor(ContextCompat.getColor(this, R.color.status_error));
        } else if (expiringSoon > 0) {
            textExpiredMedicines.setText(expiringSoon + " Истекает");
            textExpiredMedicines.setTextColor(ContextCompat.getColor(this, R.color.status_warning));
        } else {
            textExpiredMedicines.setText("Все в порядке");
            textExpiredMedicines.setTextColor(ContextCompat.getColor(this, R.color.status_success));
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showSortDialog() {
        // Создаем кастомный View для диалога
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sort, null);

        // Находим все кнопки
        MaterialButton buttonNameAsc = dialogView.findViewById(R.id.buttonNameAsc);
        MaterialButton buttonNameDesc = dialogView.findViewById(R.id.buttonNameDesc);
        MaterialButton buttonExpiry = dialogView.findViewById(R.id.buttonExpiry);
        MaterialButton buttonCreated = dialogView.findViewById(R.id.buttonCreated);
        MaterialButton buttonExpiredFirst = dialogView.findViewById(R.id.buttonExpiredFirst);
        MaterialButton buttonCancel = dialogView.findViewById(R.id.buttonCancelSort);

        // Создаем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setCancelable(true);

        AlertDialog sortDialog = builder.create();
        sortDialog.show();

        // Настраиваем окно диалога
        Window window = sortDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Обработчики кнопок
        buttonNameAsc.setOnClickListener(v -> {
            currentSortType = 0;
            applySorting();
            updateSortText(0);
            sortDialog.dismiss();
            Toast.makeText(this, "Сортировка по названию (А-Я) применена", Toast.LENGTH_SHORT).show();
        });

        buttonNameDesc.setOnClickListener(v -> {
            currentSortType = 1;
            applySorting();
            updateSortText(1);
            sortDialog.dismiss();
            Toast.makeText(this, "Сортировка по названию (Я-А) применена", Toast.LENGTH_SHORT).show();
        });

        buttonExpiry.setOnClickListener(v -> {
            currentSortType = 2;
            applySorting();
            updateSortText(2);
            sortDialog.dismiss();
            Toast.makeText(this, "Сортировка по сроку годности применена", Toast.LENGTH_SHORT).show();
        });

        buttonCreated.setOnClickListener(v -> {
            currentSortType = 3;
            applySorting();
            updateSortText(3);
            sortDialog.dismiss();
            Toast.makeText(this, "Сортировка по дате добавления применена", Toast.LENGTH_SHORT).show();
        });

        buttonExpiredFirst.setOnClickListener(v -> {
            currentSortType = 4;
            applySorting();
            updateSortText(4);
            sortDialog.dismiss();
            Toast.makeText(this, "Сначала показаны просроченные", Toast.LENGTH_SHORT).show();
        });

        buttonCancel.setOnClickListener(v -> {
            sortDialog.dismiss();
        });
    }

    private void updateSortText(int sortType) {
        String[] sortTexts = {
                "По названию ▲",
                "По названию ▼",
                "По сроку годности",
                "По дате добавления",
                "Сначала просроченные"
        };
        textSort.setText(sortTexts[sortType]);
    }

    private void applySorting() {
        switch (currentSortType) {
            case 0: // По названию А-Я
                sortByNameAsc();
                break;
            case 1: // По названию Я-А
                sortByNameDesc();
                break;
            case 2: // По сроку годности
                sortByExpiryDate();
                break;
            case 3: // По дате добавления
                sortByCreatedDate();
                break;
            case 4: // Сначала просроченные
                sortByExpiredFirst();
                break;
        }
        adapter.notifyDataSetChanged();
    }

    private void sortByNameAsc() {
        Collections.sort(medicineList, (m1, m2) -> {
            if (m1.getName() == null && m2.getName() == null) return 0;
            if (m1.getName() == null) return -1;
            if (m2.getName() == null) return 1;
            return m1.getName().compareToIgnoreCase(m2.getName());
        });
    }

    private void sortByNameDesc() {
        Collections.sort(medicineList, (m1, m2) -> {
            if (m1.getName() == null && m2.getName() == null) return 0;
            if (m1.getName() == null) return 1;
            if (m2.getName() == null) return -1;
            return m2.getName().compareToIgnoreCase(m1.getName());
        });
    }

    private void sortByExpiryDate() {
        Collections.sort(medicineList, (m1, m2) -> {
            if (m1.getExpiryDate() == null && m2.getExpiryDate() == null) return 0;
            if (m1.getExpiryDate() == null) return 1;
            if (m2.getExpiryDate() == null) return -1;
            return m1.getExpiryDate().compareTo(m2.getExpiryDate());
        });
    }

    private void sortByCreatedDate() {
        Collections.sort(medicineList, (m1, m2) -> {
            if (m1.getCreatedAt() == null && m2.getCreatedAt() == null) return 0;
            if (m1.getCreatedAt() == null) return 1;
            if (m2.getCreatedAt() == null) return -1;
            return m2.getCreatedAt().compareTo(m1.getCreatedAt()); // Сначала новые
        });
    }

    private void sortByExpiredFirst() {
        Date currentDate = new Date();
        Collections.sort(medicineList, (m1, m2) -> {
            boolean m1Expired = m1.getExpiryDate() != null && m1.getExpiryDate().before(currentDate);
            boolean m2Expired = m2.getExpiryDate() != null && m2.getExpiryDate().before(currentDate);

            if (m1Expired && !m2Expired) return -1;
            if (!m1Expired && m2Expired) return 1;

            // Если оба просрочены или оба не просрочены, сортируем по сроку годности
            if (m1.getExpiryDate() == null && m2.getExpiryDate() == null) return 0;
            if (m1.getExpiryDate() == null) return 1;
            if (m2.getExpiryDate() == null) return -1;

            return m1.getExpiryDate().compareTo(m2.getExpiryDate());
        });
    }

    private void onMedicineClick(Medicine medicine) {
        showMedicineActionsDialog(medicine);
    }

    private void showMedicineActionsDialog(Medicine medicine) {
        // Создаем кастомный диалог
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_modern_actions, null);

        // Находим View
        TextView textTitle = dialogView.findViewById(R.id.textDialogTitle);
        MaterialButton buttonDetails = dialogView.findViewById(R.id.buttonDetails);
        MaterialButton buttonEdit = dialogView.findViewById(R.id.buttonEditAction);
        MaterialButton buttonDelete = dialogView.findViewById(R.id.buttonDeleteAction);

        // Устанавливаем название лекарства
        textTitle.setText(medicine.getName());

        // Создаем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setCancelable(true);

        AlertDialog actionsDialog = builder.create();
        actionsDialog.show();

        // Настраиваем обработчики кнопок
        buttonDetails.setOnClickListener(v -> {
            actionsDialog.dismiss();
            showMedicineDetails(medicine);
        });

        buttonEdit.setOnClickListener(v -> {
            actionsDialog.dismiss();
            editMedicine(medicine);
        });

        buttonDelete.setOnClickListener(v -> {
            actionsDialog.dismiss();
            deleteMedicine(medicine);
        });

        // Настраиваем окно диалога
        Window window = actionsDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void showMedicineDetails(Medicine medicine) {
        // Создаем кастомный диалог
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_medicine_info, null);

        // Находим все View и заполняем данные
        TextView textName = dialogView.findViewById(R.id.textMedicineName);
        TextView textStatus = dialogView.findViewById(R.id.textMedicineStatus);
        TextView textForm = dialogView.findViewById(R.id.textMedicineForm);
        TextView textDosage = dialogView.findViewById(R.id.textMedicineDosage);
        TextView textExpiry = dialogView.findViewById(R.id.textMedicineExpiry);
        TextView textDaysLeft = dialogView.findViewById(R.id.textDaysLeft);
        TextView textAmount = dialogView.findViewById(R.id.textMedicineAmount);
        TextView textDescription = dialogView.findViewById(R.id.textMedicineDescription);
        TextView textCreated = dialogView.findViewById(R.id.textMedicineCreated);
        View statusIndicator = dialogView.findViewById(R.id.statusIndicator);

        // Находим кастомные кнопки
        Button buttonUnderstand = dialogView.findViewById(R.id.buttonUnderstand);
        Button buttonEdit = dialogView.findViewById(R.id.buttonEdit);
        Button buttonDelete = dialogView.findViewById(R.id.buttonDelete);

        // Заполняем данные
        textName.setText(medicine.getName());
        textForm.setText(medicine.getForm() != null ? medicine.getForm() : "Не указана");
        textDosage.setText(medicine.getDosage() != null ? medicine.getDosage() : "Не указана");
        textAmount.setText(medicine.getAmount() + " шт.");
        textDescription.setText(medicine.getDescription() != null && !medicine.getDescription().isEmpty() ?
                medicine.getDescription() : "Описание отсутствует");

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String expiryDate = medicine.getExpiryDate() != null ?
                sdf.format(medicine.getExpiryDate()) : "Не указан";

        String createdDate = medicine.getCreatedAt() != null ?
                sdf.format(medicine.getCreatedAt()) : "Неизвестно";
        textCreated.setText("Добавлено: " + createdDate);

        // Устанавливаем статус
        if (medicine.getExpiryDate() != null) {
            Date currentDate = new Date();
            long diff = medicine.getExpiryDate().getTime() - currentDate.getTime();
            long daysLeft = diff / (1000 * 60 * 60 * 24);

            if (medicine.getExpiryDate().before(currentDate)) {
                statusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.status_error));
                textStatus.setText("❌ ПРОСРОЧЕНО");
                textExpiry.setText("ПРОСРОЧЕНО: " + expiryDate);
                textDaysLeft.setText("Просрочено на: " + Math.abs(daysLeft) + " дней");
                textDaysLeft.setTextColor(ContextCompat.getColor(this, R.color.status_error));
            } else if (daysLeft <= 7) {
                statusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.status_warning));
                textStatus.setText("⚠️ СКОРО ИСТЕКАЕТ");
                textExpiry.setText("Скоро истекает: " + expiryDate);
                textDaysLeft.setText("Осталось: " + daysLeft + " дней");
                textDaysLeft.setTextColor(ContextCompat.getColor(this, R.color.status_warning));
            } else {
                statusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.status_success));
                textStatus.setText("✅ В ПОРЯДКЕ");
                textExpiry.setText("Годен до: " + expiryDate);
                textDaysLeft.setText("Осталось: " + daysLeft + " дней");
                textDaysLeft.setTextColor(ContextCompat.getColor(this, R.color.status_success));
            }
        } else {
            statusIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.text_hint));
            textStatus.setText("📋 БЕЗ СРОКА");
            textExpiry.setText("Срок годности не указан");
            textDaysLeft.setText("Срок не установлен");
            textDaysLeft.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
        }

        // Настраиваем обработчики кнопок
        buttonUnderstand.setOnClickListener(v -> {
            // Просто закрываем диалог
            if (currentDialog != null && currentDialog.isShowing()) {
                currentDialog.dismiss();
            }
        });

        buttonEdit.setOnClickListener(v -> {
            if (currentDialog != null && currentDialog.isShowing()) {
                currentDialog.dismiss();
            }
            editMedicine(medicine);
        });

        buttonDelete.setOnClickListener(v -> {
            if (currentDialog != null && currentDialog.isShowing()) {
                currentDialog.dismiss();
            }
            deleteMedicine(medicine);
        });

        // Создаем диалог БЕЗ стандартных кнопок
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setCancelable(true);

        currentDialog = builder.create();
        currentDialog.show();

        // Настраиваем окно диалога
        Window window = currentDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.dialog_background);
        }
    }

    // Добавь поле в класс MainActivity
    private AlertDialog currentDialog;

    private void editMedicine(Medicine medicine) {
        Intent intent = new Intent(MainActivity.this, EditMedicineActivity.class);
        intent.putExtra("MEDICINE_TO_EDIT", medicine);
        startActivity(intent);
    }

    private void deleteMedicine(Medicine medicine) {
        // Создаем кастомный View для диалога подтверждения
        View confirmView = getLayoutInflater().inflate(R.layout.dialog_confirm_delete, null);

        TextView textMessage = confirmView.findViewById(R.id.textDeleteMessage);
        textMessage.setText("Вы уверены, что хотите удалить \"" + medicine.getName() + "\" из аптечки?");

        // Находим кастомные кнопки
        MaterialButton buttonCancel = confirmView.findViewById(R.id.buttonCancelDelete);
        MaterialButton buttonConfirm = confirmView.findViewById(R.id.buttonConfirmDelete);

        // Создаем диалог БЕЗ стандартных кнопок
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(confirmView)
                .setCancelable(true);

        AlertDialog deleteDialog = builder.create();
        deleteDialog.show();

        // Настраиваем обработчики кнопок
        buttonCancel.setOnClickListener(v -> {
            deleteDialog.dismiss();
        });

        buttonConfirm.setOnClickListener(v -> {
            medicineViewModel.delete(medicine);
            Toast.makeText(this, "Лекарство \"" + medicine.getName() + "\" удалено", Toast.LENGTH_SHORT).show();
            deleteDialog.dismiss();
        });

        // Настраиваем окно диалога
        Window window = deleteDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.dialog_background);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (medicineViewModel != null && medicineViewModel.getAllMedicines().getValue() != null) {
            updateStatistics(medicineViewModel.getAllMedicines().getValue());
        }
    }
}