package quizserver.broker;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import quizserver.FXMLDocumentController;
import quizserver.questions.QuestionManager;

/**
 *
 * @author Alex
 */
public class BrokerManager {

    private static final Logger LOG = Logger.getLogger(BrokerManager.class.getName());
        
    private static BrokerManager instance = null;
    
    private static Map correlationRequest;
    
    private static final String MESSAGE_REQUEST = "request";
    private static int MESSAGE_ID = 1;
    
    private static QuestionManager manager;
    
    private static FXMLDocumentController ui;

    /**
     * Exists only to defeat instantiation.
     * @param c
     */
    protected BrokerManager(FXMLDocumentController c) {
        try {
            receiveMonitorMessage();
            
            receiveClientMessage();
            
            correlationRequest = new HashMap<>();
            
            this.ui = c;
            this.manager = QuestionManager.getInstance();
        } catch (JMSException | InterruptedException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * Get the Singleton Instance.
     * @return The Instance of the class.
     */
    public static BrokerManager getInstance(FXMLDocumentController c) {
        if(instance == null) {
            instance = new BrokerManager(c);
        }

        return instance;
    }
    
    public static void receiveMonitorMessage() throws JMSException, InterruptedException {
        LOG.log(Level.INFO, "Starting to listen for Monitor messages...");
        
        // Producer
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61615");
        Connection conn = connectionFactory.createConnection();

        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queueRankings = session.createQueue("rankings");

        MessageConsumer consumer = session.createConsumer(queueRankings);
        MessageListener messagelistener = (Message msg) -> {
            try {
                System.out.println("Received message from Monitor.");
                
                if (msg.getStringProperty(MESSAGE_REQUEST).equalsIgnoreCase("rankings")) {
                    sendRankingsToClient(msg.getIntProperty("rank"), msg.getIntProperty("points"), msg.getStringProperty("username"), msg.getJMSCorrelationID());
                }
            } catch (JMSException ex) {
                LOG.log(Level.SEVERE, ex.getMessage(), ex);
            }
        };
        
        consumer.setMessageListener(messagelistener);
        conn.start();
        Thread.sleep(1000);
    }
    
    public static void receiveClientMessage() throws JMSException, InterruptedException {
        LOG.log(Level.INFO, "Starting to listen for Client messages...");
        
        // Producer
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection conn = connectionFactory.createConnection();

        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queueQuestion = session.createQueue("question");

        MessageConsumer consumer = session.createConsumer(queueQuestion);
        MessageListener messagelistener = (Message msg) -> {
            try {
                System.out.println("Received message from Client.");
                
                String ID = msg.getJMSCorrelationID();
                
                if (ID == null) {
                    ID = String.format("%s", MESSAGE_ID);
                    MESSAGE_ID++;
                }
                
                String username = msg.getStringProperty("username");
                String request = msg.getStringProperty(MESSAGE_REQUEST);
                
                System.out.println(String.format("Received message with ID: %s and Request: %s from User: %s", ID, request, username));
                
                if (request.equalsIgnoreCase("question")) {
                    sendQuestionToClient(ID, username);
            
                    //Update UI.
                    manager.addRequest(username, "Fetch a Question");
                    ui.refreshUI();
                } else if (request.equalsIgnoreCase("rankings")) {
                    sendRankingsRequestToMonitor(username, ID);
            
                    //Update UI.
                    manager.addRequest(username, "Rankings");
                    ui.refreshUI();
                } else if (request.equalsIgnoreCase("answer")) {
                    int incrementAmount = 1;
                    boolean outcome = manager.checkIfCorrectAnswer((String) correlationRequest.get(ID), msg.getStringProperty("answer"));
                    
                    if (outcome) {
                        incrementAmount = 10;
                    }
                    
                    sendOutcomeToClient(outcome, ID, incrementAmount, username);
                    sendIncrementToMonitor(incrementAmount, username);
                } else if (request.equalsIgnoreCase("new")) {
                    if (manager.addNewQuestion(msg.getStringProperty("question"), msg.getStringProperty("A"), msg.getStringProperty("B"), msg.getStringProperty("C"), msg.getStringProperty("D"))) {
                        sendIncrementToMonitor(5, username);
                    }
            
                    //Update UI.
                    manager.addRequest(username, "Submit New Question");
                    ui.refreshUI();
                }
            } catch (JMSException ex) {
                LOG.log(Level.SEVERE, ex.getMessage(), ex);
            }
        };
        
        consumer.setMessageListener(messagelistener);
        conn.start();
        Thread.sleep(1000);
    }
    
    public static void sendQuestionToClient(String correlationID, String username) {
        try {
            ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61617");
            Connection conn = cf.createConnection();
            
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            Queue queueQuestion = session.createQueue("question");
            
            MessageProducer producer = session.createProducer(queueQuestion);
            
            Message msg = session.createMessage();
            
            String[] question = manager.getRandomQuestion();
            msg.setStringProperty("question", question[0]);
            msg.setStringProperty("A", question[1]);
            msg.setStringProperty("B", question[2]);
            msg.setStringProperty("C", question[3]);
            msg.setStringProperty("D", question[4]);
            
            msg.setJMSCorrelationID(correlationID);
            
            System.out.println(String.format("Sending message with ID: %s and Question: %s", msg.getJMSCorrelationID(), question[0]));
            
            conn.start();
            producer.send(msg);
            
            System.out.println("Message sent without issues.");
            
            correlationRequest.put(correlationID, question[0]);
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    public static void sendOutcomeToClient(boolean outcome, String correlationID, int incrementAmount, String username) {
        try {
            ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61617");
            Connection conn = cf.createConnection();
            
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            Queue queueQuestion = session.createQueue("question");
            
            MessageProducer producer = session.createProducer(queueQuestion);
            
            Message msg = session.createMessage();
            
            msg.setIntProperty("points", incrementAmount);
            msg.setBooleanProperty("outcome", outcome);
            
            msg.setJMSCorrelationID(correlationID);
            
            System.out.println(String.format("Sending message with ID: %s", msg.getJMSCorrelationID()));
            
            conn.start();
            producer.send(msg);
            
            System.out.println("Message sent without issues.");
            
            //Update UI.
            manager.removeRequest(username, "Question");
            ui.refreshUI();
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    public static void sendRankingsToClient(int rank, int points, String username, String correlationID) {
        try {
            ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61617");
            Connection conn = cf.createConnection();
            
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            Queue queueQuestion = session.createQueue("question");
            
            MessageProducer producer = session.createProducer(queueQuestion);
            
            Message msg = session.createMessage();
            msg.setStringProperty("username", username);
            msg.setIntProperty("points", points);
            msg.setIntProperty("rank", rank);
            msg.setJMSCorrelationID(correlationID);
            
            System.out.println(String.format("Sending message with ID: %s", msg.getJMSCorrelationID()));
            
            conn.start();
            producer.send(msg);
            
            System.out.println("Message sent without issues.");
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    public static void sendIncrementToMonitor(int incrementAmount, String username) {
        try {
            ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61615");
            Connection conn = cf.createConnection();
            
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            Queue queueRankings = session.createQueue("rankings");
            
            MessageProducer producer = session.createProducer(queueRankings);
            
            Message msg = session.createMessage();
            
            msg.setStringProperty(MESSAGE_REQUEST, "increment");
            msg.setIntProperty("points", incrementAmount);
            msg.setStringProperty("username", username);
            
            System.out.println("Sending increment to Monitor.");
            
            conn.start();
            producer.send(msg);
            
            System.out.println("Message sent without issues.");
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    public static void sendRankingsRequestToMonitor(String username, String correlationID) {
        try {
            ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61615");
            Connection conn = cf.createConnection();
            
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            Queue queueRankings = session.createQueue("rankings");
            
            MessageProducer producer = session.createProducer(queueRankings);
            
            Message msg = session.createMessage();
            
            msg.setStringProperty(MESSAGE_REQUEST, "rankings");
            msg.setStringProperty("username", username);
            
            System.out.println("Sending Rankings request to Monitor.");
            
            conn.start();
            producer.send(msg);
            
            System.out.println("Message sent without issues.");
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}