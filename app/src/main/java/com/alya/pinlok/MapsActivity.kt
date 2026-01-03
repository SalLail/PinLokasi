package com.alya.pinlok

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import java.io.File

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var db: DatabaseHelper
    private lateinit var auth: FirebaseAuth
    private var selectedLatLng: LatLng? = null
    private var photoUri: Uri? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        auth = FirebaseAuth.getInstance()
        db = DatabaseHelper(this)

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Activity Result untuk kamera
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "Foto berhasil diambil", Toast.LENGTH_SHORT).show()
            }
        }

        // tombol simpan lokasi
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            selectedLatLng?.let { latLng ->
                val view = layoutInflater.inflate(R.layout.dialog_add_location, null)
                val etName = view.findViewById<EditText>(R.id.etName)
                val etNote = view.findViewById<EditText>(R.id.etNote)
                val btnPhoto = view.findViewById<Button>(R.id.btnAddPhoto)

                // tombol ambil foto
                btnPhoto.setOnClickListener {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    val file = File.createTempFile("lokasi_", ".jpg", cacheDir)
                    photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    cameraLauncher.launch(intent)
                }

                AlertDialog.Builder(this)
                    .setTitle("Tambah Lokasi")
                    .setView(view)
                    .setPositiveButton("Simpan") { _, _ ->
                        val name = etName.text.toString()
                        val note = etNote.text.toString()
                        db.insertLocation(
                            auth.currentUser!!.uid,
                            name,
                            note,
                            latLng.latitude,
                            latLng.longitude,
                            photoUri?.toString() ?: "" // simpan path foto
                        )
                        Toast.makeText(this, "Lokasi tersimpan", Toast.LENGTH_SHORT).show()
                        loadMarkers()
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            } ?: Toast.makeText(this, "Pilih lokasi dulu di peta", Toast.LENGTH_SHORT).show()
        }

        // tombol lihat lokasi tersimpan
        findViewById<Button>(R.id.btnViewSaved).setOnClickListener {
            startActivity(Intent(this, SavedLocationsActivity::class.java))
        }

        // tombol logout
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true

        // pilih lokasi baru dengan long click
        map.setOnMapLongClickListener { latLng ->
            selectedLatLng = latLng
            map.addMarker(MarkerOptions().position(latLng).title("Lokasi dipilih"))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }

        // tampilkan lokasi tersimpan
        loadMarkers()
    }

    private fun loadMarkers() {
        map.clear()
        val locations = db.getLocationsByUser(auth.currentUser!!.uid)
        for (loc in locations) {
            map.addMarker(
                MarkerOptions().position(LatLng(loc.lat, loc.lng)).title(loc.name)
            )
        }
    }
}