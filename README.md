# StudyMate 3 📚

Um aplicativo Android para gerenciamento de estudos, desenvolvido em Kotlin com Jetpack Compose, oferecendo uma experiência completa de organização acadêmica.

## 🎯 Visão Geral

O StudyMate 3 é um aplicativo de produtividade acadêmica que combina calendário de aulas, gerenciamento de tarefas e técnica Pomodoro em uma interface intuitiva e moderna. Ideal para estudantes que buscam uma ferramenta completa para organizar sua rotina de estudos.

## ✨ Funcionalidades Principais

### 📅 **Calendário de Aulas**
- **Gerenciamento de Disciplinas**: Cadastro completo de matérias com professor, local e cor personalizada
- **Horários Semanais**: Visualização por dia da semana com horários detalhados
- **Notificações Inteligentes**: Alertas automáticos antes das aulas (configurável)
- **Interface Intuitiva**: Seleção fácil de dias e visualização clara dos horários

### ✅ **Gerenciamento de Tarefas**
- **Criação Avançada**: Tarefas com nome, descrição, data/hora de entrega e prioridade
- **Sistema de Prioridades**: Classificação em Baixa, Média e Alta prioridade
- **Filtros Poderosos**: Filtragem por disciplina, período de tempo e status
- **Vinculação com Disciplinas**: Associação automática de tarefas às matérias
- **Controle de Status**: Marcar tarefas como concluídas/pendentes
- **Notificações de Prazo**: Alertas para deadlines importantes

### ⏱️ **Pomodoro Timer**
- **Timer Personalizável**: Configuração de tempos de foco, pausa curta e pausa longa
- **Três Modos de Operação**:
  - 🍅 **Pomodoro**: Período de foco (padrão: 25 min)
  - ☕ **Pausa Curta**: Descanso breve (padrão: 5 min)
  - 🛋️ **Pausa Longa**: Descanso prolongado (padrão: 15 min)
- **Controles Completos**: Play, pause e reset do timer
- **Interface Visual**: Indicador circular de progresso animado
- **Rastreamento de Sessões**: Histórico de sessões Pomodoro realizadas

### 📱 **Widgets para Tela Inicial**
- **Widget de Tarefas**: Visualização rápida das próximas tarefas pendentes
- **Widget Pomodoro**: Controle do timer diretamente da tela inicial
- **Atualizações em Tempo Real**: Sincronização automática com o app principal

## 🏗️ Arquitetura Técnica

### **Stack Tecnológico**
- **Linguagem**: Kotlin 100%
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Arquitetura**: MVVM + Repository Pattern
- **Injeção de Dependência**: Dagger Hilt
- **Banco de Dados**: Room Database
- **Navegação**: Navigation Compose
- **Gerenciamento de Estado**: StateFlow/LiveData
- **Armazenamento**: DataStore Preferences
- **Notificações**: AlarmManager + NotificationManager
- **Background Tasks**: WorkManager

### **Estrutura do Projeto**
```
app/
├── data/
│   ├── dao/              # Data Access Objects
│   ├── model/            # Entidades do banco de dados
│   ├── repository/       # Repositórios
│   └── util/             # Type Converters
├── di/                   # Módulos de injeção de dependência
├── receiver/             # BroadcastReceivers para notificações
├── ui/
│   ├── navigation/       # Navegação do app
│   ├── screen/          # Telas principais
│   │   ├── calendar/    # Funcionalidades do calendário
│   │   ├── pomodoro/    # Timer Pomodoro
│   │   └── tasks/       # Gerenciamento de tarefas
│   ├── theme/           # Tema e estilos
│   └── widget/          # Widgets da tela inicial
└── util/                # Utilitários gerais
```

### **Modelos de Dados**

#### **Subject (Disciplina)**
```kotlin
data class Subject(
    val id: Long,
    val name: String,
    val professor: String?,
    val location: String,
    val color: Int,
    val notificationMinutesBefore: Int = 15,
    val position: Int = 0
)
```

#### **Task (Tarefa)**
```kotlin
data class Task(
    val id: Long,
    val name: String,
    val description: String?,
    val dueDate: LocalDateTime,
    val priority: Priority, // LOW, MEDIUM, HIGH
    val subjectId: Long,
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime
)
```

#### **PomodoroSession (Sessão Pomodoro)**
```kotlin
data class PomodoroSession(
    val id: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val focusDurationMinutes: Int,
    val breakDurationMinutes: Int,
    val completedCycles: Int,
    val taskId: Long?,
    val totalFocusTimeMinutes: Int
)
```

## 🚀 Requisitos do Sistema

- **Android**: API 31+ (Android 12+)
- **RAM**: Mínimo 2GB recomendado
- **Armazenamento**: ~50MB para instalação
- **Permissões**:
  - Notificações
  - Alarmes exatos
  - Boot receiver (para reagendar notificações)

## 🛠️ Configuração para Desenvolvimento

### **Pré-requisitos**
- Android Studio Hedgehog ou superior
- JDK 11+
- Android SDK 35
- Gradle 8.2+


## 🎨 Design e UX

### **Material Design 3**
- **Tema Dinâmico**: Suporte a cores dinâmicas do sistema (Android 12+)
- **Dark/Light Mode**: Tema automático baseado nas configurações do sistema
- **Componentes Modernos**: Cards, FABs, Navigation Bar, TopAppBar
- **Animações Fluidas**: Transições suaves entre telas e estados

### **Acessibilidade**
- **Content Descriptions**: Descrições para leitores de tela
- **Contraste Adequado**: Cores que atendem diretrizes WCAG
- **Tamanhos Tocáveis**: Elementos com pelo menos 48dp de área

## 🔄 Fluxo de Trabalho Típico

1. **📚 Configuração Inicial**
   - Cadastrar disciplinas com horários
   - Configurar notificações
   - Personalizar tempos do Pomodoro

2. **📝 Gerenciamento Diário**
   - Verificar horário de aulas do dia
   - Adicionar/editar tarefas pendentes
   - Executar sessões Pomodoro

3. **📊 Acompanhamento**
   - Marcar tarefas concluídas
   - Revisar histórico de estudos
   - Ajustar configurações conforme necessário

## 🔒 Privacidade e Dados

- **Armazenamento Local**: Todos os dados ficam no dispositivo
- **Sem Cloud**: Nenhuma informação é enviada para servidores externos
- **Backup Manual**: Usuário controla exportação de dados
- **Permissões Mínimas**: Apenas o necessário para funcionamento

## 🚀 Versões e Atualizações

### **Versão Atual: 1.0**
- ✅ Calendário de aulas completo
- ✅ Gerenciamento de tarefas
- ✅ Timer Pomodoro
- ✅ Widgets para tela inicial
- ✅ Sistema de notificações
- ✅ Material Design 3

### **Roadmap Futuro**
- 🔄 Sincronização em nuvem (opcional)
- 📚 Integração com calendário do sistema

---

**StudyMate 3** - Transformando a forma como você organiza seus estudos! 🎓✨
