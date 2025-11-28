package com.example.app_verduras.dal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.app_verduras.Model.Documento
import com.example.app_verduras.Model.Pedido
import com.example.app_verduras.Model.Producto
import com.example.app_verduras.Model.User

@Database(
    entities = [User::class, Pedido::class, Producto::class, Documento::class],
    version = 6, // Incrementamos la versión por cambios en los modelos para API Huerto Hogar
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun productoDao(): ProductoDao
    abstract fun documentoDao(): DocumentoDao // Añadimos el DAO para Documento

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "huertohogar_db"
                )
                    // Ojo: fallbackToDestructiveMigration borra los datos si la migración no se provee.
                    // Para producción, se necesitaría una estrategia de migración real.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
