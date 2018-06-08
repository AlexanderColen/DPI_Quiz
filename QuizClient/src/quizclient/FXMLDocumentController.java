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
import javafx.scene.control.TextField;
import quizclient.broker.BrokerManager;

/**
 *
 * @author Alexander
 */
public class FXMLDocumentController implements Initializable {
    
    private String username = "";
    private BrokerManager broker;
    
    @FXML
    private Label label;
    
    @FXML
    private Label lblOutcome;
    
    @FXML
    private Button btnNewQuestion;
    
    @FXML
    private Button btnRankings;
    
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
    private TextField txtUsername;
    
    @FXML
    private TextField txtNewQuestion;
    
    @FXML
    private TextField txtAnswerA;
    
    @FXML
    private TextField txtAnswerB;
    
    @FXML
    private TextField txtAnswerC;
    
    @FXML
    private TextField txtAnswerD;
    
    @FXML
    private void requestRankings(ActionEvent event) {
        System.out.println("Request Rankings Hit");
        this.username = txtUsername.getText();
        
        if (this.broker == null) {
            this.broker = BrokerManager.getInstance(this);
        }
        
        if (!this.username.equalsIgnoreCase("")) {
            this.broker.sendRankingsRequest(this.username);

            btnRequest.setDisable(true);
            btnRankings.setDisable(true);
            txtUsername.setDisable(true);
            System.out.println("Sending message...");
            label.setText("Waiting for message...");
        } else {
            label.setText("Please enter your username.");
        }
    }
    
    @FXML
    private void requestQuestion(ActionEvent event) {
        System.out.println("Request Question Hit");
        this.username = txtUsername.getText();
            
        if (this.broker == null) {
            this.broker = BrokerManager.getInstance(this);
        }
        
        if (!this.username.equalsIgnoreCase("")) {
            this.broker.sendQuestionRequest(this.username);

            btnRequest.setDisable(true);
            btnRankings.setDisable(true);
            txtUsername.setDisable(true);
            System.out.println("Sending message...");
            label.setText("Waiting for message...");
        } else {
            label.setText("Please enter your username.");
        }
    }
    
    @FXML
    private void submitAnswerA(ActionEvent event) {
        System.out.println("Submitting answer A...");
        
        if (this.broker == null) {
            this.broker = BrokerManager.getInstance(this);
        }
        
        setAnswerButtonDisabled(true);
        this.broker.sendAnswer(btnA.getText(), this.username);
    }
    
    @FXML
    private void submitAnswerB(ActionEvent event) {
        System.out.println("Submitting answer B...");
        
        if (this.broker == null) {
            this.broker = BrokerManager.getInstance(this);
        }
        
        setAnswerButtonDisabled(true);
        this.broker.sendAnswer(btnB.getText(), this.username);
    }
    
    @FXML
    private void submitAnswerC(ActionEvent event) {
        System.out.println("Submitting answer C...");
        
        if (this.broker == null) {
            this.broker = BrokerManager.getInstance(this);
        }
        
        setAnswerButtonDisabled(true);
        this.broker.sendAnswer(btnC.getText(), this.username);
    }
    
    @FXML
    private void submitAnswerD(ActionEvent event) {
        System.out.println("Submitting answer D...");
        
        if (this.broker == null) {
            this.broker = BrokerManager.getInstance(this);
        }
        
        setAnswerButtonDisabled(true);
        this.broker.sendAnswer(btnD.getText(), this.username);
    }
    
    @FXML
    private void submitNewQuestion(ActionEvent event) {
        System.out.println("Submitting new question...");
        
        if (this.broker == null) {
            this.broker = BrokerManager.getInstance(this);
        }
        
        this.username = txtUsername.getText();
        String question = txtNewQuestion.getText();
        String answerA = txtAnswerA.getText();
        String answerB = txtAnswerB.getText();
        String answerC = txtAnswerC.getText();
        String answerD = txtAnswerD.getText();
        
        if (!this.username.equals("") && !question.equals("") && !answerA.equals("") && !answerB.equals("") && !answerC.equals("") && !answerD.equals("")) {
            this.broker.sendNewQuestion(this.username, question, answerA, answerB, answerC, answerD);
            txtNewQuestion.setText("");
            txtAnswerA.setText("");
            txtAnswerB.setText("");
            txtAnswerC.setText("");
            txtAnswerD.setText("");
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO ???
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

    public void updateUIWithRankings(int rank, int points, String username) {
        new Thread(new Runnable() {
            @Override public void run() {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        label.setText(String.format("%s is ranked %s with %s points!", username, rank, points));
                        
                        setAnswerButtonDisabled(true);
                    }
                });
            }
        }).start();
    }
    
    private void setAnswerButtonDisabled(boolean value) {
        btnRequest.setDisable(!value);
        btnRankings.setDisable(!value);
        txtUsername.setDisable(!value);
        
        btnNewQuestion.setDisable(!value);
        txtNewQuestion.setDisable(!value);
        txtAnswerA.setDisable(!value);
        txtAnswerB.setDisable(!value);
        txtAnswerC.setDisable(!value);
        txtAnswerD.setDisable(!value);
        
        btnA.setDisable(value);
        btnB.setDisable(value);
        btnC.setDisable(value);
        btnD.setDisable(value);
    }
    
    private String getRandomAnswer(List<String> answers) {
        return answers.get(new Random().nextInt(answers.size()));
    }
}