package ch.puzzle.activemq.broker;

import ch.puzzle.activemq.broker.util.KeystoreFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;

import javax.jms.*;
import java.io.FileOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DynamicBrokerTest {
    String receivedMessage;
    CountDownLatch countDownLatch;

    BrokerService broker;

    private void setupBroker(String keystoreFile) throws Exception {
        broker = new CustomBrokerService(keystoreFile);
        broker.addConnector("ssl://localhost:61616");
        broker.start();
        ActiveMQConnectionFactory vmConnectionfactory = new ActiveMQConnectionFactory();
        vmConnectionfactory.setBrokerURL("vm://localhost?create=false");
        Connection serverConnection = vmConnectionfactory.createConnection();
        serverConnection.start();

        Session session = serverConnection.createSession(false, ActiveMQSession.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic("testTopic");

        MessageConsumer messageConsumer = session.createConsumer(topic);
        messageConsumer.setMessageListener(m -> {
            if (m instanceof ActiveMQTextMessage) {
                try {
                    ActiveMQTextMessage message = ((ActiveMQTextMessage) m);
                    receivedMessage = message.getText();
                    System.out.println(" BROK | Received Message: " + receivedMessage);
                    countDownLatch.countDown();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });

        broker.waitUntilStarted();
    }

    private void sendMessage(String message, String keystoreFile) throws Exception {
        ActiveMQSslConnectionFactory sslConnectionFactory = new ActiveMQSslConnectionFactory();
        sslConnectionFactory.setBrokerURL("ssl://localhost:61616");
        sslConnectionFactory.setKeyStore(keystoreFile);
        sslConnectionFactory.setKeyStorePassword("123456");
        sslConnectionFactory.setTrustStore(keystoreFile);
        sslConnectionFactory.setTrustStorePassword("123456");
        Connection c = sslConnectionFactory.createConnection();
        c.start();

        Session session = c.createSession(false, ActiveMQSession.AUTO_ACKNOWLEDGE);
        Topic t = session.createTopic("testTopic");

        MessageProducer messageProducer = session.createProducer(t);
        ActiveMQTextMessage amqMessage = new ActiveMQTextMessage();
        amqMessage.setText(message);
        messageProducer.send(amqMessage);
    }

    @Test
    public void mainTest() throws Exception {
        // Generate a new keystore with random certs and keys
        KeystoreFactory.generateKeyStore().store(new FileOutputStream("keystore1.ks"), KeystoreFactory.DEFAULT_PASSWORD);
        // Setup a BrokerService with randomly generated certificate and keys
        setupBroker("keystore1.ks");
        sendMessage("Message 1", "keystore1.ks");
        countDownLatch = new CountDownLatch(1);
        countDownLatch.await(2000, TimeUnit.MILLISECONDS);

        assert "Message 1".equals(receivedMessage);

        broker.stop();
        broker.waitUntilStopped();

        // Generate a new keystore with random certs and keys
        KeystoreFactory.generateKeyStore().store(new FileOutputStream("keystore2.ks"), KeystoreFactory.DEFAULT_PASSWORD);
        // Setup a BrokerService with randomly generated certificate and keys
        setupBroker("keystore2.ks");
        sendMessage("Message 2", "keystore2.ks");

        countDownLatch = new CountDownLatch(1);
        countDownLatch.await(2000, TimeUnit.MILLISECONDS);

        assert "Message 2".equals(receivedMessage);
    }
}
