package com.example.app_verduras.repository

import android.content.res.AssetManager
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.dal.ProductoDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ProductoRepository(
    private val productoDao: ProductoDao,
    private val assetManager: AssetManager
) {

    val allProducts: Flow<List<Producto>> = productoDao.getAllProductsFlow()

    init {
        // Precarga la base de datos desde el JSON si está vacía.
        CoroutineScope(Dispatchers.IO).launch {
            if (productoDao.count() == 0) {
                try {
                    val jsonString = assetManager.open("productos.json").bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(jsonString)
                    val productos = mutableListOf<Producto>()

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)

                        // --- INICIO DE LA CORRECCIÓN ---
                        val producto = Producto(
                            codigo = jsonObject.getString("codigo"),
                            nombre = jsonObject.getString("nombre"),
                            descripcion = jsonObject.getString("descripcion"),
                            categoria = jsonObject.getString("categoria"),
                            precio = jsonObject.getDouble("precio"),
                            // Se añade la lectura del campo 'stock' que faltaba
                            stock = jsonObject.getInt("stock"),
                            img = jsonObject.optString("img", null) // Usar optString para seguridad
                        )
                        // --- FIN DE LA CORRECCIÓN ---

                        productos.add(producto)
                    }
                    productoDao.insertAll(productos)
                } catch (e: Exception) {
                    // Imprime el error en Logcat para facilitar la depuración
                    e.printStackTrace()
                }
            }
        }
    }

    suspend fun getByCodigo(codigo: String): Producto? {
        return productoDao.getProductByCode(codigo)
    }

    suspend fun insert(producto: Producto) {
        productoDao.insertProduct(producto)
    }

    suspend fun clear() {
        productoDao.clearAll()
    }
}