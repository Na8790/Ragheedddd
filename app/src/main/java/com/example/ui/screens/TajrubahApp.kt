package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.viewmodel.AiPlanningState
import com.example.ui.viewmodel.TajrubahViewModel
import java.text.NumberFormat
import java.util.Locale

// Navigation screens
sealed class Screen(val route: String, val titleAr: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Discover : Screen("discover", "الاستكشاف", Icons.Default.Explore)
    object AIPlanner : Screen("ai_planner", "المخطط الذكي", Icons.Default.AutoAwesome)
    object Services : Screen("services", "الخدمات", Icons.Default.RoomService)
    object Bookings : Screen("bookings", "حجوزاتي", Icons.Default.ConfirmationNumber)
    object Profile : Screen("profile", "الحساب والمضيف", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TajrubahApp(viewModel: TajrubahViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            TajrubahBottomNavigation(navController = navController)
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Discover.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Discover.route) {
                DiscoverScreen(viewModel = viewModel)
            }
            composable(Screen.AIPlanner.route) {
                AIPlannerScreen(viewModel = viewModel)
            }
            composable(Screen.Services.route) {
                ServicesScreen(viewModel = viewModel)
            }
            composable(Screen.Bookings.route) {
                BookingsScreen(viewModel = viewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TajrubahBottomNavigation(navController: NavController) {
    val items = listOf(
        Screen.Discover,
        Screen.AIPlanner,
        Screen.Services,
        Screen.Bookings,
        Screen.Profile
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.titleAr) },
                label = { 
                    Text(
                        text = screen.titleAr,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                modifier = Modifier.testTag("nav_btn_${screen.route}")
            )
        }
    }
}

// Utility formatting YER
fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("ar", "YE"))
    format.maximumFractionDigits = 0
    return format.format(amount).replace("YER", "ر.ي").replace("ر.ي.", "ر.ي")
}

// --- DISCOVER SCREEN ---
@Composable
fun DiscoverScreen(viewModel: TajrubahViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedExperienceForBooking by remember { mutableStateOf<LocalExperience?>(null) }
    
    val categories = listOf(
        Pair("All", "الكل"),
        Pair("Cultural", "ثقافي"),
        Pair("Nature", "طبيعة"),
        Pair("Crafts", "حرف يدوية"),
        Pair("Food", "مأكولات")
    )

    val filteredExperiences = uiState.experiences.filter { exp ->
        (selectedCategory == "All" || exp.category == selectedCategory) &&
        (exp.arTitle.contains(searchQuery, ignoreCase = true) || 
         exp.arLocation.contains(searchQuery, ignoreCase = true) ||
         exp.title.contains(searchQuery, ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Dark elegant gradient with traditional amber light elements
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )
                    
                    // Arabic Typography and Calligraphy look
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "منصة تِجربة المحلية",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Right
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "اكتشف أصالة اليمن السعيد واحجز تجارب فريدة يعيشها أهل البلد",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن تجربة، مدينة أو نشاط...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            )
        }

        // Category Row Selector
        item {
            Text(
                text = "الفئات التراثية",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 4.dp),
                textAlign = TextAlign.Right
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat.first,
                        onClick = { selectedCategory = cat.first },
                        label = { Text(cat.second, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("chip_${cat.first}")
                    )
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "التجارب الأكثر طلبًا",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${filteredExperiences.size} تجربة متاحة",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // Experiences Cards List
        if (filteredExperiences.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = "لا يوجد",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "لا توجد تجارب تطابق بحثك حاليًا في اليمن",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(filteredExperiences) { exp ->
                ExperienceCard(experience = exp, onBookClick = {
                    selectedExperienceForBooking = exp
                })
            }
        }
    }

    // Detail Booking Dialog
    selectedExperienceForBooking?.let { exp ->
        BookingSheetDialog(
            experience = exp,
            userProfile = uiState.userProfile,
            onDismiss = { selectedExperienceForBooking = null },
            onConfirmBooking = { slots, date ->
                viewModel.purchaseProduct(
                    productType = "experience",
                    productId = exp.id,
                    productName = exp.title,
                    arProductName = exp.arTitle,
                    bookingDate = date,
                    slotsOrDays = slots,
                    pricePerUnit = exp.priceYER,
                    onSuccess = {
                        selectedExperienceForBooking = null
                        Toast.makeText(context, "تم الحجز بنجاح! شكراً لاكتشافك اليمن مع تجربة.", Toast.LENGTH_LONG).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

@Composable
fun ExperienceCard(experience: LocalExperience, onBookClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("experience_card_${experience.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // High-quality image from Coil
                AsyncImage(
                    model = experience.imageUrl,
                    contentDescription = experience.arTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    // If image fails, show local gorgeous gradient
                    error = painterResource(android.R.drawable.ic_menu_gallery)
                )
                
                // Fallback elegant overlay if image fails or for styling
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )

                // Category badge overlay
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = when(experience.category) {
                            "Cultural" -> "ثقافي"
                            "Nature" -> "طبيعة"
                            "Crafts" -> "حرفية"
                            "Food" -> "مأكولات"
                            else -> experience.category
                        },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Rating overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = "تقييم", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(experience.rating.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = experience.arLocation,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = experience.arTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = experience.arDescription,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("السعر للتجربة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(
                            text = formatCurrency(experience.priceYER),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = onBookClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.testTag("book_btn_${experience.id}")
                    ) {
                        Text("تفاصيل وحجز", color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BookingSheetDialog(
    experience: LocalExperience,
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onConfirmBooking: (Int, String) -> Unit
) {
    var slots by remember { mutableStateOf(1) }
    var selectedDate by remember { mutableStateOf("2026-07-25") }
    val totalCost = experience.priceYER * slots

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("booking_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "تفاصيل الحجز والتجربة",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(experience.arTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(experience.arDescription, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = "الوقت", tint = MaterialTheme.colorScheme.primary)
                    Text("المدة: ${experience.duration}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = "المضيف", tint = MaterialTheme.colorScheme.primary)
                    Text("المضيف المحلي: ${experience.hostArName}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Divider()

                // Date selector
                Text("اختر تاريخ التجربة:", fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val dates = listOf("2026-07-25", "2026-07-26", "2026-07-27")
                    dates.forEach { dt ->
                        OutlinedButton(
                            onClick = { selectedDate = dt },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedDate == dt) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
                            ),
                            border = BorderStroke(1.dp, if (selectedDate == dt) MaterialTheme.colorScheme.primary else Color.Gray)
                        ) {
                            Text(dt)
                        }
                    }
                }

                // Slots count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("عدد الأفراد / التذاكر:", fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (slots > 1) slots-- }) {
                            Icon(Icons.Default.Remove, contentDescription = "تقليل")
                        }
                        Text(slots.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { if (slots < experience.slotsAvailable) slots++ }) {
                            Icon(Icons.Default.Add, contentDescription = "زيادة")
                        }
                    }
                }

                Divider()

                // Total and Wallet Check
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("رصيدك الحالي:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(formatCurrency(userProfile.balanceYER), fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("التكلفة الإجمالية:", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(formatCurrency(totalCost), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = { onConfirmBooking(slots, selectedDate) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_booking_btn")
                    ) {
                        Text("تأكيد وحجز YER", color = Color.White)
                    }
                }
            }
        }
    }
}


// --- AI TRAVEL PLANNER SCREEN ---
@Composable
fun AIPlannerScreen(viewModel: TajrubahViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val aiState by viewModel.aiPlanningState.collectAsState()
    val context = LocalContext.current

    var durationDays by remember { mutableStateOf(3) }
    var budgetLevel by remember { mutableStateOf("متوسطة") }
    var interests by remember { mutableStateOf("") }

    val budgets = listOf("محدودة", "متوسطة", "فاخرة")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "ذكاء اصطناعي",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("مخطط الرحلات الذكي", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "صمّم رحلتك اليمنية المثالية فورياً بالاعتماد على ميزانيتك واهتماماتك الخاصة.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Form fields Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Duration selection
                    Text("مدة الرحلة بالأيام: $durationDays أيام", fontWeight = FontWeight.Bold)
                    Slider(
                        value = durationDays.toFloat(),
                        onValueChange = { durationDays = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Budget level selection
                    Text("مستوى الميزانية:", fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        budgets.forEach { b ->
                            OutlinedButton(
                                onClick = { budgetLevel = b },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (budgetLevel == b) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
                                ),
                                border = BorderStroke(1.dp, if (budgetLevel == b) MaterialTheme.colorScheme.primary else Color.Gray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(b, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Interests
                    Text("الاهتمامات والتطلعات للرحلة:", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = interests,
                        onValueChange = { interests = it },
                        placeholder = { Text("مثال: القهوة، الآثار، الشواطئ، المغامرات الطبيعية، التسوق التاريخي") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_interests_field"),
                        shape = RoundedCornerShape(8.dp),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Generate Button
                    Button(
                        onClick = {
                            if (interests.isEmpty()) {
                                Toast.makeText(context, "الرجاء كتابة اهتماماتك لنصنع خطة ملائمة لك!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.generateAiItinerary(durationDays, budgetLevel, interests)
                            }
                        },
                        enabled = aiState !is AiPlanningState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("generate_itinerary_btn")
                    ) {
                        if (aiState is AiPlanningState.Loading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(24.dp))
                        } else {
                            Text("صمّم خطة رحلتي بالذكاء الاصطناعي ✨", color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Generated Plan Display Section
        item {
            when (val state = aiState) {
                is AiPlanningState.Loading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "جاري تجميع عبق التاريخ وتصميم رحلتك اليمنية السعيدة...",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                is AiPlanningState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "✨ خطتك المصممة خصيصاً",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(onClick = { viewModel.resetAiPlanningState() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "إعادة التوليد")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.itinerary,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }
                is AiPlanningState.Error -> {
                    Text("خطأ: ${state.message}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
                else -> {
                    // Idle state - show nothing or tip
                }
            }
        }

        // History of Generated/Saved Trips
        if (uiState.savedPlans.isNotEmpty()) {
            item {
                Text(
                    text = "رحلاتك السابقة المحفوظة",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(uiState.savedPlans) { plan ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    var expandedPlan by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(plan.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("الميزانية: ${plan.budgetLevel} | المدة: ${plan.durationDays} أيام", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            IconButton(onClick = { expandedPlan = !expandedPlan }) {
                                Icon(
                                    if (expandedPlan) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "عرض"
                                )
                            }
                        }

                        if (expandedPlan) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(plan.itineraryText, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}


// --- SERVICES TAB (GUIDES, HOTELS, CARS) ---
@Composable
fun ServicesScreen(viewModel: TajrubahViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0: Guides, 1: Hotels, 2: Cars

    val tabs = listOf("مرشدين سياحيين 👤", "فنادق ونُزل 🏨", "تأجير سيارات 🚗")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        // Tab Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (activeTab) {
                0 -> {
                    // Guides
                    if (uiState.guides.isEmpty()) {
                        item { Text("لا يوجد مرشدين متاحين حالياً", modifier = Modifier.padding(16.dp)) }
                    } else {
                        items(uiState.guides) { guide ->
                            GuideItemCard(guide = guide, userProfile = uiState.userProfile, onBookClick = {
                                viewModel.purchaseProduct(
                                    productType = "guide",
                                    productId = guide.id,
                                    productName = guide.name,
                                    arProductName = guide.arName,
                                    bookingDate = "2026-07-28",
                                    slotsOrDays = 1,
                                    pricePerUnit = guide.pricePerDayYER,
                                    onSuccess = {
                                        Toast.makeText(context, "تم حجز المرشد ${guide.arName} بنجاح!", Toast.LENGTH_LONG).show()
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            })
                        }
                    }
                }
                1 -> {
                    // Hotels
                    if (uiState.hotels.isEmpty()) {
                        item { Text("لا توجد فنادق متاحة حالياً", modifier = Modifier.padding(16.dp)) }
                    } else {
                        items(uiState.hotels) { hotel ->
                            HotelItemCard(hotel = hotel, userProfile = uiState.userProfile, onBookClick = {
                                viewModel.purchaseProduct(
                                    productType = "hotel",
                                    productId = hotel.id,
                                    productName = hotel.name,
                                    arProductName = hotel.arName,
                                    bookingDate = "2026-07-28",
                                    slotsOrDays = 1,
                                    pricePerUnit = hotel.pricePerNightYER,
                                    onSuccess = {
                                        Toast.makeText(context, "تم حجز غرفة في ${hotel.arName} بنجاح!", Toast.LENGTH_LONG).show()
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            })
                        }
                    }
                }
                2 -> {
                    // Cars
                    if (uiState.cars.isEmpty()) {
                        item { Text("لا توجد مركبات متاحة حالياً", modifier = Modifier.padding(16.dp)) }
                    } else {
                        items(uiState.cars) { car ->
                            CarItemCard(car = car, userProfile = uiState.userProfile, onBookClick = {
                                viewModel.purchaseProduct(
                                    productType = "car",
                                    productId = car.id,
                                    productName = car.model,
                                    arProductName = "${car.brand} ${car.model}",
                                    bookingDate = "2026-07-28",
                                    slotsOrDays = 1,
                                    pricePerUnit = car.pricePerDayYER,
                                    onSuccess = {
                                        Toast.makeText(context, "تم حجز السيارة ${car.brand} بنجاح!", Toast.LENGTH_LONG).show()
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuideItemCard(guide: GuideProduct, userProfile: UserProfile, onBookClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(
                model = guide.imageUrl,
                contentDescription = guide.arName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(android.R.drawable.ic_menu_my_calendar)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(guide.arName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("الموقع: ${guide.arLocation}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(guide.arBio, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 2)
                Spacer(modifier = Modifier.height(4.dp))
                Text("اللغات: ${guide.languages}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(formatCurrency(guide.pricePerDayYER) + "/يوم", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    Button(
                        onClick = onBookClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("طلب إرشاد", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HotelItemCard(hotel: HotelProduct, userProfile: UserProfile, onBookClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            AsyncImage(
                model = hotel.imageUrl,
                contentDescription = hotel.arName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(android.R.drawable.ic_menu_gallery)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(hotel.arName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("الموقع: ${hotel.arLocation}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(hotel.arDescription, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(formatCurrency(hotel.pricePerNightYER) + "/ليلة", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    Button(
                        onClick = onBookClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("حجز غرفة", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CarItemCard(car: CarProduct, userProfile: UserProfile, onBookClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(
                model = car.imageUrl,
                contentDescription = car.model,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(android.R.drawable.ic_menu_compass)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text("${car.brand} ${car.model}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("النوع: سيارة دفع رباعي 4WD", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("الموقع المتاح: ${car.location}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(4.dp))
                if (car.withDriver) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("مع سائق محلي خبير بالطرق والمنحدرات", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(formatCurrency(car.pricePerDayYER) + "/يوم", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    Button(
                        onClick = onBookClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("طلب حجز سيارة", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}


// --- MY BOOKINGS SCREEN ---
@Composable
fun BookingsScreen(viewModel: TajrubahViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "حجوزاتي ورحلاتي الحالية",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (uiState.bookings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.BookOnline,
                            contentDescription = "فارغ",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "لم تقم بحجز أي تجارب أو خدمات بعد في اليمن.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "تصفح قسم الاستكشاف واحجز تجربتك التراثية الأولى!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        } else {
            items(uiState.bookings) { booking ->
                BookingItemRow(booking = booking, onCancelClick = {
                    viewModel.cancelExistingBooking(booking.id, booking.totalPaidYER)
                    Toast.makeText(context, "تم إلغاء الحجز واسترداد المبلغ ${formatCurrency(booking.totalPaidYER)} إلى محفظتك!", Toast.LENGTH_LONG).show()
                })
            }
        }
    }
}

@Composable
fun BookingItemRow(booking: Booking, onCancelClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("booking_item_${booking.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (booking.productType) {
                            "experience" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            "guide" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                        }
                    )
                ) {
                    Text(
                        text = when (booking.productType) {
                            "experience" -> "تجربة تراثية"
                            "guide" -> "مرشد محلي"
                            "hotel" -> "فندق"
                            "car" -> "سيارة دفع رباعي"
                            else -> booking.productType
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Status badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (booking.status == "ملغي") MaterialTheme.colorScheme.error.copy(alpha = 0.12f) else Color(0xFF2E7D32).copy(alpha = 0.12f)
                    )
                ) {
                    Text(
                        text = booking.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (booking.status == "ملغي") MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                    )
                }
            }

            Text(booking.arProductName, fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                fontSize = 12.sp
            ) {
                Text("التاريخ: ${booking.bookingDate}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text("العدد/المدة: ${booking.slotsOrDays}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("المبلغ المدفوع", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(formatCurrency(booking.totalPaidYER), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                if (booking.status == "نشط") {
                    OutlinedButton(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("cancel_booking_btn_${booking.id}")
                    ) {
                        Text("إلغاء واسترداد", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}


// --- USER PROFILE & HOST DASHBOARD SCREEN ---
@Composable
fun ProfileScreen(viewModel: TajrubahViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.userProfile
    val context = LocalContext.current

    // Fields for adding new experiences (Host view)
    var title by remember { mutableStateOf("") }
    var arTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var arDescription by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Sana'a") }
    var arLocation by remember { mutableStateOf("صنعاء") }
    var priceYER by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Cultural") }
    var duration by remember { mutableStateOf("3 Hours") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Profile Info Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(36.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.take(1),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(user.email, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                    ) {
                        Text(
                            if (user.role == "Traveler") "حساب مستكشف / مسافر" else "حساب مضيف محلي",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Wallet & Referral (Only shown in Traveler view for simplicity or both)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("المحفظة الرقمية والنقاط 💳", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("الرصيد المتاح بالريال اليمني", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text(formatCurrency(user.balanceYER), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Button(
                            onClick = { viewModel.topUpBalance(200000.0) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("شحن +200k ر.ي", color = MaterialTheme.colorScheme.onSecondary, fontSize = 11.sp)
                        }
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("نقاط مكافآت تجربة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text("${user.points} نقطة", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        }
                        Text("referral: ${user.referralCode}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }

        // Role switch button
        item {
            Button(
                onClick = { viewModel.toggleUserRole() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("role_switch_btn")
            ) {
                Text(
                    if (user.role == "Traveler") "التبديل إلى لوحة المضيف المحلي 🛠️" else "التبديل إلى وضع المسافر الاستكشافي 🗺️",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // HOST VIEW: Stats & Add Experience
        if (user.role == "Host") {
            item {
                Text(
                    "لوحة تحكم المضيف والخبرات",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Stats row
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("📊 أداء المضيف (أحمد الهمداني)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("إجمالي الأرباح المستلمة:")
                            Text(formatCurrency(uiState.hostStats.totalEarningsYER), fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("عمولة المنصة المقتطعة (10%):")
                            Text(formatCurrency(uiState.hostStats.commissionDeductedYER), color = MaterialTheme.colorScheme.error)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الحجوزات التي تم استلامها:")
                            Text("${uiState.hostStats.totalBookingsReceived} حجز", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Add new Experience Form
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("➕ إضافة تجربة سياحية/تراثية جديدة لليمن", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        
                        OutlinedTextField(
                            value = arTitle,
                            onValueChange = { arTitle = it },
                            label = { Text("عنوان التجربة (بالعربية)") },
                            modifier = Modifier.fillMaxWidth().testTag("add_exp_title_ar"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("عنوان التجربة (بالإنجليزية)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = arDescription,
                            onValueChange = { arDescription = it },
                            label = { Text("الوصف والتفاصيل (بالعربية)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            minLines = 2
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("الوصف والتفاصيل (بالإنجليزية)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            minLines = 2
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = arLocation,
                                onValueChange = { arLocation = it },
                                label = { Text("المدينة/الموقع (عربي)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text("الموقع (إنجليزي)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = priceYER,
                                onValueChange = { priceYER = it },
                                label = { Text("السعر (YER)") },
                                modifier = Modifier.weight(1f).testTag("add_exp_price"),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = duration,
                                onValueChange = { duration = it },
                                label = { Text("المدة") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        // Category Dropdown simulated via buttons for design simplicity and bulletproof execution
                        Text("اختر الفئة:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val categoriesList = listOf("Cultural", "Nature", "Crafts", "Food")
                            val catArMap = mapOf("Cultural" to "ثقافي", "Nature" to "طبيعة", "Crafts" to "حرفية", "Food" to "مأكولات")
                            categoriesList.forEach { c ->
                                OutlinedButton(
                                    onClick = { category = c },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (category == c) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                                    ),
                                    border = BorderStroke(1.dp, if (category == c) MaterialTheme.colorScheme.primary else Color.Gray),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text(catArMap[c] ?: c, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (arTitle.isEmpty() || arDescription.isEmpty() || priceYER.isEmpty()) {
                                    Toast.makeText(context, "الرجاء تعبئة جميع الحقول المطلوبة لإنشاء التجربة!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val price = priceYER.toDoubleOrNull() ?: 10000.0
                                    viewModel.addNewExperienceByHost(
                                        title = title.ifEmpty { arTitle },
                                        arTitle = arTitle,
                                        description = description.ifEmpty { arDescription },
                                        arDescription = arDescription,
                                        location = location,
                                        arLocation = arLocation,
                                        priceYER = price,
                                        category = category,
                                        duration = duration
                                    )
                                    // reset forms
                                    title = ""
                                    arTitle = ""
                                    description = ""
                                    arDescription = ""
                                    priceYER = ""
                                    Toast.makeText(context, "تم نشر تجربتك المحلية بنجاح في منصة تجربة!", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("add_exp_submit_btn")
                        ) {
                            Text("نشر وإتاحة للحجز الفوري 🚀", color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
