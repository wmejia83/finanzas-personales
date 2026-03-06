package com.example.finanzaspersonales_cendi

import android.content.ContentValues
import android.content.Context

class MovimientosRepository (context: Context){

    private val dbHelper = MovimientosDbHelper(context)

    //REGISTRAR /INSERTAR DATOS
    fun insert(finanza: Finanza): Long{
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(MovimientosDbHelper.COL_TIPO, finanza.tipo)
            put(MovimientosDbHelper.COL_CONCEPTO, finanza.concepto)
            put(MovimientosDbHelper.COL_MONTO, finanza.monto)
            put(MovimientosDbHelper.COL_FECHA, finanza.fecha)
        }

        return db.insert(MovimientosDbHelper.TABLE_MOVIMIENTOS,null,values)
    }

    //OBTENER REGISTROS POR TIPO ingreso/gasto

    fun getByTipo(tipo: String): MutableList<Finanza>{
        val  db = dbHelper.readableDatabase

        val cursor = db.query(
            MovimientosDbHelper.TABLE_MOVIMIENTOS,
            null,
            "${MovimientosDbHelper.COL_TIPO} = ?",
            arrayOf(tipo),
            null,
            null,
            "${MovimientosDbHelper.COL_ID} DESC" // más recientes primero
        )

        val result = mutableListOf<Finanza>()
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MovimientosDbHelper.COL_ID))
                val concepto = it.getString(it.getColumnIndexOrThrow(MovimientosDbHelper.COL_CONCEPTO))
                val monto = it.getDouble(it.getColumnIndexOrThrow(MovimientosDbHelper.COL_MONTO))
                val fecha = it.getString(it.getColumnIndexOrThrow(MovimientosDbHelper.COL_FECHA))


                result.add(
                    Finanza(
                        id = id,
                        tipo = tipo,
                        concepto = concepto,
                        monto = monto,
                        fecha = fecha
                    )
                )
            }
        }
        return result

    }

    //DEVOLVER TOTALES
    fun getTotales(): Pair<Double, Double> {
        val ingresos = getByTipo("Ingreso").sumOf { it.monto }
        val gastos = getByTipo("Gasto").sumOf { it.monto }
        return Pair(ingresos, gastos)
    }



}