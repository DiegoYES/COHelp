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

    private void cargarHistorial() {
        Cursor cursor = dbHelper.obtenerTodosHabitos();

        String[] from = {COLUMN_TIPO_HABITO, COLUMN_FECHA, COLUMN_CO2_AHORRADO};
        int[] to = {R.id.tvTipoHabito, R.id.tvFecha, R.id.tvCo2Ahorrado};

        adapter = new SimpleCursorAdapter(
                this,
                R.layout.list_item_historial,
                cursor,
                from,
                to,
                0
        );

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.tvCo2Ahorrado) {
                    double co2 = cursor.getDouble(columnIndex);
                    TextView tv = (TextView) view;
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
        if (adapter != null && adapter.getCursor() != null) {
            adapter.getCursor().close();
        }
        dbHelper.close();
        super.onDestroy();
    }
}