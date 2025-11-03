package com.example.cohelp;
import static android.provider.BaseColumns._ID;
import static com.example.cohelp.EstructuraDb.HabitosEntry.*;
import static com.example.cohelp.EstructuraDb.DesafiosEntry.*;
import com.example.cohelp.EstructuraDb.HabitosEntry;
import com.example.cohelp.EstructuraDb.DesafiosEntry;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
public class DbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "EcoTrack.sqlite";
    public static final int DATABASE_VERSION = 1;
    // Constructor
    public DbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        //  crea la tabla de habitos
        String queryCrearHabitos = "CREATE TABLE " + HabitosEntry.TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FECHA + " TEXT NOT NULL, " +
                COLUMN_TIPO_HABITO + " TEXT NOT NULL, " +
                COLUMN_CANTIDAD + " REAL NOT NULL, " +
                COLUMN_CO2_AHORRADO + " REAL NOT NULL)";
        db.execSQL(queryCrearHabitos); // Ejecuta la creación de la tabla
        //  crear la tabla de desafios
        String queryCrearDesafios = "CREATE TABLE " + DesafiosEntry.TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NOMBRE_DESAFIO + " TEXT NOT NULL, " +
                COLUMN_COMPLETADO + " INTEGER NOT NULL DEFAULT 0)"; // 0 = No completado, 1 = Completado
        db.execSQL(queryCrearDesafios); // ejecuta la creación de la tabla.
        // insertar los desafíos iniciales.
        prePoblarDesafios(db);
    }
    // insertar un conjunto inicial
    private void prePoblarDesafios(SQLiteDatabase db) {
        agregarDesafioInicial(db, "Evita el uso de plásticos por 3 días");
        agregarDesafioInicial(db, "Usa transporte público o bicicleta hoy");
        agregarDesafioInicial(db, "Separa todos tus residuos reciclables de la semana");
        agregarDesafioInicial(db, "Desconecta aparatos electrónicos que no estés usando");
    }

    // agregar un solo desafío a la tabla
    private void agregarDesafioInicial(SQLiteDatabase db, String nombreDesafio) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE_DESAFIO, nombreDesafio);
        values.put(COLUMN_COMPLETADO, 0); //no completdo por defecto
        db.insert(DesafiosEntry.TABLE_NAME, null, values); // se nserta el registro
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // aqui se elimina las tablas existentes y las vuelve a crear para empezar de cero
        db.execSQL("DROP TABLE IF EXISTS " + HabitosEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DesafiosEntry.TABLE_NAME);
        onCreate(db);
    }
    public long agregarHabito(String fecha, String tipo, double cantidad, double co2Ahorrado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FECHA, fecha);
        values.put(COLUMN_TIPO_HABITO, tipo);
        values.put(COLUMN_CANTIDAD, cantidad);
        values.put(COLUMN_CO2_AHORRADO, co2Ahorrado);
        // inserta la nueva fila.
        long id = db.insert(HabitosEntry.TABLE_NAME, null, values);
        db.close(); // se cierra la conexión
        return id;
    }
    public Cursor obtenerTodosHabitos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + HabitosEntry.TABLE_NAME + " ORDER BY " + COLUMN_FECHA + " DESC", null);
    }
    public double obtenerCo2AhorradoHoy(String fechaHoy) {
        SQLiteDatabase db = this.getReadableDatabase();
        // co2 ahorrado
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_CO2_AHORRADO + ") FROM " + HabitosEntry.TABLE_NAME +
                " WHERE " + COLUMN_FECHA + " = ?", new String[]{fechaHoy});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0); //  resultado de la suma
        }
        cursor.close();
        db.close();
        return total;
    }
    public Cursor obtenerDatosGraficaSemanal(String fechaInicioSemana) {
        SQLiteDatabase db = this.getReadableDatabase();
        //  agrupa por fecha y suma el CO2 ahorrado
        return db.rawQuery("SELECT " + _ID + ", " + COLUMN_FECHA + ", SUM(" + COLUMN_CO2_AHORRADO + ") as total_co2 " +
                "FROM " + HabitosEntry.TABLE_NAME + " " +
                "WHERE " + COLUMN_FECHA + " >= ? " + // Filtra por fecha
                "GROUP BY " + COLUMN_FECHA + " " + // Agrupa los resultados por fecha
                "ORDER BY " + COLUMN_FECHA + " ASC", new String[]{fechaInicioSemana}); // Ordena ascendente
    }
    public int eliminarHabito(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        //  devuelve el número de filas afectadas.
        int result = db.delete(HabitosEntry.TABLE_NAME, _ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }
    public Cursor obtenerDesafios() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Obtiene todos los registros de la tabla desafios
        return db.rawQuery("SELECT * FROM " + DesafiosEntry.TABLE_NAME, null);
    }
    public int actualizarDesafio(long id, boolean completado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // se convierte el booleano a 1 (completado) o 0 (no completado)
        values.put(COLUMN_COMPLETADO, completado ? 1 : 0);
        // Actualiza la fila con el ID
        int result = db.update(DesafiosEntry.TABLE_NAME, values, _ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }
}