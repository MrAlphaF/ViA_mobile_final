package com.janis_petrovs.financialapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val date: Long,
    val category: String = "Cits", // Pievienojam kategoriju (čeku lasītājam/filtram)

    // Jaunie lauki kartei (Double? nozīmē, ka tie var būt tukši/null)
    val latitude: Double? = null,
    val longitude: Double? = null
)