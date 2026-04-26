package com.example.mybudgetapp.ui.widgets

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.mybudgetapp.database.CATEGORY_KEY_FOOD
import com.example.mybudgetapp.database.CATEGORY_KEY_INCOME
import com.example.mybudgetapp.database.CATEGORY_KEY_OTHERS
import com.example.mybudgetapp.database.CATEGORY_KEY_TRANSPORTATION
import com.example.mybudgetapp.ui.theme.BudgetTheme

data class CategoryIconChoice(
    val key: String,
    val label: String,
    val ligature: String,
    val group: String,
    val keywords: List<String> = emptyList(),
)

data class CategoryColorOption(
    val hex: String,
    val label: String,
    val family: String,
    val keywords: List<String> = emptyList(),
)

const val defaultCategoryIconKey = "misc"
const val defaultCategoryColorHex = "#34944C"

val categoryIconChoices = listOf(
    CategoryIconChoice("misc", "General", "category", "Essentials", listOf("misc", "other", "others", "general", "default")),
    CategoryIconChoice("food", "Food", "fastfood", "Essentials", listOf("meal", "restaurant", "eating")),
    CategoryIconChoice("transportation", "Transportation", "directions_transit", "Essentials", listOf("transit", "travel", "commute")),
    CategoryIconChoice("income", "Income", "attach_money", "Essentials", listOf("salary", "earnings", "money")),
    CategoryIconChoice("bills", "Bills", "money_off", "Essentials", listOf("utilities", "payment")),
    CategoryIconChoice("calendar", "Calendar", "calendar_month", "Essentials", listOf("date", "schedule")),
    CategoryIconChoice("trends", "Trends", "timeline", "Essentials", listOf("analytics", "progress")),
    CategoryIconChoice("settings", "Settings", "settings", "Essentials", listOf("tools", "utility")),
    CategoryIconChoice("information", "Information", "info", "Essentials", listOf("details", "about")),
    CategoryIconChoice("support", "Support", "help", "Essentials", listOf("help", "question")),
    CategoryIconChoice("phone", "Phone", "call", "Essentials", listOf("mobile", "contact")),
    CategoryIconChoice("email", "Email", "email", "Essentials", listOf("mail", "message")),
    CategoryIconChoice("transfer", "Transfer", "send", "Essentials", listOf("move", "wire", "send")),
    CategoryIconChoice("location", "Location", "place", "Essentials", listOf("address", "pin")),
    CategoryIconChoice("done", "Done", "check_circle", "Essentials", listOf("complete", "finished")),
    CategoryIconChoice("security", "Security", "lock", "Essentials", listOf("secure", "private")),
    CategoryIconChoice("more", "More", "more_vert", "Essentials", listOf("menu", "other")),
    CategoryIconChoice("camera", "Camera", "camera_alt", "Essentials", listOf("photo", "image")),
    CategoryIconChoice("photos", "Photos", "photo_library", "Essentials", listOf("gallery", "pictures")),
    CategoryIconChoice("planner", "Planner", "event_note", "Essentials", listOf("agenda", "notes")),
    CategoryIconChoice("services", "Services", "build", "Essentials", listOf("manage", "tools")),
    CategoryIconChoice("map", "Map", "map", "Essentials", listOf("travel", "navigation")),
    CategoryIconChoice("history", "History", "history", "Essentials", listOf("past", "records")),
    CategoryIconChoice("savings", "Savings", "savings", "Essentials", listOf("save", "money")),
    CategoryIconChoice("share", "Share", "share", "Essentials", listOf("shared", "send")),
    CategoryIconChoice("compass", "Compass", "explore", "Essentials", listOf("direction", "navigation")),
    CategoryIconChoice("day", "Day", "calendar_today", "Essentials", listOf("today", "daily")),
    CategoryIconChoice("storage", "Storage", "inventory_2", "Essentials", listOf("sort", "box")),
    CategoryIconChoice("view", "View", "visibility", "Essentials", listOf("show", "see")),

    CategoryIconChoice("groceries", "Groceries", "local_grocery_store", "Food & Drink", listOf("market", "supermarket")),
    CategoryIconChoice("eating_out", "Eating Out", "restaurant", "Food & Drink", listOf("dining", "restaurant")),
    CategoryIconChoice("coffee", "Coffee", "local_cafe", "Food & Drink", listOf("cafe", "espresso")),
    CategoryIconChoice("drinks", "Drinks", "local_bar", "Food & Drink", listOf("bar", "beverages")),
    CategoryIconChoice("pizza", "Pizza", "local_pizza", "Food & Drink", listOf("slice")),
    CategoryIconChoice("bakery", "Bakery", "bakery_dining", "Food & Drink", listOf("bread", "pastry")),
    CategoryIconChoice("dessert", "Dessert", "icecream", "Food & Drink", listOf("sweet", "ice cream")),
    CategoryIconChoice("noodles", "Noodles", "ramen_dining", "Food & Drink", listOf("ramen", "soup")),
    CategoryIconChoice("breakfast", "Breakfast", "breakfast_dining", "Food & Drink", listOf("morning")),
    CategoryIconChoice("lunch", "Lunch", "lunch_dining", "Food & Drink", listOf("midday")),
    CategoryIconChoice("dinner", "Dinner", "dinner_dining", "Food & Drink", listOf("evening")),
    CategoryIconChoice("snacks", "Snacks", "cookie", "Food & Drink", listOf("treat", "cookie")),
    CategoryIconChoice("takeout", "Takeout", "takeout_dining", "Food & Drink", listOf("delivery", "to go")),
    CategoryIconChoice("meals", "Meals", "set_meal", "Food & Drink", listOf("combo", "plate")),
    CategoryIconChoice("eggs", "Eggs", "egg_alt", "Food & Drink", listOf("breakfast", "protein")),
    CategoryIconChoice("liquor", "Liquor", "liquor", "Food & Drink", listOf("alcohol", "bottle")),
    CategoryIconChoice("juice", "Juice", "local_drink", "Food & Drink", listOf("soda", "soft drink")),
    CategoryIconChoice("smoking", "Smoking", "smoking_rooms", "Food & Drink", listOf("cigarettes", "tobacco")),
    CategoryIconChoice("charity_food", "Food Bank", "food_bank", "Food & Drink", listOf("charity", "aid")),
    CategoryIconChoice("farmers_market", "Farmers Market", "shopping_basket", "Food & Drink", listOf("produce", "basket")),

    CategoryIconChoice("rent", "Rent", "home", "Home & Bills", listOf("house", "housing")),
    CategoryIconChoice("mortgage", "Mortgage", "house", "Home & Bills", listOf("loan", "house")),
    CategoryIconChoice("apartment", "Apartment", "apartment", "Home & Bills", listOf("flat")),
    CategoryIconChoice("bedroom", "Bedroom", "bed", "Home & Bills", listOf("sleep")),
    CategoryIconChoice("furniture", "Furniture", "chair", "Home & Bills", listOf("sofa", "table")),
    CategoryIconChoice("kitchen", "Kitchen", "kitchen", "Home & Bills", listOf("appliances", "home")),
    CategoryIconChoice("laundry", "Laundry", "local_laundry_service", "Home & Bills", listOf("wash", "clothes")),
    CategoryIconChoice("cleaning", "Cleaning", "cleaning_services", "Home & Bills", listOf("maid", "housekeeping")),
    CategoryIconChoice("electricity", "Electricity", "bolt", "Home & Bills", listOf("power", "energy")),
    CategoryIconChoice("water", "Water", "water_drop", "Home & Bills", listOf("utility")),
    CategoryIconChoice("internet", "Internet", "wifi", "Home & Bills", listOf("broadband", "network")),
    CategoryIconChoice("router", "Router", "router", "Home & Bills", listOf("internet", "modem")),
    CategoryIconChoice("mobile", "Mobile", "phone_iphone", "Home & Bills", listOf("cell", "phone")),
    CategoryIconChoice("devices", "Devices", "devices", "Home & Bills", listOf("electronics")),
    CategoryIconChoice("television", "Television", "tv", "Home & Bills", listOf("screen")),
    CategoryIconChoice("repairs", "Repairs", "handyman", "Home & Bills", listOf("maintenance", "fix")),
    CategoryIconChoice("tools", "Tools", "build", "Home & Bills", listOf("repair", "equipment")),
    CategoryIconChoice("construction", "Construction", "construction", "Home & Bills", listOf("building", "renovation")),
    CategoryIconChoice("insurance", "Insurance", "shield", "Home & Bills", listOf("coverage", "protection")),
    CategoryIconChoice("taxes", "Taxes", "receipt_long", "Home & Bills", listOf("tax", "government")),

    CategoryIconChoice("car", "Car", "directions_car", "Transport & Travel", listOf("vehicle", "auto")),
    CategoryIconChoice("taxi", "Taxi", "local_taxi", "Transport & Travel", listOf("cab", "ride")),
    CategoryIconChoice("bike", "Bike", "two_wheeler", "Transport & Travel", listOf("motorbike", "scooter")),
    CategoryIconChoice("bus", "Bus", "directions_bus", "Transport & Travel", listOf("coach")),
    CategoryIconChoice("train", "Train", "train", "Transport & Travel", listOf("rail")),
    CategoryIconChoice("subway", "Subway", "subway", "Transport & Travel", listOf("metro")),
    CategoryIconChoice("tram", "Tram", "tram", "Transport & Travel", listOf("streetcar")),
    CategoryIconChoice("fuel", "Fuel", "local_gas_station", "Transport & Travel", listOf("gas", "petrol")),
    CategoryIconChoice("parking", "Parking", "local_parking", "Transport & Travel", listOf("garage")),
    CategoryIconChoice("car_repair", "Car Repair", "car_repair", "Transport & Travel", listOf("mechanic")),
    CategoryIconChoice("car_wash", "Car Wash", "local_car_wash", "Transport & Travel", listOf("clean")),
    CategoryIconChoice("flight", "Flight", "flight", "Transport & Travel", listOf("airplane", "plane")),
    CategoryIconChoice("hotel", "Hotel", "hotel", "Transport & Travel", listOf("stay", "room")),
    CategoryIconChoice("luggage", "Luggage", "luggage", "Transport & Travel", listOf("bags", "travel")),
    CategoryIconChoice("commute", "Commute", "commute", "Transport & Travel", listOf("work travel")),
    CategoryIconChoice("boat", "Boat", "directions_boat", "Transport & Travel", listOf("ship", "ferry")),
    CategoryIconChoice("navigation", "Navigation", "explore", "Transport & Travel", listOf("direction", "gps")),
    CategoryIconChoice("shuttle", "Shuttle", "airport_shuttle", "Transport & Travel", listOf("van")),
    CategoryIconChoice("vacation", "Vacation", "beach_access", "Transport & Travel", listOf("beach", "holiday")),
    CategoryIconChoice("camping_trip", "Camping", "camping", "Transport & Travel", listOf("outdoors")),

    CategoryIconChoice("shopping", "Shopping", "shopping_cart", "Shopping & Lifestyle", listOf("buy", "store")),
    CategoryIconChoice("bags", "Bags", "shopping_bag", "Shopping & Lifestyle", listOf("purchases")),
    CategoryIconChoice("clothes", "Clothes", "checkroom", "Shopping & Lifestyle", listOf("fashion", "wardrobe")),
    CategoryIconChoice("gifts", "Gifts", "card_giftcard", "Shopping & Lifestyle", listOf("present")),
    CategoryIconChoice("coupons", "Coupons", "redeem", "Shopping & Lifestyle", listOf("discount", "voucher")),
    CategoryIconChoice("jewelry", "Jewelry", "diamond", "Shopping & Lifestyle", listOf("luxury")),
    CategoryIconChoice("beauty", "Beauty", "spa", "Shopping & Lifestyle", listOf("salon", "care")),
    CategoryIconChoice("self_care", "Self Care", "self_care", "Shopping & Lifestyle", listOf("wellness")),
    CategoryIconChoice("fashion", "Fashion", "style", "Shopping & Lifestyle", listOf("trend", "clothes")),
    CategoryIconChoice("store", "Store", "storefront", "Shopping & Lifestyle", listOf("shop")),
    CategoryIconChoice("art", "Art", "palette", "Shopping & Lifestyle", listOf("creative", "design")),
    CategoryIconChoice("painting", "Painting", "brush", "Shopping & Lifestyle", listOf("paint")),
    CategoryIconChoice("watch", "Watch", "watch", "Shopping & Lifestyle", listOf("timepiece")),
    CategoryIconChoice("celebration", "Celebration", "celebration", "Shopping & Lifestyle", listOf("party", "holiday")),
    CategoryIconChoice("pets", "Pets", "pets", "Shopping & Lifestyle", listOf("animals", "pet care")),
    CategoryIconChoice("flowers", "Flowers", "local_florist", "Shopping & Lifestyle", listOf("bouquet")),

    CategoryIconChoice("health", "Health", "favorite", "Health & Family", listOf("wellbeing")),
    CategoryIconChoice("hospital", "Hospital", "local_hospital", "Health & Family", listOf("clinic", "doctor")),
    CategoryIconChoice("medical", "Medical", "medical_services", "Health & Family", listOf("care")),
    CategoryIconChoice("pharmacy", "Pharmacy", "medication", "Health & Family", listOf("medicine")),
    CategoryIconChoice("fitness", "Fitness", "fitness_center", "Health & Family", listOf("gym", "exercise")),
    CategoryIconChoice("childcare", "Childcare", "child_care", "Health & Family", listOf("kids", "baby")),
    CategoryIconChoice("family", "Family", "family_restroom", "Health & Family", listOf("household")),
    CategoryIconChoice("education", "Education", "school", "Health & Family", listOf("study", "college")),
    CategoryIconChoice("books", "Books", "menu_book", "Health & Family", listOf("reading")),
    CategoryIconChoice("library", "Library", "local_library", "Health & Family", listOf("books")),
    CategoryIconChoice("volunteering", "Volunteering", "volunteer_activism", "Health & Family", listOf("charity", "giving")),
    CategoryIconChoice("soccer", "Soccer", "sports_soccer", "Health & Family", listOf("football", "sport")),
    CategoryIconChoice("basketball", "Basketball", "sports_basketball", "Health & Family", listOf("sport")),
    CategoryIconChoice("tennis", "Tennis", "sports_tennis", "Health & Family", listOf("sport")),
    CategoryIconChoice("running", "Running", "directions_run", "Health & Family", listOf("jogging", "sport")),
    CategoryIconChoice("prayer", "Prayer", "church", "Health & Family", listOf("religion", "spiritual")),

    CategoryIconChoice("salary", "Salary", "payments", "Work & Finance", listOf("income", "paycheck")),
    CategoryIconChoice("wallet", "Wallet", "account_balance_wallet", "Work & Finance", listOf("cash")),
    CategoryIconChoice("bank", "Bank", "account_balance", "Work & Finance", listOf("finance")),
    CategoryIconChoice("card", "Card", "credit_card", "Work & Finance", listOf("debit", "credit")),
    CategoryIconChoice("receipt", "Receipt", "receipt_long", "Work & Finance", listOf("bill", "invoice")),
    CategoryIconChoice("quotes", "Quote", "request_quote", "Work & Finance", listOf("estimate")),
    CategoryIconChoice("price_check", "Price Check", "price_check", "Work & Finance", listOf("pricing")),
    CategoryIconChoice("sales", "Sales", "sell", "Work & Finance", listOf("selling")),
    CategoryIconChoice("paid", "Paid", "paid", "Work & Finance", listOf("settled")),
    CategoryIconChoice("work", "Work", "work", "Work & Finance", listOf("job", "career")),
    CategoryIconChoice("business", "Business", "business_center", "Work & Finance", listOf("office", "briefcase")),
    CategoryIconChoice("investing", "Investing", "trending_up", "Work & Finance", listOf("stocks", "growth")),
    CategoryIconChoice("analytics", "Analytics", "show_chart", "Work & Finance", listOf("report", "graph")),
    CategoryIconChoice("budget", "Budget", "pie_chart", "Work & Finance", listOf("plan", "finance")),
    CategoryIconChoice("calculator", "Calculator", "calculate", "Work & Finance", listOf("math")),
    CategoryIconChoice("freelance", "Freelance", "edit", "Work & Finance", listOf("contract", "work")),
    CategoryIconChoice("subscriptions", "Subscriptions", "subscriptions", "Work & Finance", listOf("recurring", "membership")),
    CategoryIconChoice("cash", "Cash", "payments", "Work & Finance", listOf("money")),
    CategoryIconChoice("debt", "Debt", "credit_card", "Work & Finance", listOf("loan", "borrow")),
    CategoryIconChoice("refund", "Refund", "request_quote", "Work & Finance", listOf("return", "money back")),

    CategoryIconChoice("movies", "Movies", "movie", "Entertainment & Tech", listOf("cinema")),
    CategoryIconChoice("comedy", "Comedy", "theater_comedy", "Entertainment & Tech", listOf("shows")),
    CategoryIconChoice("gaming", "Gaming", "sports_esports", "Entertainment & Tech", listOf("games", "console")),
    CategoryIconChoice("music", "Music", "music_note", "Entertainment & Tech", listOf("audio")),
    CategoryIconChoice("headphones", "Headphones", "headphones", "Entertainment & Tech", listOf("audio", "music")),
    CategoryIconChoice("live_tv", "Live TV", "live_tv", "Entertainment & Tech", listOf("streaming")),
    CategoryIconChoice("video", "Video", "smart_display", "Entertainment & Tech", listOf("youtube", "streaming")),
    CategoryIconChoice("computer", "Computer", "computer", "Entertainment & Tech", listOf("pc")),
    CategoryIconChoice("smartphone", "Smartphone", "smartphone", "Entertainment & Tech", listOf("phone", "device")),
    CategoryIconChoice("photo", "Photography", "photo_camera", "Entertainment & Tech", listOf("camera", "pictures")),
    CategoryIconChoice("park", "Park", "park", "Entertainment & Tech", listOf("outdoors")),
    CategoryIconChoice("forest", "Forest", "forest", "Entertainment & Tech", listOf("nature")),
    CategoryIconChoice("hiking", "Hiking", "hiking", "Entertainment & Tech", listOf("outdoors", "trail")),
    CategoryIconChoice("globe", "Global", "public", "Entertainment & Tech", listOf("world", "internet")),
    CategoryIconChoice("events", "Events", "event", "Entertainment & Tech", listOf("ticket", "show")),
    CategoryIconChoice("sports", "Sports", "sports_soccer", "Entertainment & Tech", listOf("games", "activity")),
    CategoryIconChoice("cake", "Cake", "cake", "Entertainment & Tech", listOf("birthday", "dessert")),
    CategoryIconChoice("mall", "Mall", "local_mall", "Shopping & Lifestyle", listOf("shopping center", "retail")),
    CategoryIconChoice("science", "Science", "science", "Health & Family", listOf("lab", "study")),
    CategoryIconChoice("laptop", "Laptop", "laptop_mac", "Entertainment & Tech", listOf("computer", "work")),
    CategoryIconChoice("toys", "Toys", "toys", "Health & Family", listOf("kids", "play")),
    CategoryIconChoice("umbrella", "Umbrella", "umbrella", "Transport & Travel", listOf("rain", "weather")),
    CategoryIconChoice("baby", "Baby", "stroller", "Health & Family", listOf("child", "infant")),
    CategoryIconChoice("dentist", "Dentist", "dentistry", "Health & Family", listOf("teeth", "clinic")),
    CategoryIconChoice("mailbox", "Mailbox", "mail", "Work & Finance", listOf("post", "letters")),
    CategoryIconChoice("delivery", "Delivery", "local_shipping", "Transport & Travel", listOf("courier", "shipping")),
)

private val categoryIconAliases = mapOf(
    "fastfood" to "food",
    "directions_transit" to "transportation",
    "cookie" to "misc",
    "attach_money" to "income",
    "timeline" to "trends",
    "money_off" to "bills",
    "add" to "misc",
    "home" to "rent",
    "shopping_cart" to "shopping",
    "favorite" to "health",
    "star" to "celebration",
    "search" to "support",
    "edit" to "freelance",
    "delete" to "storage",
    "info" to "information",
    "help" to "support",
    "call" to "phone",
    "email" to "email",
    "send" to "transfer",
    "place" to "location",
    "check_circle" to "done",
    "lock" to "security",
    "more_vert" to "more",
    "camera" to "camera",
    "gallery" to "photos",
    "agenda" to "planner",
    "manage" to "services",
    "map" to "map",
    "my_places" to "location",
    "week" to "calendar",
    "calendar_alt" to "calendar",
    "history" to "history",
    "camera_roll" to "photos",
    "save" to "savings",
    "share" to "share",
    "compass" to "compass",
    "directions" to "navigation",
    "day" to "day",
    "sort" to "storage",
    "view" to "view",
    "help_drawable" to "support",
    "info_drawable" to "information",
    "settings_drawable" to "settings",
)

private val categoryIconChoiceByKey = categoryIconChoices.associateBy { it.key }
private val categoryIconSearchIndexByKey = categoryIconChoices.associate { choice ->
    choice.key to buildString {
        append(choice.label.lowercase())
        append(' ')
        append(choice.key.lowercase())
        append(' ')
        append(choice.ligature.lowercase())
        append(' ')
        append(choice.group.lowercase())
        choice.keywords.forEach {
            append(' ')
            append(it.lowercase())
        }
    }
}

private fun fallbackIconKey(fallbackCategoryKey: String): String = when (fallbackCategoryKey) {
    CATEGORY_KEY_FOOD -> "food"
    CATEGORY_KEY_TRANSPORTATION -> "transportation"
    CATEGORY_KEY_OTHERS -> "misc"
    CATEGORY_KEY_INCOME -> "income"
    else -> defaultCategoryIconKey
}

fun resolveCategoryIconChoice(
    iconKey: String,
    fallbackCategoryKey: String = "",
): CategoryIconChoice {
    val normalizedKey = iconKey.trim().ifBlank { fallbackIconKey(fallbackCategoryKey) }
    val canonicalKey = categoryIconAliases[normalizedKey] ?: normalizedKey
    return categoryIconChoiceByKey[canonicalKey]
        ?: categoryIconChoiceByKey[fallbackIconKey(fallbackCategoryKey)]
        ?: categoryIconChoiceByKey.getValue(defaultCategoryIconKey)
}

fun matchesCategoryIconChoice(
    choice: CategoryIconChoice,
    query: String,
): Boolean {
    if (query.isBlank()) return true
    val normalizedQuery = query.trim().lowercase()
    return categoryIconSearchIndexByKey[choice.key]?.contains(normalizedQuery) == true
}

private fun colorOptions(
    family: String,
    keywords: List<String>,
    hexes: List<String>,
): List<CategoryColorOption> = hexes.mapIndexed { index, hex ->
    CategoryColorOption(
        hex = hex,
        label = "$family ${index + 1}",
        family = family,
        keywords = keywords + listOf("palette", "tone ${index + 1}"),
    )
}

private data class CategoryColorFamilyPalette(
    val family: String,
    val keywords: List<String>,
    val hexes: List<String>,
)

private val categoryColorFamilies = listOf(
    CategoryColorFamilyPalette("Red", listOf("red", "scarlet", "ruby"), listOf("#F87171", "#EF4444", "#DC2626", "#B91C1C", "#991B1B")),
    CategoryColorFamilyPalette("Rose", listOf("rose", "crimson", "berry"), listOf("#FB7185", "#F43F5E", "#E11D48", "#BE123C", "#9F1239")),
    CategoryColorFamilyPalette("Pink", listOf("pink", "blush", "bubblegum"), listOf("#F472B6", "#EC4899", "#DB2777", "#BE185D", "#9D174D")),
    CategoryColorFamilyPalette("Fuchsia", listOf("fuchsia", "magenta", "orchid"), listOf("#E879F9", "#D946EF", "#C026D3", "#A21CAF", "#86198F")),
    CategoryColorFamilyPalette("Purple", listOf("purple", "grape", "plum"), listOf("#C084FC", "#A855F7", "#9333EA", "#7E22CE", "#6B21A8")),
    CategoryColorFamilyPalette("Violet", listOf("violet", "iris", "amethyst"), listOf("#A78BFA", "#8B5CF6", "#7C3AED", "#6D28D9", "#5B21B6")),
    CategoryColorFamilyPalette("Indigo", listOf("indigo", "royal", "midnight"), listOf("#818CF8", "#6366F1", "#4F46E5", "#4338CA", "#3730A3")),
    CategoryColorFamilyPalette("Blue", listOf("blue", "azure", "ocean"), listOf("#60A5FA", "#3B82F6", "#2563EB", "#1D4ED8", "#1E40AF")),
    CategoryColorFamilyPalette("Sky", listOf("sky", "cerulean", "air"), listOf("#38BDF8", "#0EA5E9", "#0284C7", "#0369A1", "#075985")),
    CategoryColorFamilyPalette("Cyan", listOf("cyan", "aqua", "glacier"), listOf("#22D3EE", "#06B6D4", "#0891B2", "#0E7490", "#155E75")),
    CategoryColorFamilyPalette("Teal", listOf("teal", "sea", "lagoon"), listOf("#2DD4BF", "#14B8A6", "#0D9488", "#0F766E", "#115E59")),
    CategoryColorFamilyPalette("Emerald", listOf("emerald", "mint", "jade"), listOf("#34D399", "#10B981", "#059669", "#047857", "#065F46")),
    CategoryColorFamilyPalette("Green", listOf("green", "leaf", "forest"), listOf("#4ADE80", "#22C55E", "#16A34A", "#15803D", "#166534")),
    CategoryColorFamilyPalette("Lime", listOf("lime", "chartreuse", "zest"), listOf("#A3E635", "#84CC16", "#65A30D", "#4D7C0F", "#3F6212")),
    CategoryColorFamilyPalette("Yellow", listOf("yellow", "lemon", "sun"), listOf("#FACC15", "#EAB308", "#CA8A04", "#A16207", "#854D0E")),
    CategoryColorFamilyPalette("Amber", listOf("amber", "honey", "gold"), listOf("#FBBF24", "#F59E0B", "#D97706", "#B45309", "#92400E")),
    CategoryColorFamilyPalette("Orange", listOf("orange", "tangerine", "citrus"), listOf("#FB923C", "#F97316", "#EA580C", "#C2410C", "#9A3412")),
    CategoryColorFamilyPalette("Coral", listOf("coral", "salmon", "peach"), listOf("#FF8A80", "#FF6F61", "#F95D6A", "#E76F51", "#D65A31")),
    CategoryColorFamilyPalette("Copper", listOf("copper", "clay", "burnt"), listOf("#E6A15A", "#C97A3D", "#A95C2B", "#8B4513", "#6F3B18")),
    CategoryColorFamilyPalette("Slate", listOf("slate", "graphite", "storm"), listOf("#94A3B8", "#64748B", "#475569", "#334155", "#1E293B")),
)

val categoryColorCatalog = List(5) { toneIndex ->
    categoryColorFamilies.map { family ->
        CategoryColorOption(
            hex = family.hexes[toneIndex],
            label = "${family.family} ${toneIndex + 1}",
            family = family.family,
            keywords = family.keywords + listOf("palette", "tone ${toneIndex + 1}"),
        )
    }
}.flatten()
    .distinctBy { it.hex.lowercase() }

private val categoryColorOptionByNormalizedHex = categoryColorCatalog.associateBy { it.hex.lowercase() }
private val categoryColorSearchIndexByHex = categoryColorCatalog.associate { option ->
    option.hex to buildString {
        append(option.label.lowercase())
        append(' ')
        append(option.family.lowercase())
        append(' ')
        append(option.hex.lowercase())
        option.keywords.forEach {
            append(' ')
            append(it.lowercase())
        }
    }
}

fun resolveCategoryColorOption(colorHex: String): CategoryColorOption? =
    categoryColorOptionByNormalizedHex[colorHex.trim().lowercase()]

fun matchesCategoryColorOption(
    option: CategoryColorOption,
    query: String,
): Boolean {
    if (query.isBlank()) return true
    val normalizedQuery = query.trim().lowercase()
    return categoryColorSearchIndexByHex[option.hex]?.contains(normalizedQuery) == true
}

private fun categoryVectorForSymbol(symbol: String): ImageVector = when (symbol) {
    "category" -> Icons.Filled.Category
    "fastfood" -> Icons.Filled.Fastfood
    "directions_transit" -> Icons.Filled.DirectionsTransit
    "attach_money" -> Icons.Filled.AttachMoney
    "money_off" -> Icons.Filled.MoneyOff
    "calendar_month" -> Icons.Filled.CalendarMonth
    "timeline" -> Icons.Filled.Timeline
    "settings" -> Icons.Filled.Settings
    "info" -> Icons.Filled.Info
    "help" -> Icons.Filled.SupportAgent
    "call" -> Icons.Filled.Call
    "email" -> Icons.Filled.Email
    "send" -> Icons.AutoMirrored.Filled.Send
    "place" -> Icons.Filled.Place
    "check_circle" -> Icons.Filled.CheckCircle
    "lock" -> Icons.Filled.Lock
    "more_vert" -> Icons.Filled.MoreVert
    "camera_alt" -> Icons.Filled.CameraAlt
    "photo_library" -> Icons.Filled.PhotoLibrary
    "event_note" -> Icons.Filled.Event
    "build" -> Icons.Filled.Build
    "map" -> Icons.Filled.Map
    "history" -> Icons.Filled.History
    "savings" -> Icons.Filled.Savings
    "share" -> Icons.Filled.Share
    "explore" -> Icons.Filled.Explore
    "calendar_today" -> Icons.Filled.CalendarToday
    "inventory_2" -> Icons.Filled.Inventory2
    "visibility" -> Icons.Filled.Visibility
    "local_grocery_store" -> Icons.Filled.LocalGroceryStore
    "restaurant" -> Icons.Filled.Restaurant
    "local_cafe" -> Icons.Filled.LocalCafe
    "local_bar" -> Icons.Filled.LocalBar
    "local_pizza" -> Icons.Filled.LocalPizza
    "bakery_dining" -> Icons.Filled.BakeryDining
    "icecream" -> Icons.Filled.Icecream
    "ramen_dining" -> Icons.Filled.RamenDining
    "breakfast_dining" -> Icons.Filled.BreakfastDining
    "lunch_dining" -> Icons.Filled.LunchDining
    "dinner_dining" -> Icons.Filled.DinnerDining
    "cookie" -> Icons.Filled.Cookie
    "takeout_dining" -> Icons.Filled.TakeoutDining
    "set_meal" -> Icons.Filled.SetMeal
    "egg_alt" -> Icons.Filled.EggAlt
    "liquor" -> Icons.Filled.Liquor
    "local_drink" -> Icons.Filled.LocalDrink
    "smoking_rooms" -> Icons.Filled.SmokingRooms
    "food_bank" -> Icons.Filled.FoodBank
    "shopping_basket" -> Icons.Filled.ShoppingBasket
    "home" -> Icons.Filled.Home
    "house" -> Icons.Filled.House
    "apartment" -> Icons.Filled.Apartment
    "bed" -> Icons.Filled.Bed
    "chair" -> Icons.Filled.Chair
    "kitchen" -> Icons.Filled.Kitchen
    "local_laundry_service" -> Icons.Filled.LocalLaundryService
    "cleaning_services" -> Icons.Filled.CleaningServices
    "bolt" -> Icons.Filled.Bolt
    "water_drop" -> Icons.Filled.WaterDrop
    "wifi" -> Icons.Filled.Wifi
    "router" -> Icons.Filled.Router
    "phone_iphone" -> Icons.Filled.PhoneIphone
    "devices" -> Icons.Filled.Devices
    "tv" -> Icons.Filled.Tv
    "handyman" -> Icons.Filled.Handyman
    "construction" -> Icons.Filled.Construction
    "shield" -> Icons.Filled.Shield
    "receipt_long" -> Icons.Filled.Receipt
    "directions_car" -> Icons.Filled.DirectionsCar
    "local_taxi" -> Icons.Filled.LocalTaxi
    "two_wheeler" -> Icons.Filled.TwoWheeler
    "directions_bus" -> Icons.Filled.DirectionsBus
    "train" -> Icons.Filled.Train
    "subway" -> Icons.Filled.Subway
    "tram" -> Icons.Filled.Tram
    "local_gas_station" -> Icons.Filled.LocalGasStation
    "local_parking" -> Icons.Filled.LocalParking
    "car_repair" -> Icons.Filled.CarRepair
    "local_car_wash" -> Icons.Filled.LocalCarWash
    "flight" -> Icons.Filled.Flight
    "hotel" -> Icons.Filled.Hotel
    "luggage" -> Icons.Filled.Luggage
    "commute" -> Icons.Filled.Commute
    "directions_boat" -> Icons.Filled.DirectionsBoat
    "airport_shuttle" -> Icons.Filled.AirportShuttle
    "beach_access" -> Icons.Filled.BeachAccess
    "camping" -> Icons.Filled.Forest
    "shopping_cart" -> Icons.Filled.ShoppingCart
    "shopping_bag" -> Icons.Filled.ShoppingBag
    "checkroom" -> Icons.Filled.Checkroom
    "card_giftcard" -> Icons.Filled.CardGiftcard
    "redeem" -> Icons.Filled.Redeem
    "diamond" -> Icons.Filled.Diamond
    "spa" -> Icons.Filled.Spa
    "self_care" -> Icons.Filled.SelfImprovement
    "style" -> Icons.Filled.Style
    "storefront" -> Icons.Filled.Storefront
    "palette" -> Icons.Filled.Palette
    "brush" -> Icons.Filled.Brush
    "watch" -> Icons.Filled.Watch
    "celebration" -> Icons.Filled.Celebration
    "pets" -> Icons.Filled.Pets
    "local_florist" -> Icons.Filled.LocalFlorist
    "favorite" -> Icons.Filled.Favorite
    "local_hospital" -> Icons.Filled.LocalHospital
    "medical_services" -> Icons.Filled.MedicalServices
    "medication" -> Icons.Filled.Medication
    "fitness_center" -> Icons.Filled.FitnessCenter
    "child_care" -> Icons.Filled.ChildCare
    "family_restroom" -> Icons.Filled.FamilyRestroom
    "school" -> Icons.Filled.School
    "menu_book" -> Icons.Filled.AutoStories
    "local_library" -> Icons.Filled.LocalLibrary
    "volunteer_activism" -> Icons.Filled.VolunteerActivism
    "sports_soccer" -> Icons.Filled.SportsSoccer
    "sports_basketball" -> Icons.Filled.SportsBasketball
    "sports_tennis" -> Icons.Filled.SportsTennis
    "directions_run" -> Icons.Filled.Hiking
    "church" -> Icons.Filled.AutoStories
    "payments" -> Icons.Filled.Payments
    "account_balance_wallet" -> Icons.Filled.AccountBalanceWallet
    "account_balance" -> Icons.Filled.AccountBalance
    "credit_card" -> Icons.Filled.CreditCard
    "request_quote" -> Icons.Filled.RequestQuote
    "price_check" -> Icons.Filled.PriceCheck
    "sell" -> Icons.Filled.Sell
    "paid" -> Icons.Filled.Paid
    "work" -> Icons.Filled.Work
    "business_center" -> Icons.Filled.BusinessCenter
    "trending_up" -> Icons.Filled.Insights
    "show_chart" -> Icons.Filled.QueryStats
    "pie_chart" -> Icons.Filled.PieChart
    "calculate" -> Icons.Filled.Calculate
    "edit" -> Icons.Filled.Edit
    "subscriptions" -> Icons.Filled.Subscriptions
    "movie" -> Icons.Filled.Movie
    "theater_comedy" -> Icons.Filled.TheaterComedy
    "sports_esports" -> Icons.Filled.SportsEsports
    "music_note" -> Icons.Filled.MusicNote
    "headphones" -> Icons.Filled.Headphones
    "live_tv" -> Icons.Filled.LiveTv
    "smart_display" -> Icons.Filled.SmartDisplay
    "computer" -> Icons.Filled.Computer
    "smartphone" -> Icons.Filled.Smartphone
    "photo_camera" -> Icons.Filled.PhotoCamera
    "park" -> Icons.Filled.Park
    "forest" -> Icons.Filled.Forest
    "hiking" -> Icons.Filled.Hiking
    "public" -> Icons.Filled.Public
    "event" -> Icons.Filled.Event
    "cake" -> Icons.Filled.Cake
    "local_mall" -> Icons.Filled.LocalMall
    "science" -> Icons.Filled.Science
    "laptop_mac" -> Icons.Filled.LaptopMac
    "toys" -> Icons.Filled.Toys
    "umbrella" -> Icons.Filled.Umbrella
    "stroller" -> Icons.Filled.Stroller
    "dentistry" -> Icons.Filled.MedicalServices
    "mail" -> Icons.Filled.Mail
    "local_shipping" -> Icons.Filled.LocalShipping
    else -> Icons.Filled.Category
}

@Composable
fun CategoryIcon(
    iconKey: String,
    fallbackCategoryKey: String = "",
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 24.dp,
) {
    val resolvedChoice = remember(iconKey, fallbackCategoryKey) {
        resolveCategoryIconChoice(iconKey = iconKey, fallbackCategoryKey = fallbackCategoryKey)
    }
    Icon(
        imageVector = categoryVectorForSymbol(resolvedChoice.ligature),
        contentDescription = null,
        modifier = modifier.size(size),
        tint = tint,
    )
}

@Composable
fun categoryPlaceholderPainter(
    iconKey: String,
    fallbackCategoryKey: String = "",
): Painter {
    val resolvedChoice = remember(iconKey, fallbackCategoryKey) {
        resolveCategoryIconChoice(iconKey = iconKey, fallbackCategoryKey = fallbackCategoryKey)
    }
    return rememberVectorPainter(categoryVectorForSymbol(resolvedChoice.ligature))
}

@Composable
fun categoryAccentColor(
    colorHex: String,
    fallbackCategoryKey: String = "",
): Color {
    val fallback = when (fallbackCategoryKey) {
        CATEGORY_KEY_FOOD -> BudgetTheme.extendedColors.food
        CATEGORY_KEY_TRANSPORTATION -> BudgetTheme.extendedColors.transit
        CATEGORY_KEY_OTHERS -> BudgetTheme.extendedColors.others
        CATEGORY_KEY_INCOME -> BudgetTheme.extendedColors.income
        else -> MaterialTheme.colorScheme.primary
    }
    return runCatching { Color(colorHex.toColorInt()) }.getOrElse { fallback }
}
