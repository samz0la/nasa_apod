package edu.cnm.deepdive.nasaapod.controller;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.DatePicker;
import android.widget.TimePicker;
import java.util.Calendar;

public class DateTimePickerFragment extends DialogFragment
    implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

  private Mode mode = Mode.DATE;
  private Calendar calendar = null;
  private OnChangeListener listener = null;

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    Dialog dialog = null;
    Calendar defaultValue = (calendar != null) ? calendar : Calendar.getInstance();
    if(mode == Mode.DATE) {
      dialog = new DatePickerDialog(getActivity(), this, defaultValue.get(Calendar.YEAR),
          defaultValue.get(Calendar.MONTH), defaultValue.get(Calendar.DAY_OF_MONTH));
    }else {
      dialog = new TimePickerDialog(getActivity(), this, defaultValue.get(Calendar.HOUR),
          defaultValue.get(Calendar.MINUTE), DateFormat.is24HourFormat(getActivity()));
    }
    return dialog;
  }

  @Override
  public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
    Calendar updateValue = (calendar != null) ? calendar : Calendar.getInstance();
    updateValue.set(Calendar.YEAR, year);
    updateValue.set(Calendar.MONTH, month);
    updateValue.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    if(listener != null) {
      listener.onChange(updateValue);
    }
  }

  @Override
  public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    Calendar updateValue = (calendar != null) ? calendar : Calendar.getInstance();
    updateValue.set(Calendar.HOUR, hourOfDay);
    updateValue.set(Calendar.MINUTE, minute);
    if(listener != null) {
      listener.onChange(updateValue);
    }
  }

  public Mode getMode() {
    return mode;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  public Calendar getCalendar() {
    return calendar;
  }

  public void setCalendar(Calendar calendar) {
    this.calendar = calendar;
  }

  public OnChangeListener getListener() {
    return listener;
  }

  public void setListener(
      OnChangeListener listener) {
    this.listener = listener;
  }

  public enum Mode {
    DATE, TIME
  }

  public interface OnChangeListener {
    void onChange(Calendar calendar);
  }
}
