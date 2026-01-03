package com.alya.pinlok

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var db: DatabaseHelper
    private lateinit var auth: FirebaseAuth
    private var selectedLatLng: LatLng? = null

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

        // tombol simpan lokasi
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            selectedLatLng?.let { latLng ->
                val view = layoutInflater.inflate(R.layout.dialog_add_location, null)
                val etName = view.findViewById<EditText>(R.id.etName)
                val etNote = view.findViewById<EditText>(R.id.etNote)

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
                            latLng.longitude
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

        // cek intent dari SavedLocationsActivity
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lng = intent.getDoubleExtra("lng", 0.0)
        val name = intent.getStringExtra("name")
        val note = intent.getStringExtra("note")

        if (lat != 0.0 && lng != 0.0) {
            val lokasi = LatLng(lat, lng)
            selectedLatLng = lokasi
            map.addMarker(MarkerOptions().position(lokasi).title(name))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(lokasi, 15f))
            Toast.makeText(this, "Lokasi: $name\nCatatan: $note", Toast.LENGTH_LONG).show()
        }

        // pilih lokasi baru dengan long click
        map.setOnMapLongClickListener { latLng ->
            selectedLatLng = latLng
            map.addMarker(MarkerOptions().position(latLng).title("Lokasi dipilih"))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }

        // tampilkan lokasi tersimpan
        loadMarkers()

        // klik marker untuk edit/delete
        map.setOnMarkerClickListener { marker ->
            val tag = marker.tag
            if (tag is LocationModel) {
                showEditDialog(tag)
            }
            true
        }
    }

    private fun loadMarkers() {
        map.clear()
        val locations = db.getLocationsByUser(auth.currentUser!!.uid)
        for (loc in locations) {
            val marker = map.addMarker(
                MarkerOptions().position(LatLng(loc.lat, loc.lng)).title(loc.name)
            )
            marker?.tag = loc
        }
    }

    private fun showEditDialog(loc: LocationModel) {
        val view = layoutInflater.inflate(R.layout.dialog_add_location, null)
        val etName = view.findViewById<EditText>(R.id.etName)
        val etNote = view.findViewById<EditText>(R.id.etNote)

        etName.setText(loc.name)
        etNote.setText(loc.note)

        AlertDialog.Builder(this)
            .setTitle("Edit Lokasi")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                db.updateLocation(loc.id, etName.text.toString(), etNote.text.toString())
                loadMarkers()
            }
            .setNegativeButton("Delete") { _, _ ->
                db.deleteLocation(loc.id)
                loadMarkers()
            }
            .show()
    }
}