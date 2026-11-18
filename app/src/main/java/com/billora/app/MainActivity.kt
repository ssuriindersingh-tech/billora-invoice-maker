
package com.billora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val Navy = Color(0xFF101C32)
private val Gold = Color(0xFFD5A84E)
private val Background = Color(0xFFF4F6FA)
private val Muted = Color(0xFF8490A1)

data class Invoice(
    val number: String,
    val customer: String,
    val total: Double
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BilloraApp()
        }
    }
}

@Composable
fun BilloraApp() {
    var currentPage by rememberSaveable { mutableStateOf("Dashboard") }
    val invoices = remember { mutableStateListOf<Invoice>() }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Navy,
            secondary = Gold,
            background = Background
        )
    ) {
        Scaffold(
            containerColor = Background,
            bottomBar = {
                NavigationBar(
                    containerColor = Navy
                ) {
                    NavigationBarItem(
                        selected = currentPage == "Dashboard",
                        onClick = { currentPage = "Dashboard" },
                        icon = { Text("⌂", fontSize = 22.sp) },
                        label = { Text("Home", color = Color.White) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Gold,
                            indicatorColor = Color(0xFF293B59),
                            unselectedIconColor = Color.White
                        )
                    )

                    NavigationBarItem(
                        selected = currentPage == "Create",
                        onClick = { currentPage = "Create" },
                        icon = { Text("+", fontSize = 22.sp) },
                        label = { Text("Create", color = Color.White) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Gold,
                            indicatorColor = Color(0xFF293B59),
                            unselectedIconColor = Color.White
                        )
                    )

                    NavigationBarItem(
                        selected = currentPage == "History",
                        onClick = { currentPage = "History" },
                        icon = { Text("▤", fontSize = 22.sp) },
                        label = { Text("History", color = Color.White) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Gold,
                            indicatorColor = Color(0xFF293B59),
                            unselectedIconColor = Color.White
                        )
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (currentPage) {
                    "Dashboard" -> DashboardScreen(
                        invoices = invoices,
                        onCreate = { currentPage = "Create" }
                    )

                    "Create" -> CreateInvoiceScreen(
                        onSave = { invoice ->
                            invoices.add(0, invoice)
                            currentPage = "History"
                        }
                    )

                    "History" -> HistoryScreen(invoices)
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    invoices: List<Invoice>,
    onCreate: () -> Unit
) {
    val totalRevenue = invoices.sumOf { it.total }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "Billora",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Navy
        )

        Text(
            "INVOICE & BILLING MAKER",
            fontSize = 11.sp,
            color = Muted,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(28.dp))

        Text(
            "Welcome back!",
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = Navy
        )

        Text(
            "Manage your business finances.",
            color = Muted
        )

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = onCreate,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold,
                contentColor = Navy
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "+  Create New Invoice",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(22.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                "Invoices",
                invoices.size.toString(),
                Modifier.weight(1f)
            )
            StatCard(
                "Revenue",
                formatMoney(totalRevenue),
                Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            "Recent Invoices",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Navy
        )

        Spacer(Modifier.height(12.dp))

        if (invoices.isEmpty()) {
            EmptyCard()
        } else {
            invoices.take(5).forEach { invoice ->
                InvoiceCard(invoice)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            Text(
                value,
                color = Navy,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("▤", fontSize = 35.sp, color = Gold)
            Spacer(Modifier.height(10.dp))
            Text(
                "No invoices yet",
                color = Navy,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Create your first professional invoice.",
                color = Muted,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun CreateInvoiceScreen(
    onSave: (Invoice) -> Unit
) {
    var customer by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("1") }
    var price by rememberSaveable { mutableStateOf("") }
    var tax by rememberSaveable { mutableStateOf("0") }

    val subtotal =
        (quantity.toDoubleOrNull() ?: 0.0) *
        (price.toDoubleOrNull() ?: 0.0)

    val taxAmount =
        subtotal * (tax.toDoubleOrNull() ?: 0.0) / 100

    val total = subtotal + taxAmount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "New Invoice",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Navy
        )

        Text(
            "Enter invoice details below.",
            color = Muted
        )

        Spacer(Modifier.height(24.dp))

        FormField("Customer Name", customer) {
            customer = it
        }

        FormField("Product / Service", description) {
            description = it
        }

        FormField(
            "Quantity",
            quantity,
            KeyboardType.Number
        ) { quantity = it }

        FormField(
            "Unit Price (₹)",
            price,
            KeyboardType.Decimal
        ) { price = it }

        FormField(
            "Tax (%)",
            tax,
            KeyboardType.Decimal
        ) { tax = it }

        Spacer(Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Navy
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                TotalLine("Subtotal", formatMoney(subtotal))
                TotalLine("Tax", formatMoney(taxAmount))

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Text(
                    "Grand Total",
                    color = Gold,
                    fontSize = 14.sp
                )

                Text(
                    formatMoney(total),
                    color = Color.White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = {
                if (
                    customer.isNotBlank() &&
                    description.isNotBlank() &&
                    quantity.toDoubleOrNull() != null &&
                    (quantity.toDoubleOrNull() ?: 0.0) > 0 &&
                    price.toDoubleOrNull() != null &&
                    (price.toDoubleOrNull() ?: -1.0) >= 0 &&
                    (tax.toDoubleOrNull() ?: -1.0) >= 0
                ) {
                    onSave(
                        Invoice(
                            number = "INV-${System.currentTimeMillis().toString().takeLast(6)}",
                            customer = customer,
                            total = total
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold,
                contentColor = Navy
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Save Invoice",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(25.dp))
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Column(Modifier.padding(bottom = 16.dp)) {
        Text(
            label,
            color = Navy,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(7.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            )
        )
    }
}

@Composable
fun TotalLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.8f))
        Text(value, color = Color.White)
    }

    Spacer(Modifier.height(10.dp))
}

@Composable
fun HistoryScreen(invoices: List<Invoice>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "Invoice History",
            fontSize = 26.sp,
            color = Navy,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        if (invoices.isEmpty()) {
            EmptyCard()
        } else {
            invoices.forEach { invoice ->
                InvoiceCard(invoice)
            }
        }
    }
}

@Composable
fun InvoiceCard(invoice: Invoice) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    invoice.customer,
                    color = Navy,
                    fontWeight = FontWeight.Bold
                )
                Text(invoice.number, color = Muted, fontSize = 12.sp)
            }

            Text(
                formatMoney(invoice.total),
                color = Navy,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun formatMoney(amount: Double): String {
    return NumberFormat.getCurrencyInstance(
        Locale("en", "IN")
    ).format(amount)
}
