 package quizmonitor;

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
import quizmonitor.broker.BrokerManager;
import quizmonitor.rankings.RankingsManager;

/**
 *
 * @author Alexander
 */
public class QuizMonitor extends Application {

    private static final Logger LOG = Logger.getLogger(QuizMonitor.class.getName());
    
    private static BrokerService broker;
    
    private static BrokerManager manager;
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.show();
        
        try {
            String brokerURL = "broker:(tcp://localhost:61615)";
            
            LOG.log(Level.INFO, String.format("Starting Monitor at: %s", brokerURL));
            
            //Create the broker service and start it.
            URI uri = new URI(brokerURL);
            broker = BrokerFactory.createBroker(uri);
            
            broker.start();
            
            manager = BrokerManager.getInstance();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
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
            LOG.log(Level.INFO, "Monitor shutting down...");
            RankingsManager.getInstance().writeRankings();
            
            if (broker != null) {
                broker.stop();
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}