package com.shevapro.filesorter.data.database

import androidx.room.*
import com.shevapro.filesorter.model.Rule
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Rule entities
 */
@Dao
interface RuleDao {
    @Query("SELECT * FROM rules")
    fun getAllRules(): Flow<List<Rule>>

    @Query("SELECT * FROM rules WHERE id = :id")
    fun getRuleById(id: Int): Rule?

    @Query("SELECT * FROM rules WHERE is_preset = 1")
    fun getPresetRules(): Flow<List<Rule>>

    @Query("SELECT * FROM rules")
    fun getActiveRules(): Flow<List<Rule>>

    @Insert
    suspend fun insertRule(rule: Rule): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateRule(rule: Rule)

    @Delete
    suspend fun deleteRule(rule: Rule)

    @Query("DELETE FROM rules")
    suspend fun deleteAllRules()
}
