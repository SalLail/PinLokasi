package com.alya.pinlok

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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

    private fun showEditDialog(loc: LocationModel) {
        val view = layoutInflater.inflate(R.layout.dialog_edit_location, null)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        auth = FirebaseAuth.getInstance()
        db = DatabaseHelper(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            selectedLatLng?.let {
                db.insertLocation(auth.currentUser!!.uid,
                    "Lokasi", "Catatan",
                    it.latitude, it.longitude)
                loadMarkers()
            }
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.setOnMapLongClickListener {
            selectedLatLng = it
            map.clear()
            map.addMarker(MarkerOptions().position(it))
        }
        loadMarkers()

        map.setOnMarkerClickListener {
            showEditDialog(it.tag as LocationModel)
            true
        }
    }

    private fun loadMarkers() {
        map.clear()
        val cursor = db.getLocationsByUser(auth.currentUser!!.uid)
        while (cursor.moveToNext()) {
            val marker = map.addMarker(
                MarkerOptions().position(
                    LatLng(cursor.getDouble(4), cursor.getDouble(5))
                ).title(cursor.getString(2))
            )
            marker?.tag = LocationModel(
                cursor.getInt(0),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getDouble(4),
                cursor.getDouble(5)
            )
        }
        cursor.close()
    }
}
