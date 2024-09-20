package com.adnalvarez.feliz_cumpleaos

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.LocalDate

// Identificador del canal de notificación
private const val CHANNEL_ID = "cumpleaños_channel"
private const val NOTIFICATION_ID = 1
private const val REQUEST_CODE_POST_NOTIFICATIONS = 100

class MainActivity : ComponentActivity() {
    lateinit var mediaPlayer: MediaPlayer

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializamos el MediaPlayer con el archivo de audio
        mediaPlayer = MediaPlayer.create(this, R.raw.feliz_cumpleanos)
        mediaPlayer.start()

        // Registrar el BroadcastReceiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                notificationReceiver,
                IntentFilter("com.adnalvarez.feliz_cumpleaos.NOTIFICATION_DISMISS"),
                RECEIVER_NOT_EXPORTED
            )
        }

        // Crear canal de notificación si es necesario
        createNotificationChannel()

        // Verificar si alguien cumple años hoy y mostrar notificación
        val birthdayPerson = checkBirthday()
        if (birthdayPerson != "Nadie cumple años hoy") {
            sendBirthdayNotification(birthdayPerson)
        }

        // Configuramos el contenido de la tarjeta
        setContent {
            BirthdayCard(birthdayPerson = birthdayPerson) { openWhatsApp(birthdayPerson) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        unregisterReceiver(notificationReceiver)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkBirthday(): String {
        val friendsBirthdays = listOf(
            "Valentino" to LocalDate.of(1995, 1, 23),
            "Emilio" to LocalDate.of(1995, 4, 5),
            "Oscar" to LocalDate.of(1995, 4, 30),
            "Axel" to LocalDate.of(1995, 5, 11),
            "Jafet" to LocalDate.of(1995, 5, 18),
            "Gabriela" to LocalDate.of(1995, 6, 1),
            "Amadeo" to LocalDate.of(1995, 7, 27),
            "Chepe" to LocalDate.of(1995, 9, 25),
            "Steven" to LocalDate.of(1995, 11, 17),
            "Adrián" to LocalDate.of(1995, 12, 25),
            //"Alejo" to LocalDate.of(1995, 6, 19),
            "Selene" to LocalDate.of(1995, 8, 16),
            //"Axel" to LocalDate.of(1995, 9, 19) testear
        )
        val currentDate = LocalDate.now()
        return friendsBirthdays.find {
            it.second.monthValue == currentDate.monthValue && it.second.dayOfMonth == currentDate.dayOfMonth
        }?.first ?: "Nadie cumple años hoy"
    }

    private fun openWhatsApp(personName: String) {
        val phoneNumbers = mapOf(
            "Valentino" to "+5493388529536", // Reemplaza con el número de tu amigo
            "Axel" to "+50361335552",
            "Emilio" to "+50584472690",
            "Oscar" to "+50375418787",
            "Jafet" to "+573222133890",
            "Gabriela" to "+14099165072",
            "Amadeo" to "+573233003677",
            "Chepe" to "+12132907709",
            "Steven" to "+50373228223",
            "Adrián" to "+573238087188",
            //"Alejo" to "+573008512002",
            "Selene" to "+5212224163725",
            // Agrega los demás amigos y sus números
        )
        val phoneNumber = phoneNumbers[personName]
        if (phoneNumber != null) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$phoneNumber?text=¡Feliz cumpleaños, $personName!")
            }
            startActivity(intent)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Canal de cumpleaños"
            val descriptionText = "Notificaciones para cumpleaños"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendBirthdayNotification(birthdayPerson: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val stopMusicIntent = Intent("com.adnalvarez.feliz_cumpleaos.NOTIFICATION_DISMISS")
        val stopMusicPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            this, 0, stopMusicIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cake)
            .setContentTitle("¡Es el cumpleaños de $birthdayPerson!")
            .setContentText("No olvides felicitar a $birthdayPerson.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDeleteIntent(stopMusicPendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission()
                return
            }
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_POST_NOTIFICATIONS
                )
            }
        }
    }

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (::mediaPlayer.isInitialized) {
                mediaPlayer.stop()
                mediaPlayer.release()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_POST_NOTIFICATIONS: Int = 100
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun BirthdayCard(birthdayPerson: String, onWhatsAppClick: () -> Unit) {
    val context = LocalContext.current
    var showStopButton by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = "Fondo de cumpleaños",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent),
                        tileMode = TileMode.Clamp
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (birthdayPerson != "Nadie cumple años hoy") "¡Feliz Cumpleaños $birthdayPerson!" else birthdayPerson,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.torta),
                contentDescription = "Imagen de una torta",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "De parte de Adrián",
                fontSize = 35.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mostrar botón "Felicitar" solo si alguien cumple años
            if (birthdayPerson != "Nadie cumple años hoy") {
                Button(onClick = onWhatsAppClick) {
                    Text(text = "Felicitar")
                }
            }
        }

        // Botón detener música
        if (showStopButton) {
            Button(
                onClick = {
                    if (context is MainActivity) {
                        context.mediaPlayer.stop()
                        showStopButton = false
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("Detener música")
            }
        }

        // Mostrar botón detener música si se está reproduciendo
        LaunchedEffect(Unit) {
            showStopButton = (context as? MainActivity)?.mediaPlayer?.isPlaying ?: false
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun BirthdayCardPreview() {
    BirthdayCard("") {}
}