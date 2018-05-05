package quizmonitor.rankings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexander
 */
public class RankingsManager {

    private static final Logger LOG = Logger.getLogger(RankingsManager.class.getName());
        
    private static RankingsManager instance = null;
    private Map rankings;

    /**
     * Exists only to defeat instantiation.
     */
    protected RankingsManager() { }

    /**
     * Get the Singleton Instance.
     * @return The Instance of the class.
     */
    public static RankingsManager getInstance() {
        if(instance == null) {
            instance = new RankingsManager();
        }

        return instance;
    }

    /**
     * Get the rankings.
     * @return The Map containing the current rankings.
     */
    public Map getRankings() {
        if (rankings == null) {
            this.readRankings();
        }
        
        return rankings;
    }
    
    public List<String> getRankingsForUI() {
        if (rankings == null) {
            this.readRankings();
        }
        
        Iterator<Entry<String, Integer>> it = this.rankings.entrySet().iterator();
        
        List<String> returnRankings = new ArrayList<>();
        
        while (it.hasNext()) {
            Map.Entry<String, Integer> pairs = it.next();

            String currentEntry = String.format("%s - %s", pairs.getKey(), pairs.getValue());
            
            returnRankings.add(currentEntry);
        }
        
        return returnRankings;
    }
    
    /**
     * Write the rankings to a specific text file.
     */
    public void writeRankings() {
        if (rankings == null) {
            System.out.println("No rankings found, thus not worth fetching and writing.");
            return;
        }
        
        System.out.println("Writing rankings...");
        
        rankings = new HashMap<>();
        rankings.put("Alex", 123);
        rankings.put("Frank", 49);
        rankings.put("Bert", 12);
        
        try {
            FileWriter fileWriter;
            BufferedWriter bufferedWriter;
            
            fileWriter = new FileWriter("rankings.txt");
            bufferedWriter = new BufferedWriter(fileWriter);
            
            Iterator<Entry<String, Integer>> it = this.rankings.entrySet().iterator();
            
            while (it.hasNext()) {
                Map.Entry<String, Integer> pairs = it.next();
                
                String currentEntry = String.format("%s-%s\n", pairs.getKey(), pairs.getValue());
                
                if (!it.hasNext()) {
                    currentEntry = currentEntry.substring(0, currentEntry.length() - 1);
                }
                
                bufferedWriter.write(currentEntry);
            }
            
            bufferedWriter.close();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    /**
     * Read the rankings from the specified text file.
     */
    public void readRankings() {
        System.out.println("Reading rankings...");
        
        String fileName = "rankings.txt";
        String entry;
        
        this.rankings = new HashMap<>();

        try {
            FileReader fileReader = new FileReader(fileName);

            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                while((entry = bufferedReader.readLine()) != null) {                    
                    String thisKey = entry.substring(0, entry.indexOf('-'));
                    String thisValue = entry.substring(entry.indexOf('-') + 1, entry.length());
                    
                    this.rankings.put(thisKey, thisValue);
                }
            }
        } catch(FileNotFoundException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);               
        } catch(IOException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    /**
     * Add a new participant in the rankings.
     * @param identifier The identifier of the participant.
     * @return Returns true if the participant was added succesfully, false if they were already registered.
     */
    public boolean addToRankings(String identifier) {
        if (rankings == null) {
            this.readRankings();
        }
        
        System.out.println("Adding to rankings...");
        
        this.rankings.put(identifier, 0);
        
        return false;
    }
    
    /**
     * Adjust the rankings by giving a specific participant points.
     * @param identifier The identifier of the participant.
     * @param points The amount of points that need to be added to the rankings for this participant.
     * @throws ParticipantNotFoundException Exception that gets thrown when the participant does not exist within the rankings.
     */
    public void adjustRankings(String identifier, Integer points) throws ParticipantNotFoundException {
        if (rankings == null) {
            this.readRankings();
        }
        
        System.out.println("Adjusting rankings...");
        
        Integer foundPoints = Integer.parseInt(this.rankings.get(identifier).toString());
        
        if (foundPoints == null) {
            throw new ParticipantNotFoundException();
        }
        
        foundPoints += points;
        
        this.rankings.replace(identifier, foundPoints);
    }
    
    /**
     * Fetch the points for a specific participant.
     * @param identifier
     * @return The points of a participant. 
     * @throws ParticipantNotFoundException Exception that gets thrown when the participant does not exist within the rankings.
     */
    public Integer fetchPointForParticipant(String identifier) throws ParticipantNotFoundException {
        if (rankings == null) {
            this.readRankings();
        }
        
        Integer foundPoints = Integer.parseInt(this.rankings.get(identifier).toString());
        
        if (foundPoints == null) {
            throw new ParticipantNotFoundException();
        }
        
        return foundPoints;
    }
}