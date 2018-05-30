package quizclient;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import quizclient.broker.BrokerManager;

/**
 *
 * @author Alexander
 */
public class FXMLDocumentController implements Initializable {
    
    private BrokerManager broker;
    
    @FXML
    private Label label;
    
    @FXML
    private Label lblOutcome;
    
    @FXML
    private Button btnRequest;
    
    @FXML
    private Button btnA;
    
    @FXML
    private Button btnB;
    
    @FXML
    private Button btnC;
    
    @FXML
    private Button btnD;
    
    @FXML
    private void requestQuestion(ActionEvent event) {
        System.out.println("Sending message...");
        label.setText("Waiting for message...");
        
        if (this.broker == null) {
            this.broker = BrokerManager.getInstance(this);
        }
        
        this.broker.sendQuestionRequest("Alex");
        
        btnRequest.setDisable(true);
    }
    
    @FXML
    private void submitAnswerA(ActionEvent event) {
        System.out.println("Submitting answer A...");
        
        if (this.broker == null) {
            this.broker = BrokerManager.getInstance(this);
        }
        
        //TODO implement UI stuff and message sending.
        setAnswerButtonDisabled(true);
        this.broker.sendAnswer(btnA.getText(), "Alex");
    }
    
    @FXML
    private void submitAnswerB(ActionEvent event) {
        System.out.println("Submitting answer B...");
        
        if (this.broker == null) {
            this.broker = BrokerManager.getInstance(this);
        }
        
        //TODO implement UI stuff and message sending.
        setAnswerButtonDisabled(true);
        this.broker.sendAnswer(btnB.getText(), "Alex");
    }
    
    @FXML
    private void submitAnswerC(ActionEvent event) {
        System.out.println("Submitting answer C...");
        
        if (this.broker == null) {
            this.broker = BrokerManager.getInstance(this);
        }
        
        //TODO implement UI stuff and message sending.
        setAnswerButtonDisabled(true);
        this.broker.sendAnswer(btnC.getText(), "Alex");
    }
    
    @FXML
    private void submitAnswerD(ActionEvent event) {
        System.out.println("Submitting answer D...");
        
        if (this.broker == null) {
            this.broker = BrokerManager.getInstance(this);
        }
        
        //TODO implement UI stuff and message sending.
        setAnswerButtonDisabled(true);
        this.broker.sendAnswer(btnD.getText(), "Alex");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
    
    public void updateUIWithResult(boolean outcome, int incrementAmount) {
        final String newLabel;
        
        if (outcome) {
            newLabel = String.format("That was the correct answer! You have earned %s points!", incrementAmount);
        } else {
            newLabel = String.format("Too bad, that was the wrong answer. You have earned %s points for trying.", incrementAmount);
        }
        
        new Thread(new Runnable() {
            @Override public void run() {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        lblOutcome.setText(newLabel);
                    }
                });
            }
        }).start();
    }
    
    public void updateUIWithNewQuestion(String question, String answerA, String answerB, String answerC, String answerD) {
        new Thread(new Runnable() {
            @Override public void run() {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        label.setText(question);
                        
                        List<String> answers = new ArrayList<>();
                        answers.add(answerA);
                        answers.add(answerB);
                        answers.add(answerC);
                        answers.add(answerD);
                        
                        String newAnswer = getRandomAnswer(answers);
                        answers.remove(newAnswer);
                        btnA.setText(newAnswer);
                        
                        newAnswer = getRandomAnswer(answers);
                        answers.remove(newAnswer);
                        btnB.setText(newAnswer);
                        
                        newAnswer = getRandomAnswer(answers);
                        answers.remove(newAnswer);
                        btnC.setText(newAnswer);
                        
                        newAnswer = getRandomAnswer(answers);
                        answers.remove(newAnswer);
                        btnD.setText(newAnswer);
                        
                        setAnswerButtonDisabled(false);
                    }
                });
            }
        }).start();
    }
    
    private void setAnswerButtonDisabled(boolean value) {
        btnRequest.setDisable(!value);
        
        btnA.setDisable(value);
        btnB.setDisable(value);
        btnC.setDisable(value);
        btnD.setDisable(value);
    }
    
    private String getRandomAnswer(List<String> answers) {
        return answers.get(new Random().nextInt(answers.size()));
    }
}