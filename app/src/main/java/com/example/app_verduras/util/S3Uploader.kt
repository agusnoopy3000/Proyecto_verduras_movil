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

    private val bucketName = "admin-app-movil"
    private val identityPoolId = "us-east-1:a344bf4a-b300-4536-bea8-3bb3802f5997"
    private val awsRegion = Regions.US_EAST_1

    private val s3Client: AmazonS3Client by lazy {
        val credentialsProvider = CognitoCachingCredentialsProvider(
            context.applicationContext,
            identityPoolId,
            awsRegion
        )
        AmazonS3Client(credentialsProvider, Region.getRegion(awsRegion))
    }

    suspend fun uploadFile(uri: Uri, fileName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val inputStream: InputStream? = contentResolver.openInputStream(uri)

                if (inputStream == null) {
                    return@withContext null
                }

                val metadata = ObjectMetadata().apply {
                    contentType = contentResolver.getType(uri)
                }

                val putObjectRequest = PutObjectRequest(
                    bucketName,
                    fileName,
                    inputStream,
                    metadata
                )

                s3Client.putObject(putObjectRequest)

                s3Client.getResourceUrl(bucketName, fileName)

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
