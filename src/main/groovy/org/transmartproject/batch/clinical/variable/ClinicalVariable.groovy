package org.transmartproject.batch.clinical.variable

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.transmartproject.batch.clinical.facts.ClinicalDataFileVariables
import org.transmartproject.batch.clinical.facts.ClinicalDataRow
import org.transmartproject.batch.concept.ConceptPath
import org.transmartproject.batch.patient.DemographicVariable

import java.util.regex.Pattern

/**
 * Represents a Variable, as defined in column map file
 */
@ToString
@EqualsAndHashCode(includes = ['filename', 'columnNumber'])
class ClinicalVariable implements Serializable {

    private static final long serialVersionUID = 1L

    public static final String SUBJ_ID = 'SUBJ_ID'
    public static final String STUDY_ID = 'STUDY_ID'
    public static final String SITE_ID = 'SITE_ID'
    public static final String VISIT_NAME = 'VISIT_NAME'
    public static final String OMIT = 'OMIT'
    public static final String DATA_LABEL = 'DATA_LABEL'
    public static final String TEMPLATE = '\\'

    public static final List<String> RESERVED_DATA_LABELS = [
         SUBJ_ID,
         STUDY_ID,
         SITE_ID,
         VISIT_NAME,
         OMIT,
         DATA_LABEL,
         TEMPLATE,
    ]

    public static final List<String> TEMPLATE_PLACEHOLDERS = [
            SITE_ID,
            VISIT_NAME,
            DATA_LABEL,
    ]

    /* The columns have fixed position, but not fixed names.
     * Most of the files have headers [filename, category_cd, col_nbr, data_label]
     * but some files dont, so we use position (not names) to identify columns
     */
    public static final String FIELD_FILENAME = 'filename'
    public static final String FIELD_CATEGORY_CODE = 'categoryCode'
    public static final String FIELD_COLUMN_NUMBER = 'columnNumber'
    public static final String FIELD_DATA_LABEL = 'dataLabel'
    public static final String FIELD_DATA_LABEL_SOURCE = 'dataLabelSource'
    public static final String FIELD_CONTROL_VOCAB_CODE = 'controlledVocabularyCode' // ignored
    public static final String FIELD_CONCEPT_TYPE = 'conceptType'

    static final FIELDS = [FIELD_FILENAME,
                           FIELD_CATEGORY_CODE,
                           FIELD_COLUMN_NUMBER,
                           FIELD_DATA_LABEL,
                           FIELD_DATA_LABEL_SOURCE,
                           FIELD_CONTROL_VOCAB_CODE,
                           FIELD_CONCEPT_TYPE]

    public static final String CONCEPT_TYPE_CATEGORICAL = 'CATEGORICAL'
    public static final String CONCEPT_TYPE_NUMERICAL = 'NUMERICAL'

    /* can be filled directly from fields */
    String filename

    String categoryCode

    Integer columnNumber

    Integer dataLabelSource

    void setColumnNumber(Integer columnNumber) {
        this.columnNumber = columnNumber - 1 /* to make it 0-based */
    }

    String dataLabel

    String conceptType

    ConceptPath topNodePath

    ConceptPath getConceptPath() {
        if (RESERVED_DATA_LABELS.contains(dataLabel)) {
            if (dataLabel == TEMPLATE) {
                throw new RuntimeException('Actual row should be provided to compose a concept from the template.')
            }
            return null
        }

        topNodePath + toPath(categoryCode) + toPath(dataLabel)
    }

    ConceptPath getConceptPath(final ClinicalDataFileVariables variables, final ClinicalDataRow row) {
        if (dataLabel != TEMPLATE) {
            return getConceptPath()
        }

        def modCategoryCode = categoryCode
        boolean dataLabelFound = false
        TEMPLATE_PLACEHOLDERS.each { String label ->
            def legacyLabel = label.replaceAll('_', '')
            if (modCategoryCode.contains(label) || modCategoryCode.contains(legacyLabel)) {
                def value = getReferencedVariableValue(variables, row, label)
                modCategoryCode.replaceAll(Pattern.quote(label), value)
                modCategoryCode.replaceAll(Pattern.quote(legacyLabel), value)
                if (label == DATA_LABEL) {
                    dataLabelFound = true
                }
            }
        }

        if (!dataLabelFound) {
            def value = getReferencedVariableValue(variables, row, DATA_LABEL)
            modCategoryCode += '+' + value
        }

        topNodePath + toPath(modCategoryCode)
    }

    private getReferencedVariableValue(ClinicalDataFileVariables variables, ClinicalDataRow row, String label) {
        def labelsVariables = variables.reservedVariablesByDataLabelMap[label]

        def variableToUse = dataLabelSource ?
                labelsVariables.find { it.columnNumber == dataLabelSource }
                : labelsVariables.find()

        variableToUse.getRowValue(row)
    }

    String getRowValue(final ClinicalDataRow row) {
        row[columnNumber]
    }

    DemographicVariable getDemographicVariable() {
        DemographicVariable.getMatching(dataLabel)
    }

    boolean isReserved() {
        dataLabel in RESERVED_DATA_LABELS
    }

    private static String toPath(String columnMappingPathFragment) {
        columnMappingPathFragment
                .replace('+', '\\')
                .replace('_', ' ')
    }

}

