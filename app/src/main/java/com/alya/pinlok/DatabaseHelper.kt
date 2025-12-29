package com.alya.pinlok

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "pinlokasi.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE lokasi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                userId TEXT,
                name TEXT,
                note TEXT,
                latitude REAL,
                longitude REAL
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS lokasi")
        onCreate(db)
    }

    fun insertLocation(userId: String, name: String, note: String, lat: Double, lng: Double) {
        val values = ContentValues().apply {
            put("userId", userId)
            put("name", name)
            put("note", note)
            put("latitude", lat)
            put("longitude", lng)
        }
        writableDatabase.insert("lokasi", null, values)
    }

    fun getLocationsByUser(userId: String): Cursor {
        return readableDatabase.rawQuery(
            "SELECT * FROM lokasi WHERE userId = ?", arrayOf(userId)
        )
    }

    fun updateLocation(id: Int, name: String, note: String) {
        val values = ContentValues().apply {
            put("name", name)
            put("note", note)
        }
        writableDatabase.update("lokasi", values, "id=?", arrayOf(id.toString()))
    }

    fun deleteLocation(id: Int) {
        writableDatabase.delete("lokasi", "id=?", arrayOf(id.toString()))
    }
}
