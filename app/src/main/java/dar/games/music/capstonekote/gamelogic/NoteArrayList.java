package dar.games.music.capstonekote.gamelogic;

import android.util.Pair;

import java.util.ArrayList;

public class NoteArrayList extends ArrayList<Note> {

    static final double FRAME_INTERVAL = 0.0464;

    private static final double NORMAL_BPM = 90;
    private static final double EXTREME_BPM = 105;
    private static final double NORMAL_SBPS = 2 * 4 * NORMAL_BPM / 60;
    private static final double EXTREME_SBPS = 2 * 4 * EXTREME_BPM / 60;
    public static final int PLAYER_ID = -1;
    private static final String TAG = NoteArrayList.class.getSimpleName();
    private int sampleId;

    public NoteArrayList(int initialCapacity, int id) {
        super(initialCapacity);
        sampleId = id;
    }

    static NoteArrayList samplePairToNoteArray(Pair<String, Integer> samplePair, int diff) {

        double bpm = NORMAL_BPM;
        if (diff == KoteGame.EXTREME_DIFFICULTY) {
            bpm = EXTREME_BPM;
        }

        String[] tempArr = samplePair.first.split(",", 0);
        NoteArrayList noteAl = new NoteArrayList(500, samplePair.second);
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

    //Find note sequences, delete the spare notes,
    //change the first note duration to the total duration of the sequence
    //Removes notes that seems like record mistake (very short and not in the answer array or surrounded by the same note)
    //TODO: Reverse the loop, this way m
    private static void convertToNoteSequences(Note[][] syncedArr, double sbps) {
        Note[] plyNotes = syncedArr[1];
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

    private void removeOffset(double additionalSyncedOffset) {

        double offset = this.get(0).getTimeStamp();
        for (Note n : this) {
            n.setTimeStamp(n.getTimeStamp() - offset + additionalSyncedOffset);
        }
    }

    Note[][] syncMusicalParts(NoteArrayList sampleArray, int diff) {
        double sbps = NORMAL_SBPS;
        if (diff == KoteGame.EXTREME_DIFFICULTY) {
            sbps = EXTREME_SBPS;
        }

        double sampleLength = sampleArray.getTotalDuration();
        if (this.get(0).equals(sampleArray.get(1))) {
            this.removeOffset(sampleArray.get(0).getDuration());
        } else
            this.removeOffset(0);

        double playerLength = this.getTotalDuration();
        Note[][] syncedArr = new Note[2][(int) Math.round(sampleLength * sbps) + 2];

        for (int i = 0; i < sampleArray.size(); i++) {
            //The note index is determined by his relative spot in the musical part
            int noteIndex = (int) Math.round(sampleArray.get(i).getTimeStamp() * sbps);
            syncedArr[0][noteIndex] = sampleArray.get(i);
        }
        syncedArr[1][0] = this.get(0);
        for (int i = 1; i < this.size(); i++) {
            double relPos = this.get(i).getTimeStamp() * sbps;
            int noteIndex = (int) Math.round(relPos * sampleLength / playerLength);
            syncedArr[1][noteIndex] = this.get(i);
        }

        convertToNoteSequences(syncedArr, sbps);

        return syncedArr;
    }

    double getTotalDuration() {
        Note last = this.get(this.size() - 1);
        return last.getTimeStamp() + last.getDuration();
    }


    public int getSampleId() {
        return sampleId;
    }

    boolean contains(Note note) {
        for (Note n : this) {
            if (note.equals(n))
                return true;
        }
        return false;
    }
}
