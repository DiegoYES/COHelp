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
        // para mostrar la lista de desafíos
        ListView lvDesafios;
        SimpleCursorAdapter adapter;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_desafios);
            dbHelper = new DbHelper(this);
            lvDesafios = findViewById(R.id.lvDesafios);
            // Carga los desafíos de la BD y los muestra en el ListView
            cargarDesafios();
            lvDesafios.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Cursor cursor = (Cursor) adapter.getItem(position);
                    // obtenemos el ID de la BD y el estado actual 0 o 1
                    long desafioId = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
                    int estadoActual = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETADO));
                    // 0 pasa a ser 1 (completado) y 1 pasa a ser 0.
                    boolean nuevoEstado = (estadoActual == 0);
                    dbHelper.actualizarDesafio(desafioId, nuevoEstado);
                    Cursor nuevoCursor = dbHelper.obtenerDesafios();
                    adapter.changeCursor(nuevoCursor);
                }
            });
        }

       // obtener los desafíos de la BD
        private void cargarDesafios() {
            Cursor cursor = dbHelper.obtenerDesafios();
            // nombres de las columnas de la BD que se van a mapear
            String[] from = {COLUMN_NOMBRE_DESAFIO, COLUMN_COMPLETADO};
            // ID de los componentes list_item_desafio que recibirán los datos
            int[] to = {R.id.tvNombreDesafio, R.id.cbCompletado};
            adapter = new SimpleCursorAdapter(
                    this,
                    R.layout.list_item_desafio, // Layout de cada ítem de la lista
                    cursor, // El Cursor con los datos.
                    from,
                    to,
                    0
            );
            adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    if (view.getId() == R.id.cbCompletado) {
                        CheckBox cb = (CheckBox) view;
                        // Obtiene el valor de la columna COLUMN_COMPLETADO
                        int completado = cursor.getInt(columnIndex);
                        // Establece el estado del CheckBox true si es 1, false si es 0
                        cb.setChecked(completado == 1);
                        return true; // Retorna true para indicar que el mapeo fue manejado manualmente
                    }
                    return false; // Retorna false para que el adaptador haga el mapeo por defecto
                }
            });
            lvDesafios.setAdapter(adapter);
        }


        @Override
        protected void onDestroy() {
            if (adapter != null && adapter.getCursor() != null) {
                adapter.getCursor().close(); // Cierra el Cursor del adaptador
            }
            dbHelper.close(); // Cierra la conexión a la base de datos
            super.onDestroy();
        }
    }