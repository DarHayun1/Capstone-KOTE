package dar.games.music.capstonekote.gamelogic;

import java.util.ArrayList;

/**
 * Class for handling Note sequences.
 */
public class NoteArrayList extends ArrayList<Note> {

    //The estimated interval in seconds which the TarsosDSP library throws a new detection.
    static final double FRAME_INTERVAL = 0.0464;

    // Constants representing the BPM of the songs (NORMAL_BPM is for the EASY & HARD difficulties)
    private static final double NORMAL_BPM = 90;
    private static final double EXTREME_BPM = 105;

    //Semi Beats per second, splitting the beats to smaller parts for calculation measures.
    private static final double NORMAL_SBPS = 2 * 4 * NORMAL_BPM / 60;
    private static final double EXTREME_SBPS = 2 * 4 * EXTREME_BPM / 60;

    //
    private String mFileName = null;

    /**
     * A simple constructor
     *
     * @param initialCapacity - For the ArrayList initialization.
     * @param fileName           - The NoteArrayList original fileName - for finding the file name
     */
    public NoteArrayList(int initialCapacity, String fileName) {
        super(initialCapacity);
        mFileName = fileName;
    }

    public NoteArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * @param melodyText
     * @param melodyNum
     * @param diff
     * @return
     */
    static NoteArrayList textToNoteArrayList(String melodyText, int melodyNum, int diff) {

        double bpm = NORMAL_BPM;
        if (diff == KoteGame.EXTREME_DIFFICULTY) {
            bpm = EXTREME_BPM;
        }

        String[] tempArr = melodyText.split(",", 0);
        String fileName = KoteGame.getDifficultyName(diff) + (melodyNum);
        NoteArrayList noteAl = new NoteArrayList(10, fileName);
        for (String n : tempArr) {
            String[] noteParts = n.trim().split("-");

            int boxNum = Character.getNumericValue(noteParts[2].charAt(0)) - 1;
            double timeStamp = ((boxNum * 4 +
                    (double) Character.getNumericValue(noteParts[2].charAt(1)) - 1) /
                    (4 * bpm / 60));
            noteAl.add(new Note(noteParts[0],
                    Integer.valueOf(noteParts[1]),
                    timeStamp,
                    1,
                    Double.valueOf(noteParts[3]) / (4 * bpm / 60)));
        }
        noteAl.removeOffset(0);
        return noteAl;
    }

    /**
     * Helper method to find note sequences, delete the spare notes,
     * change the first note duration to the total duration of the sequence.
     *
     * @param plyNotes - The note array
     * @param sbps     - semi beats per second, used to sync the array.
     */
    private static void convertToNoteSequences(Note[] plyNotes, double sbps) {
        for (int i = 0; i < plyNotes.length; i++) {
            int j = i + 1;
            double endTs = plyNotes[i].getTimeStamp() + plyNotes[i].getDuration();
            while (j < plyNotes.length) {
                if (plyNotes[j] == null) {
                    endTs += (1 / sbps);
                    j++;
                } else if (plyNotes[j].equals(plyNotes[i])) {
                    endTs = plyNotes[j].getTimeStamp() + plyNotes[j].getDuration();
                    plyNotes[j] = null;
                    j++;
                } else break;
            }
            if (j > i + 1) {
                plyNotes[i].setDuration(endTs - plyNotes[i].getTimeStamp());
                i = j - 1;
            }
        }
    }

//    /**
//     * Takes a sample pair and converting it to a NoteArrayList.
//     *
//     * @param samplePair - A Pair containing a String representing the notes and
//     *                   an Integer representing the music file id.
//     * @param diff       - The sample difficulty.
//     * @return A newly created NoteArrayList.
//     */
//    static NoteArrayList samplePairToNoteArray(Pair<String, Integer> samplePair, int diff) {
//
//        double bpm = NORMAL_BPM;
//        if (diff == KoteGame.EXTREME_DIFFICULTY) {
//            bpm = EXTREME_BPM;
//        }
//
//        String[] tempArr = samplePair.first.split(",", 0);
//        NoteArrayList noteAl = new NoteArrayList(500, samplePair.second);
//        for (String n : tempArr) {
//            String[] noteParts = n.trim().split("-");
//
//            int boxNum = Character.getNumericValue(noteParts[2].charAt(0)) - 1;
//            double timeStamp = ((boxNum * 4 +
//                    (double) Character.getNumericValue(noteParts[2].charAt(1)) - 1) /
//                    (4 * bpm / 60));
//            noteAl.add(new Note(noteParts[0],
//                    Integer.valueOf(noteParts[1]),
//                    timeStamp,
//                    1,
//                    Double.valueOf(noteParts[3]) / (4 * bpm / 60)));
//        }
//        noteAl.removeOffset(0);
//        return noteAl;
//    }

    /**
     * Syncing the current player's NoteArrayList with the original melody.
     * Shrinking / expanding the array in order to fit the beginning and the end of the
     * original melody for future comparison.
     *
     * @param sampleArray - The original melody array.
     * @param diff        - the current difficulty.
     * @return a synced 2D array of both of the melodies, every cell in the array representing
     * a part of a second (based on the SBPS - see class constants)
     */
    Note[][] syncMusicalParts(NoteArrayList sampleArray, int diff) {

        final double sbps = (diff == KoteGame.EXTREME_DIFFICULTY) ? EXTREME_SBPS : NORMAL_SBPS;

        double sampleLength = sampleArray.getTotalDuration();
        if (this.get(0).equals(sampleArray.get(1))) {
            this.removeOffset(sampleArray.get(0).getDuration());
        } else
            this.removeOffset(0);

        double playerLength = this.getTotalDuration();
        Note[][] syncedArr = new Note[2][(int) Math.round(sampleLength * sbps) + 2];

        //The note index is determined by his relative spot in the musical part
        sampleArray.forEach(note -> {
            int noteIndex = (int) Math.round(note.getTimeStamp() * sbps);
            syncedArr[0][noteIndex] = note;
        });

        syncedArr[1][0] = this.get(0);
        this.forEach(note -> {
            double relPos = note.getTimeStamp() * sbps;
            int noteIndex = (int) Math.round(relPos * sampleLength / playerLength);
            syncedArr[1][noteIndex] = note;
        });

        convertToNoteSequences(syncedArr[1], sbps);

        return syncedArr;
    }

    /**
     * @return The calculated total duration of the array in seconds.
     */
    double getTotalDuration() {
        Note last = this.get(this.size() - 1);
        return last.getTimeStamp() + last.getDuration();
    }

    public String getFileName() {
        return mFileName;
    }

    boolean contains(Note note) {
        return this.stream()
                .anyMatch(n -> n.equals(note));
    }

    /**
     * syncing the timeStamps of the notes by the first note.
     *
     * @param additionalSyncedOffset - if applied, adds an additional offset to the notes, used
     *                               for array that are missing the first note.
     */
    private void removeOffset(double additionalSyncedOffset) {

        double offset = this.get(0).getTimeStamp();
        this.forEach(n -> n.setTimeStamp(n.getTimeStamp() - offset + additionalSyncedOffset));

    }

}
