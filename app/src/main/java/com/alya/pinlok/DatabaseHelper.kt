package com.alya.pinlok

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "pinlokasi.db", null, 2) { // bump version to 2

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE lokasi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                userId TEXT,
                name TEXT,
                note TEXT,
                latitude REAL,
                longitude REAL,
                photo TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE lokasi ADD COLUMN photo TEXT")
        }
    }

    fun insertLocation(
        userId: String,
        name: String,
        note: String,
        lat: Double,
        lng: Double,
        photoPath: String
    ) {
        val values = ContentValues().apply {
            put("userId", userId)
            put("name", name)
            put("note", note)
            put("latitude", lat)
            put("longitude", lng)
            put("photo", photoPath)
        }
        writableDatabase.insert("lokasi", null, values)
    }

    fun getLocationsByUser(userId: String): List<LocationModel> {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM lokasi WHERE userId = ?", arrayOf(userId)
        )
        val list = mutableListOf<LocationModel>()
        while (cursor.moveToNext()) {
            list.add(
                LocationModel(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    note = cursor.getString(cursor.getColumnIndexOrThrow("note")),
                    lat = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                    lng = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
                    photo = cursor.getString(cursor.getColumnIndexOrThrow("photo")) ?: ""
                )
            )
        }
        cursor.close()
        return list
    }

    fun updateLocation(id: Int, name: String, note: String) {
        val values = ContentValues().apply {
            put("name", name)
            put("note", note)
        }
        writableDatabase.update("lokasi", values, "id = ?", arrayOf(id.toString()))
    }

    fun deleteLocation(id: Int) {
        writableDatabase.delete("lokasi", "id = ?", arrayOf(id.toString()))
    }
}