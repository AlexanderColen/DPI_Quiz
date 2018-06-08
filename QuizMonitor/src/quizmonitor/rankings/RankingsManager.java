package quizmonitor.rankings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
        if (this.rankings == null) {
            this.readRankings();
        }
        
        return this.rankings;
    }
    
    public List<String> getRankingsForUI() {
        if (this.rankings == null) {
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
        if (this.rankings == null) {
            LOG.log(Level.INFO, "No rankings found, thus not worth fetching and writing.");
            return;
        }
        
        LOG.log(Level.INFO, "Writing rankings...");
        
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
        LOG.log(Level.INFO, "Reading rankings...");
        
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
        if (this.rankings == null) {
            this.readRankings();
        }
        
        LOG.log(Level.INFO, "Adding to rankings...");
        
        this.rankings.put(identifier, 0);
        
        return false;
    }
    
    /**
     * Adjust the rankings by giving a specific participant points.
     * @param identifier The identifier of the participant.
     * @param points The amount of points that need to be added to the rankings for this participant.
     */
    public void adjustRankings(String identifier, Integer points) {
        if (this.rankings == null) {
            this.readRankings();
        }
        
        LOG.log(Level.INFO, "Adjusting rankings...");
        
        if (this.rankings.get(identifier) == null) {
            this.addToRankings(identifier);
        }
        
        Integer foundPoints = Integer.parseInt(this.rankings.get(identifier).toString());
        
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
        if (this.rankings == null) {
            this.readRankings();
        }
        
        Integer foundPoints = 0;
        
        if (this.rankings.get(identifier) != null) {
            foundPoints = Integer.parseInt(this.rankings.get(identifier).toString());
        } else {
            this.addToRankings(identifier);
        }
        
        return foundPoints;
    }

    public int getRankForParticipant(String username) {
        if (this.rankings == null) {
            this.readRankings();
        }
            
        Iterator it = this.rankings.entrySet().iterator();
        int count = 1;
        
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            
            if (pair.getKey().toString().equalsIgnoreCase(username)) {
                return count;
            }
            
            count++;
        }
        
        return 0;
    }
}