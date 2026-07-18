package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val email: String = "guest@tajrubah.com",
    val name: String = "زائر تجربة",
    val role: String = "Traveler", // "Traveler", "Host"
    val points: Int = 120,
    val balanceYER: Double = 150000.0,
    val avatarUrl: String = "avatar_yemen",
    val achievementsJson: String = "[\"مستكشف مبتدئ\", \"محب التراث\"]",
    val referralCode: String = "TAJ-YEM-2026"
) : Serializable

@Entity(tableName = "local_experiences")
data class LocalExperience(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val arTitle: String,
    val description: String,
    val arDescription: String,
    val hostName: String,
    val hostArName: String,
    val location: String,
    val arLocation: String,
    val priceYER: Double,
    val rating: Double,
    val imageUrl: String,
    val category: String, // "Cultural", "Nature", "Crafts", "Food"
    val duration: String,
    val slotsAvailable: Int = 5
) : Serializable

@Entity(tableName = "guides")
data class GuideProduct(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val arName: String,
    val bio: String,
    val arBio: String,
    val location: String,
    val arLocation: String,
    val languages: String,
    val rating: Double,
    val pricePerDayYER: Double,
    val imageUrl: String,
    val available: Boolean = true
) : Serializable

@Entity(tableName = "hotels")
data class HotelProduct(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val arName: String,
    val location: String,
    val arLocation: String,
    val description: String,
    val arDescription: String,
    val pricePerNightYER: Double,
    val rating: Double,
    val imageUrl: String
) : Serializable

@Entity(tableName = "cars")
data class CarProduct(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val model: String,
    val brand: String,
    val pricePerDayYER: Double,
    val rating: Double,
    val imageUrl: String,
    val withDriver: Boolean = true,
    val location: String = "Sana'a"
) : Serializable

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productType: String, // "experience", "guide", "hotel", "car"
    val productId: Int,
    val productName: String,
    val arProductName: String,
    val bookingDate: String,
    val slotsOrDays: Int,
    val totalPaidYER: Double,
    val status: String = "نشط", // "نشط" (Active), "مكتمل" (Completed), "ملغي" (Cancelled)
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "saved_trip_plans")
data class SavedTripPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val budgetLevel: String, // "محدودة", "متوسطة", "فاخرة"
    val interests: String,
    val durationDays: Int,
    val itineraryText: String, // Markdown or plain text of the plan
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
