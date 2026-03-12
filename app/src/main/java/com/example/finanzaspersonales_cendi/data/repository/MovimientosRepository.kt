package com.example.finanzaspersonales_cendi.data.repository

import android.content.ContentValues
import android.content.Context
import com.example.finanzaspersonales_cendi.data.local.MovimientosDbHelper
import com.example.finanzaspersonales_cendi.model.Movimiento


class MovimientosRepository (context: Context){

    private val dbHelper = MovimientosDbHelper(context)

    //REGISTRAR /INSERTAR DATOS
    fun insert(movimiento: Movimiento): Long{
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(MovimientosDbHelper.Companion.COL_TIPO, movimiento.tipo)
            put(MovimientosDbHelper.Companion.COL_CONCEPTO, movimiento.concepto)
            put(MovimientosDbHelper.Companion.COL_MONTO, movimiento.monto)
            put(MovimientosDbHelper.Companion.COL_FECHA, movimiento.fecha)
        }

        return db.insert(MovimientosDbHelper.Companion.TABLE_MOVIMIENTOS,null,values)
    }

    //OBTENER REGISTROS POR TIPO ingreso/gasto

    fun getByTipo(tipo: String): MutableList<Movimiento>{
        val  db = dbHelper.readableDatabase

        val cursor = db.query(
            MovimientosDbHelper.Companion.TABLE_MOVIMIENTOS,
            null,
            "${MovimientosDbHelper.Companion.COL_TIPO} = ?",
            arrayOf(tipo),
            null,
            null,
            "${MovimientosDbHelper.Companion.COL_ID} DESC" // más recientes primero
        )

        val result = mutableListOf<Movimiento>()
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MovimientosDbHelper.Companion.COL_ID))
                val concepto = it.getString(it.getColumnIndexOrThrow(MovimientosDbHelper.Companion.COL_CONCEPTO))
                val monto = it.getDouble(it.getColumnIndexOrThrow(MovimientosDbHelper.Companion.COL_MONTO))
                val fecha = it.getString(it.getColumnIndexOrThrow(MovimientosDbHelper.Companion.COL_FECHA))


                result.add(
                    Movimiento(
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