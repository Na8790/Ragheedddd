package com.example.data.repo

import com.example.data.db.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {

    // User operations
    fun getUserProfile(email: String): Flow<UserProfile?> = db.userDao().getUserProfile(email)
    suspend fun updateProfile(profile: UserProfile) = db.userDao().insertOrUpdateProfile(profile)
    suspend fun addPoints(email: String, points: Int) = db.userDao().addPoints(email, points)
    suspend fun addBalance(email: String, amount: Double) = db.userDao().addBalance(email, amount)

    // Experience operations
    val allExperiences: Flow<List<LocalExperience>> = db.experienceDao().getAllExperiences()
    suspend fun insertExperience(experience: LocalExperience) = db.experienceDao().insertExperience(experience)
    suspend fun deleteExperience(id: Int) = db.experienceDao().deleteExperience(id)

    // Guide operations
    val availableGuides: Flow<List<GuideProduct>> = db.guideDao().getAvailableGuides()

    // Hotel operations
    val allHotels: Flow<List<HotelProduct>> = db.hotelDao().getAllHotels()

    // Car operations
    val allCars: Flow<List<CarProduct>> = db.carDao().getAllCars()

    // Booking operations
    val allBookings: Flow<List<Booking>> = db.bookingDao().getAllBookings()

    suspend fun bookProduct(
        email: String,
        productType: String,
        productId: Int,
        productName: String,
        arProductName: String,
        bookingDate: String,
        slotsOrDays: Int,
        pricePerUnit: Double
    ): Boolean {
        val totalCost = pricePerUnit * slotsOrDays
        // Retrieve current profile balance
        var success = false
        val booking = Booking(
            productType = productType,
            productId = productId,
            productName = productName,
            arProductName = arProductName,
            bookingDate = bookingDate,
            slotsOrDays = slotsOrDays,
            totalPaidYER = totalCost,
            status = "نشط"
        )
        db.userDao().deductBalance(email, totalCost)
        db.userDao().addPoints(email, (totalCost / 1000).toInt()) // 1 point per 1000 YER spent
        db.bookingDao().createBooking(booking)
        return true
    }

    suspend fun cancelBooking(bookingId: Int, refundAmount: Double, email: String) {
        db.bookingDao().updateBookingStatus(bookingId, "ملغي")
        db.userDao().addBalance(email, refundAmount)
    }

    // Saved trip plans operations
    val allSavedTripPlans: Flow<List<SavedTripPlan>> = db.tripPlanDao().getAllTripPlans()
    suspend fun saveTripPlan(plan: SavedTripPlan) = db.tripPlanDao().saveTripPlan(plan)
    suspend fun deleteTripPlan(id: Int) = db.tripPlanDao().deleteTripPlan(id)
}
