package edu.cnm.deepdive.nasaapod.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.cnm.deepdive.nasaapod.BuildConfig;
import edu.cnm.deepdive.nasaapod.R;
import edu.cnm.deepdive.nasaapod.controller.DateTimePickerFragment.Mode;
import edu.cnm.deepdive.nasaapod.model.Apod;
import edu.cnm.deepdive.nasaapod.service.ApodService;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {


  private static final String DATE_FORMAT = "yyyy-MM-dd";
  private static final String CALENDAR_KEY = "calendar";
  private static final String APOD_KEY = "apod";

  private WebView webView;
  private String apiKey;
  private ProgressBar progressSpinner;
  private FloatingActionButton jumpDate;
  private Calendar calendar;
  private ApodService service;
  private Apod apod;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setUpWebView();
    setupService();
    setupUI();
    setupDefaults(savedInstanceState);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putLong(CALENDAR_KEY, calendar.getTimeInMillis());
    outState.putParcelable(APOD_KEY, apod);
  }

  private void setUpWebView() {
    webView = findViewById(R.id.web_view);
    webView.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return false;
      }

      @Override
      public void onPageFinished(WebView view, String url) {
        progressSpinner.setVisibility(View.GONE);
        if(apod != null) {
          Toast.makeText(MainActivity.this, apod.getTitle(), Toast.LENGTH_LONG).show();
        }
      }
    });
    WebSettings settings = webView.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setSupportZoom(true);
    settings.setBuiltInZoomControls(true);
    settings.setDisplayZoomControls(false);
    settings.setUseWideViewPort(true);
    settings.setLoadWithOverviewMode(true);
  }

  private void setupUI() {
    progressSpinner = findViewById(R.id.progress_spinner);
    progressSpinner.setVisibility(View.GONE);
    jumpDate = findViewById(R.id.jump_date);
    jumpDate.setOnClickListener(v -> pickDate());
  }
  private void setupService() {
    Gson gson = new GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .setDateFormat(DATE_FORMAT)
        .create();
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(getString(R.string.base_url))
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build();
    service = retrofit.create(ApodService.class);
    apiKey = BuildConfig.API_KEY;
  }

  private void setupDefaults(Bundle savedInstanceState) {
    calendar = Calendar.getInstance();
    if(savedInstanceState != null) {
      calendar.setTimeInMillis(savedInstanceState.getLong(CALENDAR_KEY, calendar.getTimeInMillis()));
      apod = savedInstanceState.getParcelable(APOD_KEY);
    }
    if(apod != null) {
      progressSpinner.setVisibility(View.VISIBLE);
      webView.loadUrl(apod.getUrl());
    }else {
      new ApodTask().execute();
    }
  }

  private void pickDate() {
    DateTimePickerFragment picker = new DateTimePickerFragment();
    picker.setMode(Mode.DATE);
    picker.setCalendar(calendar);
    picker.setListener((cal) -> new ApodTask().execute(cal.getTime()));
    picker.show(getSupportFragmentManager(), picker.getClass().getSimpleName());
  }

  private class ApodTask extends AsyncTask<Date, Void, Apod> {

    private Date date;


    @Override
    protected void onPreExecute() {
      progressSpinner.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(Apod apod) {
       MainActivity.this.apod = apod;
       //TODO Handle hdUrl
       webView.loadUrl(apod.getUrl());
    }

    @Override
    protected void onCancelled(Apod apod) {
      Context context = MainActivity.this;
      progressSpinner.setVisibility(View.GONE);
      Toast.makeText(context, R.string.error_message, Toast.LENGTH_LONG)
          .show();
    }

    @Override
    protected Apod doInBackground(Date... dates) {
      Apod apod = null;
      try {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        date = (dates.length == 0) ? calendar.getTime() : dates[0];
        Response<Apod> response = service.get(apiKey, format.format(date)).execute();
        if (response.isSuccessful()) {
          apod = response.body();
          calendar.setTime(date);
        }
      } catch (IOException e) {
        // Do nothing: apod is already null.
      } finally {
        if(apod == null) {
          cancel(true);
        }
      }
      return apod;
    }
  }
}
