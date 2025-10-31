package com.example.cohelp;

import static com.example.cohelp.EstructuraDb.DesafiosEntry.*;
import static android.provider.BaseColumns._ID;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class DesafiosActivity extends AppCompatActivity {

    DbHelper dbHelper;
    ListView lvDesafios;
    SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desafios);

        dbHelper = new DbHelper(this);
        lvDesafios = findViewById(R.id.lvDesafios);

        cargarDesafios();

        lvDesafios.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtenemos el cursor de la posición clickeada
                Cursor cursor = (Cursor) adapter.getItem(position);

                // Obtenemos el ID y el estado actual
                long desafioId = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
                int estadoActual = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETADO));

                // Invertimos el estado
                boolean nuevoEstado = (estadoActual == 0);

                // Actualizamos en la BD
                dbHelper.actualizarDesafio(desafioId, nuevoEstado);

                // Refrescamos la lista para mostrar el cambio
                Cursor nuevoCursor = dbHelper.obtenerDesafios();
                adapter.changeCursor(nuevoCursor);
            }
        });
    }

    private void cargarDesafios() {
        Cursor cursor = dbHelper.obtenerDesafios();

        String[] from = {COLUMN_NOMBRE_DESAFIO, COLUMN_COMPLETADO};
        int[] to = {R.id.tvNombreDesafio, R.id.cbCompletado};

        adapter = new SimpleCursorAdapter(
                this,
                R.layout.list_item_desafio,
                cursor,
                from,
                to,
                0
        );

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.cbCompletado) {
                    CheckBox cb = (CheckBox) view;
                    int completado = cursor.getInt(columnIndex);
                    cb.setChecked(completado == 1);
                    return true;
                }
                return false;
            }
        });

        lvDesafios.setAdapter(adapter);
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