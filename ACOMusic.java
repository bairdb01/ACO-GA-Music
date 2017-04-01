import jm.util.*;
import jm.JMC;
import jm.music.data.*;
import jm.midi.*;
import java.util.Random;
import java.util.Arrays;

/**
 * Author: Ben Baird
 * Created on: 2017-03-24
 * Last Updated on: 2017-03-31
 * Generate a musical score using ant colony optimization.
 * Ants are randomly placed on notes and navigate around based on probability.
 * Ants lay pheromones as they more, which raise the probability of other ants following.
 * Eventually the ants will form a trail, which most will follow.
 * Pheromone levels to notes are located in the pheromones array. (Makeshift undirected graph)
 *
 * Currently set up to produce a 3 instrument musical score and saves to a midi file.
 */

public class ACOMusic implements JMC {
	static int MAX_NOTE = 70;
	static int MIN_NOTE = 60;
	static double NOTE_LEN = SQ;
	static int NUM_NOTES = MAX_NOTE - MIN_NOTE + 1;
	final static Double PHEROMONE_STR = 0.1;
	final static Double PHEROMONE_EVP = 0.01;
	final static int NUM_NOTES_IN_MEL = 8;
	final static int NUM_ANTS_SILENT = 10;
	final static double BETA = 1.5; // Similarity of notes >= 1
	final static int MAX_ITERATIONS = 40;	// Melody length
	final static int TEMPO = 100; // BPM
	static Random rand = new Random();
	static Double [][] pheromones;

    public static void main(String [] args) {
        Score score = new Score("Ant Symphony", TEMPO);
        Part inst1 = new Part("BASS", SYNTH_BASS, 1);    // Represents a single instrument
		Part inst2 = new Part("SNARE", 25, 2);
		Part inst3 = new Part("SNARE", 25, 3);
		MIN_NOTE = 30;
		MAX_NOTE = 70;
		NUM_NOTES = MAX_NOTE - MIN_NOTE + 1;
		inst1.addPhrase(findTune());

		MIN_NOTE = 40;
		MAX_NOTE = 50;
		NUM_NOTES = MAX_NOTE - MIN_NOTE + 1;
		// NOTE_LEN = 1.5;
		inst2.addPhrase(findTune());

		MIN_NOTE = 30;
		MAX_NOTE = 40;
		NUM_NOTES = MAX_NOTE - MIN_NOTE + 1;
		inst3.addPhrase(findTune());

        score.addPart(inst1);
		score.addPart(inst2);
		score.addPart(inst3);
        Write.midi(score, "aco.mid");
		// View.show(score);
		// Play.midi(score);
    }

	/**
	 * Weakens all pheromone trails
	 */
	public static void evaporatePheromones(){
		for (int i = MIN_NOTE; i <= MAX_NOTE; i++){
			for (int j = MIN_NOTE; j <= MAX_NOTE; j++) {
				pheromones[i - MIN_NOTE][j - MIN_NOTE] *= (1 - PHEROMONE_EVP);
			}
		}
	}

	/**
	 * Choose a note to move to based on the strength of pheromones and
	 * proximity of the notes (closer = better desireability
	 * @return the noteId of the chosen note
	 */
	public static int chooseNote(int curNoteId){
		Double totalP = 0.0;
		for(int i = MIN_NOTE; i <= MAX_NOTE; i++) {
			double desireability = Math.pow(1.0/(Math.abs(curNoteId - i)+1), BETA);
			double ph = getPheromone(curNoteId - MIN_NOTE, i - MIN_NOTE);
			totalP += ph * desireability;
		}

		// Cumulative probability
		Double randP = rand.nextDouble(); //value b/w 0 -> 1
		Double curVal = 0.0;
		for(int i = MIN_NOTE; i <= MAX_NOTE; i++) {
			double desireability = Math.pow(1.0/(Math.abs(curNoteId - i)+1), BETA);
			double ph = getPheromone(curNoteId - MIN_NOTE, i - MIN_NOTE);
			curVal += (ph * desireability) / totalP;

			if (curVal >= randP) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * Grabs the pheromone strength between two nodtes
	 * @return pheromone level between two notes
	 **/
	public static Double getPheromone(int noteOne, int noteTwo){
		return pheromones[noteOne][noteTwo];
	}

	/**
	* Sets the strength of the pheromones from one note to another
	* @param strength The pheromone's strength
	* @param noteOne NoteOne's integer value
	* @param noteTwo NoteTwo's integer value
	**/
	public static void setPheromone(int noteOne, int noteTwo){
		pheromones[noteOne][noteTwo] += PHEROMONE_STR;
		if ( noteOne != noteTwo) {
			pheromones[noteTwo][noteOne] += PHEROMONE_STR;
		}
	}

	/**
	 * Finds a tune for an instrument and adds to a Phrase
	 **/
	public static Phrase findTune(){
		pheromones = new Double[NUM_NOTES][NUM_NOTES];
		Integer [] antLocations = new Integer[NUM_NOTES];

        Phrase phr = new Phrase(0.0);

		// Choose a random starting note for each ant
		Arrays.fill(antLocations, 0);
		for (Integer i = 0; i < NUM_ANTS_SILENT; i++) {
			int rVal = rand.nextInt(NUM_NOTES);
			antLocations[rVal]++;
		}

		int playingAntsLoc[] = new int[NUM_NOTES_IN_MEL];
		for (int i = 0; i < NUM_NOTES_IN_MEL; i++) {
			playingAntsLoc[i] = rand.nextInt(NUM_NOTES) + MIN_NOTE;
		}

		// Initialize pheromone values
		for (int i = 0; i < pheromones.length;i++) {
			Arrays.fill(pheromones[i], PHEROMONE_STR);
		}

		int curIter = 0;
		Integer newAntLocations [] = new Integer[NUM_NOTES];
		Arrays.fill(newAntLocations, 0);
		while (curIter < MAX_ITERATIONS) {
			if (curIter % 10 == 0) {
				System.out.println("Current Iteration:" + curIter);
			}

			// Move all silent ants to establish/reinforce a trail
			for (int curNote = MIN_NOTE; curNote <= MAX_NOTE; curNote++) {

				// If there are multiple ants on the note, move them off
				while (antLocations[curNote - MIN_NOTE] != 0) {
					// Choose the note to move an ant to
					int newNoteId = chooseNote(curNote);

					// Deposit pheromones on the edge (note1 <-> note2)
					setPheromone(curNote-MIN_NOTE, newNoteId - MIN_NOTE);

					// Move to the new note
					newAntLocations[newNoteId - MIN_NOTE]++;
					antLocations[curNote - MIN_NOTE]--;
				}
			}

			// Playing ants to play notes
			for (int i = 0; i < NUM_NOTES_IN_MEL; i++) {
				// Move and lay pheromones
				int newNoteId = chooseNote(playingAntsLoc[i]);
				setPheromone(playingAntsLoc[i]-MIN_NOTE, newNoteId - MIN_NOTE);
				playingAntsLoc[i] = newNoteId;
				Note note = new Note(newNoteId, NOTE_LEN);    // Generate a  note from 0 - 127, length of note
				phr.addNote(note);  // Add the note to the phrase
			}

			antLocations = newAntLocations;

			evaporatePheromones();
			curIter++;
		}

		return phr;
	}

	public static void printPheromones(){
		for (int i = 0; i < NUM_NOTES; i++) {
			for (int j = i; j < NUM_NOTES; j++) {
				System.out.println("P["+i+"]["+j+"]: " + pheromones[i][j]);
			}
		}
	}
}
