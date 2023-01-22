package gh2;
import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

/**
 * A client that uses the synthesizer package to replicate a plucked guitar string sound
 */
public class GuitarHero {
    public static final double CONCERT_BASE = 440.0;

    public static final String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
    public static void main(String[] args) {
        int len = keyboard.length();
        GuitarString[] guitarStrings = new GuitarString[len];
        for (int i = 0;i < len;i++) {
            double frequency = CONCERT_BASE * Math.pow(2, (i - 24.0) / 12.0);
            guitarStrings[i] = new GuitarString(frequency);
        }
        while (true) {
            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                int index = keyboard.indexOf(key);
                if (index == -1) {
                    continue;
                } else {
                    guitarStrings[index].pluck();
                }
            }
            double sample = 0.0;
            /* compute the superposition of samples */
            for (int i = 0;i < len;i++) {
                sample += guitarStrings[i].sample();
            }
            /* play the sample on standard audio */
            StdAudio.play(sample);

            /* advance the simulation of each guitar string by one step */
            for (int i = 0;i < len;i++) {
                guitarStrings[i].tic();
            }
        }
    }
}

