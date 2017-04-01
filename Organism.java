import java.util.Random;
import jm.util.*;
import jm.JMC;
import jm.music.data.*;
import jm.midi.*;

/**
 * Author: Benjamin Baird
 * Created on: 2017-03-28
 * Last Updated on: 2017-03-31
 * Filename: Organism
 * Description: A class to represent individuals of the population
 */
public class Organism {
    Double fitness = 1.0;
    Phrase phrase;
    Random rand;

    public Organism(int length, int MIN_NOTE, int MAX_NOTE, double NOTE_LEN){
        this.phrase = new Phrase(0.0);
        this.genPhrase(length, MIN_NOTE, MAX_NOTE, NOTE_LEN);
        this.rand = new Random();
    }

    /**
     * Generate a random phrase of length notes
     * @param length
     */
    void genPhrase(int length, int MIN_NOTE, int MAX_NOTE, double NOTE_LEN){
        Random rand = new Random();

        for (int i = 0; i < length; i++) {
            int newNoteId = rand.nextInt(MAX_NOTE-MIN_NOTE+1) + MIN_NOTE;
            this.phrase.addNote(new Note(newNoteId, NOTE_LEN));
        }
    }

    Double getFitness(){
        return this.fitness;
    }

    Phrase getPhrase(){
        return this.phrase;
    }

    void setPhrase(Phrase p) {
        this.phrase = p;
    }
    void setFitness(Double fitness) {
        this.fitness = fitness;
    }

    void uniformCrossover(Phrase p1, Phrase p2){
        // Take a note from one or the other phrase
        Note [] p1Notes = p1.getNoteArray();
        Note [] p2Notes = p2.getNoteArray();

        Phrase newPhrase = new Phrase(0.0);

        for(int i = 0; i < p1Notes.length; i++) {
            int choice = rand.nextInt(2) + 1;
            if (choice == 1) {
                newPhrase.addNote(p1Notes[i]);
            } else {
                newPhrase.addNote(p2Notes[i]);
            }
        }
        setPhrase(newPhrase);
    }

    // @Override
    // public String toString(){
    //     String s = String.format("Phrase: %s\nFitness: %.0f\n", this.phrase.toString(), this.fitness);
    //     return s;
    // }
}
