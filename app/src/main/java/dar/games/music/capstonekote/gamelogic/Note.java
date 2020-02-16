package dar.games.music.capstonekote.gamelogic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DecimalFormat;

import be.tarsos.dsp.util.PitchConverter;
import dar.games.music.capstonekote.R;

public class Note {

    private static final String[] notesNamesArray = {"C", "C#", "D", "D#", "E", "F", "F#",
            "G", "G#", "A", "A#", "B"};
    private static final String SILENCE_NOTE = "S";
    private static final int SILENCE_NOTE_VALUE = -6;

    private double probability;
    private String name;
    private int octave;
    private double timeStamp;

    private boolean used = false;
    private double duration;

    /**
     * A simple constructor
     * @param name - The note symbol (from notesNamesArray) or SILENCE_NOTE if silence detected.
     * @param octave - the note octave.
     * @param timeStamp - The time stamp from the start of the note relative to the recording.
     * @param probability - The note detection probability score from 0.0-1.0.
     * @param duration - The note duration in seconds.
     */
    Note(String name, int octave, double timeStamp, double probability, double duration) {
        this.name = name;
        this.octave = octave;
        this.timeStamp = timeStamp;
        this.probability = probability;
        this.duration = duration;
    }

    /**
     * Converting recording data to a Note Object.
     * @param pitchInHz
     * @param timeStamp
     * @param prob
     * @return A newly created Note object
     */
    @SuppressWarnings("JavaDoc")
    public static Note convertToNote(double pitchInHz, double timeStamp, double prob) {

        if (pitchInHz < 0)
            return new Note("S", 0, timeStamp, prob, NoteArrayList.FRAME_INTERVAL);
        int midiKey = PitchConverter.hertzToMidiKey(pitchInHz);
        return new Note(notesNamesArray[midiKey % 12], ((midiKey / 12) - 2), timeStamp,
                prob, NoteArrayList.FRAME_INTERVAL);
    }

    public int getAbsoluteNoteValue() {
        return getNoteValue() + 12 * this.octave;
    }

    /**
     * finding the res for the note.
     * @return
     */
    public int getSoundRes() {
        if (this.getOctave() == 2) {
            switch (this.getNoteValue()) {
                case 0:
                    return R.raw.c2;
                case 1:
                    return R.raw.db2;
                case 2:
                    return R.raw.d2;
                case 3:
                    return R.raw.eb2;
                case 4:
                    return R.raw.e2;
                case 5:
                    return R.raw.f2;
                case 6:
                    return R.raw.gb2;
                case 7:
                    return R.raw.g2;
                case 8:
                    return R.raw.ab2;
                case 9:
                    return R.raw.a2;
                case 10:
                    return R.raw.bb2;
                default:
                    return R.raw.b2;
            }
        } else if (this.getOctave() == 3) {
            switch (this.getNoteValue()) {
                case 0:
                    return R.raw.c3;
                case 1:
                    return R.raw.db3;
                case 2:
                    return R.raw.d3;
                case 3:
                    return R.raw.eb3;
                case 4:
                    return R.raw.e3;
                case 5:
                    return R.raw.f3;
                case 6:
                    return R.raw.gb3;
                case 7:
                    return R.raw.g3;
                case 8:
                    return R.raw.ab3;
                case 9:
                    return R.raw.a3;
                case 10:
                    return R.raw.bb3;
                default:
                    return R.raw.b3;
            }
        } else {
            switch (this.getNoteValue()) {
                case 0:
                    return R.raw.c4;
                case 1:
                    return R.raw.db4;
                case 2:
                    return R.raw.d4;
                case 3:
                    return R.raw.eb4;
                case 4:
                    return R.raw.e4;
                case 5:
                    return R.raw.f4;
                case 6:
                    return R.raw.gb4;
                case 7:
                    return R.raw.g4;
                case 8:
                    return R.raw.ab4;
                case 9:
                    return R.raw.a4;
                case 10:
                    return R.raw.bb4;
                default:
                    return R.raw.b4;
            }
        }
    }

    public int getImageRes() {
        if (this.getOctave() == 2) {
            switch (this.getNoteValue()) {
                case 0:
                    return R.drawable.pian_g_n12;
                case 1:
                    return R.drawable.pian_g_n11;
                case 2:
                    return R.drawable.pian_g_n10;
                case 3:
                    return R.drawable.pian_g_n9;
                case 4:
                    return R.drawable.pian_g_n8;
                case 5:
                    return R.drawable.pian_g_n7;
                case 6:
                    return R.drawable.pian_g_n6;
                case 7:
                    return R.drawable.pian_g_n5;
                case 8:
                    return R.drawable.pian_g_n4;
                case 9:
                    return R.drawable.pian_g_n3;
                case 10:
                    return R.drawable.pian_g_n2;
                default:
                    return R.drawable.pian_g_n1;
            }
        } else if (this.getOctave() == 3) {
            switch (this.getNoteValue()) {
                case 0:
                    return R.drawable.pian_g_0;
                case 1:
                    return R.drawable.pian_g_1;
                case 2:
                    return R.drawable.pian_g_2;
                case 3:
                    return R.drawable.pian_g_3;
                case 4:
                    return R.drawable.pian_g_4;
                case 5:
                    return R.drawable.pian_g_5;
                case 6:
                    return R.drawable.pian_g_6;
                case 7:
                    return R.drawable.pian_g_7;
                case 8:
                    return R.drawable.pian_g_8;
                case 9:
                    return R.drawable.pian_g_9;
                case 10:
                    return R.drawable.pian_g_10;
                default:
                    return R.drawable.pian_g_11;
            }
        } else {
            switch (this.getNoteValue()) {
                case 0:
                    return R.drawable.pian_g_12;
                case 1:
                    return R.drawable.pian_g_13;
                case 2:
                    return R.drawable.pian_g_14;
                case 3:
                    return R.drawable.pian_g_15;
                case 4:
                    return R.drawable.pian_g_16;
                case 5:
                    return R.drawable.pian_g_17;
                case 6:
                    return R.drawable.pian_g_18;
                case 7:
                    return R.drawable.pian_g_19;
                case 8:
                    return R.drawable.pian_g_20;
                case 9:
                    return R.drawable.pian_g_21;
                case 10:
                    return R.drawable.pian_g_22;
                default:
                    return R.drawable.pian_g_23;
            }
        }
    }

    /**
     * Comparison method also allowing to compare two null objects as a success.
     * @param noteA
     * @param noteB
     * @return true if both of the notes are null or getting true on the equals() method.
     */
    static boolean nullableEquals(Note noteA, Note noteB) {
        if (noteA == null && noteB == null) {
            return true;
        } else if (noteA != null)
            return noteA.equals(noteB);
        else return false;
    }


    boolean isSilence() {
        return this.name.equals(SILENCE_NOTE);
    }
    // ********************
    // Getters and Setters
    // ********************

    boolean isUsed() {
        return used;
    }

    void setUsed() {
        this.used = true;
    }

    public String getName() {
        return name;
    }

    public int getOctave() {
        return octave;
    }

    double getDuration() {
        return duration;
    }

    void setDuration(double duration) {
        this.duration = duration;
    }

    public double getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(double timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getProbability() {
        return probability;
    }

    private int getNoteValue() {
        if (!this.isSilence())
            for (int i = 0; i < notesNamesArray.length; i++) {
                if (this.getName().equals(notesNamesArray[i])) {
                    return i;
                }
            }
        return SILENCE_NOTE_VALUE;
    }


    @NonNull
    @Override
    public String toString() {
        DecimalFormat numFormat = new DecimalFormat("0.0000");
        return this.name + "-" + this.octave + "-"
                + numFormat.format(this.timeStamp) + numFormat.format(this.duration);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof Note) {
            return ((Note)obj).getName().equals(this.name);
        }
        return false;
    }
}
