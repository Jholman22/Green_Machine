package com.example.greenmachine // Asegúrate que tu package sea el correcto
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.greenmachine.ui.theme.GreenMachineTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import android.graphics.Bitmap
import android.graphics.BitmapFactory

data class SpeciesDetail(
    val nombreComun: String,
    val nombreCientifico: String,
    val familia: String,
    val descripcion: String,
    val usosMedicinales: List<String>,
    val propiedades: List<String>,
    val preparacion: String,
    val precauciones: String
)
private const val NUM_CLASSES = 11

private val speciesDetailsMap = mapOf(
    "Sabila" to SpeciesDetail(
        nombreComun = "Sábila o Aloe Vera",
        nombreCientifico = "Aloe vera",
        familia = "Asphodelaceae",
        descripcion = "Planta suculenta con hojas carnosas y alargadas que contienen un gel transparente en su interior, muy apreciado por sus propiedades medicinales y cosméticas.",
        usosMedicinales = listOf(
            "Tratamiento de quemaduras solares y heridas leves.",
            "Hidratación profunda de la piel y el cabello.",
            "Alivio del estreñimiento (consumo del jugo).",
            "Cuidado post-afeitado o post-depilación."
        ),
        propiedades = listOf("Cicatrizante", "Antiinflamatorio", "Hidratante", "Laxante suave"),
        preparacion = "Uso tópico del gel extraído directamente de la hoja. Consumo del jugo procesado.",
        precauciones = "El consumo interno debe ser moderado. La capa amarilla bajo la corteza (aloína) puede ser irritante y laxante fuerte; se recomienda retirarla."
    ),

    "Calendula" to SpeciesDetail(
        nombreComun = "Caléndula",
        nombreCientifico = "Calendula officinalis",
        familia = "Asteraceae",
        descripcion = "Planta de flores naranjas o amarillas, ampliamente utilizada para el cuidado de la piel y mucosas.",
        usosMedicinales = listOf(
            "Calmar irritaciones y dermatitis leves.",
            "Favorecer la cicatrización superficial de heridas.",
            "Enjuagues para aftas o irritaciones bucales leves."
        ),
        propiedades = listOf("Antiinflamatoria", "Cicatrizante", "Antiséptica suave"),
        preparacion = "Infusión de flores para compresas o enjuagues; uso tópico en ungüentos o cremas.",
        precauciones = "Posible alergia en personas sensibles a Asteraceae. Para uso interno, moderación y consulta profesional; evitar en embarazo sin supervisión."
    ),
    "Manzanilla" to SpeciesDetail(
        nombreComun = "Manzanilla",
        nombreCientifico = "Matricaria chamomilla",
        familia = "Asteraceae",
        descripcion = "Planta herbácea anual con flores blancas de centro amarillo y aroma dulce, muy utilizada en infusiones por sus efectos digestivos y calmantes.",
        usosMedicinales = listOf(
            "Alivio de indigestión, cólicos y gases.",
            "Apoyo para conciliar el sueño y reducir la ansiedad leve.",
            "Calmar irritaciones leves de piel y ojos (en compresas)."
        ),
        propiedades = listOf("Digestiva", "Carminativa", "Antiespasmódica", "Sedante suave", "Antiinflamatoria"),
        preparacion = "Infusión de flores (1–2 cucharaditas por taza, 5–10 minutos). También se usa en compresas y enjuagues.",
        precauciones = "Posible alergia en personas sensibles a Asteraceae. Usar con moderación en embarazo y lactancia. Puede potenciar el efecto de anticoagulantes; suspender si aparece irritación."
    ),
    "Paico" to SpeciesDetail(
        nombreComun = "Paico o Epazote",
        nombreCientifico = "Dysphania ambrosioides",
        familia = "Amaranthaceae",
        descripcion = "Herbácea aromática tradicional en la cocina latinoamericana; hojas de aroma intenso y uso popular digestivo.",
        usosMedicinales = listOf(
            "Alivio de gases y cólicos.",
            "Apoyo en casos de dispepsia (digestiones pesadas).",
            "Uso tradicional contra parásitos intestinales."
        ),
        propiedades = listOf("Carminativa", "Antiespasmódica", "Digestiva"),
        preparacion = "Infusión suave de hojas frescas o secas; también se usa como condimento en sopas y frijoles.",
        precauciones = "En dosis altas puede ser tóxica (ascaridol). Evitar en embarazo, lactancia y en niños. No usar de forma prolongada."
    ),
    "Cimarron" to SpeciesDetail(
        nombreComun = "Cimarrón o Culantro",
        nombreCientifico = "Eryngium foetidum",
        familia = "Apiaceae",
        descripcion = "Hierba aromática con hojas largas y dentadas. Su olor y sabor son similares al cilantro, pero mucho más intensos. Muy usado en la gastronomía caribeña y asiática.",
        usosMedicinales = listOf(
            "Alivio de dolores estomacales y gases.",
            "Tratamiento de la fiebre y el resfriado.",
            "Estímulo del apetito."
        ),
        propiedades = listOf("Digestivo", "Carminativo (antigases)", "Antiinflamatorio", "Febrífugo"),
        preparacion = "Infusión de las hojas. Uso de las hojas frescas o secas como condimento.",
        precauciones = "Seguro en cantidades culinarias. En dosis medicinales, consultar a un especialista."
    ),
    "Coca" to SpeciesDetail(
        nombreComun = "Hoja de Coca",
        nombreCientifico = "Erythroxylum coca",
        familia = "Erythroxylaceae",
        descripcion = "Arbusto originario de los Andes, con un profundo significado cultural y religioso para las comunidades indígenas. Sus hojas son conocidas por sus propiedades estimulantes.",
        usosMedicinales = listOf(
            "Combatir el mal de altura (\"soroche\").",
            "Aumentar la energía y reducir la sensación de hambre.",
            "Alivio de dolores de cabeza y malestares digestivos."
        ),
        propiedades = listOf("Energizante", "Estimulante suave", "Digestivo", "Analgésico local"),
        preparacion = "Masticado de las hojas (acullico) o en infusión (mate de coca).",
        precauciones = "Planta controlada en muchos países debido a su alcaloide. Su uso tradicional es legal y aceptado en países como Bolivia y Perú, pero su exportación y posesión pueden ser ilegales en otros lugares."
    ),
    "Hierbabuena" to SpeciesDetail(
        nombreComun = "Hierbabuena o Menta Verde",
        nombreCientifico = "Mentha spicata",
        familia = "Lamiaceae",
        descripcion = "Planta aromática muy popular, con hojas lanceoladas y un aroma fresco y penetrante. Es un híbrido natural entre diferentes especies de menta.",
        usosMedicinales = listOf(
            "Alivio de la indigestión, náuseas y cólicos.",
            "Descongestionante para las vías respiratorias.",
            "Propiedades antisépticas para el mal aliento."
        ),
        propiedades = listOf("Digestiva", "Antiespasmódica", "Carminativa", "Antiséptica"),
        preparacion = "Infusión de las hojas frescas o secas. Masticado de las hojas.",
        precauciones = "Evitar su consumo en grandes cantidades durante el embarazo y la lactancia."
    ),

    "Bolbo" to SpeciesDetail(
        nombreComun = "Boldo",
        nombreCientifico = "Peumus boldus",
        familia = "Monimiaceae",
        descripcion = "Arbusto aromático; sus hojas de sabor amargo se emplean tradicionalmente para molestias digestivas y hepáticas leves.",
        usosMedicinales = listOf(
            "Dispepsia y digestiones pesadas.",
            "Estimulación del flujo biliar (uso tradicional).",
            "Alivio de cólicos leves."
        ),
        propiedades = listOf("Colerético", "Colagogo", "Antiespasmódico", "Digestivo"),
        preparacion = "Infusión suave de hojas secas (no concentrada).",
        precauciones = "Evitar en embarazo, lactancia, obstrucción biliar, cálculos y enfermedades hepáticas. No usar crónicamente."
    ),

    "No id" to SpeciesDetail(
        nombreComun = "No   Identificado",
        nombreCientifico = "N/A",
        familia = "N/A",
        descripcion = "Esta categoría se usa para plantas que no han sido identificadas como medicinales en el contexto de este proyecto.",
        usosMedicinales = listOf("Ninguno conocido."),
        propiedades = listOf("Ninguna."),
        preparacion = "No aplica.",
        precauciones = "No se recomienda su consumo."
    ),
    "Ruda" to SpeciesDetail(
        nombreComun = "Ruda",
        nombreCientifico = "Ruta graveolens",
        familia = "Rutaceae",
        descripcion = "Arbusto de olor fuerte y característico, con hojas de color verde azulado. Ha sido utilizada desde la antigüedad en rituales y como planta medicinal.",
        usosMedicinales = listOf(
            "Alivio de cólicos y dolores menstruales.",
            "Tratamiento de espasmos y dolores de cabeza.",
            "Tradicionalmente usada como amuleto de protección."
        ),
        propiedades = listOf("Antiespasmódica", "Emenagoga (regula la menstruación)", "Sedante"),
        preparacion = "Infusión de las hojas en muy pequeñas cantidades.",
        precauciones = "Planta tóxica en dosis altas. Es abortiva, por lo que está estrictamente prohibida en el embarazo. Puede causar irritación en la piel (fotosensibilidad)."
    ),
    "Valeriana" to SpeciesDetail(
        nombreComun = "Valeriana",
        nombreCientifico = "Valeriana officinalis",
        familia = "Caprifoliaceae",
        descripcion = "Planta herbácea cuyas raíces son famosas por sus efectos relajantes sobre el sistema nervioso central. Es uno de los remedios naturales más populares para el insomnio.",
        usosMedicinales = listOf(
            "Tratamiento del insomnio y problemas para dormir.",
            "Reducción de la ansiedad, el estrés y el nerviosismo.",
            "Alivio de la agitación y la irritabilidad."
        ),
        propiedades = listOf("Sedante", "Relajante", "Ansiolítico", "Hipotensora suave"),
        preparacion = "Infusión de la raíz seca, extractos, cápsulas o tinturas.",
        precauciones = "Puede causar somnolencia, no se debe conducir ni operar maquinaria pesada tras su consumo. No combinar con alcohol u otros sedantes."
    )
)
private val labelList  = listOf(
    "Sabila",
    "Calendula",
    "Manzanilla",
    "Paico",
    "Cimarron",
    "Coca",
    "Hierbabuena",
    "Bolbo",
    "No id",
    "Ruda",
    "Valeriana"
)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GreenMachineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Llamamos a nuestro diseño final que combina todo
                    GreenMachineCameraScreen(
                        message = "Green Machine",
                        from = "¡Identifica plantas al instante!"
                    )
                }
            }
        }
    }
}

@Composable
fun GreenMachineCameraScreen(message: String, from: String, modifier: Modifier = Modifier) {
    // --- ESTADOS ---
    // Guarda la URI (dirección) de la foto tomada por el usuario.
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    // Guarda si tenemos permiso para usar la cámara.
    var hasCameraPermission by remember { mutableStateOf(false) }

    // --- CONTEXTO Y LANZADORES ---
    val context = LocalContext.current
    // 🚩 Carga del modelo YOLOv8-s-cls
    val interpreter = remember { loadModelFile(context) }

    var predictionResult by remember { mutableStateOf("Esperando imagen") }
    var speciesDetail by remember { mutableStateOf<SpeciesDetail?>(null) }


    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
        }
    )
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri?.let { uri ->
                    val bitmap = uriToBitmap(context, uri)
                    val inputBuffer = preprocessBitmap(bitmap)
                    val outputBuffer = Array(1) { FloatArray(NUM_CLASSES ) }

                    interpreter.run(inputBuffer, outputBuffer)

                    val predictedIndex = outputBuffer[0].indices.maxByOrNull { outputBuffer[0][it] } ?: -1
                    val predictedLabel = if (predictedIndex in labelList.indices) {
                        labelList[predictedIndex]
                    } else {
                        "Noid" // Por si algo falla
                    }
                    speciesDetail = speciesDetailsMap[predictedLabel] ?: speciesDetailsMap["NoIdentificado"]

                    val probability = outputBuffer[0][predictedIndex] * 100
                    predictionResult = "Predicción: ${labelList[predictedIndex]} (${probability.toInt()}%)"

                }
            }
        }
    )

    // --- EFECTO INICIAL ---
    // Al entrar a la pantalla, pide el permiso de la cámara una sola vez.
    LaunchedEffect(key1 = true) {
        permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    // --- INTERFAZ DE USUARIO ---
    // Box permite apilar elementos. El primero queda al fondo.
    Box(modifier = modifier.fillMaxSize()) {

        // Capa 1: Imagen de fondo decorativa
        Image(
            painter = painterResource(id = R.drawable.fondo), // <-- CAMBIA ESTO
            contentDescription = "fondo",
            contentScale = ContentScale.Crop,
            alpha = 0.5F,
            modifier = Modifier.fillMaxSize()
        )

        // Capa 2: La foto tomada por el usuario (si existe)
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Foto capturada",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
// Capa 3 y 4 combinadas: Caja blanca con texto y botón dentro
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(14.dp)
                .fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState), // ← Habilita scroll
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logogm),
                    contentDescription = "Logo de Green Machine",
                    modifier = Modifier
                        .size(150.dp)  // Tamaño del logo
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = message,
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = from,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    enabled = hasCameraPermission,
                    onClick = {
                        val file = createImageFile(context)
                        imageUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        imageUri?.let {
                            cameraLauncher.launch(it)
                        }
                    }
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_camera), contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Tomar Foto")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = predictionResult,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                speciesDetail?.let { detail ->
                    Text("🌿 ${detail.nombreComun}", fontSize = 24.sp)
                    Text("🔬 ${detail.nombreCientifico}", fontSize = 18.sp)
                    Text("🌱 Familia: ${detail.familia}", fontSize = 18.sp)
                    Text("📝 ${detail.descripcion}", fontSize = 16.sp)

                    Text("🩺 Usos medicinales:", fontSize = 18.sp)
                    detail.usosMedicinales.forEach { uso ->
                        Text("- $uso", fontSize = 16.sp)
                    }

                    Text("💡 Propiedades:", fontSize = 18.sp)
                    detail.propiedades.forEach { prop ->
                        Text("- $prop", fontSize = 16.sp)
                    }

                    Text("🍵 Preparación: ${detail.preparacion}", fontSize = 16.sp)
                    Text("⚠️ Precauciones: ${detail.precauciones}", fontSize = 16.sp)
                }

            }
        }

    }
}

// Función auxiliar para crear un archivo temporal para la foto
private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    return File.createTempFile(
        imageFileName,
        ".jpg",
        context.cacheDir
    )
}

private fun loadModelFile(context: Context): Interpreter {
    val assetFileDescriptor = context.assets.openFd("best_float16.tflite")
    val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
    val fileChannel = fileInputStream.channel
    val startOffset = assetFileDescriptor.startOffset
    val declaredLength = assetFileDescriptor.declaredLength

    val mappedByteBuffer: MappedByteBuffer =
        fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

    return Interpreter(mappedByteBuffer)
}

// Convierte Uri a Bitmap
private fun uriToBitmap(context: Context, uri: Uri): Bitmap {
    val inputStream = context.contentResolver.openInputStream(uri)
    return BitmapFactory.decodeStream(inputStream)
}

// Preprocesa Bitmap: redimensiona y normaliza si es necesario
private fun preprocessBitmap(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
    val inputSize = 600 // o el tamaño de entrada de tu modelo
    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

    val input = Array(1) { Array(inputSize) { Array(inputSize) { FloatArray(3) } } }
    for (y in 0 until inputSize) {
        for (x in 0 until inputSize) {
            val pixel = resizedBitmap.getPixel(x, y)
            input[0][y][x][0] = ((pixel shr 16 and 0xFF) / 255.0f)
            input[0][y][x][1] = ((pixel shr 8 and 0xFF) / 255.0f)
            input[0][y][x][2] = ((pixel and 0xFF) / 255.0f)
        }
    }
    return input
}

@Preview(showBackground = true)
@Composable
fun GreenMachineCameraPreview() {
    GreenMachineTheme {
        GreenMachineCameraScreen(message = "Green Machine", from = "AprendeConmigo")
    }
}