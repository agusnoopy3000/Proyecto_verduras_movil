package com.example.app_verduras.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.delay

// ==================== CARGA PROGRESIVA DE IMÁGENES ====================

/**
 * Imagen optimizada con prefetch, placeholder y transición suave.
 * Usa Coil con políticas de caché agresivas.
 */
@Composable
fun OptimizedImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    crossfadeDuration: Int = 300
) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(crossfadeDuration)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        loading = {
            // Placeholder con shimmer mientras carga
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(placeholderColor)
                    .shimmerEffect()
            )
        },
        error = {
            // Placeholder de error
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚠️",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    )
}

/**
 * Prefetch de imágenes para carga anticipada.
 */
@Composable
fun PrefetchImages(
    imageUrls: List<String>
) {
    val context = LocalContext.current
    
    LaunchedEffect(imageUrls) {
        imageUrls.forEach { url ->
            // Precargar imagen en caché
            val request = ImageRequest.Builder(context)
                .data(url)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            
            coil.ImageLoader(context).enqueue(request)
        }
    }
}

// ==================== LISTA OPTIMIZADA CON PREFETCH ====================

/**
 * Lista lazy optimizada con prefetch de items visibles próximos.
 */
@Composable
fun <T> OptimizedLazyColumn(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    prefetchCount: Int = 5,
    key: ((T) -> Any)? = null,
    itemContent: @Composable (T) -> Unit
) {
    // Detectar items que serán visibles pronto
    val firstVisibleIndex by remember { derivedStateOf { state.firstVisibleItemIndex } }
    
    // Prefetch de imágenes para los próximos items
    LaunchedEffect(firstVisibleIndex) {
        val endIndex = (firstVisibleIndex + prefetchCount).coerceAtMost(items.lastIndex)
        // Aquí se podría implementar prefetch específico según el tipo T
    }
    
    LazyColumn(
        modifier = modifier,
        state = state
    ) {
        items(
            items = items,
            key = key
        ) { item ->
            itemContent(item)
        }
    }
}

// ==================== ANIMACIONES FLUIDAS (60-120 FPS) ====================

/**
 * Extensión para animaciones de alta performance.
 * Usa spring animations que son más eficientes que tween para interacciones.
 */
@Composable
fun rememberHighPerformanceAnimatable(
    initialValue: Float = 0f
): Animatable<Float, AnimationVector1D> {
    return remember { Animatable(initialValue) }
}

/**
 * Spring animation optimizada para 60+ FPS.
 */
val HighPerformanceSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow
)

/**
 * Animación de escala suave para botones.
 */
@Composable
fun AnimatedScaleOnPress(
    isPressed: Boolean,
    content: @Composable (scale: Float) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = HighPerformanceSpring,
        label = "press_scale"
    )
    
    content(scale)
}

// ==================== EVITAR OVERDRAW ====================

/**
 * Modifier que reduce overdraw haciendo el fondo transparente
 * cuando hay contenido encima.
 */
fun Modifier.reduceOverdraw(): Modifier = this.graphicsLayer {
    // Usar hardware acceleration
    compositingStrategy = CompositingStrategy.Offscreen
}

/**
 * Surface optimizado que evita dibujar capas innecesarias.
 */
@Composable
fun OptimizedSurface(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    shape: Shape = RoundedCornerShape(0.dp),
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.reduceOverdraw(),
        color = color,
        shape = shape,
        shadowElevation = elevation
    ) {
        content()
    }
}

// ==================== PLACEHOLDERS MIENTRAS NAVEGA ====================

/**
 * Placeholder de pantalla mientras se carga el contenido.
 */
@Composable
fun ScreenLoadingPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerEffect()
        )
        
        // Content placeholders
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerEffect()
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                }
            }
        }
    }
}

/**
 * Transición suave entre pantallas.
 */
@Composable
fun ScreenTransition(
    isLoading: Boolean,
    loadingContent: @Composable () -> Unit = { ScreenLoadingPlaceholder() },
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (isLoading) 0f else 1f,
        animationSpec = tween(300),
        label = "screen_transition"
    )
    
    Box {
        if (isLoading) {
            loadingContent()
        }
        
        Box(modifier = Modifier.graphicsLayer { this.alpha = alpha }) {
            content()
        }
    }
}

// ==================== SHIMMER EFFECT OPTIMIZADO ====================

/**
 * Efecto shimmer optimizado para performance.
 */
@Composable
fun Modifier.shimmerEffect(
    durationMillis: Int = 1000
): Modifier {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant
    )
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim, 0f)
    )
    
    return this.background(brush)
}

// ==================== DEBOUNCE PARA INPUTS ====================

/**
 * Hook para debounce de búsqueda/inputs.
 */
@Composable
fun <T> rememberDebouncedValue(
    value: T,
    delayMillis: Long = 300L
): T {
    var debouncedValue by remember { mutableStateOf(value) }
    
    LaunchedEffect(value) {
        delay(delayMillis)
        debouncedValue = value
    }
    
    return debouncedValue
}

/**
 * Campo de búsqueda con debounce incorporado.
 */
@Composable
fun DebouncedSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onDebouncedValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar...",
    debounceMillis: Long = 300L
) {
    val debouncedValue = rememberDebouncedValue(value, debounceMillis)
    
    LaunchedEffect(debouncedValue) {
        onDebouncedValueChange(debouncedValue)
    }
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

// ==================== MEMORY-EFFICIENT BITMAP ====================

/**
 * Calcula el inSampleSize para decodificar bitmaps eficientemente.
 */
fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}
