package com.shevapro.filesorter.data.repository

import com.shevapro.filesorter.data.database.RuleDao
import com.shevapro.filesorter.model.ConditionType
import com.shevapro.filesorter.model.LogicalOperator
import com.shevapro.filesorter.model.Rule
import com.shevapro.filesorter.model.RuleCondition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date

/**
 * Repository for managing Rule entities
 */
class RuleRepository(private val ruleDao: RuleDao) {

    fun getAllRules(): Flow<List<Rule>> = ruleDao.getAllRules()

    fun getPresetRules(): Flow<List<Rule>> = ruleDao.getPresetRules()

    fun getActiveRules(): Flow<List<Rule>> = ruleDao.getActiveRules()

    suspend fun getRuleById(id: Int): Rule? = ruleDao.getRuleById(id)

    suspend fun insertRule(rule: Rule): Long = ruleDao.insertRule(rule)

    suspend fun updateRule(rule: Rule) = ruleDao.updateRule(rule)

    suspend fun deleteRule(rule: Rule) = ruleDao.deleteRule(rule)

    suspend fun deleteAllRules() = ruleDao.deleteAllRules()

    /**
     * Creates preset rules for common file types
     */
    suspend fun createPresetRules() {
        // Check if any preset rules already exist
        val existingPresets = ruleDao.getPresetRules().first() // Get the first emission which represents the current state

        // Only proceed if no preset rules are found
        if (existingPresets.isEmpty()) {
            // Music files rule
            val musicRule = Rule(
                name = "Music Files",
                conditions = listOf(
                    RuleCondition(
                        type = ConditionType.FILE_TYPE,
                        value = "mp3",
                        operator = "equals"
                    ),
                    RuleCondition(
                        type = ConditionType.FILE_TYPE,
                        value = "flac",
                        operator = "equals"
                    ),
                    RuleCondition(
                        type = ConditionType.FILE_TYPE,
                        value = "wav",
                        operator = "equals"
                    )
                ),
                logicalOperator = LogicalOperator.OR,
                destination = "Music",
                isPreset = true
            )

            // Image files rule
            val imageRule = Rule(
                name = "Image Files",
                conditions = listOf(
                    RuleCondition(
                        type = ConditionType.FILE_TYPE,
                        value = "jpg",
                        operator = "equals"
                    ),
                    RuleCondition(
                        type = ConditionType.FILE_TYPE,
                        value = "png",
                        operator = "equals"
                    ),
                    RuleCondition(
                        type = ConditionType.FILE_TYPE,
                        value = "gif",
                        operator = "equals"
                    )
                ),
                logicalOperator = LogicalOperator.OR,
                destination = "Pictures",
                isPreset = true
            )

            // Document files rule
            val documentRule = Rule(
                name = "Document Files",
                conditions = listOf(
                    RuleCondition(
                        type = ConditionType.FILE_TYPE,
                        value = "pdf",
                        operator = "equals"
                    ),
                    RuleCondition(
                        type = ConditionType.FILE_TYPE,
                        value = "doc",
                        operator = "equals"
                    ),
                    RuleCondition(
                        type = ConditionType.FILE_TYPE,
                        value = "docx",
                        operator = "equals"
                    ),
                    RuleCondition(
                        type = ConditionType.FILE_TYPE,
                        value = "txt",
                        operator = "equals"
                    )
                ),
                logicalOperator = LogicalOperator.OR,
                destination = "Documents",
                isPreset = true
            )

            // Archive files rule
            val archiveRule = Rule(
                name = "Archive Files",
                conditions = listOf(
                    RuleCondition(
                        type = ConditionType.FILE_TYPE,
                        value = "zip",
                        operator = "equals"
                    ),
                    RuleCondition(
                        type = ConditionType.FILE_TYPE,
                        value = "rar",
                        operator = "equals"
                    ),
                    RuleCondition(
                        type = ConditionType.FILE_TYPE,
                        value = "7z",
                        operator = "equals"
                    ),
                    RuleCondition(
                        type = ConditionType.FILE_TYPE,
                        value = "tar",
                        operator = "equals"
                    )
                ),
                logicalOperator = LogicalOperator.OR,
                destination = "Download/Archives",
                isPreset = true
            )

            ruleDao.insertRule(musicRule)
            ruleDao.insertRule(imageRule)
            ruleDao.insertRule(documentRule)
            ruleDao.insertRule(archiveRule)
        }
    }
}