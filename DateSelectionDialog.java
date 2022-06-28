package com.arsinex.com;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.widget.DatePicker;

import java.util.Calendar;

public class DateSelectionDialog implements DatePickerDialog.OnDateSetListener {

    private Activity activity;
    private String selectedDate;

    public DateSelectionDialog(Activity activity){
        this.activity = activity;
    }

    public void OpenCalender(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(activity.getApplicationContext(),
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        selectedDate = dayOfMonth + "/" + month + "/" + year;
    }

    public String getSelectedDate() {
        return selectedDate;
    }
}
