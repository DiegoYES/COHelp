package com.example.cohelp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegistroActivity extends AppCompatActivity {

    // Factores de Emisión (¡Explica esto en tu documento!)
    private static final double CO2_POR_KM_AUTO = 200.0; // g de CO2 por km en auto
    private static final double CO2_POR_KG_RECICLADO = 1500.0; // g de CO2 ahorrados por kg reciclado
    private static final double CO2_POR_KWH_AHORRADO = 500.0; // g de CO2 por kWh de electricidad

    Spinner spinnerHabitos;
    EditText etCantidad;
    Button btnGuardarHabito;
    TextView tvUnidad;

    DbHelper dbHelper;
    String habitoSeleccionado = "";
    String[] tiposDeHabito = {"Caminar/Bici (en vez de auto)", "Reciclar plástico/papel", "Reducir electricidad"};
    String[] unidades = {"km recorridos", "kg reciclados", "kWh ahorrados"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        dbHelper = new DbHelper(this);

        spinnerHabitos = findViewById(R.id.spinnerHabitos);
        etCantidad = findViewById(R.id.etCantidad);
        btnGuardarHabito = findViewById(R.id.btnGuardarHabito);
        tvUnidad = findViewById(R.id.tvUnidad);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, tiposDeHabito);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHabitos.setAdapter(adapter);

        spinnerHabitos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                habitoSeleccionado = tiposDeHabito[position];
                tvUnidad.setText("Cantidad (" + unidades[position] + "):");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnGuardarHabito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarHabito();
            }
        });
    }

    private void guardarHabito() {
        String cantidadStr = etCantidad.getText().toString();
        if (cantidadStr.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa una cantidad", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double cantidad = Double.parseDouble(cantidadStr);
            double co2Ahorrado = 0.0;

            switch (habitoSeleccionado) {
                case "Caminar/Bici (en vez de auto)":
                    co2Ahorrado = cantidad * CO2_POR_KM_AUTO;
                    break;
                case "Reciclar plástico/papel":
                    co2Ahorrado = cantidad * CO2_POR_KG_RECICLADO;
                    break;
                case "Reducir electricidad":
                    co2Ahorrado = cantidad * CO2_POR_KWH_AHORRADO;
                    break;
            }

            String fechaHoy = getFechaDeHoy();

            long id = dbHelper.agregarHabito(fechaHoy, habitoSeleccionado, cantidad, co2Ahorrado);

            if (id > 0) {
                Toast.makeText(this, "¡Hábito guardado! CO2 ahorrado: " + co2Ahorrado + "g", Toast.LENGTH_LONG).show();
                finish(); // Cierra esta actividad y regresa al MainActivity
            } else {
                Toast.makeText(this, "Error al guardar el hábito", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFechaDeHoy() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}