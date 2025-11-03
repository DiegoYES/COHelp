package com.example.cohelp;
import android.provider.BaseColumns;
public class EstructuraDb {
    private EstructuraDb() {}
    public static abstract class HabitosEntry implements BaseColumns {
        public static final String TABLE_NAME = "habitos";
        // Columna para la fecha en formato 'YYYY-MM-DD'
        public static final String COLUMN_FECHA = "fecha";
        // Columna para el tipo de hábito: 'caminar', 'reciclar', 'electricidad'
        public static final String COLUMN_TIPO_HABITO = "tipo_habito";
        // Columna para la cantidad (ej. km, kg, o 1.0 para 'sí')
        public static final String COLUMN_CANTIDAD = "cantidad";
        // Columna para el CO2 ahorrado (calculado)
        public static final String COLUMN_CO2_AHORRADO = "co2_ahorrado";
    }

    //define el contenido de la tabla Desafios
    public static abstract class DesafiosEntry implements BaseColumns {
        public static final String TABLE_NAME = "desafios";
        // Columna para el texto del desafío
        public static final String COLUMN_NOMBRE_DESAFIO = "nombre_desafio";
        // Columna para el estado: 0 = no completado, 1 = completado
        public static final String COLUMN_COMPLETADO = "completado";
    }
}
