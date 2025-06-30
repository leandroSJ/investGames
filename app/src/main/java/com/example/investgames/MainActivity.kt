package com.example.investgames

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.investgames.ui.theme.InvestGamesTheme
import java.nio.file.WatchEvent
import com.example.investgames.*
import com.example.investgames.data.MetaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import androidx.compose.foundation.lazy.items
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.investgames.data.Recompensa
import com.example.investgames.data.recompensas
import java.util.Date
import android.Manifest
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontStyle
import com.example.investgames.data.checkRecompensas
import kotlinx.coroutines.flow.firstOrNull


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        criarCanalNotificacao(applicationContext)
        solicitarPermissaoNotificacao()
        setContent {
            InvestGamesTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val sessionManager = remember { SessionManager(context) }
                val prefs = context.getSharedPreferences("recompensas", Context.MODE_PRIVATE)
                prefs.edit().clear().apply()


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
    private fun solicitarPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}

fun criarCanalNotificacao(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val nome = "Recompensas"
        val descricao = "Notificações das recompensas de aportes"
        val importancia = NotificationManager.IMPORTANCE_DEFAULT
        val canal = NotificationChannel("recompensa_channel", nome, importancia).apply {
            description = descricao
        }
        val manager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(canal)
    }
}

fun enviarNotificacao(context: Context, recompensa: Recompensa) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) {
        // Sem permissão, não pode enviar a notificação
        return
    }

    val builder = NotificationCompat.Builder(context, "recompensa_channel")
        .setSmallIcon(android.R.drawable.ic_dialog_info) // Substitua pelo seu ic_reward se desejar
        .setContentTitle(recompensa.titulo)
        .setContentText(recompensa.mensagem)
        .setStyle(NotificationCompat.BigTextStyle().bigText("${recompensa.mensagem}\n\n${recompensa.recompensa}"))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(context)) {
        notify((1000..9999).random(), builder.build())
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

    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showSobreDialog by remember { mutableStateOf(false) }
    var showDicasDialog by remember { mutableStateOf(false) }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val currentRoute = currentBackStackEntry?.destination?.route
    var metaValor by remember { mutableStateOf(0f) }
    var metaObjetivo by remember { mutableStateOf("") }
    var totalAportado by remember { mutableStateOf(0f) }

    // 🔁 Carregar do banco de dados
    // 🔁 Atualiza sempre que navegar para a tela "home"
    LaunchedEffect(currentRoute) {
        if (currentRoute == "home") {
            db.metasDao().getMetaAtual().collect { meta ->
                metaValor = meta?.valor ?: 0f
                metaObjetivo = meta?.objetivo ?: ""
            }
            totalAportado = db.aporteDao().obterTotalAportado() ?: 0f
        }
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

                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair")
                    }

                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Notas da versão") },
                                onClick = {
                                    expanded = false
                                    showDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sobre o App") },
                                onClick = {
                                    expanded = false
                                    showSobreDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Dicas de Investimento") },
                                onClick = {
                                    expanded = false
                                    showDicasDialog = true
                                }
                            )

                        }
                    }
                }
                ,
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
                    onNotificationClick = onShowRecompensa,
                    onLogoutClick = onLogout,
                    onNavigateInicio = { navController.navigate("home") },
                    onNavigateInserirAporte = { navController.navigate("aporte") },
                    onNavigateHistorico = { navController.navigate("historico") },
                    onNavigateMetas = { navController.navigate("metas") },
                    onNavigateDicas = { navController.navigate("dicas") }

                )
            }


            composable("metas") {
                MetasScreen()
            }

            composable("aporte") {
                InserirAporte()
            }

            composable("historico") {
                HistoricoAportesScreen(userName = userName)
            }



        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Fechar")
                }
            },
            title = { Text("Notas da versão: V2.2.0-beta\n") },
            text = {
                Column {
                    Text("- Adicionado sistema de recompensas' \n")
                    Text("- Adicionado notificações' \n")
                    Text("- Adicionado Botão resetar recompensas \n")
                    Text("- Correções de Bugs \n")
                    Text("- OBS: Essa é a ultima versao \n")
                    Text("- Estarei focando em outros projetos... \n")

                }
            }
        )
    }
    if (showSobreDialog) {
        AlertDialog(
            onDismissRequest = { showSobreDialog = false },
            confirmButton = {
                TextButton(onClick = { showSobreDialog = false }) {
                    Text("Fechar")
                }
            },
            title = { Text("Sobre o InvestGames\n") },
            text = {
                Column {
                    Text("- Versão: V2.2.0-beta.\n")
                    Text("- Dev: Leandro de Jesus. \n")
                    Text("- Objetivo: Acompanhar aportes financeiros e metas de forma gamificada. \n")
                    Text("- Aviso: Este app não oferece recomendações financeiras. Use por sua conta e risco.\n")
                }
            }
        )
    }

    if (showDicasDialog) {
        AlertDialog(
            onDismissRequest = { showDicasDialog = false },
            confirmButton = {
                TextButton(onClick = { showDicasDialog = false }) {
                    Text("Fechar")
                }
            },
            title = {
                Text("📘 Plano de Sobrevivência Financeira em Tempos de Crise")
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {
                    Text(
                        "OBS: Nada disso aqui é garantia de ganho, porém eu, Leandro, vou fazer dessa forma. Cada um faz por sua conta e risco.",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Estamos vivendo o que pode ser o prelúdio de uma das maiores rupturas geopolíticas da história moderna. As peças estão se movendo rapidamente:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        Text("• Tensões militares entre Rússia, China, Irã, Israel, EUA e OTAN.")
                        Text("• Conflitos com choques econômicos: sanções, bloqueios e sabotagens.")
                        Text("• Manipulação de preços de commodities como arma econômica.")
                        Text("• Inflação global descontrolada e instabilidade monetária.")
                        Text("• EUA podem surgir como “salvador mundial” em cenário forjado.")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("🛡️ Fase 1 – Construção da Linha de Defesa: Reserva de Guerra (0 a 8 meses)",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🎯 Objetivo: Garantir que você não será forçado a vender ativos no pior momento.")

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🔒 Meta de Reserva: De R$ 3.000 a R$ 5.000")

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🔑 Onde guardar:", fontWeight = FontWeight.SemiBold)
                    Column {
                        Text("• Tesouro Selic")
                        Text("• Conta remunerada com liquidez diária (ex: PicPay, Nubank, Itaú)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📌 Por quê? Acesso rápido ao dinheiro em caso de guerra, hiperinflação ou lockdown.")

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("🔥 Fase 2 – Blindagem contra Inflação e Colapsos Econômicos (8 a 18 meses)",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🎯 Objetivo: Proteger seu poder de compra com ativos que sobem em crises.")

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("💰 Alocação recomendada:", fontWeight = FontWeight.SemiBold)
                    Column {
                        Text("• Ouro (GOLD11 ou fundos lastreados) – 30%")
                        Text("• Tesouro IPCA+ (2029 ou 2035) – 30%")
                        Text("• Fundos/ETFs de Commodities – 20%")
                        Text("• Liquidez (caixa extra) – 20%")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🔎 Observação: Dólar pode explodir. Considere ativos dolarizados (ex: IVVB11).")

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("🚀 Fase 3 – Ataque Estratégico: Comprar Ativos em Pânico (2026 em diante)",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🎯 Objetivo: Enquanto o mundo está com medo, você vai às compras.")

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🛡️ Regras de ouro:", fontWeight = FontWeight.SemiBold)
                    Column {
                        Text("• Comprar empresas resilientes (energia, saneamento, exportadoras).")
                        Text("• Evitar varejo alimentar tradicional (histórico de falências).")
                        Text("• Varejo só se for Assaí ou Carrefour, no máximo 5% do portfólio.")
                        Text("• ETFs internacionais quando o dólar aliviar.")
                        Text("• FIIs com grandes descontos.")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("🎯 Fase 4 – Construção da Bola de Neve Patrimonial (2027 em diante)",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🎯 Objetivo: Sair da linha de pobreza patrimonial e acumular ativos.")

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📈 Seu foco a partir daqui:", fontWeight = FontWeight.SemiBold)
                    Column {
                        Text("• Manter aportes mensais.")
                        Text("• Reinvestir todos os dividendos.")
                        Text("• Ver quedas como oportunidades.")
                        Text("• Estudar temas avançados:")
                        Text("    - Internacionalização de patrimônio")
                        Text("    - Moedas fortes e criptomoedas")
                        Text("    - Fundos estruturados e alternativos")
                    }
                }
            }
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    onNotificationClick: () -> Unit,
    onNavigateInicio: () -> Unit,
    onNavigateInserirAporte: () -> Unit,
    onNavigateHistorico: () -> Unit,
    onNavigateMetas: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateDicas: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    val metaState by db.metasDao().getMetaAtual().collectAsState(initial = null)
    val totalAportado by produceState(initialValue = 0f) {
        value = db.aporteDao().obterTotalAportado() ?: 0f
    }

    val aporteDisplay = if (totalAportado <= 0f) "R$ 0,00" else "R$ %.2f".format(totalAportado)
    val percentual = if ((metaState?.valor ?: 0f) <= 0f) 0f else (totalAportado / (metaState?.valor ?: 1f)).coerceIn(0f, 1f)

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

        PieChart(percent = percentual)

        Text(
            text = "Meta atual: R$ %.2f".format(metaState?.valor ?: 0f),
            style = MaterialTheme.typography.bodyLarge
        )
        if (!metaState?.objetivo.isNullOrBlank()) {
            Text(
                text = "Objetivo: ${metaState?.objetivo}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun InserirAporte() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    var valorAporte by remember { mutableStateOf("") }
    var mostrarAporteAtual by remember { mutableStateOf(false) }
    var totalAportado by remember { mutableStateOf(0f) }

    // Carregar total atual
    LaunchedEffect(Unit) {
        totalAportado = db.aporteDao().obterTotalAportado() ?: 0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Inserir Aporte", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))
        Text("Valor do aporte", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(
            value = valorAporte,
            onValueChange = { valorAporte = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val valor = valorAporte.toFloatOrNull()
                if (valor != null && valor > 0) {
                    scope.launch {
                        db.aporteDao().inserirAporte(AporteEntity(valor = valor))
                        valorAporte = ""
                        totalAportado = db.aporteDao().obterTotalAportado() ?: 0f

                        // ✅ Obtém meta atual do banco
                        val meta = db.metasDao().getMetaAtual().firstOrNull()
                        val valorMeta = meta?.valor ?: 0f

                        // ✅ 1. Verifica e desbloqueia recompensas com base no total acumulado
                        checkRecompensas(
                            context = context,
                            totalAportado = totalAportado,
                            valorMeta = valorMeta
                        ) { recompensa ->
                            if (recompensa.id == 1 || recompensa.id == 12) {
                                enviarNotificacao(context, recompensa)
                            }
                        }

                        // ✅ 2. Notificação baseada no valor do aporte atual (pode repetir)
                        val recompensaNotificacao = when {
                            valor in 20f..100f -> recompensas.first { it.id == 2 }
                            valor in 200f..300f -> recompensas.first { it.id == 3 }
                            valor > 350f && valor < 400f -> recompensas.first { it.id == 4 }
                            valor >= 400f && valor < 450f -> recompensas.first { it.id == 5 }
                            valor == 450f -> recompensas.first { it.id == 6 }
                            valor == 500f -> recompensas.first { it.id == 7 }
                            valor == 550f -> recompensas.first { it.id == 8 }
                            valor == 600f -> recompensas.first { it.id == 9 }
                            valor == 650f -> recompensas.first { it.id == 10 }
                            valor >= 700f -> recompensas.first { it.id == 11 }
                            else -> null
                        }

                        recompensaNotificacao?.let {
                            enviarNotificacao(context, it) // sempre que valor bater
                        }
                    }

                    Toast.makeText(context, "Aporte inserido com sucesso!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Inserir aporte")
        }


        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { mostrarAporteAtual = !mostrarAporteAtual },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            Text(if (mostrarAporteAtual) "Ocultar Aporte" else "Ver Aporte")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (mostrarAporteAtual) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Total aportado: R$ %.2f".format(totalAportado), style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                db.aporteDao().deleteTodosAportes()
                                totalAportado = 0f
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Excluir Aporte")
                    }
                }
            }
        }
        Button(
            onClick = {
            val prefs = context.getSharedPreferences("recompensas", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            Toast.makeText(context, "Recompensas resetadas!", Toast.LENGTH_SHORT).show()
        },modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
            ){
            Text("Resetar Recompensas")
        }

    }
}

@Composable
fun HistoricoAportesScreen(userName: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    var listaAportes by remember { mutableStateOf(emptyList<AporteEntity>()) }

    // Carrega todos os aportes assim que a tela for aberta
    LaunchedEffect(Unit) {
        db.aporteDao().getTodosAportes().collect { aportes ->
            listaAportes = aportes
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Histórico de Aportes", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        if (listaAportes.isEmpty()) {
            Text("Nenhum aporte registrado ainda.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                items(listaAportes) { aporte ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Data: ${SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(aporte.data))}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text("Usuário: $userName", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "Valor: R$ %.2f".format(aporte.valor),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                db.aporteDao().deleteTodosAportes()
                                listaAportes = emptyList()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text("Excluir Todos os Aportes")
                    }
                }
            }
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