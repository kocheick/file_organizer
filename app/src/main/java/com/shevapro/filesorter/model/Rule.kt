package com.shevapro.filesorter.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.shevapro.filesorter.Utility
import java.util.Date

/**
 * Represents a condition type for rule-based filtering
 */
enum class ConditionType {
    FILE_TYPE,
    NAME_PATTERN,
    DATE,
    SIZE
}

/**
 * Represents the logical operator for combining conditions
 */
enum class LogicalOperator {
    AND,
    OR
}

// ScheduleType enum has been moved to TaskRecord.kt

/**
 * Represents a single condition in a rule
 */
data class RuleCondition(
    val type: ConditionType,
    val value: String,
    val operator: String = "equals" // e.g., "equals", "contains", "greater_than", etc.
)

/**
 * Entity representing a file organization rule
 */
@Entity(tableName = "rules")
data class Rule(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "conditions") val conditions: List<RuleCondition>,
    @ColumnInfo(name = "logical_operator") val logicalOperator: LogicalOperator = LogicalOperator.AND,
    @ColumnInfo(name = "destination_uri") val destination: String,
    @ColumnInfo(name = "is_preset") val isPreset: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
) {
    companion object {
        val EMPTY = Rule(
            name = "",
            conditions = emptyList(),
            destination = ""
        )
    }

    fun toUIRule(): UIRule {
        return UIRule(
            name = name,
            conditions = conditions,
            logicalOperator = logicalOperator,
            destination = Utility.formatUriToUIString(destination),
            isPreset = isPreset,
            id = id
        )
    }
}

/**
 * UI representation of a rule
 */
data class UIRule(
    val name: String,
    val conditions: List<RuleCondition>,
    val logicalOperator: LogicalOperator = LogicalOperator.AND,
    val destination: String,
    val isPreset: Boolean = false,
    val id: Int = 0
) {
    companion object {
        val EMPTY = UIRule(
            name = "",
            conditions = emptyList(),
            destination = ""
        )
    }

    fun toRule(): Rule {
        return Rule(
            name = name,
            conditions = conditions,
            logicalOperator = logicalOperator,
            destination = destination,
            isPreset = isPreset,
            id = id
        )
    }
}

/**
 * Type converters for Room database
 */
class RuleConverters {
    @TypeConverter
    fun fromConditionList(conditions: List<RuleCondition>): String {
        return conditions.joinToString("|") {
            "${it.type.name},${it.value},${it.operator}"
        }
    }

    @TypeConverter
    fun toConditionList(data: String): List<RuleCondition> {
        if (data.isEmpty()) return emptyList()

        return data.split("|").map {
            val parts = it.split(",")
            RuleCondition(
                type = ConditionType.valueOf(parts[0]),
                value = parts[1],
                operator = parts[2]
            )
        }
    }

    @TypeConverter
    fun fromLogicalOperator(operator: LogicalOperator): String {
        return operator.name
    }

    @TypeConverter
    fun toLogicalOperator(name: String): LogicalOperator {
        return LogicalOperator.valueOf(name)
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    // Note: ScheduleType converters have been moved to TaskConverters in TaskRecord.kt
}