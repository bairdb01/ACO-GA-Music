import jm.util.*;
import jm.JMC;
import jm.music.data.*;
import jm.midi.*;
import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;
/**
 * Author: Benjamin Baird
 * Created on: 2017-03-28
 * Last Updated on: 2017-03-31
 * Filename: Environment
 * Description: This class will be the environment of the genetic algorithm.
 *             Manipulates the organisms and handles the evaluations to produce a song.
 *
 *
 * Possible changes: Allow replacing fraction of population
 *                   Change from note range to a list of notes.
 */
public class GAMusic implements JMC{
    static int MAX_NOTE = 70;
	static int MIN_NOTE = 60;
	static double NOTE_LEN = SQ; // How long the note is played
	final static int MAX_ITERATIONS = 150;	// Melody length

    int nReplace = 0;
    ArrayList<Organism> population;
    ArrayList<Double> roulette;
    final int MAX_EPOCH = 100;
    final double MAX_FITNESS = 999.93; //999.93; //1073741000  // 28
    final int PHRASE_LEN = 120; // How many notes in the song. >= 2
    Random rand;

    /**
     * Constructor
     */
    public GAMusic(){
        this.population = new ArrayList<>();
        this.roulette = new ArrayList<>();
        rand = new Random();
    }

    /**
     * Chooses a random fraction of the population to replace
     */
    public void randNReplace() {
        this.nReplace = this.population.size();
    }

    /**
     * Finds an organisms fitness
     */
    public void organismFitness(Organism organism){
        musicFitness(organism);
    }

    /**
     * Finds and sets the fitness of an organisms generated phrase
     */
    public void musicFitness(Organism organism) {
        Phrase p = organism.getPhrase();
        Note [] notes = p.getNoteArray();
        double fitness = 0.0;

        // Closer notes have a higher fitness, but must be somewhat different
        for (int i = 1; i < notes.length; i++) {
            fitness += (notes[i].getPitch() - notes[i-1].getPitch());
        }
        if (fitness < (PHRASE_LEN)) {
            fitness += (double)PHRASE_LEN + (double)PHRASE_LEN/3.0;
        }
        organism.setFitness(fitness);
    }

    /**
     * Converts Binary to decimal
     * @return the decimal value
     */
    public Double binaryToDecimal(String binary){
        Double value = 0.0;
        for (int i = 0; i < binary.length(); i++) {
            if ( binary.charAt(i) == '1') {
                value += Math.pow(2, ((binary.length()-1) - i));
            }
        }
        return value;
    }


    /**
     * Creates the initial population
     * @param n Number of organisms in the population; must be greater than 2
     */
    public int createPopulation(int n) {
        if (n < 2) {
            return -1;
        }

        for (int i = 0; i < n; i++) {
            Organism toAdd = new Organism(PHRASE_LEN, MIN_NOTE, MAX_NOTE, NOTE_LEN);
            this.organismFitness(toAdd);
            this.population.add(toAdd);
        }
        return 0;
    }

    /**
     * Sorts the current population based on fitness in descending order
     */
    public void sortPopulation(){
        // Sort the population based on fitness
        population.sort((o1, o2) -> {
            if (o1.fitness > o2.fitness) {
                return -1;
            } else if (o1.fitness < o2.fitness) {
                return 1;
            } else {
                return 0;
            }
        });
    }

    /**
     * Iterations that will mutate and create better organisms for the problem
     * @return the most fit phrase
     */
    public Phrase evolve(){
        // Iterate until error is low or value is met
        int epoch = 0;
        while (epoch < MAX_EPOCH && population.get(0).getFitness() < MAX_FITNESS) {
            if (epoch % 100 == 0) {
                System.out.println("EPOCH:" + epoch);
            }

            // Sort the population based on fitness
            this.sortPopulation();

            // Calculate the total fitness of population
            Double totalFitness = 0.0;
            for (Organism org: this.population) {
                totalFitness += org.fitness;
            }


            // Figure out how much of the total fitness each organism contributes to total fitness
            for (Organism org: this.population) {
                this.roulette.add(org.fitness/totalFitness);
            }

            // How many of the population to replace, currently replaces all organisms
            randNReplace();

            // Randomly choose organisms to replace
            // Currently replaces all

            // Create new children to replace the selected few organisms
            ArrayList<Organism> children;
            children = new ArrayList<>();

            while (children.size() < this.nReplace) {
                Organism newChild = new Organism(PHRASE_LEN, MIN_NOTE, MAX_NOTE, NOTE_LEN);
                // Choose 2 parents to crossover and create a child, using weighted roulette
                int parentOneID = -1;
                int parentTwoID = -1;

                for (int i = 0; i < 2; i++) {

                    // Don't pick the same parent twice
                    int parentIndex;
                    do {
                        Double randValue = rand.nextDouble();
                        Double rouletteValue = 0.0;
                        parentIndex = -1;

                        while (rouletteValue < randValue) {
                            parentIndex++;
                            rouletteValue += this.roulette.get(parentIndex);
                        }
                    } while (parentOneID == parentIndex);

                    if (i == 0) {
                        parentOneID = parentIndex;
                    } else {
                        parentTwoID = parentIndex;
                    }
                }

                // Choose bits from each parent and create a child using uniform crossover
                Phrase p1Phrase = this.population.get(parentOneID).getPhrase();
                Phrase p2Phrase = this.population.get(parentTwoID).getPhrase();
                newChild.uniformCrossover(p1Phrase, p2Phrase);


                // Mutate child a little
                mutateOrganism(newChild);
                children.add(newChild);
            }

            // Evaluate fitness of new organisms
            for (Organism organism : children) {
                this.organismFitness(organism);
            }

            // New children replace the ones to be replaced, replace all currently
            this.population = children;

            epoch++;
            this.roulette.clear();
        }
        // Sort the population based on fitness
        this.sortPopulation();

        return this.population.get(0).phrase;
    }

    /**
     * Mutates organism's phrase with a probability of 1/length for each bit
     * @param organism
     */
    public void mutateOrganism (Organism organism) {
        Phrase orgPhrase = organism.getPhrase();
        Note [] noteList = orgPhrase.getNoteArray();

        for (int i = 0; i < orgPhrase.length(); i++) {
            Double randNum = rand.nextDouble();
            // Notes have an equal probability to be mutated
            if (randNum < 1.0/orgPhrase.length()) {
                int pitch = noteList[i].getPitch();
                int mutateVal = rand.nextInt();
                if (mutateVal == 0) {
                    mutateVal = -1;
                }

                if ( pitch + mutateVal <= MAX_NOTE && pitch + mutateVal >= MIN_NOTE) {
                    noteList[i].setPitch(pitch + 1);
                }
            }
        }
        organism.setPhrase(new Phrase(noteList));
    }

    /**
     * Prints the current population
     */
    // public void printPopulation(){
    //     for (int i = 0; i < this.population.size(); i++) {
    //         System.out.println(this.population.get(i).toString());
    //     }
    // }

}
