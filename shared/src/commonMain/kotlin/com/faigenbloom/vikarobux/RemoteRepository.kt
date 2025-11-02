package com.faigenbloom.vikarobux

import com.faigenbloom.vikarobux.models.Item
import com.faigenbloom.vikarobux.models.ItemWrapper
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class RemoteRepository {
    suspend fun getItems(): ItemWrapper =
        apiClient.get("/getList").body()

    suspend fun addItem(item: Item): ItemWrapper =
        apiClient.post("/addItem") {
            setBody(item)
        }.body()

    suspend fun markDone(id: String, state: Boolean): ItemWrapper =
        apiClient.get("/markDone/$id/$state").body()
}