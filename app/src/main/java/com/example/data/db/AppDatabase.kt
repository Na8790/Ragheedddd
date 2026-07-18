package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE email = :email LIMIT 1")
    fun getUserProfile(email: String): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET points = points + :addPoints WHERE email = :email")
    suspend fun addPoints(email: String, addPoints: Int)

    @Query("UPDATE user_profile SET balanceYER = balanceYER - :amount WHERE email = :email")
    suspend fun deductBalance(email: String, amount: Double)

    @Query("UPDATE user_profile SET balanceYER = balanceYER + :amount WHERE email = :email")
    suspend fun addBalance(email: String, amount: Double)
}

@Dao
interface ExperienceDao {
    @Query("SELECT * FROM local_experiences ORDER BY rating DESC")
    fun getAllExperiences(): Flow<List<LocalExperience>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperiences(experiences: List<LocalExperience>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperience(experience: LocalExperience)

    @Query("DELETE FROM local_experiences WHERE id = :id")
    suspend fun deleteExperience(id: Int)
}

@Dao
interface GuideDao {
    @Query("SELECT * FROM guides WHERE available = 1")
    fun getAvailableGuides(): Flow<List<GuideProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuides(guides: List<GuideProduct>)
}

@Dao
interface HotelDao {
    @Query("SELECT * FROM hotels ORDER BY rating DESC")
    fun getAllHotels(): Flow<List<HotelProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHotels(hotels: List<HotelProduct>)
}

@Dao
interface CarDao {
    @Query("SELECT * FROM cars")
    fun getAllCars(): Flow<List<CarProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCars(cars: List<CarProduct>)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createBooking(booking: Booking)

    @Query("UPDATE bookings SET status = :status WHERE id = :bookingId")
    suspend fun updateBookingStatus(bookingId: Int, status: String)
}

@Dao
interface TripPlanDao {
    @Query("SELECT * FROM saved_trip_plans ORDER BY timestamp DESC")
    fun getAllTripPlans(): Flow<List<SavedTripPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTripPlan(plan: SavedTripPlan)

    @Query("DELETE FROM saved_trip_plans WHERE id = :id")
    suspend fun deleteTripPlan(id: Int)
}

@Database(
    entities = [
        UserProfile::class,
        LocalExperience::class,
        GuideProduct::class,
        HotelProduct::class,
        CarProduct::class,
        Booking::class,
        SavedTripPlan::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun experienceDao(): ExperienceDao
    abstract fun guideDao(): GuideDao
    abstract fun hotelDao(): HotelDao
    abstract fun carDao(): CarDao
    abstract fun bookingDao(): BookingDao
    abstract fun tripPlanDao(): TripPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tajrubah_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDefaultData(database)
                }
            }
        }

        private suspend fun populateDefaultData(db: AppDatabase) {
            // 1. Default User Profile
            db.userDao().insertOrUpdateProfile(
                UserProfile(
                    email = "njybalqadry32@gmail.com", // From metadata
                    name = "المهندسة رغد",
                    role = "Traveler",
                    points = 250,
                    balanceYER = 850000.0,
                    avatarUrl = "avatar_yemen",
                    achievementsJson = "[\"رحّالة متمرس\", \"مستكشف صنعاء\", \"حامي التراث\"]",
                    referralCode = "RAGHAD-TAJ"
                )
            )

            // 2. Default Local Experiences (Yemen)
            db.experienceDao().insertExperiences(
                listOf(
                    LocalExperience(
                        title = "UNESCO Old Sana'a Architectural Tour & Rooftop Tea",
                        arTitle = "جولة معمارية في صنعاء القديمة وشاي فوق السطوح",
                        description = "Walk through the thousand-year-old clay and gingerbread buildings of UNESCO World Heritage Old Sana'a, learn about the gypsum arches, and enjoy Yemeni tea with cardamoms on a historic skyscraper rooftop overlooking the majestic city skyline.",
                        arDescription = "تجوّل بين المباني الطينية التاريخية المزينة بالقمريات الجصية البيضاء في صنعاء القديمة المدرجة ضمن التراث العالمي، وتعرف على الفن المعماري الفريد، واستمتع بشرب الشاي الصنعاني المخدر بالهيل فوق أسطح ناطحات السحاب القديمة المطلة على المدينة.",
                        hostName = "Ahmed Al-Hamdani",
                        hostArName = "أحمد الهمداني",
                        location = "Old Sana'a",
                        arLocation = "صنعاء القديمة",
                        priceYER = 15000.0,
                        rating = 4.9,
                        imageUrl = "https://images.unsplash.com/photo-1541432901042-2d8bd64b4a9b?q=80&w=600&auto=format&fit=crop", // Sana'a lookalike or generic historic yemen architecture
                        category = "Cultural",
                        duration = "4 Hours"
                    ),
                    LocalExperience(
                        title = "Dragon's Blood Forest Trekking & Local Honey Tasting",
                        arTitle = "مسار غابة دم الأخوين وتذوق العسل السقطري",
                        description = "Embark on an eco-hiking adventure through the prehistoric Dragon's Blood Tree forest in the Dixam Plateau, meet local Bedouin herders, and taste raw, authentic Socotran honey straight from the hives.",
                        arDescription = "انطلق في مغامرة هايكنج بيئية فريدة بين أشجار دم الأخوين الأسطورية في هضبة دكسم بسقطرى، والتقِ بالرعاة البدو المحليين، وتذوق العسل السقطري النادر المستخلص مباشرة من خلايا النحل الجبلية.",
                        hostName = "Salem Socotri",
                        hostArName = "سالم السقطري",
                        location = "Dixam Plateau, Socotra",
                        arLocation = "هضبة دكسم، سقطرى",
                        priceYER = 35000.0,
                        rating = 5.0,
                        imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=600&auto=format&fit=crop", // Island / Nature
                        category = "Nature",
                        duration = "1 Day"
                    ),
                    LocalExperience(
                        title = "Traditional Gingerbread Brick Making Workshop",
                        arTitle = "ورشة صناعة الطوب الطيني والنقوش الجصية",
                        description = "Get your hands dirty with the master artisans of Shibam. Learn the ancient technique of mixing mud with straw, baking bricks in solar kilns, and carving traditional geometric plaster decorations.",
                        arDescription = "اصنع بيدك الطوب الطيني التقليدي مع كبار الحرفيين في شبام حضرموت. تعلم تقنيات خلط الطين بالقش وحرقه بالطرق التقليدية، ونقش الزخارف الجصية الهندسية الأصيلة.",
                        hostName = "Yasser Ba-Zuhair",
                        hostArName = "ياسر بازهير",
                        location = "Shibam, Hadramout",
                        arLocation = "شبام، حضرموت",
                        priceYER = 20000.0,
                        rating = 4.8,
                        imageUrl = "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?q=80&w=600&auto=format&fit=crop", // Crafts
                        category = "Crafts",
                        duration = "3 Hours"
                    ),
                    LocalExperience(
                        title = "High-Altitude Coffee Harvesting in Haraz Mountains",
                        arTitle = "جني وحصاد البن في جبال حراز الشاهقة",
                        description = "Visit a steep terraced organic coffee farm in Al-Hutayb village. Help pick red coffee cherries, learn about the centuries-old sun-drying process, and brew fresh Yemeni Qishr coffee with farm owners.",
                        arDescription = "قم بزيارة مزارع البن العضوي المعلقة في جبال حراز بقرية الحطيب. شارك المزارعين في قطف ثمار البن الحمراء النادجة، وتعرف على تجفيفها بالشمس، وتذوق قهوة القشر اليمنية الطازجة.",
                        hostName = "Yahya Al-Harazi",
                        hostArName = "يحيى الحرازي",
                        location = "Haraz Mountains",
                        arLocation = "جبال حراز",
                        priceYER = 18000.0,
                        rating = 4.9,
                        imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?q=80&w=600&auto=format&fit=crop", // Coffee / Greenery
                        category = "Food",
                        duration = "6 Hours"
                    ),
                    LocalExperience(
                        title = "Adeni Fishermen Red Sea Catch & Beach Zurbian Feast",
                        arTitle = "رحلة الصيد مع صيادي عدن ووليمة زربيان على الشاطئ",
                        description = "Sail on a traditional wooden boat with local fishermen in Aden's Elephant Bay, pull up nets from the Red Sea, and feast on a freshly cooked Adeni Zurbian rice meal with your catch right on the golden sand.",
                        arDescription = "أبحر في قارب خشبي تقليدي مع صيادين محليين في خليج الفيل بعدن، وشارك في صيد السمك الطازج من البحر الأحمر، واستمتع بتناول وجبة زربيان عدني مطبوخة على الحطب مباشرة على الشاطئ.",
                        hostName = "Faris Al-Adeni",
                        hostArName = "فارس العدني",
                        location = "Aden Coast",
                        arLocation = "سواحل عدن",
                        priceYER = 25000.0,
                        rating = 4.7,
                        imageUrl = "https://images.unsplash.com/photo-1544551763-46a013bb70d5?q=80&w=600&auto=format&fit=crop", // Fishermen / Coast
                        category = "Food",
                        duration = "5 Hours"
                    )
                )
            )

            // 3. Default Guides
            db.guideDao().insertGuides(
                listOf(
                    GuideProduct(
                        name = "Adel Al-Sana'ani",
                        arName = "عادل الصنعاني",
                        bio = "Professional guide licensed by Ministry of Tourism. Specialist in historic architecture and Islamic history of Yemen.",
                        arBio = "مرشد سياحي مرخص من وزارة السياحة. متخصص في العمارة التاريخية والتاريخ الإسلامي في اليمن وصنعاء القديمة.",
                        location = "Sana'a & Haraz",
                        arLocation = "صنعاء وحراز",
                        languages = "Arabic, English, French",
                        rating = 4.9,
                        pricePerDayYER = 25000.0,
                        imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=400&auto=format&fit=crop"
                    ),
                    GuideProduct(
                        name = "Mazin Socotri",
                        arName = "مازن السقطري",
                        bio = "Native Socotri wilderness explorer. Over 10 years of experience guiding camp treks through Socotra's lagoons and canyons.",
                        arBio = "ابن سقطرى ومستكشف طبيعتها. أكثر من 10 سنوات في قيادة رحلات التخييم والاستكشاف في الكهوف والأودية السقطرية.",
                        location = "Socotra",
                        arLocation = "سقطرى",
                        languages = "Arabic, English, Socotri",
                        rating = 5.0,
                        pricePerDayYER = 35000.0,
                        imageUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=400&auto=format&fit=crop"
                    )
                )
            )

            // 4. Default Hotels
            db.hotelDao().insertHotels(
                listOf(
                    HotelProduct(
                        name = "Taj Sheba Hotel",
                        arName = "فندق تاج سبأ",
                        location = "Sana'a",
                        arLocation = "صنعاء",
                        description = "Combining modern luxury with traditional Yemeni royal architecture in the heart of the capital.",
                        arDescription = "يجمع بين الفخامة المعاصرة والطراز المعماري اليمني الملكي التقليدي في قلب العاصمة صنعاء.",
                        pricePerNightYER = 65000.0,
                        rating = 4.6,
                        imageUrl = "https://images.unsplash.com/photo-1566073771259-6a8506099945?q=80&w=600&auto=format&fit=crop"
                    ),
                    HotelProduct(
                        name = "Socotra Eco-Lodge",
                        arName = "نُزل سقطرى البيئي",
                        location = "Hadiboh, Socotra",
                        arLocation = "حديبو، سقطرى",
                        description = "Eco-friendly stone bungalows designed to blend with nature, using solar energy and traditional materials.",
                        arDescription = "أكواخ حجرية صديقة للبيئة مصممة للاندماج مع الطبيعة الساحرة، تعتمد بالكامل على الطاقة الشمسية والمواد المحلية.",
                        pricePerNightYER = 40000.0,
                        rating = 4.8,
                        imageUrl = "https://images.unsplash.com/photo-1439066615861-d1af74d74000?q=80&w=600&auto=format&fit=crop"
                    )
                )
            )

            // 5. Default Cars
            db.carDao().insertCars(
                listOf(
                    CarProduct(
                        model = "Land Cruiser 4WD",
                        brand = "Toyota",
                        pricePerDayYER = 45000.0,
                        rating = 4.9,
                        imageUrl = "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?q=80&w=600&auto=format&fit=crop",
                        withDriver = true,
                        location = "Sana'a / Aden / Socotra"
                    ),
                    CarProduct(
                        model = "Hilux Double Cabin 4WD",
                        brand = "Toyota",
                        pricePerDayYER = 35000.0,
                        rating = 4.7,
                        imageUrl = "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?q=80&w=600&auto=format&fit=crop",
                        withDriver = true,
                        location = "Aden / Mukalla"
                    )
                )
            )
        }
    }
}
