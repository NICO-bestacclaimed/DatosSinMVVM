package com.example.datossinmvvm

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenProduct() {
    val context = LocalContext.current
    val db = crearDatabase(context)
    val dao = db.productDao()
    val coroutineScope = rememberCoroutineScope()

    var editingProductId by remember { mutableStateOf<Int?>(null) }
    var name by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    val productsList = remember { mutableStateListOf<Product>() }

    fun refreshProducts() {
        coroutineScope.launch {
            productsList.clear()
            productsList.addAll(dao.getAllProducts())
        }
    }

    LaunchedEffect(Unit) {
        refreshProducts()
    }

    val onSaveProduct = {
        val price = priceStr.toDoubleOrNull() ?: 0.0
        val stock = stockStr.toIntOrNull() ?: 0

        if (name.isNotBlank()) {
            coroutineScope.launch {
                val currentId = editingProductId
                if (currentId != null) {
                    val updated = Product(id = currentId, name = name, price = price, stock = stock, category = category)
                    dao.updateProduct(updated)
                    Toast.makeText(context, "Producto actualizado", Toast.LENGTH_SHORT).show()
                } else {
                    val newProduct = Product(name = name, price = price, stock = stock, category = category)
                    dao.insertProduct(newProduct)
                    Toast.makeText(context, "Producto registrado", Toast.LENGTH_SHORT).show()
                }
                editingProductId = null
                name = ""
                priceStr = ""
                stockStr = ""
                category = ""
                refreshProducts()
            }
        } else {
            Toast.makeText(context, "Ingrese el nombre del producto", Toast.LENGTH_SHORT).show()
        }
    }

    val onEliminarUltimo = {
        coroutineScope.launch {
            dao.deleteLastProduct()
            refreshProducts()
            Toast.makeText(context, "Último producto eliminado", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ejercicio 2: CRUD Productos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                actions = {
                    IconButton(onClick = { refreshProducts() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                    IconButton(onClick = { onEliminarUltimo() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar Último", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = if (editingProductId == null) "Agregar Nuevo Producto" else "Editar Producto ID: $editingProductId",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Producto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Precio (S/)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("Stock") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onSaveProduct() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(if (editingProductId == null) "Guardar Producto" else "Actualizar")
                }

                if (editingProductId != null) {
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            editingProductId = null
                            name = ""
                            priceStr = ""
                            stockStr = ""
                            category = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Lista de Productos Registrados:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (productsList.isEmpty()) {
                Text("No hay productos registrados en la base de datos.", fontSize = 14.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(productsList, key = { it.id }) { prod ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "ID: ${prod.id} - ${prod.name}", style = MaterialTheme.typography.titleSmall)
                                    Text(text = "Precio: S/ ${prod.price} | Stock: ${prod.stock} | Cat: ${prod.category}", fontSize = 13.sp)
                                }
                                IconButton(onClick = {
                                    editingProductId = prod.id
                                    name = prod.name
                                    priceStr = prod.price.toString()
                                    stockStr = prod.stock.toString()
                                    category = prod.category
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        dao.deleteProduct(prod)
                                        refreshProducts()
                                        Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
