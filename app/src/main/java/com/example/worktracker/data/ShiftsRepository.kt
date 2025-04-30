package com.example.worktracker.data

import kotlinx.coroutines.flow.Flow

interface ShiftsRepository {
    /**
     * Retrieve all the items from the the given data source.
     */
    suspend fun getAllItems(): List<Shift>

    fun getAllItemsFlow(): Flow<List<Shift>>

    /**
     * Retrieve an item from the given data source that matches with the [id].
     */
    suspend fun getItemStream(id: Int): Shift?

    /**
     * Insert item in the data source
     */
    suspend fun insertItem(item: Shift)

    /**
     * Delete item from the data source
     */
    suspend fun deleteItem(item: Shift)

    /**
     * Update item in the data source
     */
    suspend fun updateItem(item: Shift)
}