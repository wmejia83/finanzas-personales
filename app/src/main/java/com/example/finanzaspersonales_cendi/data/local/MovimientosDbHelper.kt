package com.example.finanzaspersonales_cendi.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MovimientosDbHelper(context: Context): SQLiteOpenHelper
    (
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION
    )


{
    override fun onCreate(db: SQLiteDatabase) {
        // Se ejecuta SOLO la primera vez que la DB se crea
        db.execSQL(SQL_CREATE_TABLE_MOVIMIENTOS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Para el curso: estrategia simple (borrar y crear)
        // En producción: migraciones cuidadosas
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MOVIMIENTOS")
        onCreate(db)
    }
    companion object {
        const val DATABASE_NAME = "finanzas_personales.db"
        const val DATABASE_VERSION = 1


        const val TABLE_MOVIMIENTOS = "movimientos"
        const val COL_ID = "id"
        const val COL_TIPO = "tipo"
        const val COL_CONCEPTO = "concepto"
        const val COL_MONTO = "monto"
        const val COL_FECHA = "fecha"


        private const val SQL_CREATE_TABLE_MOVIMIENTOS = """
          CREATE TABLE $TABLE_MOVIMIENTOS (
              $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
              $COL_TIPO TEXT NOT NULL,
              $COL_CONCEPTO TEXT NOT NULL,
              $COL_MONTO REAL NOT NULL,
              $COL_FECHA TEXT NOT NULL
          )
      """
    }

}