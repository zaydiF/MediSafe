package com.example.medisafe.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "medicines")
public class Medicine implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String form;
    public String dosage;
    public Date expiryDate;
    public int amount;
    public String description;
    public Date createdAt;

    public Medicine(String name, String form, String dosage, Date expiryDate,
                    int amount, String description) {
        this.name = name;
        this.form = form;
        this.dosage = dosage;
        this.expiryDate = expiryDate;
        this.amount = amount;
        this.description = description;
        this.createdAt = new Date();
    }

    // Геттеры
    public int getId() { return id; }
    public String getName() { return name; }
    public String getForm() { return form; }
    public String getDosage() { return dosage; }
    public Date getExpiryDate() { return expiryDate; }
    public int getAmount() { return amount; }
    public String getDescription() { return description; }
    public Date getCreatedAt() { return createdAt; }

    // Сеттеры
    public void setName(String name) { this.name = name; }
    public void setForm(String form) { this.form = form; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
    public void setAmount(int amount) { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
}