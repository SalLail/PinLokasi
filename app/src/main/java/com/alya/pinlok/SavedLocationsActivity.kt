package com.alya.pinlok

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class SavedLocationsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: LocationAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_locations)

        auth = FirebaseAuth.getInstance()
        db = DatabaseHelper(this)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val locations = db.getLocationsByUser(userId)
            adapter = LocationAdapter(locations) { loc ->
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("lat", loc.lat)
                intent.putExtra("lng", loc.lng)
                intent.putExtra("name", loc.name)
                intent.putExtra("note", loc.note)
                startActivity(intent)
            }
            recyclerView.adapter = adapter
        }
    }
}