package com.example.medisafe;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.medisafe.data.Medicine;
import com.example.medisafe.viewmodel.MedicineViewModel;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditMedicineActivity extends AppCompatActivity {

    private TextInputEditText editTextMedicineName;
    private TextInputEditText editTextMedicineForm;
    private TextInputEditText editTextMedicineDosage;
    private TextInputEditText editTextMedicineAmount;
    private TextInputEditText editTextMedicineDescription;
    private Button buttonExpiryDate;
    private Button buttonUpdate;
    private Button buttonCancel;
    private TextView textSelectedDate;

    private Calendar calendar;
    private Date selectedExpiryDate;
    private Medicine medicineToEdit;
    private MedicineViewModel medicineViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_medicine);

        // УСТАНАВЛИВАЕМ ЦВЕТ ВЕРХНЕЙ ПАНЕЛИ СТАТУСА
        setStatusBarColor();

        // Код для изменения цвета экшнбара
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF1976D2")));
        }

        // Получаем лекарство для редактирования
        medicineToEdit = (Medicine) getIntent().getSerializableExtra("MEDICINE_TO_EDIT");
        if (medicineToEdit == null) {
            Toast.makeText(this, "Ошибка: лекарство не найдено", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupViewModel();
        populateData();
        setupDatePicker();
        setupButtons();
    }

    // МЕТОД ДЛЯ ИЗМЕНЕНИЯ ЦВЕТА ВЕРХНЕЙ ПАНЕЛИ СТАТУСА
    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.primary_blue));

            // Если хочешь светлый текст на темном фоне (белый текст на синем)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            }
        }
    }

    private void initViews() {
        editTextMedicineName = findViewById(R.id.editTextMedicineName);
        editTextMedicineForm = findViewById(R.id.editTextMedicineForm);
        editTextMedicineDosage = findViewById(R.id.editTextMedicineDosage);
        editTextMedicineAmount = findViewById(R.id.editTextMedicineAmount);
        editTextMedicineDescription = findViewById(R.id.editTextMedicineDescription);
        buttonExpiryDate = findViewById(R.id.buttonExpiryDate);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonCancel = findViewById(R.id.buttonCancel);
        textSelectedDate = findViewById(R.id.textSelectedDate); // НОВОЕ ПОЛЕ!

        calendar = Calendar.getInstance();
    }

    private void setupViewModel() {
        medicineViewModel = new ViewModelProvider(this).get(MedicineViewModel.class);
    }

    private void populateData() {
        // Заполняем поля данными из лекарства
        editTextMedicineName.setText(medicineToEdit.getName());
        editTextMedicineForm.setText(medicineToEdit.getForm());
        editTextMedicineDosage.setText(medicineToEdit.getDosage());
        editTextMedicineAmount.setText(String.valueOf(medicineToEdit.getAmount()));
        editTextMedicineDescription.setText(medicineToEdit.getDescription());

        // Устанавливаем дату срока годности
        if (medicineToEdit.getExpiryDate() != null) {
            selectedExpiryDate = medicineToEdit.getExpiryDate();
            updateDateDisplay(selectedExpiryDate);
        }
    }

    private void setupDatePicker() {
        buttonExpiryDate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        // Устанавливаем текущую дату или дату из лекарства
        Calendar defaultCalendar = Calendar.getInstance();
        if (selectedExpiryDate != null) {
            defaultCalendar.setTime(selectedExpiryDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    selectedExpiryDate = calendar.getTime();
                    updateDateDisplay(selectedExpiryDate);
                },
                defaultCalendar.get(Calendar.YEAR),
                defaultCalendar.get(Calendar.MONTH),
                defaultCalendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void updateDateDisplay(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String dateString = sdf.format(date);

        // Обновляем текст кнопки
        buttonExpiryDate.setText("Дата выбрана: " + dateString);

        // Обновляем поле отображения даты
        if (textSelectedDate != null) {
            textSelectedDate.setText("Выбрана дата: " + dateString);
            textSelectedDate.setTextColor(ContextCompat.getColor(this, R.color.accent_green));
        }
    }

    private void setupButtons() {
        buttonUpdate.setOnClickListener(v -> updateMedicine());
        buttonCancel.setOnClickListener(v -> finish());
    }

    private void updateMedicine() {
        String name = editTextMedicineName.getText().toString().trim();
        String form = editTextMedicineForm.getText().toString().trim();
        String dosage = editTextMedicineDosage.getText().toString().trim();
        String amountStr = editTextMedicineAmount.getText().toString().trim();
        String description = editTextMedicineDescription.getText().toString().trim();

        // Валидация
        if (name.isEmpty()) {
            editTextMedicineName.setError("Введите название лекарства");
            return;
        }

        int amount = 0;
        if (!amountStr.isEmpty()) {
            try {
                amount = Integer.parseInt(amountStr);
            } catch (NumberFormatException e) {
                editTextMedicineAmount.setError("Введите корректное число");
                return;
            }
        }

        // Обновляем данные лекарства
        medicineToEdit.name = name;
        medicineToEdit.form = form;
        medicineToEdit.dosage = dosage;
        medicineToEdit.expiryDate = selectedExpiryDate;
        medicineToEdit.amount = amount;
        medicineToEdit.description = description;

        // Сохраняем изменения
        medicineViewModel.update(medicineToEdit);

        Toast.makeText(this, "Лекарство \"" + name + "\" обновлено!", Toast.LENGTH_SHORT).show();
        finish();
    }
}