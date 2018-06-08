package quizmonitor;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import quizmonitor.broker.BrokerManager;
import quizmonitor.rankings.RankingsManager;

/**
 *
 * @author Alexander
 */
public class FXMLDocumentController implements Initializable {

    private static final Logger LOG = Logger.getLogger(FXMLDocumentController.class.getName());
        
    @FXML
    public ListView<String> listview;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        BrokerManager.getInstance(this);
        
        refreshUI();
    }
    
    public void refreshUI() {
        assert listview != null : "fx:id=\"listview\" was not injected: check your FXML file 'list.fxml'.";

        final Task<List<String>> listViewTask = new Task<List<String>>() {
            @Override
            protected List<String> call() throws SQLException {
                return RankingsManager.getInstance().getRankingsForUI();
            }
        };

        listViewTask.setOnSucceeded((WorkerStateEvent event) -> {
            listview.setItems(FXCollections.observableArrayList(listViewTask.getValue()));
        });

        listViewTask.setOnFailed((WorkerStateEvent event) -> {
            LOG.log(Level.SEVERE, listViewTask.getException().getMessage(), listViewTask.getException());
        });

        new Thread(listViewTask).start();
    }
}