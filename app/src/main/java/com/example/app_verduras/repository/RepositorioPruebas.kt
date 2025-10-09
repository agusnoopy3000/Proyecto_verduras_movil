package com.example.app_verduras.repository



import com.example.app_verduras.Model.Producto

class RepositorioPruebas {
    private val products = listOf(
        Producto("F001", "Manzana roja", "Frutas", 990.0, 12, "https://picsum.photos/100"),
        Producto("V001", "Zanahoria orgánica", "Verduras", 750.0, 30, "https://picsum.photos/101"),
        Producto("L001", "Leche entera", "Lácteos", 1200.0, 8, "https://picsum.photos/102"),
    )

    fun getProducts(): List<Producto> = products
    fun getCategories(): List<String> = products.map { it.categoria }.distinct()
}
