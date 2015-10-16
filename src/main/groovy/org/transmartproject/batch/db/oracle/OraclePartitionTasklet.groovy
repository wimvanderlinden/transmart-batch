package org.transmartproject.batch.db.oracle

import groovy.util.logging.Slf4j
import org.transmartproject.batch.beans.Oracle
import org.transmartproject.batch.db.AbstractPartitionTasklet

/**
 * Partitions a table
 */
@Slf4j
@Oracle
class OraclePartitionTasklet extends AbstractPartitionTasklet {

    @Override
    protected String createPartitionIfNeeded() {
        def tableIdentifier = unqualifiedTable.toUpperCase()
        def tableIfno = jdbcTemplate.queryForMap("""
            SELECT *
                FROM all_tables
                WHERE table_name = :tableName
            """, [tableName: tableIdentifier])

        if (tableIfno.PARTITIONED.toUpperCase() == 'YES') {
            def partitions = jdbcTemplate.queryForList("""
            SELECT *
                FROM all_tab_partitions
                WHERE table_name = :tableName and partition_name = :partitionName
            """, [tableName: tableIdentifier, partitionName: partitionByColumnValue])

            if (!partitions) {
                jdbcTemplate.update("""ALTER TABLE ${tableName}
                ADD PRATITION \"${partitionByColumnValue}\"
                VALUES('${partitionByColumnValue}')""", [:])
            } else {
                log.info("Partition with name '${partitionByColumnValue}' for ${tableName} table already exists.")
            }
        } else {
            log.warn("${tableName} table is not partitioned. Skipping partition creation.")
        }

        tableName
    }

}
