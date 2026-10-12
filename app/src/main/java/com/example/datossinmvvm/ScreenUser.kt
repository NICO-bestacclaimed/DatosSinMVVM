package com.example.datossinmvvm

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenUser() {
    val context = LocalContext.current
    val db = crearDatabase(context)
    val dao = db.userDao()
    val coroutineScope = rememberCoroutineScope()

    var id by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    val dataUser = remember { mutableStateOf("") }

    val onAgregar = {
        if (firstName.isNotBlank() || lastName.isNotBlank()) {
            val user = User(0, firstName, lastName)
            coroutineScope.launch {
                AgregarUsuario(user = user, dao = dao)
                firstName = ""
                lastName = ""
                val data = getUsers(dao = dao)
                dataUser.value = data
                Toast.makeText(context, "Usuario agregado exitosamente", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Por favor ingrese nombre o apellido", Toast.LENGTH_SHORT).show()
        }
    }

    val onListar = {
        coroutineScope.launch {
            val data = getUsers(dao = dao)
            dataUser.value = data
            Toast.makeText(context, "Lista actualizada", Toast.LENGTH_SHORT).show()
        }
    }

    val onEliminarUltimo = {
        coroutineScope.launch {
            EliminarUltimoUsuario(dao = dao)
            val data = getUsers(dao = dao)
            dataUser.value = data
            Toast.makeText(context, "Último registro eliminado", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laboratorio 07: Usuarios") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { onAgregar() }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Usuario")
                    }
                    IconButton(onClick = { onListar() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Listar Usuarios")
                    }
                    IconButton(onClick = { onEliminarUltimo() }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar Último Registro",
                            tint = MaterialTheme.colorScheme.error
                        )
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Gestión de Usuarios (Room)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("ID (solo lectura)") },
                readOnly = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name:") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name:") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onAgregar() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Agregar", fontSize = 14.sp)
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { onListar() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Listar", fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { onEliminarUltimo() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar Último Registro", fontSize = 14.sp)
            }

            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Usuarios Registrados:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = if (dataUser.value.isEmpty()) "No hay datos para mostrar. Presiona 'Listar' o 'Agregar'." else dataUser.value,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun crearDatabase(context: Context): UserDatabase {
    return Room.databaseBuilder(
        context,
        UserDatabase::class.java,
        "user_db"
    ).build()
}

suspend fun getUsers(dao: UserDao): String {
    var rpta = ""
    val users = dao.getAll()
    users.forEach { user ->
        val fila = "${user.uid} - ${user.firstName} - ${user.lastName}\n"
        rpta += fila
    }
    return rpta
}

suspend fun AgregarUsuario(user: User, dao: UserDao) {
    try {
        dao.insert(user)
    } catch (e: Exception) {
        Log.e("User", "Error: insert: ${e.message}")
    }
}

suspend fun EliminarUltimoUsuario(dao: UserDao) {
    try {
        dao.deleteLast()
    } catch (e: Exception) {
        Log.e("User", "Error: deleteLast: ${e.message}")
    }
}
