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

        loadImage()
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

    private fun loadImage(url: String = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBxMSEhUSEhIVFRUVFRAVEhUSExUVFRUVFRUWFhUWFRYYHSggGBolHRUVITEhJSkrLi8uFx8zODMtNygtLisBCgoKDg0OGhAQGy0fHiUtLS0tKy0tLS0tLSstLS0tLS0tLS0rKy0tLS0tLS0tLSstLS0tLS0tLS0tLS0tLS0tLf/AABEIAKQBNAMBIgACEQEDEQH/xAAcAAABBQEBAQAAAAAAAAAAAAAAAgMEBQYHAQj/xABMEAACAQIDBAYFCAYGCQUAAAABAgMAEQQSIQUxQVEGEyJhcZEHMlKBoRRCYpKxwdHwFSNUgtPhM0NTcpOiFiQ0RGNzg8PSVZSywvH/xAAZAQADAQEBAAAAAAAAAAAAAAAAAQIDBAX/xAAmEQACAgICAgICAgMAAAAAAAAAAQIRAyESMQQTQVEioQWRMmHw/9oADAMBAAIRAxEAPwDBPVLtWW7W5VdyVm8U12PjSEhqiiimMKKKKACiiigAooooAKKdw2GeRssaM7WvlRSxtzsOGu+pZ2Fiv2ab/Db8KAK+nYWsb1JOxcR/YS/UNNzYCWMZnidRp2mRguu65tYUAbvY8YljDdx+0VNlwGh/e+6qXoPjxYxHfratmVv/AJvur0scuUUzxskOM2jI7UwhCn96snEP1w8TXUsXhAwP732Vgtq7OMWIB4Hd5VGVXTNcEqtBtfE2FvD7KzbmrbbT9oj87qqDXPndyOvxo1GzylCkilAVgdI4lJY616NBSKAHKKdwuEklOWKN5GAuRGjOQN1yFGg76lnYOKH+7Tf4bfhQBVmnBuqYdiYn9nl+o1In2fLGLvE6jddkYLfle1r91AiKBXj0um3NAxFFFFAHt6M1eV6KAPVJqVEKiZqlQtpQJnkz0h0zDN50mc1IwWg140xENaKkzYex03V5RQ7NFMNDWXnHaPjWoxDWBNZedrsTUoEIooopjCiiigAooooAKKKKANrsYrFhI7aGQPJJ9JusdFv3BVAA4XJ4mokxeQsEF8oLNqAABxJYgCoxxNsPBy6tx7xNLceOoPvHOtp0HXAwzD5bNgXikwUr5WlE2WYtHmEoZQElyEqEFyLOATc3Ca2Zjo50UxGNkkiSIdZ1EjxrMxi3FVz7rkAsuhFiWHC5GWwmJMTB1A3dpTudT6yOOKkaEV2zoPtPZWEkwMqzwROdlypi2Mu6bPhHyPc9l7jEG2/Twrh0MRayKLs1lUDeWbQDzNBRe7LcQYtlBOVZJUF99lYgX8q6fB2rEbj94rmOD2c+KxcoiIy9bK5kPqKhkNm0334Ab/MjtOwdmxLEosXIA7Tki+nsqbDw18a6cGSk0cXk4uUk0UuLFl/Ps1jekcmZozxuPsrq+Iw0dv6NfI8vGsltvDQ3B6mM25qeXjWzyaMFidnL9tev5fZVW1bvanVZv9nhP7l/vqnmdB/u0I8Yv51x5HcjvxaikZwUoVeNOn7PB/hfzpo4tR/UQf4X86g0sqXNAqbLjUJ7WHhI+iHjb3FWt5g+FImwylesiJKi2dWtnjJ0GYjRkJ0DgDXQgEi4M2+yWWHCRKumdBK/0ne5uedhZR3DvNQg/WzRxXt1kkcd7XtnYLe3HfuqLJirQQW3dRGPet0Ye5lIq022mzkgwjwSwGaSJWxKzy4t+rlAUkWwouhuW3keru4gIrZD2jgIngWQRGMyRTyxGzkgQxGYrIzOVkugF2VIwGkW2azKMzs2bJIpG5iFkXg6MQGRhxBHlod4FbPaG18EI7yjrZW/pIYGjCOM2f8AW4mIBypYBgGYtfV0J1GJw4zTIEW2aRcqgk2u4sLnUgczyoKG8WmR3S98rOtzxysRf4VHan8bIGkkYahpJGB5hmJHwNRzQMKKKKACiiigAp6JrCma9HKgBYFzTl+VLiivUqKC1BLYyl7UVMEYoqqJ5D+2HstZ2rzbsmlqo6hFoKKKKYwooooAKKKKAJuydmPiHyJYADM7tfKi3AubanUgADUk1YYvYkMfrYlv/br981SOhwLCSNSAZZ8DECdwL9eov3XIra7H2nGI4YkzASLiJI3bFSRWKKJrYjqkCxkx5mF8x0C3K2YAjnKnJG3VOs0VwZI5EKlSTkEgCubC+VcyPfVQRuqIcUn9hF9bEfxa0vTDL8tJW13wbGXJI0qMfk0hQiRgC90WEk29YHeRc+dFMAgiEzIJJJHZIgyh8oUqLqp0LliRfeMotvoAzPyxP7GH62I/jU4mPsD1aRxkggsmctYixAaR2y3BOq2PfWl2zteWJyjFkYfNYZW7tCKq8ekpdosTHZzHJJGxChx1auxBZfWBMboVbVWHAgggI1nRLDiLCRm1jLeVzz7TLGPAKAR/fbnWz6N4vUrflb41jdmSWw2H/wCRF9lTtnY3JIpvxAPnVRdMymrRvMU2n55ViukM1vz3VrpJLrfuFZ7ZWz1xWPigk1S5eQc1RS2XwJAB7ia6XpWcy3JIhejnZuIk2hh8QsMhhR5M8uUiMfqnXRjo2pAsL76v/TvsjETDCyQwSSrEMV1piQuUzmDKSq627Da24VqPSL0t/RWGjeOFXZ3EUak5Y0AQtc2G4BbBRb4VmOhPpeGIm6nGpFBmVikwcrHmAvlfOezcXsc28W4iuVuztSo4g0lNSPXSfTXhsC0kWLwc+Hd5SyYhIJY3JIF1lKoTY71J49muYtSGJtUnAThJFLeoezKOcbaSDyJtyIB4VGNeSDQ+B+ygZYLiHhLwtldVdwyuCVzqcpZSpDKTlG4i9he9hTXyyPjBD9fE/wAatbsTZ8ZlxM8iCRhisRHEjKGUMGzMxU6Me2gAINteNraHa+zMcrxRhLtK5ijEbxtlkAzmNspsjBbsQbaXPA0hHMvlacIIfrYn+NU3Y2GkxDMkKRQgL+tlAkOVG0y3dmN21GVbX1vpez+NkaSc4afKWztEHGUskoJVbSL66ZtCLkWJI1sak9FpsuGkIsM0yC500Edxc92ZvM0WMi4ro/El74lzYEm2GXcN5/pt1VOKwYAzxydYoIDXXI6X3FluRlPAgnkbEi+8b5WMFh3ws3yZnkxDyFsVHhHmVer6qYNI6mSBQWHK+Y2N71mdtzRSYrFtEVKGLVo1yxvIvUda6LwRpQ7AaaEUxGeoqTg4M1+6o7DWgZ5RRRQAUuEa0igGgCUCy07HiudRRiDXvWX4UE0WaSiioCSUU7FxFbVlzOe6odO4s9s+NNUiwooooAKKKKACvWW1exDUe6tBLhsNIgvIyuBrp/KolPiylGyr2ZiAA6Z+rLGJ45LkBZI8+XMRqoIdu0NxCnder39P49ACpw0Zswzq2Eu2Zg7WDuUS7AE9Wq3OpvWw6C9A4DGs7gTPJcxhxdVUEjRNzMSDvvwtrW5k2DEYmWTDxlNAP1CFLa3Pq2FjaoeWuilj+zge0Mc7s8+IlSSd06tQhjaylOrLMYuwLJmUDfc3I01ttgbSihgiM3WBSuKCvDl6yNme3WKG0JAzW5EgjUCpvT/ogcNnkVAqrlYFBlBVmVMroNFYFlIIAuM19bGshBiEMfVSZgAxZHUBipYKGBUkZlOVTvuCONyK0jJSVozlFrTOs4jAYh+qGGeOTCMJLT4d5IXUC/UxgI9rImVMzBwBG2ZezZsVtvaEDSNFG7TsoxDtiDlVC/yWRZjGouWEjBWZi1iykroxvnBFGBYYogG9wIpQDe17jvsPIcq86yOMNkYyOysmYpkVFYFXsCSWYqSuoAFzvNrUI2WDk/UQD/gRfZTbzWqLHNaKD/kQ/ZTcktBFHQujm0OsisTqPxqgxO3mwOPixCjNkY5lvbMjAqy352Jt3gVU7E2oY3tfQ3qzwfR/9K4swLiFhYRvIC0ZfMFZQVADDXtX38DXRyuBgo1kOw4DbWzdrw9WGinVrF4JQOsUjnG2oI1sw9xrNba9C2BlB+TvNhm1tZjNHfvWS7H3MK5b6QfR5NssRymTr42JBlSMoInFsobtG1+BuPVNWHom6XY/5fBhhNJPDI2WSORjIFTKSXUtcpltfQ2rnOsz/TLobidmSBJwrI9+qljvkcDeNdVYC11PuJGtZ019FendU/RZLWzCfD9VffnuQbfuZ/devnWgAokGh8D9lKAqZMJI4mRkyrMquCyC5Vb5SjEXA37t9TJ0VFWafD7SaFMSFy3bHYyzEXZSpgYMh4HSug9FdsPJCsmI6nDvjp5o8KcPGI5Hfqz8oxbF2IMrZeqVrABnGhDVynFTDrcTG9wjYid1KAFkbOwNgSMykWBFx6qm+li3HsyE/wBeNd/6l9fGockilFsuNudHY8JjMGsYlTrOokaDEFGmgPXZQrlNLEKGHG2+qTYcoCdWQp7UUqh79W3Vg9YkhG5WXjuGXWwJIsIcOkd3RjJJrlOTKqkgjPcm7MBu0FjY3NrVK6NbCSVnaUXjjC3W5XMzXygkaheyxNjfQDjUe1Waep0VfSfGYnHTnETmDMQqqqT4dUjRb5UjHWGyi5O/eTVVkESPdlLuoQKjq4VcysxZlutzkAABJ1JNrC/R9obEN8keCjLZc4RcJG7FPatkJsN2vEisrNgs/VmTDqkUzZY5o4kjs17XGQAMAd4YG4BtY6i1NGbgVmyouyTzqrxK2Y1pxhsgy8QLHuPEe41fdDuhqSSibEHQEFI+/gXP2Dz5UPIoq2JQbdIrOiPo+kxWV5maOM62Udu3AktovhYnwromzfRngU3w5++Vma/uvbyFbXAYRUUAAAdwqRiWCqSSAACSTwrFyk9tmiSRxv0kejmKCA4vBBlVNZ4rlgEO+RL6i2lxutqLW15ZX1LhsUJFN1urAgqw3qeBB7uFck6S+ixw7PgWVkNyIZGyuv0Uc6MOWYjxO+tMeZNbInjaZzWvQaXicO8btHIjI6mzKwIZTyINIArczFBjRXoWigQ2TRRRQMKKKLUAFeqt6FWnA1qQGj6GQxibtgHlfWtz002Os2G6yJBnjF+yNSvEaVy/Z+MKOG5V2LojtRZkseVjeuTLalZ1Y6caDoNjx8ngQ5f6NvWJAH6yTW4I5VvJWVlEgJOXK/gi+so53AK2P31yfGxjB4kQk5EJZoGsSpRmLMjW1BVmaxAOjW0teulbF2heMR51Knua+viKnluipRtWVc+DXFwyQEKQ8eW6MWBzFbG53HytyrnO09hwYImEwozLvklRZGk+l2wQo7ltbvOtdKwloMQYhxZSOAylrgDn/KpPSTZQlVJo442mDKkTTDNHEG1eZ0/rMigkKdL25VfjyrRHkQ+UcKxuMQbooBfd/q8Gvh2NalY7o7OIoJJsMAmIXMJIYMhw6swWJ5TGoWzE3ytrl3EE6b6KaTERq7S46SCfOsUeLfC5doAJIxSKIR/6svYLGQkELYLdmW2O6fbcxM+JwokmLQSJgsTBEqiNFEtrhkB1ZWV1BJY2A11NdZypFPiGtHCP+DGPehZGHuZWHuqOuI4V5FL1maG/6xZJTDf54ZiWiv7Wa7KOJZxvKg10shBI4gkEHQgjeCOBoFRNee2teYPbkuHnjxEL5ZImzId/AggjipBII5E1Baa9RzTsK3Z9F9CfStBtB48LNA8c8mYWAEkDlVLGx3i4UmxHdc1d9K+lWC2OqGSEhps+RcPEgLZMua5uoAGdd5r506GbaXBY2DFMhdYmclVIBIaN00J0+df3VofSf05j2qcOY4Xi6kT36xlObreqtbLy6s+dIoiekDp1NtSRbr1UEZJiiBvqdC8jfOa2nIAkDeScoK8FeigBQFTOskksmZm7IjUE30J0UX3C/CoqCrnZcXV2nYW4wA73fg4HsKdb7iQAL9q2U6NIJmx2VsKG8mIkCymSWdowe0ip1jC+XczEht9wLDjViuCiuAsENybWEEV+Frdmo3R9D8mw6KCSVcADUkmeWwFb3o/sn5O2eUL1hHZAN8gO8kjiTppwB51ySk5Ol/Z1Rioq3/RFXooqwGSSOJbD1TGgPmoBXzBrP4DZqRs3CN3wxN+C3lDAn3NrytXQNtOXQIvziCbchf77eRqmfY7EAZSQVsbDcQ7EH4nzrys2b1Z+MNpL9nVjfPH+Wt/otNi7NR7z4echxeHMrCRcnZbLZri4JBHx31iemOycJAGw6SXeF4WiRpSzqWZGc5L72DMTpxvVouxcbFcQytGpNyFkZR42sRfvqk2jsgxsr4mXrZsxbTMzNut1sj6lBl0FuY7x6EPMcobjX2c78dctSszmF2YZZ207Ika/f2jpW6wmzzHqOW7ge4/jS9jbMUKGG77eZqx2hi0hjzN4AcSTuAHG9Dk5bY1HjpE/ZuK7Op3X38Lc/CmMar4k5b5Yha/tP+C/E93FvZCFxme1jYhRrbkWPE/nWrhBWkE2tmU2lLXZDGFygAVEmOXUC57+FWeIkFVaTBybbufOlPXQQ+2cw9MEAkOHnygSHro3IHrKmQrfwzN51zkR1270k7IMuC6yNSzQP1hA1JjIyyWHdo3gprjqBX3HXka7MDfBWcuX/N0Mrhr0U71bDSvK3MrK6iiikWe09hsOXNhTIqTE5AsKBM9aLKSOVPR4UMtxvqMza1K2dNZtd1ZyurRpFCtn4AuTWn6H4xoJTC2/5t+NN7NwjA9YB2T6w++nNtRBXWRT2ltu4iuacuWjqhCtmy6cQLLg+s+fEVYH/wCQ8q86LbTLKhvwFxUfY+M+UwtGTvUix76pOjcckEjRsrWVrA2JB8DxrPi2jTkkzpm2UBaGcGxV1VvAkffVjsLFZ0KEgNe6ki4uNCGHIjQ1m9uzA4OQK2Zsuig3a45DfTfQ/aOYAzAo3EscmY8yCN/hVRUlKyZODjVnsXROOFmKSPCVEyYfrsauIhwglBWRsJAO0GKk2zXteuc9N9mzDHQlYmGGi+R4fDH1rRRFVXORucksx/vVq/SBZcWJYEklDoC5hlXKCptr+rbW1uPCrXo3tdWULKhXn1rBrf5RWjy5IvrRj68bjp7/AO/2cKxJBdyNQWcjkQWNSP0i5FpAkulgZVu4HAdYpElu7NarzpV0dcYuX5LAxhzAx5R2dQCQvde9VY6PYv8AZ3/y/jXUnas5W0vkifKl/Z4vrYj+NQMSv7PF9bEfxqmr0Zxh3YZ/8v40r/RbGfsz+af+VMXJfZEGJT9nh+tif41BxSfs8P1sT/GrzG7PlhsJYnS+7MND4Hcd3Co1BRKGKT9nh+tif41ejEr+zw/WxP8AGqIKUKQFhFjrerDCp01yNIfd1zOB7hUqJ2dszksx1LMSSfEmqyOrbZsi51MmZlBGYKbNlHBSb20rDL0b4uzc7FxTRxwMjZWCSWI4XlmB+BNdI2biSSsUpD2uFfN2+ze/WG51uLWvz5A1yfY+LFshW6XJUXsy35NY77C4IO7hW62HPHpYNw3uD/8AWvB8jyJY5aPTWJSibjDwBiTzOnhwqaYAKh7PxAt/OpU04tvrfBLCsbm+zzcnPlRXbWnyLcbxuHPurnm0gzOXa5Lbzw8B3CtntCYFwCeGlVW2mUIxYgcSTYADia5Fklnk3dJfB6eGPCK12V+zscIlObRQCSTuFuJJ3VzvpV0qfE4gOhKxx3EQ3X5uRzNtOQ8TT/SbbJm7CXEY4cXI3Fu7kPyMw6V6eFa2ZZH+Vo6n6PukfWAox1Gv41tcVtBUXMx0Hx5W5nuri3QssJrRjM3IcPE8K6xhsKbhpNSPV5L4d/fVpyTaRnNRf5M8aOXEeteOM8L/AKxhyNvUHx8KmLhggsNB3cqlLILVA2hjFQXYgDvqqSRlbbHYcSAcoF76eNc06d9BQS0+FXKwJLINx7x31sItsIzDKb+GtWm1JlVBKxAW6q5PzcxCq3hcgHxB512eBkXJwl0zi8/HJRU49o+dhjmXssuo0N9D76K6ft3oXHNKZLAXAv489KK9J+JL4aPOXnx+YuzkFFelqL1xnogtLza0kUWoAVenovjTAWnY1NQy4m06KbT7QRra6G9T+lWzTE6svquNBy8O6sPh0kBzoCcupIB08adx+1ppCpkckqLL3CsfXvRv7dFzs7FSRTAo2UXBYDW4GpFjWyj25fUC/wC6lc1wGL7Vydb1ertYc62jGjiytyZs12weX+VKdXap5fBfwrGLtgc6dXbQ506MuJshtM/kLTq7Rbv+FYxdtjnTqbdXnToVM2S41uZ86dTEOfnHzrIxbdXnVng9rKeNOkJ2aWONz/WNSnhYfPf3XPwFebMnDbjWj2dg/nkeH406QlZyP0hQYnEIsUWHmaOJ2keV7KCQpWyKTcixOtc6wGzZZyRFGzkbwoua+pNpw5o3UDerbvCuGeirEdTtDI4IDh0uRYZlNx99FI1jJpUZwdFcd+yTfUpJ6N4wb8LP/hMfsFfVaRi24eVK6sch5UUUpSPk9dl4gb8POPGGT/xqXFgZRb9TMOd4nGtzu05W+Nd86edIG2fHHKsCSIzFHJJuptdbKBYg2bew3DffTI7M9JMkzZRFAoPNCf8Au1hlcV2bY5T+EYjAdYu9HHO6sON9dK1mCxRjIBI3KdDcai/nrW92btjNKIpI0JZWKmJTqVsbWLGwtc3NhoOdWzZd2VF7iQzeS/jXn5vAh5CuMv0dkPNePUo/syeA23pofKpE22zbf8avcXhVyO2ViQjlbdmxAJBAFz8DXKv0l2rX47iVP3CvH8r+OnhpKVpnbgz48tuqLbam1WbcSbbrcKzmP2zIxszEixWxvYg79OffWzw5VsOc72FiRa+hA0+frr3VpcQkVrlBuHD+Vdn8f4Klu+jHyvM4ao4Ril47xvqnxUxbQaD4murdOJ4Dh5EUKrkDLu3gg299re+uTM1zXrrAsbOOOd5V9Gk6D7Z+TyBSLgnlqK7AMerKGHEa2+2uAQPkIa4FudavZXSNpyIEOUG13vY94UcPH/8AaxmpW2ujoXFpJ9m42n0kCMY4laWX2E4f323J79eQNUWJ6O4rFHrMVPlHzYodAo5ZjvPfarnA4dYVAUDvPG/G9TTjQRas+VBX0UeG2SsHqE8N5v50/wBJsdfAYlWNrwSj35TlHnan8ROovqK5z042u00nyaO+RDeQ7sz8j3L9vhVYouc1ROWSjDY5sD0gSQQiKROtK3CsTrl4A87a17VDh8PhlFpXJbjk3DuvXte4s00u0eJLx8bfTKiwosPyKtBsocz8K9Gyl51xeyJ3cGRNn4VHJzSrGAL3fS/cLAknuqdhcMjLe19+uutja4pUeyE51JXCng9ROaa0VCDT2MnBx8qu+i/RjD4klpZ1jRSBlLZWc77AnQDv31VNgWP9Zap2ysNPEpEWJCgm5BRGubAX7QPIVEZJPbKlFtaOi4vZCiDqcKkOW1gFZde8neaxM3o9xR3KvuIp+KTFcZ4m/vYeP7rVMilxHt4f/At9jVq80DCOGaKQ+jfG8FA99ej0bY/kvnWh+XTLvkww/wCmw/7lVO3+k00WTJLE182bql5Wte7k+XKhZYvoHjmhhPRljzxUe808nouxvtr8a82FtrETyBTJKATwYjy0NbfGJJBYCXfb+nzyfFQgv/OpeeKdMPXMyCeirF8ZVoxPoxnjRpHm7KKzNlUsbKLmwGp0rSNtaQb5cOPFJP4lQNp7cLRSKZ8ObxyCwRrm6kWGZyL+II7jVe2LD1y+TFYfZeHbdjG+plP+atLsvobEwDHES5eayQjyBN6w+G9bRvgn3Guu7Az/ACQG7EXFrCY+Pqm33VjkzSi9F+pNGj2B0MhwxDdZK5H9pJceW6tNJKoG8D3iuavisQB/tEQ/6AJ+LGqzG43Ef+oKnhh4fvU1usyM/SyF6VtqyfLCqykKEiyBS4FrEknVlY5r6gLutY2uYfQ1hLIFlYW+nkA055rDyFZvpNLmmu8wnbKt5AEUnfZSABqBb3WrUejw3lAAJXiP1h4fQNc+d6s3gklR1PoaAFmsF/pLdixWwAtqoAJ1PPfvPDQmWuLdKp5RKcmJeIa3VVY+F+szH7Kzkm05h/v8n+Gn/jWuLKlFIylik3Z0/wBMmItgVPHrowutt6vf56ndfdm8LXI5DsDEfrBfmN5P3g0naW1Xdcr4pphcHIyra/Pdv1pnY81nBAO8bh+BFLN+SLxJx0zsuz3Uz4e5uCGGqjS8bHflW49/urZpiY03Ee6wFco6U4yRMNE6F0kFshAIO6x0Lm+njWKk6RY7jLL5VHjSSgGWDk7PoLaO1kMbrcao41txUjjpXCpMWTIddMx58/7zfaapJtrzt60r0iLEdobt/d94ozx9iNMFwvZ1fZWZsG6gi9jyPD/l/fUXHdPIwMoBJAsdLajTcd1HRrE3wsnaF7aAsp4b7CQGuZYnZ8pZjzZuHf7/ALay8V8L2PNHmXO29vCb5tvGs670s7Ll/Irz9FS8x8fwrr5x+WZKDXSIjtc61KwExRwwOoIr39ES93mfwp5NiSkWJA8/wqZSg1Vlx5J3Ruv9JlyAs4GgzFjbd+RVTJ0tznLFe3tNpfwXf51QR9HGO9vJTVns/YCKQSWuPokVzOONLuzoU5t9UXaNOELL2nI7BcABb/Ot3Vmh0WmYnO41JJ13k7zWzw+QC3a8v51GxfVg/O8v51GPI49DyRUuzNL0RPtfEUVfLIn0/L+dFbc5/Zhwj9FB1S/kn8aUI1/Jpzq1r0RrTEIVF/JNKCr3eZpxY15fGliNOVSMSuTu8zUqFk7vM0yqJy+NSEVOQ86loqyRHKvd8akLKvd8aixlOQqUjpyFQ0UmDrG2jBT4gmmJdm4c/wBVH9SpiunIU6JU7qLYnQxs/DQowKootyS1XmMxiPwA8FHxqrWZO6nDiE7vKlWwFsIzvW/7opqTCwEG8a/UWlfKU7qWMQvdTtiIMWz4AdIlHhGn4Vo8FOix5Qot/dUVUidakx4pbVLjZVk3MnEfZTM6x21QeQpK4ledJfErzrT4MyuxOGhJv1a38B+FTtkRxI4IjUfuio82ISlYfFKDxrKSNkxe12QtuFVdk5DzqdicQhOtRuuj5DypILGuqQ/NHmaIMGl/UFPfKI+VKjxSX3UMZPxgXIq2GlVrxLUuXGJbdUY4uPlSSEMiIV6qC/Cl/LE5UkY5L7qbQ7LzBOFjYc6pZIhfS3lUlNorl3VEOPTlUpBZ7buHlSXA9keRrw7QXlTZ2gvs1fELFZR7PwNOIo5fCm1xq+z8adTGj2fjRQWSAO4eRpJty+FefLx7Pxpt9oD2aOLHY5n/ADaoeMk/Nq9faA5VExG0R7NVGOyJSAN+bfyoqL+ke6it+JlZR5250Z251AzGjMa24kWWIY86UGPOqy5ou1LiFlqCedKW/Oqm7V6C3OlwHZeR39qn0J9qs+pbnTgLc6lwKUjQq59qnAx9qs6pbmaWGfmangPkaDN9KlZvpVnszczRnbmaOAWaK/0vjSw30vjWaDPzNKDvzNHAVmnU/S+NPoR7XxrKo7czTyyN30cB2alSPa+NesB7XxrMh25mgs3M0cRWX7KvtfGhFX2qzpLd9LUHvqHApMv5FX2hSAi+1VIXNeZzS4DsvCqcxQMvtVRAnvpQBo4hZeOU50zZOYqqYGkZTQohZcWTmK8snOqkA0WNHEdl4Mtt9NEJVaL14AaXELLPsd1JulQMtehadBZZR5KlR5KqY1p8UqHZYsU7qZkZKi2ppqKCx92WokzLSHqNIO+riiGxeZaKikGitKMynLV5mryitiRV6C1FFIYoNXoavaKlghaNSw1e0UmUhYalZ6KKljDOaM9FFIBQalBqKKGAtXp5GoooYDwaklqKKQHmevVeiikygZqSHoopAGc04r0UUhnhc0kuaKKYgD151hoooGKzmgOaKKQHpkNAkNFFMB1JDTwc0UUhnjSGmXkNFFAmMu5qOzmiirRLGyxoooqyD//Z"){

        Glide.with(this)
            .load(url)
            .into(findViewById<ImageView>(R.id.img_banner))
    }//cierra loadImage

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

}