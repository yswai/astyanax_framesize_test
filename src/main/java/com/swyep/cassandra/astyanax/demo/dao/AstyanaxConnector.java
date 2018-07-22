package com.swyep.cassandra.astyanax.demo.dao;

import com.google.common.collect.ImmutableMap;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

public class AstyanaxConnector {

    private final Keyspace keyspace;

    public AstyanaxConnector() throws ConnectionException {
        AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder()
                .forCluster("ClusterName")
                .forKeyspace("ysw")
                .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()
                        .setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE)
                )
                .withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl("MyConnectionPool")
                        .setPort(9160)
                        .setMaxConnsPerHost(1)
                        .setSeeds("127.0.0.1:9160")
                )
                .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
                .buildKeyspace(ThriftFamilyFactory.getInstance());
        context.start();

        this.keyspace = context.getClient();

        try {
            this.keyspace.createKeyspace(ImmutableMap.<String, Object>builder()
                    .put("strategy_options", ImmutableMap.<String, Object>builder()
                            .put("replication_factor", "1")
                            .build())
                    .put("strategy_class", "SimpleStrategy")
                    .build()
            );
        } catch (Exception exception) {
            System.out.println("Keyspace existed, skipping");
        }
    }

    public Keyspace getKeyspace() {
        return keyspace;
    }
}
