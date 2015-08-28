package org.transmartproject.batch.clinical.facts

import groovy.util.logging.Slf4j
import org.transmartproject.batch.clinical.variable.ClinicalVariable
import org.transmartproject.batch.patient.DemographicVariable

import static org.transmartproject.batch.clinical.variable.ClinicalVariable.*

/**
 * Holds the variables defined for a file
 */
@Slf4j
class ClinicalDataFileVariables {

    Map<String, List<ClinicalVariable>> reservedVariablesByDataLabelMap = [:]
    Map<DemographicVariable, List<ClinicalVariable>> demographicVariablesMap = [:]
    List<ClinicalVariable> simpleVariables = []

    static ClinicalDataFileVariables fromVariableList(List<ClinicalVariable> list) {
        def (resVars, simpleVariables) = list.split { it.reserved }

        def reservedVariablesByDataLabelMap = resVars.groupBy { it.dataLabel }
        def demographicVariablesMap = simpleVariables.groupBy { it.demographicVariable }
        demographicVariablesMap.remove(null)

        new ClinicalDataFileVariables(
                reservedVariablesByDataLabelMap:    reservedVariablesByDataLabelMap,
                demographicVariablesMap:            demographicVariablesMap,
                simpleVariables:                    simpleVariables)
    }

    String getPatientId(ClinicalDataRow row) {
        reservedVariablesByDataLabelMap[SUBJ_ID]?.first()?.getRowValue(row)
    }

    String getSiteId(ClinicalDataRow row) {
        reservedVariablesByDataLabelMap[SITE_ID]?.first()?.getRowValue(row)
    }

    String getVisitName(ClinicalDataRow row) {
        reservedVariablesByDataLabelMap[VISIT_NAME]?.first()?.getRowValue(row)
    }

    Map<DemographicVariable, String> getDemographicVariablesValues(ClinicalDataRow row) {
        demographicVariablesMap.collectEntries {
            [ it.key, it.value.first().getRowValue(row) ]
        }
    }
}
