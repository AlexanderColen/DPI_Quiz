package quizserver.questions;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 *
 * @author Alexander
 */
public final class QuestionManager {

    private static final Logger LOG = Logger.getLogger(QuestionManager.class.getName());
        
    private static QuestionManager instance = null;
    
    private Map requests;
    
    private final Map questions;
    private final List<String> answersA;
    private final List<String> answersB;
    private final List<String> answersC;
    private final List<String> answersD;

    /**
     * Exists only to defeat instantiation.
     */
    protected QuestionManager() {
        this.questions = new HashMap<>();
        this.answersA = new ArrayList<>();
        this.answersB = new ArrayList<>();
        this.answersC = new ArrayList<>();
        this.answersD = new ArrayList<>();
        
        this.addNewQuestion("What temperature (Celsius) does water boil at?", "100", "50", "200", "78");
        this.addNewQuestion("What planet is the closest to the Sun?", "Mercury", "Venus", "Earth", "Mars");
        this.addNewQuestion("What symbol does the chemical element Hydrogen have?", "H", "O", "C", "He");
        this.addNewQuestion("How long is a decennium?", "10 years", "10 days", "10 months", "10 centuries");
        this.addNewQuestion("What is the largest country on Earth?", "Russia", "United States", "Canada", "China");
    }

    /**
     * Get the Singleton Instance.
     * @return The Instance of the class.
     */
    public static QuestionManager getInstance() {
        if(instance == null) {
            instance = new QuestionManager();
        }

        return instance;
    }

    /**
     * Get the questions.
     * @return The Map containing the current questions.
     */
    public Map getQuestions() {
        return this.questions;
    }
    
    public String[] getRandomQuestion() {
        int random = new Random().nextInt(this.questions.size()) + 1;
        
        String[] question = new String[5];
        
        question[0] = (String) this.questions.get(random);
        question[1] = (String) this.answersA.get(random - 1);
        question[2] = (String) this.answersB.get(random - 1);
        question[3] = (String) this.answersC.get(random - 1);
        question[4] = (String) this.answersD.get(random - 1);
        
        return question;
    }
    
    public void addRequest(String username, String question) {
        this.requests.put(username, question);
    }
    
    public void removeRequest(String username, String question) {
        this.requests.remove(username, question);
    }

    /**
     * Get the requests in a format suitable to show in the UI.
     * @return A List with the requests as formatted Strings.
     */
    public List<String> getRequestsForUI() {
        if (this.requests == null) {
            this.requests = new HashMap<>();
        }
        
        List<String> returnRequests = new ArrayList<>();
        
        Iterator<Map.Entry<String, Integer>> it = this.requests.entrySet().iterator();
        
        while (it.hasNext()) {
            Map.Entry<String, Integer> pairs = it.next();
            
            String currentEntry = String.format("%s requested: '%s'", pairs.getKey(), pairs.getValue());
            
            returnRequests.add(currentEntry);
        }
        
        return returnRequests;
    }
    
    /**
     * Add a new question to the Map.
     * @param question The new question.
     * @param answerA The correct answer for the question.
     * @param answerB A second answer to the question.
     * @param answerC A third answer to the question.
     * @param answerD A fourth answer to the question.
     * @return True if the question was added succesfully, otherwise false.
     */
    public boolean addNewQuestion(String question, String answerA, String answerB, String answerC, String answerD) {
        System.out.println("Adding new question hit.");
        
        if (!this.verifyUniqueQuestion(question)) {
            System.out.println("Duplicate.");
        }
        
        System.out.println("Adding question.");
        
        this.questions.put(this.questions.size() +1, question);
        this.answersA.add(answerA);
        this.answersB.add(answerB);
        this.answersC.add(answerC);
        this.answersD.add(answerD);
        
        return true;
    }
    
    /**
     * Check if the answer is correct for a question.
     * @param question The question.
     * @param answer The chosen answer to the question.
     * @return True if it was correct, false if not.
     */
    public boolean checkIfCorrectAnswer(String question, String answer) {
        int questionID = 0;
        
        for (int i = 0; i < this.questions.size(); i++) {
            if (((String) this.questions.get(i + 1)).equalsIgnoreCase(question)) {
                questionID = i;
                break;
            }
        }
                
        return this.answersA.get(questionID).equalsIgnoreCase(answer);
    }
    
    /**
     * Verify if a question is unique enough.
     * @param newQuestion The new question that is going to be added.
     */
    private boolean verifyUniqueQuestion(String newQuestion) {
        if (this.questions == null || this.questions.isEmpty()) {
            return true;
        }
        
        Iterator<Map.Entry<Integer, String>> it = this.questions.entrySet().iterator();
        
        while (it.hasNext()) {
            String question = it.next().getValue();
            
            List<String> questionWords = this.splitSentenceIntoWords(question);
            List<String> newWords = this.splitSentenceIntoWords(newQuestion);
            
            float sameWords = 0;
            
            for (String word : questionWords) {
                for (String newWord : newWords) {
                    if (word.equalsIgnoreCase(newWord)) {
                        sameWords++;
                    }
                }
            }
            
            if (sameWords / questionWords.size() * 100 <= 0.9) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Split a sentence into individual words.
     * @param sentence The sentence that needs to be split.
     * @return A List containing all the words from the given sentence.
     */
    private List<String> splitSentenceIntoWords(String sentence) {
        List<String> words = new ArrayList<>();
        
        BreakIterator breakIterator = BreakIterator.getWordInstance();
        breakIterator.setText(sentence);
        
        int lastIndex = breakIterator.first();
        
        while (BreakIterator.DONE != lastIndex) {
            int firstIndex = lastIndex;
            lastIndex = breakIterator.next();
            
            if (lastIndex != BreakIterator.DONE && Character.isLetterOrDigit(sentence.charAt(firstIndex))) {
                words.add(sentence.substring(firstIndex, lastIndex));
            }
        }

        return words;
    }
}