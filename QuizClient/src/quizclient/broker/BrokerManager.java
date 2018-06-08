package quizclient.broker;

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
import quizclient.FXMLDocumentController;

/**
 *
 * @author Alex
 */
public final class BrokerManager {

    private static final Logger LOG = Logger.getLogger(BrokerManager.class.getName());
        
    private static BrokerManager instance;
    private static FXMLDocumentController ui;
    
    private static String correlationID;
    
    private static final String OUTCOME_PROPERTY = "outcome";
    private static final String QUESTION_PROPERTY = "question";
    private static final String RANK_PROPERTY = "rank";
    private static final String ANSWER_A = "A";
    private static final String ANSWER_B = "B";
    private static final String ANSWER_C = "C";
    private static final String ANSWER_D = "D";
    
    /**
     * Exists only to defeat instantiation.
     */
    protected BrokerManager(FXMLDocumentController c) {
        this.receiveServerMessage();
        ui = c;
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
    
    public void sendQuestionRequest(String username) {
        try {
            ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection conn = cf.createConnection();
            
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            Queue queueQuestion = session.createQueue("question");
            
            MessageProducer producer = session.createProducer(queueQuestion);
            
            Message msg = session.createMessage();
            msg.setStringProperty("request", "question");
            msg.setStringProperty("username", username);
            
            conn.start();
            producer.send(msg);
            
            System.out.println("Message sent without issues.");
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    public void receiveServerMessage() {
        try {
            // Producer
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61617");
            Connection conn = connectionFactory.createConnection();
            
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queueQuestion = session.createQueue("question");
            
            MessageConsumer consumer = session.createConsumer(queueQuestion);
            MessageListener messagelistener = (Message msg) -> {
                try {
                    System.out.println("Received message from server...");
                    
                    String correlation = msg.getJMSCorrelationID();
                    
                    System.out.println(String.format("Received correlationID: %s", correlation));
                    
                    boolean questionExists = msg.propertyExists(QUESTION_PROPERTY);
                    boolean outcomeExists = msg.propertyExists(OUTCOME_PROPERTY);
                    boolean rankExists = msg.propertyExists(RANK_PROPERTY);
                    
                    if (questionExists) {
                        String receivedQuestion = msg.getStringProperty(QUESTION_PROPERTY);
                        String answer_A = msg.getStringProperty(ANSWER_A);
                        String answer_B = msg.getStringProperty(ANSWER_B);
                        String answer_C = msg.getStringProperty(ANSWER_C);
                        String answer_D = msg.getStringProperty(ANSWER_D);

                        System.out.println(String.format("Received question: %s", receivedQuestion));
                        correlationID = correlation;
                        
                        //Update UI.
                        ui.updateUIWithNewQuestion(receivedQuestion, answer_A, answer_B, answer_C, answer_D);
                    } else if (outcomeExists) {
                        boolean receivedOutcome = msg.getBooleanProperty(OUTCOME_PROPERTY);
                        int points = msg.getIntProperty("points");
                        
                        //Update UI.
                        ui.updateUIWithResult(receivedOutcome, points);
                    } else if (rankExists) {
                        int rank = msg.getIntProperty("rank");
                        int points = msg.getIntProperty("points");
                        String username = msg.getStringProperty("username");
                        
                        //Show in UI.
                        ui.updateUIWithRankings(rank, points, username);
                    }
                } catch (JMSException ex) {
                    LOG.log(Level.SEVERE, ex.getMessage(), ex);
                }
            };
            
            consumer.setMessageListener(messagelistener);
            conn.start();
            Thread.sleep(1000);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void sendAnswer(String text, String username) {
        try {
            ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection conn = cf.createConnection();
            
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            Queue queueQuestion = session.createQueue("question");
            
            MessageProducer producer = session.createProducer(queueQuestion);
            
            Message msg = session.createMessage();
            msg.setStringProperty("request", "answer");
            msg.setStringProperty("username", username);
            msg.setStringProperty("answer", text);
            msg.setJMSCorrelationID(correlationID);
            
            System.out.println(String.format("Sending message with ID: %s", msg.getJMSCorrelationID()));
            
            conn.start();
            producer.send(msg);
            
            System.out.println("Message sent without issues.");
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void sendRankingsRequest(String username) {
        try {
            ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection conn = cf.createConnection();
            
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            Queue queueQuestion = session.createQueue("question");
            
            MessageProducer producer = session.createProducer(queueQuestion);
            
            Message msg = session.createMessage();
            msg.setStringProperty("request", "rankings");
            msg.setStringProperty("username", username);
            
            conn.start();
            producer.send(msg);
            
            System.out.println("Message sent without issues.");
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void sendNewQuestion(String username, String question, String answerA, String answerB, String answerC, String answerD) {
        try {
            ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection conn = cf.createConnection();
            
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            Queue queueQuestion = session.createQueue("question");
            
            MessageProducer producer = session.createProducer(queueQuestion);
            
            Message msg = session.createMessage();
            msg.setStringProperty("request", "new");
            msg.setStringProperty("username", username);
            msg.setStringProperty("question", question);
            msg.setStringProperty("A", answerA);
            msg.setStringProperty("B", answerB);
            msg.setStringProperty("C", answerC);
            msg.setStringProperty("D", answerD);
            
            conn.start();
            producer.send(msg);
            
            System.out.println("Message sent without issues.");
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}