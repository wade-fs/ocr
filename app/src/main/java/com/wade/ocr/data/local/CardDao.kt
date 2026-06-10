package com.wade.ocr.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wade.ocr.data.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CardEntity): Long

    @Update
    suspend fun update(card: CardEntity)

    @Delete
    suspend fun delete(card: CardEntity)

    /** Get all cards ordered by newest first */
    @Query("SELECT * FROM business_cards ORDER BY id DESC")
    fun getAll(): Flow<List<CardEntity>>

    /** Find a card by its primary key */
    @Query("SELECT * FROM business_cards WHERE id = :id")
    suspend fun getById(id: Long): CardEntity?

    /** Get cards belonging to a specific category */
    @Query("SELECT * FROM business_cards WHERE category = :category")
    fun getByCategory(category: String): Flow<List<CardEntity>>

    /** Move a card to another category */
    @Query("UPDATE business_cards SET category = :newCategory WHERE id = :cardId")
    suspend fun moveCard(cardId: Long, newCategory: String)
}
