package com.example.medisafe;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medisafe.data.Medicine;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private List<Medicine> medicineList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Medicine medicine);
    }

    public MedicineAdapter(List<Medicine> medicineList, OnItemClickListener listener) {
        this.medicineList = medicineList;
        this.listener = listener;
    }

    public void updateMedicines(List<Medicine> newMedicines) {
        medicineList.clear();
        medicineList.addAll(newMedicines);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine medicine = medicineList.get(position);

        holder.textMedicineName.setText(medicine.getName());
        holder.textMedicineForm.setText(medicine.getForm());
        holder.textMedicineDosage.setText(medicine.getDosage());
        holder.textMedicineAmount.setText(medicine.getAmount() + " шт");

        if (medicine.getExpiryDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String expiryDate = sdf.format(medicine.getExpiryDate());

            if (medicine.getExpiryDate().before(new Date())) {
                holder.textMedicineExpiry.setText("ПРОСРОЧЕНО: " + expiryDate);
                holder.textMedicineExpiry.setTextColor(Color.RED);
            } else {
                holder.textMedicineExpiry.setText("Годен до: " + expiryDate);
                holder.textMedicineExpiry.setTextColor(Color.BLACK);
            }
        } else {
            holder.textMedicineExpiry.setText("Срок годности не указан");
            holder.textMedicineExpiry.setTextColor(Color.LTGRAY);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(medicine);
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView textMedicineName;
        TextView textMedicineForm;
        TextView textMedicineDosage;
        TextView textMedicineAmount;
        TextView textMedicineExpiry;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            textMedicineName = itemView.findViewById(R.id.textMedicineName);
            textMedicineForm = itemView.findViewById(R.id.textMedicineForm);
            textMedicineDosage = itemView.findViewById(R.id.textMedicineDosage);
            textMedicineAmount = itemView.findViewById(R.id.textMedicineAmount);
            textMedicineExpiry = itemView.findViewById(R.id.textMedicineExpiry);
        }
    }
}