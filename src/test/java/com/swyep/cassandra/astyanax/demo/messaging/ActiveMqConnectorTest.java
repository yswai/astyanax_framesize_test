package com.swyep.cassandra.astyanax.demo.messaging;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Before;
import org.junit.Test;

import javax.jms.*;

public class ActiveMqConnectorTest {

    private ActiveMqConnector activeMqConnector;
    private MessageProducer producer;
    private String queueName = "ysw-queue-1";

    @Before
    public void before() throws JMSException {
        activeMqConnector = new ActiveMqConnector("tcp://127.0.0.1:61616");
        // Producer
        Session jmsSession = activeMqConnector.getSession(true, 1);
        Destination destination = new ActiveMQQueue(queueName);
        jmsSession.createQueue(queueName);
        this.producer = jmsSession.createProducer(destination);
        ActiveMQTextMessage message = new ActiveMQTextMessage();
        message.setText("Hellloooo");
        this.producer.send(message);
        jmsSession.commit();
        jmsSession.close();
    }

    @Test
    public void testGetConnection() throws JMSException {
        // Consumer
        MessageListener listener = new MessageListener() {
            public void onMessage(Message message) {
                try {
                    System.out.println(((ActiveMQTextMessage)message).getText());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        };

        Session jmsSession = activeMqConnector.getSession(true, 1);
        MessageConsumer consumer = jmsSession.createConsumer(new ActiveMQQueue(queueName));
        consumer.setMessageListener(listener);
        while (true);
    }

}
