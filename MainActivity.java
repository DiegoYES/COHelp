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
import static com.example.cohelp.EstructuraDb.HabitosEntry.COLUMN_FECHA;

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
        // Enlazamos los componentes de la interfaz
        tvCo2Hoy = findViewById(R.id.tvCo2Hoy);
        chartSemanal = findViewById(R.id.chartSemanal);
        btnRegistrarHabito = findViewById(R.id.btnRegistrarHabito);
        btnVerHistorial = findViewById(R.id.btnVerHistorial);
        btnVerDesafios = findViewById(R.id.btnVerDesafios);
        // Botón para ir a  registro de nuevos hábitos
        btnRegistrarHabito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
                startActivity(intent);
            }
        });

        // Botón para ir a historial de hábitos
        btnVerHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistorialActivity.class);
                startActivity(intent);
            }
        });

        // Botón para ir a  desafíos
        btnVerDesafios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DesafiosActivity.class);
                startActivity(intent);
            }
        });

        // notificación
        programarAlarmaDiaria();
        // Solicita el permiso de notificaciones
        pedirPermisoNotificaciones();
    }
    private void pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                //  cuadro de diálogo para solicitar el permiso
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarga los datos del dashboard
        cargarDatosDashboard();
    }

    private void cargarDatosDashboard() {
        // Obtienemos la fecha
        String fechaHoy = getFechaDeHoy();
        // Consultamos con la BD para obtener el total de CO2 ahorrado
        double co2Hoy = dbHelper.obtenerCo2AhorradoHoy(fechaHoy);
        // se actualiza el TextView del dashboard con el valor formateado
        tvCo2Hoy.setText(String.format(Locale.getDefault(), "%.2f g", co2Hoy));
        // función para cargar y dibujar la gráfica.
        cargarDatosGrafica();
    }
    private void cargarDatosGrafica() {
        // Calcula la fecha de inicio para consultar los últimos 7 días
        String fechaInicioSemana = getFechaHaceXDias(7);
        // Obtiene los datos de  fecha y suma total de CO2 por día
        Cursor cursor = dbHelper.obtenerDatosGraficaSemanal(fechaInicioSemana);
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;
        while (cursor.moveToNext()) {
            // Obtiene los valores de CO2 y fecha
            float totalCo2 = cursor.getFloat(cursor.getColumnIndexOrThrow("total_co2"));
            String fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA));
            // se añade una entrada  a la gráfica, el índice 'i' es la posición en el eje X.
            entries.add(new BarEntry(i, totalCo2));
            // Formatea la fecha para usarla como etiqueta en el eje X
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
        // Configuración de la Gráfica
        BarDataSet dataSet = new BarDataSet(entries, "CO2 Ahorrado (g)");
        dataSet.setColor(Color.GREEN);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);

        chartSemanal.setData(barData);
        chartSemanal.getDescription().setEnabled(false); // Oculta la descripción
        chartSemanal.setFitBars(true);
        chartSemanal.animateY(1000);

        // etiquetas de las fechas
        XAxis xAxis = chartSemanal.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Usamos las fechas como etiquetas
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(-45);
        chartSemanal.invalidate(); // Redibuja la gráfica
    }

    /**
     * Genera la fecha actual en formato yyyy-MM-dd.
     */
    private String getFechaDeHoy() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(Calendar.getInstance().getTime());
    }
    private String getFechaHaceXDias(int diasAtras) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -diasAtras); // Resta los días
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(cal.getTime());
    }

    @Override
    protected void onDestroy() {
        // Cierra la conexión
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
        // Establece la hora de la alarma
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        // Si la hora programada ya pasó hoy, se programa para mañana
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        // Configuramos la alarma para que se repita todos los días
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, // Intervalo de repetición
                pendingIntent
        );
    }
}