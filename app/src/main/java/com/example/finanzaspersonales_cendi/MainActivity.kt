// =====================================================
// [0] IMPORTS (Dependencias que usa este archivo)
// - Android UI (Button, TextView, etc.)
// - Compat (AppCompatActivity, Toolbar, etc.)
// - MPAndroidChart (PieChart, PieData, PieDataSet, PieEntry)
// =====================================================

package com.example.finanzaspersonales_cendi

import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.os.Bundle
import android.os.UserManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ToolbarWidgetWrapper
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlin.toString

// =====================================================
// [1] MODELO DE DATOS (Data Class)
// Representa un movimiento financiero simple.
// - concepto: qué fue
// - monto: cuánto fue
// - fecha: cuándo fue
//
// Nota didáctica: esta clase es la "estructura" que usarán
// ingresos y gastos para guardar info de forma ordenada.
// =====================================================
data class Finanza(
    val concepto: String,
    val monto: Double,
    val fecha: String
)



// =====================================================
// [2] ACTIVITY PRINCIPAL
// Aquí vive toda la lógica de pantalla:
// - Inicializa UI
// - Maneja menú
// - Muestra listas
// - Calcula totales
// - Pinta el gráfico
// =====================================================
class MainActivity : AppCompatActivity(), android.hardware.SensorEventListener {

    //declarar una variable para toda la clase
    private lateinit var sensorManager: android.hardware.SensorManager

    private var ultimaAlertaMs = 0L

    // Este objeto se encarga de abrir la cámara y traernos el resultado
    private val camaraLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            // La foto se tomó con éxito
            Toast.makeText(this, "Foto del recibo capturada", Toast.LENGTH_SHORT).show()
        }
    }




    // =====================================================
    // [3] DECLARACIÓN DE VARIABLES / DATOS (estado del Activity)
    //
    // 3.1 Listas de datos (simulan datos reales)
    // - listaIngresos: entradas positivas de dinero
    // - listaGastos: salidas de dinero
    //
    // Nota didáctica: aquí se define la "fuente de verdad" del UI:
    // los totales, el listado y el gráfico dependen de estas listas.
    // =====================================================
    private val listaIngresos = mutableListOf(
        Finanza("Venta de sitio web", 800000.00, "01 Feb 2026"),
        Finanza("Pago de suscripción", 50000.00, "02 Feb 2026"),
        Finanza("Salario", 5000000.00, "03 Feb 2026")
    )

    private val listaGastos = mutableListOf(
        Finanza("Compra de Café", 40000.00, "01 Feb 2026"),
        Finanza("Pago Internet", 190000.00, "02 Feb 2026"),
        Finanza("Servicios públicos", 500000.00, "02 Feb 2026"),

        )

    private var tipoActual = "Ingreso"

    // =====================================================
    // 3.2 Referencia a componente visual (se inicializa luego)
    // - lateinit: la variable existirá, pero se asigna en onCreate
    // =====================================================
    private lateinit var pieChart: PieChart




    // =====================================================
    // [4] CICLO DE VIDA: onCreate()
    // Este es el punto de arranque:
    // 1) Se monta la pantalla (layout)
    // 2) Se conectan componentes UI (findViewById)
    // 3) Se llama el flujo inicial: cálculos + mostrar lista
    // 4) Se configuran botones (listeners)
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
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // -----------------------------------------------------
        // [4.5] GRÁFICO: conectar el PieChart por ID
        // -----------------------------------------------------
        pieChart = findViewById(R.id.pieChart)


        // -----------------------------------------------------
        // [4.6] FLUJO INICIAL DE PANTALLA
        // - calcularTotales(): actualiza botones, balance y gráfico
        // - mostrarIngresos(): carga la lista inicial en pantalla
        // -----------------------------------------------------
        calcularTotales()

        // -----------------------------------------------------
        // [4.7] CONFIGURACIÓN DE BOTONES (Listeners)
        // Cada botón cambia lo que se muestra en el contenedor.
        // -----------------------------------------------------


        // Click en botón ingresos:
        // - Muestra lista de ingresos
        // - Muestra un mensaje rápido (Toast)
        findViewById<Button>(R.id.btn_ingresos).setOnClickListener {
            mostrarIngresos()
            Toast.makeText(this, "Mostrando Ingresos", Toast.LENGTH_SHORT).show()
        }

        // Click en botón gastos:
        // - Muestra lista de gastos
        // - Muestra un mensaje rápido (Toast)
        findViewById<Button>(R.id.btn_gastos).setOnClickListener {
            mostrarGastos()
            Toast.makeText(this, "Mostrando Gastos", Toast.LENGTH_SHORT).show()
        }

        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId){
                R.id.nav_ingresos -> {
                    tipoActual = "Ingreso"
                    mostrarIngresos()
                    true
                }

                R.id.nav_gastos -> {
                    tipoActual = "Gasto"
                    mostrarGastos()
                    true
                }

                else -> false

            }

        }


        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_add).setOnClickListener {
            configurarDialogRegistroAvanzado()
        }


        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MovimientosFragment())
                .commit()
        }



        //inicializar sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as android.hardware.SensorManager

        val acelerometro = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)

        if(acelerometro != null){
            sensorManager.registerListener(
                this,
                acelerometro,
                android.hardware.SensorManager.SENSOR_DELAY_NORMAL
            )
        }else{
            Toast.makeText(this, "Este dispositivo no tiene acelerómetro", Toast.LENGTH_LONG).show()
        }


    }//cierra OnCreate




    private fun intentarAbrirCamara() {
        // Verificamos si el usuario ya nos dio permiso antes
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            camaraLauncher.launch(null) // Abrir cámara directamente
        } else {
            // Si no tiene permiso, lo pedimos formalmente
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)
        }
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
            R.id.menu_limpiar -> limpiarTodo()
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
        val formato = java.text.NumberFormat.getInstance()
        return formato.format(this)
    }


    // =====================================================
    // [7] ACCIONES DEL MENÚ (Funciones ejecutadas desde Toolbar)
    // =====================================================

    // Muestra un AlertDialog con resumen de ingresos, gastos y balance.
    // Calcula totales con sumOf sobre cada lista.
    private fun mostrarResumen() {
        val ing = listaIngresos.sumOf { it.monto }  // total ingresos
        val gas = listaGastos.sumOf { it.monto } // total gastos
        val balance = ing - gas                  // diferencia

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

    // Limpia las listas y limpia el contenedor visual.
    // Luego recalcula totales para dejar el UI coherente.
    private fun limpiarTodo() {
        listaIngresos.clear(); listaGastos.clear()
        findViewById<LinearLayout>(R.id.container_item).removeAllViews()
        calcularTotales()
        Toast.makeText(this, "Listas limpiadas", Toast.LENGTH_SHORT).show()
    }

    // Confirmación para salir. finishAffinity() cierra la app completa.
    private fun confirmarSalida() {
        androidx.appcompat.app.AlertDialog.Builder(this)
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

    // =====================================================
    // [9] RENDER LISTA: Mostrar ingresos
    //
    // Flujo:
    // 1) obtener el contenedor (LinearLayout)
    // 2) limpiar lo anterior
    // 3) por cada ingreso:
    //    - inflar una vista item_financiero
    //    - llenar textos
    //    - personalizar color/icono
    //    - agregar al contenedor
    // =====================================================

    fun mostrarIngresos(){


        //Buscamos el fragmento que pusimos en el "hueco" (fragment_container)
        val  fragmentActual = supportFragmentManager.findFragmentById(R.id.fragment_container)

        //Extraer la vista del fragmento para poder buscar dentro
        val  vistaFragmento = fragmentActual?.view

        val container = vistaFragmento?.findViewById<LinearLayout>(R.id.container_item)

        if(container != null){
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
        }



    }//cierra mostrar ingresos

    // =====================================================
    // [10] RENDER LISTA: Mostrar gastos (similar a ingresos)
    // Misma lógica, pero cambia el color/icono.
    // =====================================================
    fun mostrarGastos(){

        //Buscamos el fragmento que pusimos en el "hueco" (fragment_container)
        val  fragmentActual = supportFragmentManager.findFragmentById(R.id.fragment_container)

        //Extraer la vista del fragmento para poder buscar dentro
        val  vistaFragmento = fragmentActual?.view

        val container = vistaFragmento?.findViewById<LinearLayout>(R.id.container_item)


        if( container != null){

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
        }

    }//cierra mostrar Gastos

    // =====================================================
    // [11] GRÁFICO: Actualizar PieChart
    //
    // Este método recibe los totales ya calculados y hace:
    // 1) convertir a PieEntry (Float + label)
    // 2) construir el DataSet (PieDataSet)
    // 3) asignar colores
    // 4) construir PieData y asignarlo al chart
    // 5) invalidate() para redibujar
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
        val etConcepto = view.findViewById<android.widget.EditText>(R.id.etConcepto)
        val etMonto = view.findViewById<android.widget.EditText>(R.id.etMonto)

        val btnCamera = view.findViewById<android.widget.ImageView>(R.id.btn_camara)
        btnCamera.setOnClickListener {
            intentarAbrirCamara()
        }




        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Nuevo $tipoActual")
            .setView(view)
            .setPositiveButton("Guardar",null)
            .setNegativeButton("Cancelar",null)
            .create()

        // Listener personalizado para validar antes de cerrar el diálogo
        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {

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
                val fechaActual = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                    .format(java.util.Date())

                val nuevo = Finanza(concepto, monto, fechaActual)

                // Guardar según el tipo actual seleccionado
                if (tipoActual == "Ingreso") {
                    listaIngresos.add(nuevo)
                    mostrarIngresos()
                } else {
                    listaGastos.add(nuevo)
                    mostrarGastos()
                }

                calcularTotales()
                dialog.dismiss() // Cerrar manualmente si todo es correcto
            }
        }
        dialog.show()

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type != android.hardware.Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val ahora = System.currentTimeMillis()

        if((x > 5 || x < -5) && (ahora - ultimaAlertaMs > 1500)){
            ultimaAlertaMs = ahora
            mostrarResumen()
        }

    }


}