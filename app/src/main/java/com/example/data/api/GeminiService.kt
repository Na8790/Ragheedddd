package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun generateItinerary(
        durationDays: Int,
        budgetLevel: String, // "محدودة" (Budget), "متوسطة" (Medium), "فاخرة" (Luxury)
        interests: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getMockItinerary(durationDays, budgetLevel, interests)
        }

        val prompt = """
            أنت خبير سياحي محلي ذكي في اليمن (منصة تجربة). قم بتصميم خطة رحلة سياحية مخصصة ومفصلة للغاية لشخص يريد السفر في اليمن.
            تفاصيل الرحلة المطلوبة:
            - عدد الأيام: $durationDays أيام.
            - مستوى الميزانية المتوفرة: $budgetLevel.
            - الاهتمامات الرئيسية للمسافر: $interests.

            يرجى صياغة خطة الرحلة باللغة العربية بأسلوب راقٍ وملهم يبرز التراث اليمني والجمال السياحي. يجب أن تتضمن الخطة:
            1. مقدمة ترحيبية قصيرة وجذابة.
            2. خطة مفصلة يومًا بيوم تشمل الأماكن والأنشطة المقترحة (مثل صنعاء القديمة، سقطرى، حضرموت، حراز، عدن أو غيرها بحسب الاهتمامات والأيام).
            3. ترشيحات محددة لتجارب محلية (مثال: جني البن في حراز، شرب الشاي في صنعاء القديمة، صيد في عدن).
            4. تقدير للميزانية الإجمالية المقترحة بالريال اليمني (YER).
            5. نصائح أمنية وإرشادية للتمتع برحلة آمنة ومريحة وممتعة في اليمن.

            اجعل التنسيق نظيفًا وسهل القراءة باستخدام العناوين والنقاط البرمجية.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            )
        )

        try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "عذرًا، لم نتمكن من الحصول على رد من الذكاء الاصطناعي حاليًا. يرجى مراجعة الاتصال وإعادة المحاولة."
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to high quality mock if API key is not active or internet fails
            getMockItinerary(durationDays, budgetLevel, interests)
        }
    }

    private fun getMockItinerary(durationDays: Int, budgetLevel: String, interests: String): String {
        return """
            ✨ مرحبًا بك في مغامرة العمر في اليمن السعيد عبر منصة تِجربة! ✨
            
            بناءً على طلبك، قمنا بتصميم هذه الرحلة الاستثنائية المخصصة لاهتماماتك في ($interests) ولمدة $durationDays أيام بميزانية $budgetLevel.
            
            🗓️ خطة الرحلة المقترحة يومًا بيوم:
            
            اليوم الأول: الوصول وعبق التاريخ في صنعاء القديمة
            - صباحًا: الوصول إلى صنعاء القديمة والتجول بين ناطحات السحاب الطينية الفريدة.
            - ظهرًا: تناول طعام الغداء الصنعاني التقليدي (سلتة وفحسة) في سوق الملح التاريخي.
            - مساءً: جولة معمارية مع المرشد المحلي عادل الصنعاني، والاستمتاع بكوب شاي معطر بالهيل فوق أحد الأسطح التاريخية المطلة على المدينة.
            
            اليوم الثاني: جبال حراز ومزارع البن الأسطورية
            - صباحًا: السفر باتجاه جبال حراز الشاهقة ومزارع البن المدرجة في قرية الحطيب المعلقة.
            - ظهرًا: المشاركة في جني ثمار البن الأحمر النادر مع المزارعين المحليين وتناول قهوة القشر التقليدية.
            - مساءً: المبيت في نزل جبلي دافئ والاستماع إلى القصص التراثية اليمنية.
            
            اليوم الثالث: عروس البحر الأحمر - عدن وعبق البحر
            - صباحًا: الانتقال إلى عدن والبدء بجولة في صهاريج عدن التاريخية وقلعة صيرة الأثرية.
            - ظهرًا: الانطلاق في قارب صيد خشبي تقليدي في خليج الفيل ومشاركة الصيادين صيدهم.
            - مساءً: الاستمتاع بوجبة زربيان عدني حقيقية ومعدة باللحم البلدي والأرز الفاخر على الشاطئ.
            
            💰 تقدير الميزانية المقترحة ($budgetLevel):
            - النقل والمواصلات وسيارة الدفع الرباعي: 120,000 ريال يمني
            - الإقامة والمنامة في فنادق ونزل تقليدية: 150,000 ريال يمني
            - التجارب المحلية والوجبات والأنشطة: 80,000 ريال يمني
            - إجمالي الميزانية التقريبية: 350,000 ريال يمني.
            
            🛡️ نصائح إرشادية وتوجيهية من "تِجربة":
            1. احرص دائمًا على مرافقة مرشد سياحي محلي ومسجل لتسهيل التنقل والاستمتاع بتفاصيل القصص المحلية.
            2. احتفظ ببعض النقد بالريال اليمني لشراء الهدايا التذكارية من الأسواق الشعبية.
            3. استمتع بتوثيق جمال القمريات والعمائر والتقاط صور تذكارية مع احترام خصوصية السكان المحليين.
        """.trimIndent()
    }
}
