// =====================================================
// [0] IMPORTS (Dependencias que usa este archivo)
// - Android UI (Button, TextView, etc.)
// - Compat (AppCompatActivity, Toolbar, etc.)
// - MPAndroidChart (PieChart, PieData, PieDataSet, PieEntry)
// =====================================================

package com.example.finanzaspersonales_cendi.ui.activities

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.finanzaspersonales_cendi.ui.fragments.MovimientosFragment
import com.example.finanzaspersonales_cendi.data.repository.MovimientosRepository
import com.example.finanzaspersonales_cendi.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.finanzaspersonales_cendi.model.Movimiento

// =====================================================
// [2] ACTIVITY PRINCIPAL
// Aquí vive toda la lógica de pantalla:
// - Inicializa UI
// - Maneja menú
// - Muestra listas
// - Calcula totales
// - Pinta el gráfico
// =====================================================
class MainActivity : AppCompatActivity(){

    // =====================================================
    // [3] DECLARACIÓN DE VARIABLES
    // =====================================================
    private lateinit var repo: MovimientosRepository
    private lateinit var prefs: SharedPreferences

    private var ultimaAlertaMs = 0L
    private var tipoActual = "Ingreso"
    private lateinit var pieChart: PieChart

    // =====================================================
    // [4] CICLO DE VIDA: onCreate()
    // =====================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // -----------------------------------------------------
        // [4.1] Ajuste de pantalla Edge-to-Edge (opcional)
        // Permite que la app use toda la pantalla y gestione insets.
        // -----------------------------------------------------
        enableEdgeToEdge()

        // -----------------------------------------------------
        // [4.2] Cargar el layout principal de esta Activity
        // -----------------------------------------------------
        setContentView(R.layout.activity_main)

        repo = MovimientosRepository(this)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // -----------------------------------------------------
        // [4.3] Manejo de insets (barras del sistema)
        // Ajusta padding para que contenido no quede debajo del status bar.
        // -----------------------------------------------------
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // -----------------------------------------------------
        // [4.4] Toolbar (barra superior)
        // 1) Se busca por ID
        // 2) Se define como ActionBar
        // -----------------------------------------------------
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        pieChart = findViewById(R.id.pieChart)

        //Recuperando los valores de la cajita
        if(savedInstanceState != null){
            //1-recuperar tipo actual
            tipoActual = savedInstanceState.getString(KEY_TIPO_ACTUAL,"ingreso") ?: "ingreso"

        }else{
            //Inicio normal: SharePreferences manda
            loadFromPrefs()
        }

        calcularTotales()

        if(tipoActual === "Ingreso"){
            mostrarMovimientos("Ingreso")
        } else{
            mostrarMovimientos("Gasto")
        }

        // -----------------------------------------------------
        // [4.7] CONFIGURACIÓN DE BOTONES (Listeners)
        // Cada botón cambia lo que se muestra en el contenedor.
        // -----------------------------------------------------

        findViewById<Button>(R.id.btn_ingresos).setOnClickListener {
            tipoActual = "Ingreso"
            mostrarMovimientos("Ingreso")
            saveToPrefs()
            Toast.makeText(this, "Mostrando Ingresos", Toast.LENGTH_SHORT).show()
        }

        // Click en botón gastos:
        // - Muestra lista de gastos
        // - Muestra un mensaje rápido (Toast)
        findViewById<Button>(R.id.btn_gastos).setOnClickListener {
            tipoActual = "Gasto"
            mostrarMovimientos("Gasto")
            saveToPrefs()
            Toast.makeText(this, "Mostrando Gastos", Toast.LENGTH_SHORT).show()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        //marcar el nav seleccionado
        bottomNav.selectedItemId = if(tipoActual == "Ingreso"){
            R.id.nav_ingresos
        }else{
            R.id.nav_gastos
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId){
                R.id.nav_ingresos -> {
                    tipoActual = "Ingreso"
                    mostrarMovimientos("Ingreso")
                    saveToPrefs()
                    true
                }

                R.id.nav_gastos -> {
                    tipoActual = "Gasto"
                    mostrarMovimientos("Gasto")
                    saveToPrefs()
                    true
                }

                else -> false

            }

        }

        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            configurarDialogRegistroAvanzado()
        }

        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MovimientosFragment())
                .commit()
        }

        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MovimientosFragment())
                .commitNow()
        }

    }//cierra OnCreate


    override fun onPause() {
        super.onPause()
        saveToPrefs()
    }

    //nuestro maletin-cajita
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        //Guardar qué vista estaba viendo el usuario
        outState.putString(KEY_TIPO_ACTUAL,tipoActual)

        //opcional guardar el texto del balance
        val balanceText = findViewById<TextView>(R.id.txt_total_balance).text.toString()

        //opcional (recomendado) guardar listas

    }


    // =====================================================
    // [5] MENÚ EN TOOLBAR
    //
    // onCreateOptionsMenu: "infla" (carga) el menú XML en la toolbar
    // onOptionsItemSelected: decide qué hacer cuando se toca una opción
    // =====================================================

    //Inflar el menú en la toolbar - mostrar los tres punticos
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu);
        return true
    }

    //acciones del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_resumen -> mostrarResumen()
            //R.id.menu_limpiar -> limpiarTodo()
            R.id.menu_salir -> confirmarSalida()
            else -> Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
        }

        return true
    }

    // =====================================================
    // [6] HELPERS / UTILIDADES (pequeñas funciones de apoyo)
    // Aquí agregas cosas reutilizables para no repetir lógica.
    // =====================================================
    // Esta es una función de extensión para Double
    // Permite llamar: 1234567.0.formatearMiles()
    // para mostrar "1,234,567" dependiendo de la configuración regional.
    fun Double.formatearMiles(): String {
        val formato = NumberFormat.getInstance()
        return formato.format(this)
    }

    // =====================================================
    // [7] ACCIONES DEL MENÚ (Funciones ejecutadas desde Toolbar)
    // =====================================================
    private fun mostrarResumen() {
        val (totalIngresos, totalGastos) = repo.getTotales()

        val balance = totalIngresos - totalGastos                  // diferencia

        AlertDialog.Builder(this)
            .setTitle("Resumen")
            // ¡Aquí usamos nuestra extensión .formatearMiles()!
            .setMessage(
                "Ingresos: $ ${totalIngresos.formatearMiles()}\n" +
                        "Gastos: $ ${totalGastos.formatearMiles()}\n" +
                        "Balance: $ ${balance.formatearMiles()}"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    // Confirmación para salir. finishAffinity() cierra la app completa.
    private fun confirmarSalida() {
        AlertDialog.Builder(this)
            .setTitle("Salir")
            .setMessage("¿Deseas salir de la aplicación?")
            .setPositiveButton("Sí") { _, _ -> finishAffinity() }
            .setNegativeButton("No", null)
            .show()
    }

    // =====================================================
    // [8] LÓGICA PRINCIPAL: Totales + UI + Gráfico
    //
    // Esta función es clave:
    // 1) calcula totales
    // 2) actualiza botones
    // 3) actualiza balance (color + alerta)
    // 4) actualiza el gráfico con esos totales
    // =====================================================
    private fun calcularTotales(){

        val (totalIngresos, totalGastos) = repo.getTotales()
        val balance = totalIngresos - totalGastos

        //actualizar el texto de los botones con el valor
        findViewById<Button>(R.id.btn_gastos).text = "Gastos \n $ ${totalGastos.formatearMiles()}"
        findViewById<Button>(R.id.btn_ingresos).text = "Ingreos \n $ ${totalIngresos.formatearMiles()}"

        //actualizamos el balance en la vista
        val textBalance = findViewById<TextView>(R.id.txt_total_balance)
        textBalance.text = "$ ${balance.formatearMiles()}"

        if(balance >= 0){
            textBalance.setTextColor(Color.parseColor("#4CAF50"))
        }else{
            textBalance.setTextColor(Color.parseColor("#F44336"))
            //Mostrar Alerta
            Toast.makeText(this,"Atención: Tienes saldo negativo", Toast.LENGTH_SHORT).show()
        }

        actualizarPieChart(totalIngresos, totalGastos)

    }//cierra calcularTotales

    private fun mostrarMovimientos(tipo: String){
        val  fragmentActual = supportFragmentManager.findFragmentById(R.id.fragment_container)
        val  vistaFragmento = fragmentActual?.view
        val container = vistaFragmento?.findViewById<LinearLayout>(R.id.container_item)
        if(container != null){

            val movimientos = repo.getByTipo(tipo) //

            container.removeAllViews()//limpiar pantalla

            movimientos.forEach {  movimiento ->
                val itemView = layoutInflater.inflate(R.layout.item_financiero,container,false)

                //llenar los datos
                itemView.findViewById<TextView>(R.id.txt_concepto).text = movimiento.concepto
                itemView.findViewById<TextView>(R.id.txt_fecha).text = movimiento.fecha

                //para personalizar color lo agrego a una variable
                val txtMonto = itemView.findViewById<TextView>(R.id.txt_monto)

                val icon = itemView.findViewById<ImageView>(R.id.icon_type)

                if(tipo == "Ingreso"){
                    txtMonto.text = "+$ ${movimiento.monto.formatearMiles()}"
                    txtMonto.setTextColor(Color.parseColor("#4CAF50"))
                    icon.setBackgroundResource(R.drawable.shape_circle_green)
                    icon.setImageResource(android.R.drawable.ic_input_add)
                }else{
                    txtMonto.text = "-$ ${movimiento.monto.formatearMiles()}"
                    txtMonto.setTextColor(Color.parseColor("#F44336"))
                    icon.setBackgroundResource(R.drawable.shape_circle_red)
                    icon.setImageResource(android.R.drawable.ic_delete)
                }

                container.addView(itemView)

            }//cierra foreach
        }

    }

    // =====================================================
    // [11] GRÁFICO: Actualizar PieChart
    // =====================================================
    //*******Pintar el gráfico
    private fun actualizarPieChart(totalIngresos: Double, totalGastos: Double) {
        val entries = listOf(PieEntry(totalIngresos.toFloat(), "Ingresos"), PieEntry(totalGastos.toFloat(), "Gastos"))
        val dataSet = PieDataSet(entries, "")

        dataSet.colors = listOf(
            getColor(R.color.colorIngreso),
            getColor(R.color.colorGasto)
        )

        pieChart.data = PieData(dataSet)
        //pieChart.animateX(1000)
        //pieChart.animateY(1000)
        pieChart.animateXY(1000,1000)

        pieChart.invalidate()
    }

    private fun configurarDialogRegistroAvanzado(){
        val view = layoutInflater.inflate(R.layout.dialog_registro, null)
        val etConcepto = view.findViewById<EditText>(R.id.etConcepto)
        val etMonto = view.findViewById<EditText>(R.id.etMonto)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Nuevo $tipoActual")
            .setView(view)
            .setPositiveButton("Guardar",null)
            .setNegativeButton("Cancelar",null)
            .create()

        // Listener personalizado para validar antes de cerrar el diálogo
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                //limpiamos-El "Reinicio" del estado
                etConcepto.error = null
                etMonto.error = null

                //Obtiene el texto: -obtenemos los valores y limpiamos
                val concepto = etConcepto.text.toString().trim()
                val monto = etMonto.text.toString().trim().toDoubleOrNull()

                // Validaciones de entrada
                if (concepto.isEmpty()) {
                    etConcepto.error = "Requerido"  //muestra la alerta
                    etConcepto.requestFocus()   //enfoca el edith text
                    return@setOnClickListener
                }

                if (monto == null || monto <= 0.0) {
                    etMonto.error = "Monto debe ser > 0"
                    etMonto.requestFocus()
                    return@setOnClickListener
                }

                // Generación de fecha automática
                val fechaActual = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(Date())

                val nuevoMovimiento = Movimiento(
                    tipo = tipoActual,
                    concepto = concepto,
                    monto = monto,
                    fecha = fechaActual
                )

                // Guardar según el tipo actual seleccionado
                repo.insert(nuevoMovimiento)

                refrescarListaActual()

                calcularTotales()

                dialog.dismiss() // Cerrar manualmente si todo es correcto
            }
        }
        dialog.show()

    }

    fun refrescarListaActual(){
        if(tipoActual == "Ingreso") mostrarMovimientos("Ingreso") else mostrarMovimientos("Gasto")
    }

    private fun saveToPrefs() {
        val balanceText = findViewById<TextView>(R.id.txt_total_balance).text.toString()

        prefs.edit()
            .putString(PREF_TIPO_ACTUAL, tipoActual)
            .apply()
    }
    private fun loadFromPrefs() {
        tipoActual = prefs.getString(PREF_TIPO_ACTUAL, "Ingreso") ?: "Ingreso"
    }

    private companion object {

        //Bundle keys(rotación)
        const val  KEY_TIPO_ACTUAL = "KEY_TIPO_ACTUAL"

        //SharedPreferences
        const val  PREFS_NAME = "finanzas_prefs"
        const val  PREF_TIPO_ACTUAL = "pref_tipo_actual"
    }

}//cierra clase