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

    // Factores de Emisión (
    private static final double CO2_POR_KM_AUTO = 200.0; // g de CO2 que se *evitan* por cada km que no se usa el auto
    private static final double CO2_POR_KG_RECICLADO = 1500.0; // g de CO2 *ahorrados* por cada kg de material reciclado
    private static final double CO2_POR_KWH_AHORRADO = 500.0; // g de CO2 *ahorrados* por cada kWh de electricidad no consumido
    // Componentes de la Interfaz
    Spinner spinnerHabitos;
    EditText etCantidad;
    Button btnGuardarHabito;
    TextView tvUnidad;
    // Variables
    DbHelper dbHelper;
    String habitoSeleccionado = "";
    // Lista de opciones para el Spinner
    String[] tiposDeHabito = {"Caminar/Bici (en vez de auto)", "Reciclar plástico/papel", "Reducir electricidad"};
    String[] unidades = {"km recorridos", "kg reciclados", "kWh ahorrados"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        dbHelper = new DbHelper(this);
        // Enlazamos los componentes
        spinnerHabitos = findViewById(R.id.spinnerHabitos);
        etCantidad = findViewById(R.id.etCantidad);
        btnGuardarHabito = findViewById(R.id.btnGuardarHabito);
        tvUnidad = findViewById(R.id.tvUnidad);

        //Configuración del Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, tiposDeHabito);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHabitos.setAdapter(adapter);
        // detectar cuándo el usuario selecciona un hábito
        spinnerHabitos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Guarda el hábito seleccionado
                habitoSeleccionado = tiposDeHabito[position];
                tvUnidad.setText("Cantidad (" + unidades[position] + "):");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //  botón de guardar
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
            // Conversión y Cálculo
            double cantidad = Double.parseDouble(cantidadStr);
            double co2Ahorrado = 0.0;
            // Determina el factor de emisión a usar según el hábito seleccionado
            switch (habitoSeleccionado) {
                case "Caminar/Bici (en vez de auto)":
                    // CO2 = Cantidad (km) * Factor (g/km)
                    co2Ahorrado = cantidad * CO2_POR_KM_AUTO;
                    break;
                case "Reciclar plástico/papel":
                    // CO2 = Cantidad (kg) * Factor (g/kg)
                    co2Ahorrado = cantidad * CO2_POR_KG_RECICLADO;
                    break;
                case "Reducir electricidad":
                    // CO2 = Cantidad (kWh) * Factor (g/kWh)
                    co2Ahorrado = cantidad * CO2_POR_KWH_AHORRADO;
                    break;
            }

            // Obtención de la fecha
            String fechaHoy = getFechaDeHoy();
            //  insertar el registro.
            long id = dbHelper.agregarHabito(fechaHoy, habitoSeleccionado, cantidad, co2Ahorrado);
            // mensaje
            if (id > 0) {
                Toast.makeText(this, "¡Hábito guardado! CO2 ahorrado: " + String.format(Locale.getDefault(), "%.2f", co2Ahorrado) + "g", Toast.LENGTH_LONG).show();
                finish(); //volver a la pantalla principal
            } else {
                Toast.makeText(this, "Error al guardar el hábito", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            // error
            Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
        }
    }
    //genera la fecha
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