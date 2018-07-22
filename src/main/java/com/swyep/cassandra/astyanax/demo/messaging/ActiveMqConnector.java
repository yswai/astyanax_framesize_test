package com.swyep.cassandra.astyanax.demo.messaging;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

public class ActiveMqConnector {

    private final ActiveMQConnectionFactory connectionFactory;

    public ActiveMqConnector(String brokerUrl) {
        this.connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
    }

    public Connection getConnection() {
        try {
            return connectionFactory.createConnection();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public Session getSession(boolean b, int i) {
        try {
            Connection connection = getConnection();
            Session session = connection.createSession(b, i);
            connection.start();
            return session;
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
