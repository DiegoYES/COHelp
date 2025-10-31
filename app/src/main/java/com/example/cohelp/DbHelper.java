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

    public DbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String queryCrearHabitos = "CREATE TABLE " + HabitosEntry.TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FECHA + " TEXT NOT NULL, " +
                COLUMN_TIPO_HABITO + " TEXT NOT NULL, " +
                COLUMN_CANTIDAD + " REAL NOT NULL, " +
                COLUMN_CO2_AHORRADO + " REAL NOT NULL)";
        db.execSQL(queryCrearHabitos);

        String queryCrearDesafios = "CREATE TABLE " + DesafiosEntry.TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NOMBRE_DESAFIO + " TEXT NOT NULL, " +
                COLUMN_COMPLETADO + " INTEGER NOT NULL DEFAULT 0)";
        db.execSQL(queryCrearDesafios);

        prePoblarDesafios(db);
    }

    private void prePoblarDesafios(SQLiteDatabase db) {
        agregarDesafioInicial(db, "Evita el uso de plásticos por 3 días");
        agregarDesafioInicial(db, "Usa transporte público o bicicleta hoy");
        agregarDesafioInicial(db, "Separa todos tus residuos reciclables de la semana");
        agregarDesafioInicial(db, "Desconecta aparatos electrónicos que no estés usando");
    }

    private void agregarDesafioInicial(SQLiteDatabase db, String nombreDesafio) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE_DESAFIO, nombreDesafio);
        values.put(COLUMN_COMPLETADO, 0);
        db.insert(DesafiosEntry.TABLE_NAME, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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

        long id = db.insert(HabitosEntry.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public Cursor obtenerTodosHabitos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + HabitosEntry.TABLE_NAME + " ORDER BY " + COLUMN_FECHA + " DESC", null);
    }

    public double obtenerCo2AhorradoHoy(String fechaHoy) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_CO2_AHORRADO + ") FROM " + HabitosEntry.TABLE_NAME +
                " WHERE " + COLUMN_FECHA + " = ?", new String[]{fechaHoy});

        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    public Cursor obtenerDatosGraficaSemanal(String fechaInicioSemana) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " + _ID + ", " + COLUMN_FECHA + ", SUM(" + COLUMN_CO2_AHORRADO + ") as total_co2 " +
                "FROM " + HabitosEntry.TABLE_NAME + " " +
                "WHERE " + COLUMN_FECHA + " >= ? " +
                "GROUP BY " + COLUMN_FECHA + " " +
                "ORDER BY " + COLUMN_FECHA + " ASC", new String[]{fechaInicioSemana});
    }

    public int eliminarHabito(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(HabitosEntry.TABLE_NAME, _ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }

    public Cursor obtenerDesafios() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DesafiosEntry.TABLE_NAME, null);
    }

    public int actualizarDesafio(long id, boolean completado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPLETADO, completado ? 1 : 0);

        int result = db.update(DesafiosEntry.TABLE_NAME, values, _ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }
}
