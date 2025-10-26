package com.example.app_verduras.util

import android.content.Context
import android.net.Uri
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class S3Uploader(private val context: Context) {

    // --- ¡DATOS CASI COMPLETOS! ---
    private val bucketName = "admin-app-movil" // ¡CORRECTO!
    private val identityPoolId = "us-east-1:a344bf4a-b300-4536-bea8-3bb3802f5997" // Pega aquí el ID que copiaste de AWS
    private val region = Regions.US_EAST_1 // Ya está correcto

    private val s3Client: AmazonS3Client by lazy {
        val credentialsProvider = CognitoCachingCredentialsProvider(
            context,
            identityPoolId,
            region
        )
        // Corregido: El constructor espera un objeto Region, no el enum Regions.
        AmazonS3Client(credentialsProvider, Region.getRegion(region))
    }

    suspend fun uploadFile(uri: Uri, fileName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                
                if (inputStream == null) {
                    // Log error or throw exception
                    return@withContext null
                }

                val metadata = ObjectMetadata().apply {
                    contentType = contentResolver.getType(uri)
                    contentLength = contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0
                }

                // Sube el archivo
                val putObjectRequest = PutObjectRequest(
                    bucketName,
                    fileName, // La clave del objeto en S3 será su nombre
                    inputStream,
                    metadata
                )

                s3Client.putObject(putObjectRequest)

                // Devuelve la URL pública del archivo
                s3Client.getResourceUrl(bucketName, fileName)

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
