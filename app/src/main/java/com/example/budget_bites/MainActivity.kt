package com.example.budget_bites

import androidx.compose.material3.ExperimentalMaterial3Api
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Locale

data class Meal(
    val id: String = "",
    val name: String = "",
    val emoji: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val ingredients: List<String> = emptyList()
)

data class Expense(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0
)

data class ShoppingItem(
    val id: String = "",
    val name: String = ""
)

data class CustomerProfile(
    val name: String = "",
    val surname: String = "",
    val phone: String = "",
    val email: String = ""
)

class MainActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    App()
                }
            }
        }
    }

    @Composable
    fun App() {
        var currentUser by remember {
            mutableStateOf(auth.currentUser)
        }

        if (currentUser == null) {
            LoginScreen(
                onLoginSuccess = {
                    currentUser = auth.currentUser
                }
            )
        } else {
            BudgetBitesHome(
                onLogout = {
                    auth.signOut()
                    currentUser = null
                }
            )
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var createAccount by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Budget Bites",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (createAccount) {
                "Create your account"
            } else {
                "Welcome back"
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        if (createAccount) {

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = ""
                },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = surname,
                onValueChange = {
                    surname = it
                    errorMessage = ""
                },
                label = { Text("Surname") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    errorMessage = ""
                },
                label = { Text("Phone number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = ""
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = ""
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (createAccount) {

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = ""
                },
                label = { Text("Confirm password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (successMessage.isNotEmpty()) {
            Text(
                text = successMessage,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        Button(
            onClick = {

                errorMessage = ""
                successMessage = ""

                if (createAccount) {

                    if (
                        name.isBlank() ||
                        surname.isBlank() ||
                        phone.isBlank() ||
                        email.isBlank() ||
                        password.isBlank() ||
                        confirmPassword.isBlank()
                    ) {
                        errorMessage = "Please fill in all fields."
                        return@Button
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                        errorMessage = "Please enter a valid email address."
                        return@Button
                    }

                    if (phone.trim().length < 7) {
                        errorMessage = "Please enter a valid phone number."
                        return@Button
                    }

                    if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters."
                        return@Button
                    }

                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match."
                        return@Button
                    }

                    loading = true

                    auth.createUserWithEmailAndPassword(
                        email.trim(),
                        password
                    ).addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            val user = auth.currentUser

                            if (user != null) {

                                val profile = hashMapOf(
                                    "name" to name.trim(),
                                    "surname" to surname.trim(),
                                    "phone" to phone.trim(),
                                    "email" to email.trim(),
                                    "budget" to 0.0
                                )

                                db.collection("users")
                                    .document(user.uid)
                                    .set(profile, SetOptions.merge())
                                    .addOnSuccessListener {

                                        loading = false
                                        successMessage =
                                            "Account created successfully."

                                        onLoginSuccess()
                                    }
                                    .addOnFailureListener { error ->

                                        loading = false
                                        errorMessage =
                                            error.message ?: "Could not save your account."
                                    }
                            } else {
                                loading = false
                                errorMessage = "Account was created but could not be loaded."
                            }

                        } else {

                            loading = false

                            errorMessage =
                                when {
                                    task.exception?.message?.contains(
                                        "already in use",
                                        true
                                    ) == true ->
                                        "An account with this email already exists."

                                    task.exception?.message?.contains(
                                        "badly formatted",
                                        true
                                    ) == true ->
                                        "Please enter a valid email address."

                                    else ->
                                        task.exception?.message
                                            ?: "Could not create account."
                                }
                        }
                    }

                } else {

                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter your email and password."
                        return@Button
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                        errorMessage = "Please enter a valid email address."
                        return@Button
                    }

                    loading = true

                    auth.signInWithEmailAndPassword(
                        email.trim(),
                        password
                    ).addOnCompleteListener { task ->

                        loading = false

                        if (task.isSuccessful) {
                            onLoginSuccess()
                        } else {
                            errorMessage = "Invalid email or password."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {

            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    if (createAccount) "Create Account" else "Login"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = {
                createAccount = !createAccount
                errorMessage = ""
                successMessage = ""
            }
        ) {
            Text(
                if (createAccount) {
                    "Already have an account? Login"
                } else {
                    "Don't have an account? Create one"
                }
            )
        }
    }
}

@Composable
fun BudgetBitesHome(
    onLogout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val user = auth.currentUser

    var selectedScreen by remember { mutableStateOf("Home") }

    var profile by remember {
        mutableStateOf(CustomerProfile())
    }

    var budget by remember {
        mutableDoubleStateOf(0.0)
    }

    var expenses by remember {
        mutableStateOf<List<Expense>>(emptyList())
    }

    var shoppingList by remember {
        mutableStateOf<List<ShoppingItem>>(emptyList())
    }

    var meals by remember {
        mutableStateOf<List<Meal>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    if (user == null) {
        return
    }

    fun loadData() {

        loading = true

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    profile = CustomerProfile(
                        name = document.getString("name") ?: "",
                        surname = document.getString("surname") ?: "",
                        phone = document.getString("phone") ?: "",
                        email = document.getString("email") ?: ""
                    )

                    budget = document.getDouble("budget") ?: 0.0
                }
            }

        db.collection("users")
            .document(user.uid)
            .collection("expenses")
            .get()
            .addOnSuccessListener { snapshot ->

                expenses = snapshot.documents.map { document ->

                    Expense(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        amount = document.getDouble("amount") ?: 0.0
                    )
                }
            }

        db.collection("users")
            .document(user.uid)
            .collection("shoppingList")
            .get()
            .addOnSuccessListener { snapshot ->

                shoppingList = snapshot.documents.map { document ->

                    ShoppingItem(
                        id = document.id,
                        name = document.getString("name") ?: ""
                    )
                }
            }

        db.collection("meals")
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.isEmpty) {

                    seedMeals(db)

                    meals = defaultMeals()

                } else {

                    meals = snapshot.documents.map { document ->

                        Meal(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            emoji = document.getString("emoji") ?: "",
                            category = document.getString("category") ?: "",
                            price = document.getDouble("price") ?: 0.0,
                            description = document.getString("description") ?: "",
                            ingredients =
                                (document.get("ingredients") as? List<*>)
                                    ?.mapNotNull { it?.toString() }
                                    ?: emptyList()
                        )
                    }
                }

                loading = false
            }
            .addOnFailureListener {

                meals = defaultMeals()
                loading = false
            }
    }

    LaunchedEffect(user.uid) {
        loadData()
    }

    val totalSpent = expenses.sumOf { it.amount }
    val moneyRemaining = budget - totalSpent

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Budget Bites")
                }
            )
        },
        bottomBar = {

            NavigationBar {

                NavigationBarItem(
                    selected = selectedScreen == "Home",
                    onClick = {
                        selectedScreen = "Home"
                    },
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = {
                        Text("Home")
                    }
                )

                NavigationBarItem(
                    selected = selectedScreen == "Meals",
                    onClick = {
                        selectedScreen = "Meals"
                    },
                    icon = {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = "Meals"
                        )
                    },
                    label = {
                        Text("Meals")
                    }
                )

                NavigationBarItem(
                    selected = selectedScreen == "Shopping",
                    onClick = {
                        selectedScreen = "Shopping"
                    },
                    icon = {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Shopping"
                        )
                    },
                    label = {
                        Text("Shopping")
                    }
                )

                NavigationBarItem(
                    selected = selectedScreen == "Stores",
                    onClick = {
                        selectedScreen = "Stores"
                    },
                    icon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Stores"
                        )
                    },
                    label = {
                        Text("Stores")
                    }
                )

                NavigationBarItem(
                    selected = selectedScreen == "Profile",
                    onClick = {
                        selectedScreen = "Profile"
                    },
                    icon = {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Profile"
                        )
                    },
                    label = {
                        Text("Profile")
                    }
                )
            }
        }
    ) { padding ->

        if (loading) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        } else {

            when (selectedScreen) {

                "Home" -> {

                    HomeScreen(
                        modifier = Modifier.padding(padding),
                        profile = profile,
                        budget = budget,
                        totalSpent = totalSpent,
                        moneyRemaining = moneyRemaining,
                        expenses = expenses,
                        onBudgetChange = { newBudget ->

                            budget = newBudget

                            db.collection("users")
                                .document(user.uid)
                                .set(
                                    mapOf("budget" to newBudget),
                                    SetOptions.merge()
                                )
                        },
                        onAddExpense = { expenseName, amount ->

                            if (
                                expenseName.isNotBlank() &&
                                amount > 0
                            ) {

                                val expenseData = hashMapOf(
                                    "name" to expenseName.trim(),
                                    "amount" to amount
                                )

                                db.collection("users")
                                    .document(user.uid)
                                    .collection("expenses")
                                    .add(expenseData)
                                    .addOnSuccessListener { document ->

                                        expenses = expenses + Expense(
                                            id = document.id,
                                            name = expenseName.trim(),
                                            amount = amount
                                        )
                                    }
                            }
                        }
                    )
                }

                "Meals" -> {

                    MealsScreen(
                        modifier = Modifier.padding(padding),
                        meals = meals,
                        budget = budget,
                        moneyRemaining = moneyRemaining,
                        shoppingList = shoppingList,
                        onAddToShoppingList = { ingredient ->

                            val alreadyExists =
                                shoppingList.any {
                                    it.name.equals(
                                        ingredient,
                                        ignoreCase = true
                                    )
                                }

                            if (!alreadyExists) {

                                db.collection("users")
                                    .document(user.uid)
                                    .collection("shoppingList")
                                    .add(
                                        mapOf(
                                            "name" to ingredient
                                        )
                                    )
                                    .addOnSuccessListener { document ->

                                        shoppingList =
                                            shoppingList + ShoppingItem(
                                                id = document.id,
                                                name = ingredient
                                            )
                                    }
                            }
                        }
                    )
                }

                "Shopping" -> {

                    ShoppingScreen(
                        modifier = Modifier.padding(padding),
                        shoppingList = shoppingList,
                        onRemoveItem = { item ->

                            db.collection("users")
                                .document(user.uid)
                                .collection("shoppingList")
                                .document(item.id)
                                .delete()
                                .addOnSuccessListener {

                                    shoppingList =
                                        shoppingList.filterNot {
                                            it.id == item.id
                                        }
                                }
                        }
                    )
                }

                "Stores" -> {

                    StoresScreen(
                        modifier = Modifier.padding(padding)
                    )
                }

                "Profile" -> {

                    ProfileScreen(
                        modifier = Modifier.padding(padding),
                        profile = profile,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    profile: CustomerProfile,
    budget: Double,
    totalSpent: Double,
    moneyRemaining: Double,
    expenses: List<Expense>,
    onBudgetChange: (Double) -> Unit,
    onAddExpense: (String, Double) -> Unit
) {
    var budgetInput by remember {
        mutableStateOf(
            if (budget == 0.0) "" else budget.toString()
        )
    }

    var expenseName by remember {
        mutableStateOf("")
    }

    var expenseAmount by remember {
        mutableStateOf("")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        item {

            Text(
                text = "Hello, ${profile.name.ifBlank { "there" }}!",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text("Manage your food budget and shopping.")

            Spacer(modifier = Modifier.height(20.dp))
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        "Your Budget",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = {
                            budgetInput = it
                        },
                        label = {
                            Text("Budget amount")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {

                            val newBudget =
                                budgetInput.toDoubleOrNull()

                            if (newBudget != null && newBudget >= 0) {
                                onBudgetChange(newBudget)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Budget")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        "Money Overview",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Budget: ${money(budget)}"
                    )

                    Text(
                        "Money spent: ${money(totalSpent)}"
                    )

                    Text(
                        "Money remaining: ${money(moneyRemaining)}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        "Add Purchase",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = expenseName,
                        onValueChange = {
                            expenseName = it
                        },
                        label = {
                            Text("What did you buy?")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = {
                            expenseAmount = it
                        },
                        label = {
                            Text("Amount")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {

                            val amount =
                                expenseAmount.toDoubleOrNull()

                            if (
                                expenseName.isNotBlank() &&
                                amount != null &&
                                amount > 0
                            ) {

                                onAddExpense(
                                    expenseName,
                                    amount
                                )

                                expenseName = ""
                                expenseAmount = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Purchase")
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        item {

            Text(
                "Recent Purchases",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (expenses.isEmpty()) {

            item {
                Text("No purchases recorded yet.")
            }

        } else {

            items(
                items = expenses,
                key = { it.id }
            ) { expense ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(expense.name)

                    Text(money(expense.amount))
                }
            }
        }
    }
}

@Composable
fun MealsScreen(
    modifier: Modifier = Modifier,
    meals: List<Meal>,
    budget: Double,
    moneyRemaining: Double,
    shoppingList: List<ShoppingItem>,
    onAddToShoppingList: (String) -> Unit
) {
    var selectedCategory by remember {
        mutableStateOf("All")
    }

    val categories =
        listOf("All") + meals
            .map { it.category }
            .distinct()

    val filteredMeals =
        if (selectedCategory == "All") {
            meals
        } else {
            meals.filter {
                it.category == selectedCategory
            }
        }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        item {

            Text(
                "Meal Recommendations",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (budget > 0) {
                Text(
                    "Based on your remaining budget of ${money(moneyRemaining)}"
                )
            } else {
                Text(
                    "Set a budget on the Home screen to get budget-based recommendations."
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                categories.forEach { category ->

                    val selected =
                        selectedCategory == category

                    if (selected) {

                        Button(
                            onClick = {
                                selectedCategory = category
                            },
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(category)
                        }

                    } else {

                        OutlinedButton(
                            onClick = {
                                selectedCategory = category
                            },
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(category)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        items(
            items = filteredMeals,
            key = { it.id }
        ) { meal ->

            MealCard(
                meal = meal,
                budget = budget,
                moneyRemaining = moneyRemaining,
                shoppingList = shoppingList,
                onAddToShoppingList = onAddToShoppingList
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun MealCard(
    meal: Meal,
    budget: Double,
    moneyRemaining: Double,
    shoppingList: List<ShoppingItem>,
    onAddToShoppingList: (String) -> Unit
) {
    val affordable =
        budget <= 0.0 || meal.price <= moneyRemaining

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "${meal.emoji} ${meal.name}",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = money(meal.price),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = meal.category
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = meal.description
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            meal.ingredients.forEach { ingredient ->

                val alreadyAdded =
                    shoppingList.any {
                        it.name.equals(
                            ingredient,
                            ignoreCase = true
                        )
                    }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text("• $ingredient")

                    if (!alreadyAdded) {

                        IconButton(
                            onClick = {
                                onAddToShoppingList(ingredient)
                            }
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add ingredient"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (affordable) {

                Text(
                    text = "Within your budget",
                    color = MaterialTheme.colorScheme.primary
                )

            } else {

                Text(
                    text = "Above your remaining budget",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ShoppingScreen(
    modifier: Modifier = Modifier,
    shoppingList: List<ShoppingItem>,
    onRemoveItem: (ShoppingItem) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "Shopping List",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (shoppingList.isEmpty()) {

            Text(
                "Your shopping list is empty."
            )

        } else {

            LazyColumn {

                items(
                    items = shoppingList,
                    key = { it.id }
                ) { item ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = item.name,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                onRemoveItem(item)
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Remove"
                            )
                        }
                    }

                    Divider()
                }
            }
        }
    }
}

@Composable
fun StoresScreen(
    modifier: Modifier = Modifier
) {
    val context =
        androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(50.dp))

        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(70.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Find Grocery Stores",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "Find nearby stores where you can buy the ingredients on your shopping list."
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {

                val uri =
                    Uri.parse(
                        "https://www.google.com/maps/search/grocery+stores"
                    )

                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        uri
                    )

                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Find Stores")
        }
    }
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    profile: CustomerProfile,
    onLogout: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Icon(
            Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "My Profile",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(20.dp))

        ProfileRow(
            label = "Name",
            value = profile.name
        )

        ProfileRow(
            label = "Surname",
            value = profile.surname
        )

        ProfileRow(
            label = "Phone",
            value = profile.phone
        )

        ProfileRow(
            label = "Email",
            value = profile.email
        )

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {

            Icon(
                Icons.Default.Logout,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Logout")
        }
    }
}

@Composable
fun ProfileRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {

        Text(
            label,
            style = MaterialTheme.typography.labelLarge
        )

        Text(
            value.ifBlank { "Not provided" }
        )

        Divider(
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}


fun money(amount: Double): String {
    return "R " + String.format(
        Locale.US,
        "%.2f",
        amount
    )
}

fun defaultMeals(): List<Meal> {

    return listOf(

        Meal(
            id = "meal1",
            name = "Chicken Pasta",
            emoji = "🍝",
            category = "Lunch",
            price = 35.0,
            description = "Simple chicken pasta meal.",
            ingredients = listOf(
                "Chicken",
                "Pasta",
                "Tomato",
                "Onion"
            )
        ),

        Meal(
            id = "meal2",
            name = "Egg Fried Rice",
            emoji = "🍳",
            category = "Budget",
            price = 25.0,
            description = "Affordable fried rice with eggs.",
            ingredients = listOf(
                "Rice",
                "Eggs",
                "Onion",
                "Mixed vegetables"
            )
        ),

        Meal(
            id = "meal3",
            name = "Chicken Wrap",
            emoji = "🌯",
            category = "Lunch",
            price = 30.0,
            description = "Chicken and vegetables in a wrap.",
            ingredients = listOf(
                "Chicken",
                "Wrap",
                "Lettuce",
                "Tomato"
            )
        ),

        Meal(
            id = "meal4",
            name = "Tuna Sandwich",
            emoji = "🥪",
            category = "Lunch",
            price = 22.0,
            description = "Quick tuna sandwich.",
            ingredients = listOf(
                "Bread",
                "Tuna",
                "Mayonnaise",
                "Lettuce"
            )
        ),

        Meal(
            id = "meal5",
            name = "Vegetable Rice",
            emoji = "🍚",
            category = "Vegetarian",
            price = 18.0,
            description = "Rice with mixed vegetables.",
            ingredients = listOf(
                "Rice",
                "Mixed vegetables",
                "Onion"
            )
        ),

        Meal(
            id = "meal6",
            name = "Peanut Butter Toast",
            emoji = "🍞",
            category = "Breakfast",
            price = 15.0,
            description = "Quick and inexpensive breakfast.",
            ingredients = listOf(
                "Bread",
                "Peanut butter"
            )
        ),

        Meal(
            id = "meal7",
            name = "Omelette",
            emoji = "🍳",
            category = "Breakfast",
            price = 20.0,
            description = "Egg omelette with vegetables.",
            ingredients = listOf(
                "Eggs",
                "Onion",
                "Tomato"
            )
        ),

        Meal(
            id = "meal8",
            name = "Bean Curry",
            emoji = "🍛",
            category = "Vegetarian",
            price = 28.0,
            description = "Affordable bean curry.",
            ingredients = listOf(
                "Beans",
                "Tomato",
                "Onion",
                "Rice"
            )
        )
    )
}

fun seedMeals(
    db: FirebaseFirestore
) {
    defaultMeals().forEach { meal ->

        val mealData = hashMapOf(
            "name" to meal.name,
            "emoji" to meal.emoji,
            "category" to meal.category,
            "price" to meal.price,
            "description" to meal.description,
            "ingredients" to meal.ingredients
        )

        db.collection("meals")
            .document(meal.id)
            .set(mealData)
    }
}