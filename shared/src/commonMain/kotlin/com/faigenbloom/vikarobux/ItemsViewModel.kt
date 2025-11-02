package com.faigenbloom.vikarobux

import com.faigenbloom.vikarobux.models.Item
import com.faigenbloom.vikarobux.models.ItemWrapper
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock.System
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime

class ItemsViewModel(
    val repository: RemoteRepository,
) : BaseViewModel() {
    private val settings: Settings = Settings()
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items = _items.asStateFlow()
    private val _hintItems = MutableStateFlow<List<String>>(emptyList())
    val hintItems = _hintItems.asStateFlow()
    private val _total = MutableStateFlow<Int>(0)
    val total = _total.asStateFlow()
    private var lastUpdateDate: Long? = null

    init {
        load()
    }

    @OptIn(ExperimentalTime::class)
    private fun updateDate(): String {
        val now = System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val date = "${now.date} ${now.time.hour}:${now.time.minute.toString().padStart(2, '0')}"
        lastUpdateDate = parseDateToMillis(date);
        return date
    }

    @OptIn(ExperimentalTime::class)
    fun addItem(description: String, quantity: Int) {
        val now = System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val date = updateDate()

        val newItem = Item(
            id = now.toString(),
            date = date,
            description = description,
            quantity = quantity,
            isDone = false
        )

        _items.update { old ->
            (old + newItem).sortedByDescending { it.parseDateTime() }
        }
        _hintItems.update { _items.value.map { it.description }.distinct() }
        _total.update { _items.value.sumOf { if (it.isDone.not()) it.quantity else 0 } }
        saveToPrefs()

        launch {
            repository.addItem(newItem)
        }
    }

    fun markAsDone(id: String, isDone: Boolean = true) {
        _items.update { list ->
            list.map { item ->
                if (item.id == id) item.copy(isDone = isDone) else item
            }.sortedByDescending { it.parseDateTime() }
        }
        _hintItems.update { _items.value.map { it.description }.distinct() }
        _total.update { _items.value.sumOf { if (it.isDone.not()) it.quantity else 0 } }
        updateDate()
        saveToPrefs()
        launch {
            repository.markDone(id, isDone)
        }
    }

    private fun saveToPrefs() {
        val json = Json.encodeToString(ItemWrapper(_items.value, lastUpdateDate))
        settings.putString("key", json)
    }

    private fun load() {
        launch {
            val json = settings.getStringOrNull("key")
            if (json != null) {
                runCatching {
                    val items = Json.decodeFromString<List<Item>>(json).sortedByDescending { it.parseDateTime() }
                    if (_items.value.size < items.size) {
                        _items.update { items }
                        saveToPrefs()
                    }
                    _hintItems.value = _items.value.map { it.description }.distinct()
                    _total.update { _items.value.sumOf { if (it.isDone.not()) it.quantity else 0 } }
                }
            }

            val items = repository.getItems()
            if (_items.value.size < items.items.size) {
                _items.update { items.items }
                saveToPrefs()
                _hintItems.value = _items.value.map { it.description }.distinct()
                _total.update { _items.value.sumOf { if (it.isDone.not()) it.quantity else 0 } }
            }
        }
      
    }


    private fun migrate() {
        launch {
           repository.addItem(Item(id = "2025-11-01T17:35:56.407973", date = "2025-11-01 17:35", description = "За телефончик", quantity = 40, isDone = false))
           repository.addItem(Item(id = "2025-11-01T17:35:56.322233", date = "2025-11-01 17:34", description = "За телефончик", quantity = 40, isDone = false))
           repository.addItem(Item(id = "2025-11-01T17:35:56.323232", date = "2025-11-01 17:33", description = "За телефончик", quantity = 40, isDone = false))
           repository.addItem(Item(id = "2025-11-01T17:35:56.111213", date = "2025-11-01 17:32", description = "За телефончик", quantity = 40, isDone = false))
           repository.addItem(Item(id = "2025-11-01T17:35:56.3567786", date = "2025-11-01 17:31", description = "За телефончик", quantity = 40, isDone = false))
           repository.addItem(Item(id = "2025-11-01T17:35:56.677676", date = "2025-11-01 17:30", description = "За телефончик", quantity = 40, isDone = false))
           repository.addItem(Item(id = "2025-11-01T17:35:56.768855", date = "2025-11-01 17:29", description = "За еду", quantity = 60, isDone = false))
           repository.addItem(Item(id = "2025-11-01T17:35:56.5675676", date = "2025-11-01 17:28", description = "За 10 по информатике", quantity = 20, isDone = false))
           repository.addItem(Item(id = "2025-11-01T17:35:56.567869", date = "2025-11-01 17:27", description = "За 10 по информатике", quantity = 20, isDone = false))
           repository.addItem(Item(id = "2025-11-01T17:35:56.218704", date = "2025-11-01 17:26", description = "За еду", quantity = 10, isDone = false))
        }
    }

    // --- Парсим дату обратно в LocalDateTime для сортировки
    private fun Item.parseDateTime(): LocalDateTime {
        // формат был "yyyy-MM-dd HH:mm"
        val (datePart, timePart) = date.split(" ")
        val (year, month, day) = datePart.split("-").map { it.toInt() }
        val (hour, minute) = timePart.split(":").map { it.toInt() }
        return LocalDateTime(year, month, day, hour, minute)
    }

    @OptIn(ExperimentalTime::class)
    fun parseDateToMillis(dateString: String): Long {
        val (datePart, timePart) = dateString.split(" ")
        val (year, month, day) = datePart.split("-").map { it.toInt() }
        val (hour, minute) = timePart.split(":").map { it.toInt() }

        val localDateTime = LocalDateTime(year, month, day, hour, minute)
        val instant = localDateTime.toInstant(TimeZone.currentSystemDefault())
        return instant.toEpochMilliseconds()
    }
}
