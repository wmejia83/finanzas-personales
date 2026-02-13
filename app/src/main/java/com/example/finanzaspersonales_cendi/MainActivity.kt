package com.example.finanzaspersonales_cendi

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ToolbarWidgetWrapper
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

data class Finanza(
    val concepto: String,
    val monto: Double,
    val fecha: String
)

class MainActivity : AppCompatActivity() {

    private val listaIngresos = mutableListOf(
        Finanza("Venta de sitio web", 800000.00, "01 Feb 2026"),
        Finanza("Pago de suscripción", 50000.00, "02 Feb 2026"),
        Finanza("Salario", 5000000.00, "03 Feb 2026")
    )

    private val listaGastos = mutableListOf(
        Finanza("Compra de Café", 40000.00, "01 Feb 2026"),
        Finanza("Pago Internet", 190000.00, "02 Feb 2026"),
        Finanza("Servicios públicos", 500000.00, "02 Feb 2026"),
        Finanza("Negociación", 6000000.00, "02 Feb 2026"),
    )


    private lateinit var pieChart: PieChart


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        //*****GRÁFICO******
        pieChart = findViewById(R.id.pieChart)

        calcularTotales()
        mostrarIngresos()

        //Configurar Botones
        findViewById<Button>(R.id.btn_ingresos).setOnClickListener {
            mostrarIngresos()
            Toast.makeText(this, "Mostrando Ingresos", Toast.LENGTH_SHORT).show()
        }

        //Click en boton gastos
        findViewById<Button>(R.id.btn_gastos).setOnClickListener {
            mostrarGastos()
            Toast.makeText(this, "Mostrando Gastos", Toast.LENGTH_SHORT).show()
        }


    }//cierra OnCreate



    //Inflar el menú en la toolbar - mostrar los tres punticos
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu);
        return true
    }

    //acciones del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_resumen -> mostrarResumen()
            R.id.menu_limpiar -> limpiarTodo()
            R.id.menu_salir -> confirmarSalida()
            else -> Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
        }

        return true
    }

    // Esta es una función de extensión para Double
    fun Double.formatearMiles(): String {
        val formato = java.text.NumberFormat.getInstance()
        return formato.format(this)
    }

    private fun mostrarResumen() {
        val ing = listaIngresos.sumOf { it.monto } // Supongamos que monto es Double
        val gas = listaGastos.sumOf { it.monto }
        val balance = ing - gas

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Resumen")
            // ¡Aquí usamos nuestra extensión .formatearMiles()!
            .setMessage(
                "Ingresos: $ ${ing.formatearMiles()}\n" +
                        "Gastos: $ ${gas.formatearMiles()}\n" +
                        "Balance: $ ${balance.formatearMiles()}"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun limpiarTodo() {
        listaIngresos.clear(); listaGastos.clear()
        findViewById<LinearLayout>(R.id.container_item).removeAllViews()
        calcularTotales()
        Toast.makeText(this, "Listas limpiadas", Toast.LENGTH_SHORT).show()
    }

    private fun confirmarSalida() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Salir")
            .setMessage("¿Deseas salir de la aplicación?")
            .setPositiveButton("Sí") { _, _ -> finishAffinity() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun calcularTotales(){
        //sumar cada lista por separado
        val totalIngresos = listaIngresos.sumOf {  it.monto}
        val totalGastos = listaGastos.sumOf { it.monto }
        val balance = totalIngresos - totalGastos

        //actualizar el texto de los botones con el valor
        findViewById<Button>(R.id.btn_gastos).text = "Gastos \n $ ${totalGastos}"
        findViewById<Button>(R.id.btn_ingresos).text = "Ingreos \n $ ${totalIngresos}"

        //actualizamos el balance en la vista
        val textBalance = findViewById<TextView>(R.id.txt_total_balance)
        textBalance.text = "$ ${balance}"

        if(balance >= 0){
            textBalance.setTextColor(Color.parseColor("#4CAF50"))
        }else{
            textBalance.setTextColor(Color.parseColor("#F44336"))
            //Mostrar Alerta
            Toast.makeText(this,"Atención: Tienes saldo negativo", Toast.LENGTH_SHORT).show()
        }

        actualizarPieChart(totalIngresos, totalGastos)

    }//cierra calcularTotales

    private fun mostrarIngresos(){

        val container = findViewById<LinearLayout>(R.id.container_item)
        container.removeAllViews()//limpiar pantalla

        listaIngresos.forEach {  ingreso ->
            val itemView = layoutInflater.inflate(R.layout.item_financiero,container,false)

            //llenar los datos
            itemView.findViewById<TextView>(R.id.txt_concepto).text = ingreso.concepto
            itemView.findViewById<TextView>(R.id.txt_fecha).text = ingreso.fecha

            //para personalizar color lo agrego a una variable
            val txtMonto = itemView.findViewById<TextView>(R.id.txt_monto)
            txtMonto.text = "+$ ${ingreso.monto}"
            txtMonto.setTextColor(Color.parseColor("#4CAF50"))

            val icon = itemView.findViewById<ImageView>(R.id.icon_type)
            icon.setBackgroundResource(R.drawable.shape_circle_green)
            icon.setImageResource(android.R.drawable.ic_input_add)

            container.addView(itemView)

        }//cierra foreach

    }//cierra mostrar ingresos

    private fun mostrarGastos(){

        val container = findViewById<LinearLayout>(R.id.container_item)
        container.removeAllViews()//limpiar pantalla

        listaGastos.forEach {  gasto ->
            val itemView = layoutInflater.inflate(R.layout.item_financiero,container,false)

            //llenar los datos
            itemView.findViewById<TextView>(R.id.txt_concepto).text = gasto.concepto
            itemView.findViewById<TextView>(R.id.txt_fecha).text = gasto.fecha

            //para personalizar color lo agrego a una variable
            val txtMonto = itemView.findViewById<TextView>(R.id.txt_monto)
            txtMonto.text = "+$ ${gasto.monto}"
            txtMonto.setTextColor(Color.parseColor("#F44336"))

            val icon = itemView.findViewById<ImageView>(R.id.icon_type)
            icon.setBackgroundResource(R.drawable.shape_circle_red)
            icon.setImageResource(android.R.drawable.ic_delete)

            container.addView(itemView)

        }//cierra foreach

    }//cierra mostrar Gastos


    //*******Pintar el gráfico
    private fun actualizarPieChart(totalIngresos: Double, totalGastos: Double) {
        val entries = listOf(PieEntry(totalIngresos.toFloat(), "Ingresos"), PieEntry(totalGastos.toFloat(), "Gastos"))
        val dataSet = PieDataSet(entries, "")

        dataSet.colors = listOf(
            getColor(R.color.colorIngreso),
            getColor(R.color.colorGasto)
        )

        pieChart.data = PieData(dataSet)
        pieChart.invalidate()
    }
}