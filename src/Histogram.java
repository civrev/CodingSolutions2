import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import java.util.concurrent.TimeUnit;

public class Histogram {
    /**
     * Represents a histogram of word frequency in a text file
     *
     * Class for parsing a text file into a histogram based on word
     * frequency, and then displaying the histogram as ASCII art
     **/

    int longestKey = 0;

    private boolean sorted = false;
    private HistEntry[] sortedEntries;

    public HashMap<String, Integer> entries = new HashMap<String, Integer>();

    //Constructors
    public Histogram(){}
    public Histogram(String filename) throws FileNotFoundException{
        /**
         * Creates a Histogram with a specified text file read in
         **/
        readInput(filename);
    }
    
    public static void main(String[] args) throws IOException{
    	/**
    	 * For presentation purposes only
    	 **/

        long lStartTime, lEndTime, output;
    	
        Histogram histogram;

        lStartTime = System.nanoTime();
        
        //the input file
		histogram = new Histogram("input.txt");
		
		//some random books as text files from gutenburg.org ~10k unique words
		//histogram = new Histogram("king_edward_vii.txt");
		//histogram = new Histogram("ben_franklin.txt");
		//histogram = new Histogram("kjv_bible.txt");
		//histogram = new Histogram("plato_republic.txt");

        lEndTime = System.nanoTime();
        output = lEndTime - lStartTime;
        System.out.println("Reading time in milliseconds: " + output / 1000000);

        lStartTime = System.nanoTime();

        //sorting explicity for timing purposes
        histogram.getSortedEntries();
        lEndTime = System.nanoTime();
        output = lEndTime - lStartTime;
        System.out.println("Sorting time in milliseconds: " + output / 1000000);

        lStartTime = System.nanoTime();

        //write output
		histogram.writeHistogramToFile("output.txt");

        lEndTime = System.nanoTime();
        output = lEndTime - lStartTime;
        System.out.println("Writing time in milliseconds: " + output / 1000000);
    }

    public void readInput(String textFileName) throws FileNotFoundException{
        /**
         * Reads the file specified into a hashmap
         *
         * Reads the a text file treating any non-alphabetical character
         * as a delimiter to identify words in the file. Words are then put
         * into a case-insensitive HashMap as the key and their frequency
         * at which the words occur in the file as the value.
         *
         * At this time a word is also checked to see if it the longest word
         * in the HashMap
         **/

        sorted = false;

        Scanner scan = new Scanner(new File(textFileName));
        scan.useDelimiter("[^a-zA-Z]");

        while(scan.hasNext()){
            String word = scan.next();

            if(!validate(word))
                continue;

            word = word.toLowerCase();

            if(word.length() > longestKey)
                longestKey = word.length();

            if(entries.containsKey(word)){
                entries.put(word, entries.get(word) + 1);
            }else{
                entries.put(word, 1);
            }

        }
        
        scan.close();

    }
    
    public void writeHistogramToFile(String textFileName) throws IOException {
        /**
         * Efficiently writes the ASCII art representation of the histogram
         **/
    	BufferedWriter writer = new BufferedWriter(new FileWriter(textFileName));
        for(HistEntry entry : getSortedEntries()) {
            writer.write(entry.toCharArray());
            writer.write("\n");
        }
        writer.close();
    }
    
    public boolean validate(String word) {
        /**
         * Checks if the string is a word that should be included in the histogram
         **/
        if (word.length() == 0)
            return false;

        char let = word.charAt(0);
        return (let >= 'A' && let <= 'Z') || (let >= 'a' && let <= 'z');
    }

    public HistEntry[] getSortedEntries(){
        /**
         * Returns all the histogram entries sorted by frequency
         **/

        if(sorted)
            return sortedEntries;

        int index = 0;
        sortedEntries = new HistEntry[entries.size()];
        for(Entry<String, Integer> entry : entries.entrySet()){
            sortedEntries[index] = new HistEntry(entry.getKey(), entry.getValue());
            index++;
        }

        Arrays.parallelSort(sortedEntries);

        sorted = true;

        return sortedEntries;
    }

    class HistEntry implements Comparable<HistEntry>{
        /**
         * Helper class for sorting and printing entries in the histogram
         * 
         * Has the short and fast toString method for object inspection,
         * and a longer method (toCharArray) to get the ASCII art representation
         * of the line in the histogram
         **/
        String key;
        Integer value;

        public HistEntry(String key, Integer value) {
            this.key = key;
            this.value = value;
        }

        public char[] toCharArray() {
            String prefix = String.format("%1$" + longestKey + "s", key) + " | ";
            String suffix = " (" + value.toString() + ")";
            
            char[] outputArray = new char[prefix.length() + value + suffix.length()];
            Arrays.fill(outputArray, '=');
            
            for(int i = 0; i < prefix.length(); i++)
            	outputArray[i] = prefix.charAt(i);

            for(int i = suffix.length() - 1; i >= 0; i--)
                outputArray[prefix.length() + value + i] = suffix.charAt(i);
            
            return outputArray;
        }

        @Override
        public String toString(){
            return String.format("%1$" + longestKey + "s", key) + " | (" + value.toString() + ")";
        }

        @Override
        public int compareTo(HistEntry other) {
            return -1 * value.compareTo(other.value);
        }
    }

    @Override
    public String toString(){

        if(entries.isEmpty())
            return "Histogram[ empty ]";

        String output = "";

        for(HistEntry entry : getSortedEntries()){
            output += entry.toString() + "\n";
        }

        return output;
    }

}
