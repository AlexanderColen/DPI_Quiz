package quizserver;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

/**
 *
 * @author Alexander
 */
public class QuizServer extends Application {

    private static final Logger LOG = Logger.getLogger(QuizServer.class.getName());
    
    private static BrokerService broker;
    
    @Override
    public void start(Stage stage) throws Exception {
        try {
            String brokerURL = "broker:(tcp://localhost:61616)";
            
            LOG.log(Level.INFO, String.format("Starting Server at: %s", brokerURL));
            
            //Create the broker service and start it.
            URI uri = new URI(brokerURL);
            broker = BrokerFactory.createBroker(uri);
            
            broker.start();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
        
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void stop(){
        try {
            LOG.log(Level.INFO, "Server shutting down...");
            
            if (broker != null) {
                broker.stop();
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
        
        System.exit(0);
    }
}