package com.example.cohelp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.cohelp.EstructuraDb.HabitosEntry;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    DbHelper dbHelper;
    TextView tvCo2Hoy;
    BarChart chartSemanal;
    Button btnRegistrarHabito, btnVerHistorial, btnVerDesafios;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DbHelper(this);

        tvCo2Hoy = findViewById(R.id.tvCo2Hoy);
        chartSemanal = findViewById(R.id.chartSemanal);
        btnRegistrarHabito = findViewById(R.id.btnRegistrarHabito);
        btnVerHistorial = findViewById(R.id.btnVerHistorial);
        btnVerDesafios = findViewById(R.id.btnVerDesafios);

        btnRegistrarHabito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
                startActivity(intent);
            }
        });

        btnVerHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistorialActivity.class);
                startActivity(intent);
            }
        });

        btnVerDesafios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DesafiosActivity.class);
                startActivity(intent);
            }
        });

        programarAlarmaDiaria();
        pedirPermisoNotificaciones();
    }

    private void pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosDashboard();
    }

    private void cargarDatosDashboard() {
        String fechaHoy = getFechaDeHoy();
        double co2Hoy = dbHelper.obtenerCo2AhorradoHoy(fechaHoy);
        tvCo2Hoy.setText(String.format(Locale.getDefault(), "%.2f g", co2Hoy));
        cargarDatosGrafica();
    }

    private void cargarDatosGrafica() {
        String fechaInicioSemana = getFechaHaceXDias(7);
        Cursor cursor = dbHelper.obtenerDatosGraficaSemanal(fechaInicioSemana);

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int i = 0;
        while (cursor.moveToNext()) {
            float totalCo2 = cursor.getFloat(cursor.getColumnIndexOrThrow("total_co2"));
            String fecha = cursor.getString(cursor.getColumnIndexOrThrow(HabitosEntry.COLUMN_FECHA));

            entries.add(new BarEntry(i, totalCo2));
            labels.add(fecha.substring(8) + "/" + fecha.substring(5, 7));
            i++;
        }
        cursor.close();

        if (entries.isEmpty()) {
            chartSemanal.clear();
            chartSemanal.setNoDataText("No hay datos de la última semana");
            chartSemanal.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "CO2 Ahorrado (g)");
        dataSet.setColor(Color.GREEN);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);

        chartSemanal.setData(barData);
        chartSemanal.getDescription().setEnabled(false);
        chartSemanal.setFitBars(true);
        chartSemanal.animateY(1000);

        XAxis xAxis = chartSemanal.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(-45);

        chartSemanal.invalidate();
    }

    private String getFechaDeHoy() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    private String getFechaHaceXDias(int diasAtras) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -diasAtras);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(cal.getTime());
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    private void programarAlarmaDiaria() {
        Intent intent = new Intent(this, NotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }
}