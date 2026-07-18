package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repo.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AiPlanningState {
    object Idle : AiPlanningState
    object Loading : AiPlanningState
    data class Success(val itinerary: String) : AiPlanningState
    data class Error(val message: String) : AiPlanningState
}

data class HostStats(
    val totalEarningsYER: Double = 380000.0,
    val commissionDeductedYER: Double = 38000.0, // 10%
    val activeExperiencesCount: Int = 3,
    val totalBookingsReceived: Int = 14
)

data class TajrubahUiState(
    val userProfile: UserProfile = UserProfile(),
    val experiences: List<LocalExperience> = emptyList(),
    val guides: List<GuideProduct> = emptyList(),
    val hotels: List<HotelProduct> = emptyList(),
    val cars: List<CarProduct> = emptyList(),
    val bookings: List<Booking> = emptyList(),
    val savedPlans: List<SavedTripPlan> = emptyList(),
    val hostStats: HostStats = HostStats()
)

class TajrubahViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TajrubahUiState())
    val uiState: StateFlow<TajrubahUiState> = _uiState.asStateFlow()

    private val _aiPlanningState = MutableStateFlow<AiPlanningState>(AiPlanningState.Idle)
    val aiPlanningState: StateFlow<AiPlanningState> = _aiPlanningState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            // Observe User profile (using our metadata email as key)
            repository.getUserProfile("njybalqadry32@gmail.com")
                .filterNotNull()
                .onEach { profile ->
                    _uiState.update { it.copy(userProfile = profile) }
                }
                .launchIn(viewModelScope)

            // Observe experiences
            repository.allExperiences
                .onEach { list ->
                    _uiState.update { it.copy(experiences = list) }
                }
                .launchIn(viewModelScope)

            // Observe guides
            repository.availableGuides
                .onEach { list ->
                    _uiState.update { it.copy(guides = list) }
                }
                .launchIn(viewModelScope)

            // Observe hotels
            repository.allHotels
                .onEach { list ->
                    _uiState.update { it.copy(hotels = list) }
                }
                .launchIn(viewModelScope)

            // Observe cars
            repository.allCars
                .onEach { list ->
                    _uiState.update { it.copy(cars = list) }
                }
                .launchIn(viewModelScope)

            // Observe bookings
            repository.allBookings
                .onEach { list ->
                    _uiState.update { it.copy(bookings = list) }
                }
                .launchIn(viewModelScope)

            // Observe saved plans
            repository.allSavedTripPlans
                .onEach { list ->
                    _uiState.update { it.copy(savedPlans = list) }
                }
                .launchIn(viewModelScope)
        }
    }

    // Book experience, hotel, guide, car
    fun purchaseProduct(
        productType: String, // "experience", "guide", "hotel", "car"
        productId: Int,
        productName: String,
        arProductName: String,
        bookingDate: String,
        slotsOrDays: Int,
        pricePerUnit: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val user = _uiState.value.userProfile
            val totalCost = pricePerUnit * slotsOrDays
            if (user.balanceYER < totalCost) {
                onError("عذرًا، رصيدك الحالي غير كافٍ لإجراء هذا الحجز. يرجى شحن الرصيد أولًا.")
                return@launch
            }

            try {
                repository.bookProduct(
                    email = user.email,
                    productType = productType,
                    productId = productId,
                    productName = productName,
                    arProductName = arProductName,
                    bookingDate = bookingDate,
                    slotsOrDays = slotsOrDays,
                    pricePerUnit = pricePerUnit
                )
                onSuccess()
            } catch (e: Exception) {
                onError("حدث خطأ أثناء الحجز: ${e.localizedMessage}")
            }
        }
    }

    // Cancel dynamic booking
    fun cancelExistingBooking(bookingId: Int, refundAmount: Double) {
        viewModelScope.launch {
            val user = _uiState.value.userProfile
            repository.cancelBooking(bookingId, refundAmount, user.email)
        }
    }

    // Add Balance / Top up
    fun topUpBalance(amount: Double) {
        viewModelScope.launch {
            val user = _uiState.value.userProfile
            repository.addBalance(user.email, amount)
            // also reward some points for loyalty topup
            repository.addPoints(user.email, (amount / 5000).toInt())
        }
    }

    // AI Itinerary Plan Generator
    fun generateAiItinerary(
        durationDays: Int,
        budgetLevel: String,
        interests: String,
        onComplete: () -> Unit = {}
    ) {
        _aiPlanningState.value = AiPlanningState.Loading
        viewModelScope.launch {
            try {
                val itinerary = GeminiApiClient.generateItinerary(durationDays, budgetLevel, interests)
                _aiPlanningState.value = AiPlanningState.Success(itinerary)
                
                // Automatically save generated trip plan to Database!
                val planTitle = "رحلة $interests في اليمن - $durationDays أيام"
                repository.saveTripPlan(
                    SavedTripPlan(
                        title = planTitle,
                        budgetLevel = budgetLevel,
                        interests = interests,
                        durationDays = durationDays,
                        itineraryText = itinerary
                    )
                )
                onComplete()
            } catch (e: Exception) {
                _aiPlanningState.value = AiPlanningState.Error("فشل في توليد الخطة الذكية: ${e.localizedMessage}")
            }
        }
    }

    fun resetAiPlanningState() {
        _aiPlanningState.value = AiPlanningState.Idle
    }

    // Host - Add custom experience
    fun addNewExperienceByHost(
        title: String,
        arTitle: String,
        description: String,
        arDescription: String,
        location: String,
        arLocation: String,
        priceYER: Double,
        category: String,
        duration: String
    ) {
        viewModelScope.launch {
            val newExp = LocalExperience(
                title = title,
                arTitle = arTitle,
                description = description,
                arDescription = arDescription,
                hostName = _uiState.value.userProfile.name,
                hostArName = _uiState.value.userProfile.name,
                location = location,
                arLocation = arLocation,
                priceYER = priceYER,
                rating = 5.0, // newly added starts with 5.0!
                imageUrl = when(category) {
                    "Nature" -> "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=600&auto=format&fit=crop"
                    "Crafts" -> "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?q=80&w=600&auto=format&fit=crop"
                    "Food" -> "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?q=80&w=600&auto=format&fit=crop"
                    else -> "https://images.unsplash.com/photo-1541432901042-2d8bd64b4a9b?q=80&w=600&auto=format&fit=crop"
                },
                category = category,
                duration = duration,
                slotsAvailable = 10
            )
            repository.insertExperience(newExp)
            
            // update host statistics
            _uiState.update {
                it.copy(
                    hostStats = it.hostStats.copy(
                        activeExperiencesCount = it.hostStats.activeExperiencesCount + 1
                    )
                )
            }
        }
    }

    // Toggle User role (Traveler <-> Host)
    fun toggleUserRole() {
        viewModelScope.launch {
            val user = _uiState.value.userProfile
            val newRole = if (user.role == "Traveler") "Host" else "Traveler"
            repository.updateProfile(user.copy(role = newRole))
        }
    }
}

class TajrubahViewModelFactory(
    private val application: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TajrubahViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TajrubahViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
