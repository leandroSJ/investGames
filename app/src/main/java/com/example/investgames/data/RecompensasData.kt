package com.example.investgames.data

data class Recompensa(
    val id: Int,
    val titulo: String,
    val mensagem: String,
    val recompensa: String
)

val recompensas = listOf(
    Recompensa(
        id = 1,
        titulo = "1º Registro no aplicativo",
        mensagem = "Parabéns por essa iniciativa, você acaba de quebrar uma barreira em sua vida, e por isso você merece comemorar",
        recompensa = "Faça uma oração em agradecimento a Deus, por te conceder essa oportunidade de guardar algo. Dias bons virão! "
    ),
    Recompensa(
        id = 2,
        titulo = "2º aporte (entre 20 e 100)",
        mensagem = "A base é o fundamento de tudo, todo edifício começa da base e é construído aos poucos",
        recompensa = "Sair para comer uma acarajé, um açaí, uma pizza, assistir um filme legal com a família, faça sua escolha!"
    ),
    Recompensa(
        id = 3,
        titulo = "3º aporte (entre 200 e 300)",
        mensagem = "Graça e Paz, que Deus nosso pai vos abençoe e multiplique esse valor",
        recompensa = "Faça um passeio com sua esposa, vá para algum lugar, coma algo diferente, quebre a rotina!"
    ),
    Recompensa(
        id = 4,
        titulo = "4º aporte (acima de 350)",
        mensagem = "Você está dando passos largos, continue firme e forte!",
        recompensa = "Compre um livro que te inspire ou um presente simples para alguém que você ama."
    ),
    Recompensa(
        id = 5,
        titulo = "5º aporte (acima de 400)",
        mensagem = "A dedicação está rendendo frutos, parabéns!",
        recompensa = "Reserve um tempo para uma atividade que te relaxe: um banho quente, meditação ou caminhada."
    ),
    Recompensa(
        id = 6,
        titulo = "6º aporte (mais de 450)",
        mensagem = "A cada passo, você se aproxima da sua meta!",
        recompensa = "Planeje uma pequena viagem de fim de semana ou um jantar especial."
    ),
    Recompensa(
        id = 7,
        titulo = "7º aporte (mais de 500)",
        mensagem = "Você está no caminho certo, mantenha a constância!",
        recompensa = "Presenteie-se com algo que você deseja há tempos, um mimo para celebrar."
    ),
    Recompensa(
        id = 8,
        titulo = "8º aporte (mais de 550)",
        mensagem = "Persistência é a chave do sucesso, parabéns pela força!",
        recompensa = "Organize um encontro com amigos ou familiares para comemorar suas conquistas."
    ),
    Recompensa(
        id = 9,
        titulo = "9º aporte (mais de 600)",
        mensagem = "Seu esforço está valendo a pena, continue assim!",
        recompensa = "Invista em algo para seu desenvolvimento pessoal, curso online ou workshop."
    ),
    Recompensa(
        id = 10,
        titulo = "10º aporte (mais de 650)",
        mensagem = "Quase lá! Continue firme, seu sonho está próximo.",
        recompensa = "Faça um dia de autocuidado, cuidando de si mesmo com carinho e atenção."
    ),
    Recompensa(
        id = 11,
        titulo = "11º aporte (mais de 700)",
        mensagem = "Sua disciplina está te levando longe, parabéns!",
        recompensa = "Invista em uma experiência nova, um hobby ou aprendizado diferente."
    ),
    Recompensa(
        id = 12,
        titulo = "Meta Atingida!",
        mensagem = "Você conquistou sua meta, que orgulho!",
        recompensa = "Celebre essa vitória com uma festa, uma viagem ou algo que sempre quis fazer."
    )
)