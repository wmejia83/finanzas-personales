package com.example.finanzaspersonales_cendi.model

data class Movimiento(
    val id: Long = 0,
    val tipo: String, //ingreso - egreso
    val concepto: String,
    val monto: Double,
    val fecha: String
)