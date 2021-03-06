package middleware.core;

import java.util.Optional;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

import atlassharedclasses.*;
import middleware.logging.ATLASLog;

public class CIActiveMQConsumer implements Runnable, ExceptionListener {
	private String queueName;
	private String vehicleName;
	private boolean mqListen = true;
	private int pollInterval = 1000;
	private ATLASEventQueue<CIEvent> carsQueue;
	private ATLASObjectMapper atlasObjMapper;

	public CIActiveMQConsumer(String vehicleName, String queueName, ATLASEventQueue carsQueue) {
		this.queueName = queueName;
		this.carsQueue = carsQueue;
		this.vehicleName = vehicleName;
		atlasObjMapper = new ATLASObjectMapper();
	}

	public void handleMessage(Message m) {
		try {
			if (m instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) m;
				String text = textMessage.getText();
				System.out.println("CIActiveMQConsumer - received " + text);
				
				ATLASSharedResult res = atlasObjMapper.deserialise(text);
				System.out.println(res);
				if (res.getContentsClass() == CIEvent.class) {
					Optional<CIEvent> e_o = res.getCIEvent();
					if (e_o.isPresent()) {
						CIEvent e = e_o.get();
						System.out.println("adding to CIEventQueue");
						carsQueue.add(e);
						ATLASLog.logCIInbound(queueName, text);
					}
				}
			} else {
				System.out.println("Received: " + m);
			}
		} catch (Exception e) {
			System.out.println("handleMessage caught: " + e);
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			// Create a ConnectionFactory
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					"failover:(tcp://localhost:61616)");
			// Create a Connection
			Connection connection = connectionFactory.createConnection();
			connection.start();

			connection.setExceptionListener(this);

			// Create a Session
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create the destination (Topic or Queue)
			// TODO: decide an appropriate scheme for the queues - Should we use Topics or
			// Queues for ActiveMQ connections?
			Destination destination = session.createTopic(queueName);

			// Create a MessageConsumer from the Session to the Topic or Queue
			MessageConsumer consumer = session.createConsumer(destination);

			// Wait for a message while the thread is active
			while (mqListen) {
				Message message = consumer.receive(pollInterval);
				if (message != null)
					handleMessage(message);
			}

			consumer.close();
			session.close();
			connection.close();
		} catch (Exception e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
		}
	}

	public synchronized void stop() {
		mqListen = false;
	}

	public synchronized void onException(JMSException ex) {
		System.out.println("JMS Exception occured.  Shutting down client.");
	}
}