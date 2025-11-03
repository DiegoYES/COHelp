package com.example.cohelp;
import static com.example.cohelp.EstructuraDb.HabitosEntry.*;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import java.util.Locale;

public class HistorialActivity extends AppCompatActivity {
        DbHelper dbHelper;
        // para mostrar la lista de hábitos
        ListView lvHistorial;
        SimpleCursorAdapter adapter;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_historial);
            dbHelper = new DbHelper(this);
            lvHistorial = findViewById(R.id.lvHistorial);
            cargarHistorial();
        }

       //obtener todos los hábitos de la BD

        private void cargarHistorial() {
            // Obtiene el Cursor con todos los hábitos, ordenados por fecha descendente.
            Cursor cursor = dbHelper.obtenerTodosHabitos();

            // Define el mapeo de columnas de la BD a vistas del layout
            // Nombres de las columnas de la BD que se van a mapear
            String[] from = {COLUMN_TIPO_HABITO, COLUMN_FECHA, COLUMN_CO2_AHORRADO};
            int[] to = {R.id.tvTipoHabito, R.id.tvFecha, R.id.tvCo2Ahorrado};
            adapter = new SimpleCursorAdapter(
                    this,
                    R.layout.list_item_historial, // Layout de cada ítem de la lista
                    cursor,
                    from,
                    to,
                    0
            );
            adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                //calculo de co2
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    if (view.getId() == R.id.tvCo2Ahorrado) {
                        // Obtiene el valor double de la columna
                        double co2 = cursor.getDouble(columnIndex);
                        TextView tv = (TextView) view;
                        // Formatea el número a dos decimales, añadiendo '+' y "g CO2"
                        tv.setText(String.format(Locale.getDefault(), "+%.2f g CO2", co2));
                        return true;
                    }
                    return false;
                }
            });
            lvHistorial.setAdapter(adapter);
        }
        @Override
        protected void onDestroy() {
            //  cerramos el cursor asociado al adaptador para liberar recursos
            if (adapter != null && adapter.getCursor() != null) {
                adapter.getCursor().close();
            }
            // se cierra la conexión a la base de datos
            dbHelper.close();
            super.onDestroy();
        }
    }