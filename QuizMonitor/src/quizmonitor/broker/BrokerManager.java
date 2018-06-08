package quizmonitor.broker;

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
import quizmonitor.FXMLDocumentController;
import quizmonitor.rankings.ParticipantNotFoundException;
import quizmonitor.rankings.RankingsManager;

/**
 *
 * @author Alex
 */
public final class BrokerManager {

    private static final Logger LOG = Logger.getLogger(BrokerManager.class.getName());
        
    private static BrokerManager instance;
    private static RankingsManager manager;
    
    private static FXMLDocumentController ui;
    
    private static final String MESSAGE_REQUEST = "request";
    private static final String USERNAME = "username";
    
    /**
     * Exists only to defeat instantiation.
     */
    protected BrokerManager(FXMLDocumentController c) {
        try {
            this.receiveServerMessage();
            this.ui = c;
            this.manager = RankingsManager.getInstance();
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
    
    public void receiveServerMessage() throws JMSException, InterruptedException {
        LOG.log(Level.INFO, "Starting to listen for Server messages...");
        
        // Producer
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61615");
        Connection conn = connectionFactory.createConnection();

        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queueRankings = session.createQueue("rankings");

        MessageConsumer consumer = session.createConsumer(queueRankings);
        MessageListener messagelistener = (Message msg) -> {
            try {
                System.out.println("Received message from Service.");
                
                String username = msg.getStringProperty(USERNAME);
                
                if (msg.getStringProperty(MESSAGE_REQUEST).equalsIgnoreCase("increment")) {
                    System.out.println("Increment request.");
                    
                    manager.adjustRankings(username, msg.getIntProperty("points"));
                    
                    ui.refreshUI();
                } else if (msg.getStringProperty(MESSAGE_REQUEST).equalsIgnoreCase("rankings")) {
                    System.out.println("Rankings request.");
                    
                    Integer foundPoints = manager.fetchPointForParticipant(username);
                    int rank = manager.getRankForParticipant(username);
                    
                    sendServerMessage(rank, foundPoints, username, msg.getJMSCorrelationID());
                }
            } catch (JMSException | ParticipantNotFoundException ex) {
                LOG.log(Level.SEVERE, ex.getMessage(), ex);
            }
        };
        
        consumer.setMessageListener(messagelistener);
        conn.start();
        Thread.sleep(1000);
    }
    
    private static void sendServerMessage(int rank, Integer points, String username, String ID) {
        try {
            ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61615");
            Connection conn = cf.createConnection();
            
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            Queue queueRankings = session.createQueue("rankings");
            
            MessageProducer producer = session.createProducer(queueRankings);
            
            Message msg = session.createMessage();
            
            msg.setStringProperty(MESSAGE_REQUEST, "rankings");
            msg.setIntProperty("rank", rank);
            msg.setIntProperty("points", points);
            msg.setStringProperty(USERNAME, username);
            msg.setJMSCorrelationID(ID);
            
            System.out.println("Sending Rankings to Server.");
            
            conn.start();
            producer.send(msg);
            
            System.out.println("Message sent without issues.");
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}