package com.shevapro.filesorter.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shevapro.filesorter.data.repository.RuleRepository
import com.shevapro.filesorter.model.ConditionType
import com.shevapro.filesorter.model.LogicalOperator
import com.shevapro.filesorter.model.Rule
import com.shevapro.filesorter.model.RuleCondition
import com.shevapro.filesorter.model.UIRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for managing rules
 */
class RuleViewModel(
    application: Application,
    private val ruleRepository: RuleRepository
) : AndroidViewModel(application) {

    private val _allRules = MutableStateFlow<List<UIRule>>(emptyList())
    val allRules: StateFlow<List<UIRule>> = _allRules.asStateFlow()

    private val _presetRules = MutableStateFlow<List<UIRule>>(emptyList())
    val presetRules: StateFlow<List<UIRule>> = _presetRules.asStateFlow()

    private val _currentRule = MutableStateFlow<UIRule>(UIRule.EMPTY)
    val currentRule: StateFlow<UIRule> = _currentRule.asStateFlow()

    init {
        loadRules()
        viewModelScope.launch {
            // Create preset rules if none exist
            if (_presetRules.value.isEmpty()) {
                ruleRepository.createPresetRules()
            }
        }
    }

    private fun loadRules() {
        viewModelScope.launch {
            ruleRepository.getAllRules().collectLatest { rules ->
                _allRules.value = rules.map { it.toUIRule() }
            }
        }

        viewModelScope.launch {
            ruleRepository.getPresetRules().collectLatest { rules ->
                _presetRules.value = rules.map { it.toUIRule() }
            }
        }
    }

    fun setCurrentRule(rule: UIRule) {
        _currentRule.value = rule
    }

    fun updateRuleName(name: String) {
        _currentRule.value = _currentRule.value.copy(name = name)
    }

    fun updateRuleDestination(destination: String) {
        _currentRule.value = _currentRule.value.copy(destination = destination)
    }

    fun updateRuleLogicalOperator(operator: LogicalOperator) {
        _currentRule.value = _currentRule.value.copy(logicalOperator = operator)
    }

    fun addCondition(condition: RuleCondition) {
        val currentConditions = _currentRule.value.conditions.toMutableList()
        currentConditions.add(condition)
        _currentRule.value = _currentRule.value.copy(conditions = currentConditions)
    }

    fun removeCondition(index: Int) {
        val currentConditions = _currentRule.value.conditions.toMutableList()
        if (index in currentConditions.indices) {
            currentConditions.removeAt(index)
            _currentRule.value = _currentRule.value.copy(conditions = currentConditions)
        }
    }

    fun updateCondition(index: Int, condition: RuleCondition) {
        val currentConditions = _currentRule.value.conditions.toMutableList()
        if (index in currentConditions.indices) {
            currentConditions[index] = condition
            _currentRule.value = _currentRule.value.copy(conditions = currentConditions)
        }
    }

    fun saveRule() {
        viewModelScope.launch {
            val rule = _currentRule.value.toRule()
            if (rule.id == 0) {
                ruleRepository.insertRule(rule)
            } else {
                ruleRepository.updateRule(rule)
            }
            _currentRule.value = UIRule.EMPTY
        }
    }

    fun deleteRule(rule: UIRule) {
        viewModelScope.launch {
            ruleRepository.deleteRule(rule.toRule())
        }
    }

    fun resetCurrentRule() {
        _currentRule.value = UIRule.EMPTY
    }
}
/**
 * Updates preset rules if they are outdated (e.g., missing TXT extension in document files)
 */