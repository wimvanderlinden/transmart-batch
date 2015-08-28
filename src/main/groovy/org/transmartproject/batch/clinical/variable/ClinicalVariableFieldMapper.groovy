package org.transmartproject.batch.clinical.variable

import groovy.transform.CompileStatic
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper
import org.springframework.batch.item.file.mapping.FieldSetMapper
import org.springframework.batch.item.file.transform.FieldSet
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.validation.BindException
import org.transmartproject.batch.beans.JobScopeInterfaced
import org.transmartproject.batch.concept.ConceptPath

/**
 * Fill the calculated fields of {@link ClinicalVariable}.
 */
@Component
@JobScopeInterfaced
@CompileStatic
class ClinicalVariableFieldMapper implements FieldSetMapper<ClinicalVariable> {

    @Value("#{jobParameters['TOP_NODE']}")
    ConceptPath topNodePath

    private final FieldSetMapper<ClinicalVariable> delegate =
            new BeanWrapperFieldSetMapper<>(
                    targetType: ClinicalVariable,
                    strict: false /* allow unmappable columns 5 and 6 */)

    @Override
    ClinicalVariable mapFieldSet(FieldSet fieldSet) throws BindException {
        ClinicalVariable item = delegate.mapFieldSet(fieldSet)
        item.topNodePath = topNodePath
        item
    }
}
