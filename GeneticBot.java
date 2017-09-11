import java.io.*;
import java.util.*;

/**
 * Created by Corban on 7/16/2017.
 */

public class GeneticBot {

    //Instance Variables
    private ArrayList<String> geneticComponents;
    private ArrayList<String> pop;
    private ArrayList<Double> scores;
    private double globalMin = 500;
    public String goal = "";
    private int generation;
    private double minError = 500;
    private double sumError = 0;
    private double maxError = -500;


    //Constructor
    public GeneticBot(){
        geneticComponents = new ArrayList<>();
        pop = new ArrayList<>();
        scores = new ArrayList<>();
        generation = 0;
        int populationSize = 50;


        //Scans in Genetic Material Components
        try {

            Scanner s = new Scanner(new File("geneticComponents.txt"));

            while(s.hasNextLine()){
                String nucleotides = s.nextLine();

                //Dissembles Entries
                for (int i = 0; i < nucleotides.length(); i++){
                    String component = nucleotides.substring(i, i + 1);

                    if(!geneticComponents.contains(component)) {
                        geneticComponents.add(component);
                    }
                }
            }

            //Establish Initial Population

            Random rand = new Random();
            int goalLength = rand.nextInt(10) + 1;
            goal = randomString(goalLength);

            //goal = "?";
            for (int i = 0; i < populationSize; i++) {
                pop.add(randomString(goal.length()));
            }

        } catch (FileNotFoundException e) {
            System.out.print("Error: Genetic Components File Not Found \n " +
                    "-----FAILURE-----");
        }
    }

    //Genetic Methods
    public void fitnessCalculation(){
        ArrayList<String> individuals = new ArrayList<>(pop);
        for(String individual : individuals) {
            double individualScore = 0;
            for (int i = 0; i < individual.length(); i++) {
                individualScore += distanceBetweenComponents(individual.substring(i, i + 1), goal.substring(i, i + 1));
            }
            minError = Math.min(individualScore, minError);
            maxError = Math.max(individualScore, maxError);
            sumError += individualScore;
            individualScore /= 10;
            scores.add(individualScore);
        }

    }

    public String crossOver(List<String> parents){
        StringBuilder child = new StringBuilder();
        Random rand = new Random();

        //Randomly Picks Components of Parents to Give Child
        for (int i = 0; i < parents.get(0).length(); i++) {
            double temp = rand.nextDouble();

            if(temp > .5){
                child.append(parents.get(0).substring(i, i + 1));
            }
            else if(temp > .007){
                child.append(parents.get(1).substring(i, i + 1));
            }
            else{
                //Random Mutation
                child.append(geneticComponents.get(rand.nextInt(geneticComponents.size())));
            }
        }
        return child.toString();
    }//Assumes Parents are Compatible for Cross Over & Only 2 Parents

    public void selection(){

        ArrayList<String> breeders = new ArrayList<>();

        ArrayList<Double> topScores = new ArrayList<>(scores);
        Collections.sort(topScores);
        for(int i = 0; i < pop.size()/2; i++)
            breeders.add(pop.get(scores.indexOf(topScores.get(i))));

        ArrayList<String> newPop = new ArrayList<>();
        while(newPop.size() < pop.size()){
            //Select Breeding Pair
            String first = breeders.remove(new Random().nextInt(breeders.size()));
            String second = breeders.remove(new Random().nextInt(breeders.size()));
            ArrayList<String> pair = new ArrayList<>();
            pair.add(first);
            pair.add(second);

            //Breed
            String child = crossOver(pair);
            newPop.add(child);

            //Add Back to the Mix
            breeders.add(first);
            breeders.add(second);

        }

        //Re-Populate
        pop.clear();
        scores.clear();
        for(String str : newPop){
            pop.add(str);
        }

        //Sets-Up Stat's for Next Gen
        ++generation;
        globalMin = Math.min(globalMin, minError);
        minError = 500;
        maxError = -500;
        sumError = 0;
    }


    //Display Methods
    public void printPopulation(){
        StringBuilder ret = new StringBuilder("\n==== Generation #" + generation + " ==== \n");
        ArrayList<String> organisms = new ArrayList<>(pop);
        for (int i = 0; i < organisms.size(); i++) {
            if(i % 2 == 0)
                ret.append("[" + organisms.get(i)).append("]\t["
                        + scores.get(i) + "]\t");
            else
                ret.append("[" + organisms.get(i)).append("]\t["  + scores.get(i) + "]\n");
        }
        ret.append("Min Error: " + minError + "\tMax Error: " + maxError + "\tAvg Error: " + getAverageError() + "\n");
        FileWriter fw = null;
        try {
            fw = new FileWriter(new File("Progress.txt"), true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw, true);
            pw.println(ret);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.out.println(ret.toString());
    }



    private double getAverageError(){
        return sumError/pop.size();
    }
    private double getMinError(){
        return minError;
    }
    private double getGlobalMin(){
        return globalMin;
    }







    //Helper Methods
    private String randomString(int length){
        StringBuilder ret = new StringBuilder();

        //Anti Re-Initialization
        Random rand = new Random();
        int index = -1;
        int bankSize = geneticComponents.size();

        for (int i = 0; i < length; i++) {
            index = rand.nextInt(bankSize);
            ret.append(geneticComponents.get(index));
        }

        return ret.toString();
    }

    private int distanceBetweenComponents(String comp1, String comp2){
        if(Objects.equals(comp1, comp2))
            return 0;

        int start = geneticComponents.indexOf(comp1);
        int end = geneticComponents.indexOf(comp2);
        int directDistance = Math.abs(end - start);
        int circularDistance = start + geneticComponents.size() - end;

        /* Nasty Cheap Way of Finding Circular Distance
        int pointer = geneticComponents.indexOf(comp1);

        int distanceForwards = 0;
        while(!geneticComponents.get(pointer).equals(comp2)){
            pointer = (pointer + 1) % geneticComponents.size();
            distanceForwards++;
        }

        int distanceBackwards = 0;
        while(!geneticComponents.get(pointer).equals(comp2)){
           pointer = --pointer >= 0? pointer: geneticComponents.size() - 1;
           distanceBackwards++;
        }
        */
        return Math.min(directDistance, circularDistance);
    }

    //Main Method
    public static void main(String[] args){
        GeneticBot distortedTestSubject = new GeneticBot();

        for (int i = 0; i < 2000; i++) {
            distortedTestSubject.fitnessCalculation();
            distortedTestSubject.printPopulation();
            if(distortedTestSubject.getAverageError() <= 0.0)
                break;
            distortedTestSubject.selection();
        }

        try {
            FileWriter fw = new FileWriter(new File("Progress.txt"), true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw, true);
            pw.println("Global Minimum: " + distortedTestSubject.getGlobalMin());
            pw.println("Goal: " + distortedTestSubject.goal);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



}
