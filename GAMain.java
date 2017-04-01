import jm.util.*;
import jm.JMC;
import jm.music.data.*;
import jm.midi.*;

/**
 * Author: Ben Baird
 * Created on: 2017-03-28
 * Last Updated on: 2017-03-31
 * Description: Initializes the genetic algorithm to begin creating music.
 *              The most fit organism's score is written to a midi file.
 * Currently setup with 10 in the population generating music for 1 instrument
 *
 **/
public class GAMain implements JMC{

    public static void main(String[] args) {
        int TEMPO = 80; // BPM

        GAMusic env = new GAMusic();
        Score score = new Score("Ant Symphony", TEMPO);
        Part inst1 = new Part("BASS", SYNTH_BASS, 1);    // Represents a single instrument

        env.createPopulation(10);
        System.out.println("Begin evolving");
        inst1.addPhrase(env.evolve());
        System.out.println("End evolving");
        score.addPart(inst1);
        Write.midi(score, "ga.mid");

    }
}
