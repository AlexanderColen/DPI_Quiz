package quizmonitor.broker;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author Alex
 */
public final class BrokerManager {

    private static final Logger LOG = Logger.getLogger(BrokerManager.class.getName());
        
    private static BrokerManager instance;
    
    private static int MESSAGE_ID = 1;
    
    /**
     * Exists only to defeat instantiation.
     */
    protected BrokerManager() {
        this.receiveServerMessage();
    }

    /**
     * Get the Singleton Instance.
     * @return The Instance of the class.
     */
    public static BrokerManager getInstance() {
        if(instance == null) {
            instance = new BrokerManager();
        }

        return instance;
    }
    
    public void receiveServerMessage() {
        try {
            // Producer
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61617");
            Connection conn = connectionFactory.createConnection();
            
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue loanReply = session.createQueue("loanReply");
            
            MessageConsumer consumer = session.createConsumer(loanReply);
            MessageListener messagelistener = (Message msg) -> {
                try {
                    System.out.println("Received message from bank...");
                    
                    String correlation = msg.getJMSCorrelationID();
                    
                    System.out.println(String.format("Received correlationID: %s", correlation));
                    
                    //TODO do something with message.
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
}