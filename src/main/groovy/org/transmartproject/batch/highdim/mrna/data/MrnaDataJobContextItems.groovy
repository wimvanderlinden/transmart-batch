package org.transmartproject.batch.highdim.mrna.data

import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.item.ExecutionContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.transmartproject.batch.db.AbstractPartitionTasklet
import org.transmartproject.batch.highdim.assays.SaveAssayIdListener

/**
 * Account for values stored in the job execution context.
 */

@JobScope
@Component
class MrnaDataJobContextItems {

    @Value('#{jobExecution.executionContext}')
    ExecutionContext jobExecutionContext

    String getPartitionTableName() {
        jobExecutionContext.getString(AbstractPartitionTasklet.PARTITION_TABLE_NAME)
    }

    Map<String, Long> getSampleCodeAssayIdMap() {
        jobExecutionContext.get(SaveAssayIdListener.MAPPINGS_CONTEXT_KEY)
    }
}
