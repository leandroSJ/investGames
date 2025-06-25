package com.example.investgames

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.*
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import com.example.investgames.components.PieChart
import com.example.investgames.data.SessionManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.investgames.ui.theme.InvestGamesTheme
import java.nio.file.WatchEvent
import com.example.investgames.*
import com.example.investgames.data.MetaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InvestGamesTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val sessionManager = remember { SessionManager(context) }

                // ✅ Verifica sessão ao abrir o app
                LaunchedEffect(Unit) {
                    val userName = sessionManager.getUserSession()
                    if (!userName.isNullOrBlank()) {
                        navController.navigate("main/$userName") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AnimatedNavHost(
                        navController = navController,
                        startDestination = "login",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth / 4 },
                                animationSpec = tween(durationMillis = 300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth / 8 },
                                animationSpec = tween(durationMillis = 300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> -fullWidth / 4 },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth / 8 },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("login") {
                            val scope = rememberCoroutineScope()
                            LoginScreen(
                                innerPadding = innerPadding,
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                },
                                onLoginSuccess = { userName ->
                                    // ✅ Salva a sessão e navega

                                    scope.launch {
                                        sessionManager.saveUserSession(userName)
                                        navController.navigate("main/$userName") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("main/{userName}") { backStackEntry ->
                            val userName = backStackEntry.arguments?.getString("userName") ?: "Usuário"
                            MainScreen(
                                userName = userName,
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("main/{$userName}") { inclusive = true }
                                    }
                                },
                                onShowRecompensa = {
                                    // ação de mostrar recompensa
                                }
                            )
                        }
                    }
                    }
                }
            }
        }
    }


@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userName: String,
    onLogout: () -> Unit,
    onShowRecompensa: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf("home", "aporte", "historico", "metas")
    val icons = listOf(Icons.Default.Home, Icons.Default.TrendingUp, Icons.Default.AttachMoney, Icons.Default.Flag)
    val labels = listOf("Início", "Inserir Aporte", "Histórico", "Metas")

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val currentRoute = currentBackStackEntry?.destination?.route
    var metaValor by remember { mutableStateOf(0f) }
    var metaObjetivo by remember { mutableStateOf("") }
    var totalAportado by remember { mutableStateOf(0f) }

    // 🔁 Carregar do banco de dados
    LaunchedEffect(Unit) {
        db.metasDao().getMetaAtual().collect { meta ->
            metaValor = meta?.valor ?: 0f
            metaObjetivo = meta?.objetivo ?: ""
        }
        totalAportado = db.aporteDao().obterTotalAportado() ?: 0f
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Olá, $userName",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onShowRecompensa) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificações")
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, route ->
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = {
                            navController.navigate(route) {
                                // Evita empilhar várias instâncias da mesma tela
                                launchSingleTop = true
                                // Pop up to home para limpar a pilha se necessário
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                restoreState = true
                            }
                        },
                        icon = { Icon(icons[index], contentDescription = labels[index]) },
                        label = { Text(labels[index]) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    userName = userName,
                    totalAportado = totalAportado, // você pode trocar pelo valor real, se tiver
                    meta = metaValor,       // mesma coisa: pode vir do banco ou ViewModel
                    metaObjetivo = metaObjetivo,
                    onNotificationClick = onShowRecompensa,
                    onLogoutClick = onLogout,
                    onNavigateInicio = { navController.navigate("home") },
                    onNavigateInserirAporte = { navController.navigate("aporte") },
                    onNavigateHistorico = { navController.navigate("historico") },
                    onNavigateMetas = { navController.navigate("metas") }
                )
            }


            composable("metas") {
                MetasScreen()
            }

        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    totalAportado: Float,
    meta: Float,
    metaObjetivo: String,
    onNotificationClick: () -> Unit,
    onNavigateInicio: () -> Unit,
    onNavigateInserirAporte: () -> Unit,
    onNavigateHistorico: () -> Unit,
    onNavigateMetas: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val aporteDisplay = if (totalAportado <= 0f) "R$ 0,00" else "R$ %.2f".format(totalAportado)
    val percentual = if (meta <= 0f) 0f else (totalAportado / meta).coerceIn(0f, 1f)

    Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                "Total aportado",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                aporteDisplay,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp),
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Gráfico de pizza usando Canvas
        PieChart(percent = percentual)

            Text(
                text = "Meta atual: R$ %.2f".format(meta),
                style = MaterialTheme.typography.bodyLarge
            )
            if (metaObjetivo.isNotBlank()){
                Text(
                    text = "objetivo: $metaObjetivo",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top =4.dp)
                )
            }
        }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetasScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    var valorMeta by remember { mutableStateOf("") }
    var objetivoSelecionado by remember { mutableStateOf("") }
    var outroObjetivo by remember { mutableStateOf("") }
    var mostrarCardRecompensa by remember { mutableStateOf(false) }
    var mostrarMetaAtual by remember { mutableStateOf(false) }
    var metaAtual by remember { mutableStateOf<MetaEntity?>(null) }

    val opcoes = listOf("Carro", "Reserva de emergência", "Viagens", "Outros")
    val icones = listOf(Icons.Default.DirectionsCar, Icons.Default.Savings, Icons.Default.Flight, Icons.Default.Edit)

        // Carregar meta atual do banco
    LaunchedEffect(Unit) {
        db.metasDao().getMetaAtual().collect { meta ->
            metaAtual = meta
            if (meta != null) {
                valorMeta = meta.valor.toString()
                objetivoSelecionado = meta.objetivo
            } else {
                valorMeta = ""
                objetivoSelecionado = ""
            }
        }
    }


        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text("Definir Metas", style = MaterialTheme.typography.headlineMedium)
        }
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Valor da meta R$", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = valorMeta,
                onValueChange = { valorMeta = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ex: 1000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            if (mostrarMetaAtual) {
                if (metaAtual != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text("Meta Atual", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Valor: R$ %.2f".format(metaAtual!!.valor), style = MaterialTheme.typography.bodyLarge)
                            Text("Objetivo: ${metaAtual!!.objetivo}", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    // Excluir meta e objetivo no banco
                                    CoroutineScope(Dispatchers.IO).launch {
                                        db.metasDao().deleteTodasMetas()
                                    }
                                    // Limpar estado local e esconder card
                                    metaAtual = null
                                    valorMeta = ""
                                    objetivoSelecionado = ""
                                    mostrarMetaAtual = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Excluir Meta")
                            }
                        }
                    }
                } else {
                    Text("Nenhuma meta definida", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (!mostrarMetaAtual) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Qual seu objetivo?", style = MaterialTheme.typography.labelLarge)
                opcoes.forEachIndexed { i, item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = objetivoSelecionado == item,
                            onCheckedChange = {
                                objetivoSelecionado = if (it) item else ""
                            }
                        )
                        Icon(icones[i], contentDescription = item, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item)
                    }
                }

                if (objetivoSelecionado == "Outros") {
                    OutlinedTextField(
                        value = outroObjetivo,
                        onValueChange = { outroObjetivo = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Descreva seu objetivo") }
                    )
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val valor = valorMeta.toFloatOrNull() ?: 0f
                    val objetivo = if (objetivoSelecionado == "Outros") outroObjetivo.trim() else objetivoSelecionado

                    if (valor > 0f && objetivo.isNotBlank()) {
                        val novaMeta = MetaEntity(valor = valor, objetivo = objetivo)

                        scope.launch {
                            db.metasDao().insertMeta(novaMeta)

                            // Atualizar estado local para refletir a nova meta salva
                            metaAtual = novaMeta
                            valorMeta = novaMeta.valor.toString()
                            objetivoSelecionado = novaMeta.objetivo

                            // Fecha o card da meta atual
                            mostrarMetaAtual = false

                            // Opcional: mostrar uma mensagem toast confirmando o salvamento
                            Toast.makeText(context, "Meta salva com sucesso!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Preencha valor e objetivo corretamente", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Definir nova meta")
            }

            // Botão para mostrar/esconder o card da meta atual

            Button(onClick = { mostrarMetaAtual = !mostrarMetaAtual },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                ) {
                Text(if (mostrarMetaAtual) "Ocultar Meta Atual" else "Ver Meta Atual")
            }

            Spacer(modifier = Modifier.height(16.dp))





            Spacer(modifier = Modifier.height(32.dp))

            if (mostrarCardRecompensa) {
                Spacer(modifier = Modifier.height(32.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Cadastrar Recompensa", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            label = { Text("Descrição da recompensa") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { mostrarCardRecompensa = false }) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }



}



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    innerPadding: PaddingValues = PaddingValues(0.dp),
    onNavigateToRegister: () -> Unit = {},
    onLoginSuccess: (String) -> Unit
) {
    var emailOrCpf by remember {mutableStateOf("")}
    var password by remember {mutableStateOf("")}
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo InvestGames",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 16.dp)
        )
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = emailOrCpf,
            onValueChange = {emailOrCpf = it},
            label = {Text("E-mail ou CPF")},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {password = it},
            label = { Text("Senha")},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Default.Visibility
                else Icons.Default.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = image,
                        contentDescription = if (passwordVisible) "Esconder senha" else "Mostrar senha"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val db = remember { AppDatabase.getDatabase(context) }
        val sessionManager = remember { SessionManager(context) }
        Button(
            onClick = {
                scope.launch {
                    val user = db.userDao().login(emailOrCpf, password)
                    if (user != null) {
                        Toast.makeText(context, "Bem-vindo, ${user.name}", Toast.LENGTH_SHORT).show()
                        onLoginSuccess(user.name)
                    } else {
                        Toast.makeText(context, "E-mail, CPF ou senha inválidos", Toast.LENGTH_SHORT).show()
                    }
                }
            },
                modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
                }
        Text(
            text = "Cadastre-se",
            style = MaterialTheme.typography.bodyMedium,
            color =  MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .clickable{
                    onNavigateToRegister()
                }
        )



    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit = {}) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo InvestGames",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp)
        )
        Text("Cadastro", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = cpf,
            onValueChange = { cpf = it },
            label = { Text("CPF") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(24.dp))

        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val db = remember { AppDatabase.getDatabase(context) }

        Button(onClick = {
            if (name.isNotBlank() && email.isNotBlank() && cpf.isNotBlank() && password.isNotBlank() && !isLoading) {
                isLoading = true
                if (!isCpfValid(cpf)) {
                    Toast.makeText(context, "CPF inválido", Toast.LENGTH_SHORT).show()
                    return@Button // Interrompe a ação do botão
                }
                val newUser = User(cpf = cpf, name = name, email = email, password = password)

                scope.launch {
                    db.userDao().insertUser(newUser)
                    isLoading = false
                    onRegisterSuccess()
                }
            }
        }, modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            else Text("Cadastrar")
        }
    }
}

fun isCpfValid(cpf: String): Boolean {
    val cleanCpf = cpf.filter { it.isDigit() }

    if (cleanCpf.length != 11 || cleanCpf.all { it == cleanCpf[0] }) return false

    val numbers = cleanCpf.map { it.toString().toInt() }

    // Valida o primeiro dígito verificador
    val firstDigit = (0..8).sumOf { (10 - it) * numbers[it] } % 11
    val expectedFirst = if (firstDigit < 2) 0 else 11 - firstDigit
    if (numbers[9] != expectedFirst) return false

    // Valida o segundo dígito verificador
    val secondDigit = (0..9).sumOf { (11 - it) * numbers[it] } % 11
    val expectedSecond = if (secondDigit < 2) 0 else 11 - secondDigit
    if (numbers[10] != expectedSecond) return false

    return true
}
/*
@Preview(showBackground = true)
@Composable
fun MetasScreenPreview() {
    InvestGamesTheme {
        MainScreen(
            userName = "Leandro SJ",
            onLogout = {},
            onShowRecompensa = {}
        )
    }
}
*/

@Preview(showBackground = true)
@Composable
fun MetasScreenPreview() {
    InvestGamesTheme {
        MetasScreen()
    }
}