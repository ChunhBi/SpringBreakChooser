package com.example.springbreakchooser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.springbreakchooser.databinding.ActivityMainBinding
import java.util.Locale
import kotlin.random.Random

private const val SHAKE_THRESHOLD = 5

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var tts: TextToSpeech

    private var currentLanguageIdx: Int = 0
    private val startForVoiceInput =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val spokenText: String =
                    result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        .let { results ->
                            results?.get(0) ?: " "
                        }
                binding.textInput.setText(spokenText)
            }
        }
    private var isOpeningMap = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tts = TextToSpeech(this, this)

        binding.selectLanguage.onItemSelectedListener = SpinnerDetailActivity(this)
        binding.voiceInputBtn.setOnClickListener {
            val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE, getLanguage().toString()
                )
            }
            startForVoiceInput.launch(speechIntent)
        }
        binding.openMapBtn.setOnClickListener {
            openMap()
        }

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometerSensor != null) {
            val sensorListener = AccelerometerListener(this)
            sensorManager.registerListener(sensorListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        else {
            Log.d("shake_detect","Device not available!")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Successfully initialized, set the desired language
            setCurrentLanguageSpeaker()
        } else {
            Toast.makeText(this, "TTS Initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        isOpeningMap = false
    }

    override fun onDestroy() {
        if (tts != null) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    fun openMap() {
        if (isOpeningMap) return
        isOpeningMap = true
        val randomNumber = Random.nextInt(2)
        val location: String
        greet()
        Thread.sleep(500L)
        when (randomNumber) {
            0 -> location = when (currentLanguageIdx) {
                0 -> "42.36026, -71.05728" // Boston
                1 -> "39.9075, 116.39723" // beijing
                2 -> "41.88929, 12.49355" // Rome
                3 -> "48.85679, 2.35108" // paris
                else -> "42.36026, -71.05728" // Boston
            }

            else -> location = when (currentLanguageIdx) {
                0 -> "51.50335, -0.07940" // London
                1 -> "31.22031, 121.46239" // shanghai
                2 -> "43.76907, 11.25583" // Florence
                3 -> "43.29643, 5.37784" // marseille
                else -> "51.50335, -0.07940" // London
            }
        }
        val geoUri = Uri.parse("geo:" + location)
        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
        startActivity(mapIntent)
    }

    fun setCurrentLanguageIdx(idx: Int) {
        currentLanguageIdx = idx
        setCurrentLanguageSpeaker()
        binding.selectLanguageTitle.text = when (idx) {
            0 -> "Choose your language"
            1 -> "选择你的语言"
            2 -> "Scegli la tua LINGUA"
            3 -> "Choisissez votre langue"
            else -> "Choose your language"
        }
        binding.voiceInputBtn.text  = when (idx) {
            0 -> "Voice Input"
            1 -> "语音输入"
            2 -> "Input Vocale"
            3 -> "Entrée Vocale"
            else -> "Voice Input"
        }
        binding.openMapBtn.text  = when (idx) {
            0 -> "Open Map"
            1 -> "打开地图"
            2 -> "Apri la Mappa"
            3 -> "Ouvrir la Carte"
            else -> "Open Map"
        }
        binding.textInput.hint  = when (idx) {
            0 -> "Type or Say anything"
            1 -> "键入或语音输入"
            2 -> "Digita o dì qualsiasi cosa"
            3 -> "Tapez ou dites n'importe quoi"
            else -> "Type or Say anything"
        }
    }

    private fun setCurrentLanguageSpeaker() {
        val result = tts.setLanguage(
            getLanguage()
        )
//        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//            Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show()
//        } else {
//            Log.d("change_language", "Changed language.")
//        }
    }

    private fun getLanguage() : Locale {
        return when (currentLanguageIdx) {
            0 -> Locale.US
            1 -> Locale.CHINA
            2 -> Locale.ITALY
            3 -> Locale.FRANCE
            else -> Locale.US
        }
    }

    private fun greet() {
        val textToSpeak = when (currentLanguageIdx) {
            0 -> "Hello"
            1 -> "你好"
            2 -> "Ciao"
            3 -> "Bonjour"
            else -> "Hello"
        }
        speakText(textToSpeak)
    }

    private fun speakText(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, null)
    }

}

class SpinnerDetailActivity(val mainActivity: MainActivity
) : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // Code to execute when an item is selected
//        val selectedItem = parent?.getItemAtPosition(position) as String  // Example
        mainActivity.setCurrentLanguageIdx(position)
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Code to execute when no item is selected (optional)
    }
}

class AccelerometerListener(val mainActivity: MainActivity
): SensorEventListener {
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
//                Log.d("shake_detect",x.toString() + " " + y.toString() + " "+ z.toString())

                val acceleration = Math.sqrt((x * x + z * z).toDouble())
//                Log.d("shake_detect",acceleration.toString())
                if (acceleration > SHAKE_THRESHOLD) {
                    // Shake detected!
                    Log.d("shake_detect","Shake detected2!")
                    mainActivity.openMap()
                }
            }
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}