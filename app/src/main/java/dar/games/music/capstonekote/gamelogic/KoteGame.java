package dar.games.music.capstonekote.gamelogic;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import dar.games.music.capstonekote.repository.GameResultModel;

public class KoteGame {


    public static final int EASY_DIFFICULTY = 0;
    public static final int HARD_DIFFICULTY = 1;
    public static final int EXTREME_DIFFICULTY = 2;
    public static final int GAME_ENDED = -1;

    private final static double RELATIVE_SCORE_FACTOR = 0.9;

    private NoteArrayList currentSample;
    private int round;
    private List<Pair<String, Integer>> mMusicalParts;
    private int totalScore;
    private MutableLiveData<Integer> playsLeft;
    private int currentScore;
    private int difficulty;

    public KoteGame(int difficulty, ArrayList<Pair<String, Integer>> musicalPartsPairs) {
        round = 1;
        playsLeft = new MutableLiveData<>(3);
        totalScore = 0;
        this.difficulty = difficulty;
        Collections.shuffle(musicalPartsPairs);
        if (musicalPartsPairs.size() > 6)
            mMusicalParts = musicalPartsPairs.subList(0, 5);
        else
            mMusicalParts = musicalPartsPairs;
        currentSample = NoteArrayList.samplePairToNoteArray(mMusicalParts.get(0), this.difficulty);
    }

    private static boolean checkInvalidAttemptLength(Note[][] syncedArray) {
        int numOfAnsNotes = 0;
        int numOfPlyNotes = 0;
        for (int i = 0; i < syncedArray[1].length; i++) {
            if (syncedArray[0][i] != null)
                numOfAnsNotes++;
            if (syncedArray[1][i] != null)
                numOfPlyNotes++;
        }
        return numOfPlyNotes > numOfAnsNotes * 5;


    }

    //score a single note between 1(not even close) to 10(exact same note)
    //check what if there is at least 2 consecutive notes like the answer note (or near
    // it - which will give a lower score)
    private static int scoreSingleNote(Note[][] syncedArr, int startIndex) {

        int bestMatch = 0;
        for (int i = 0; i < syncedArr[1].length; i++) {
            if (syncedArr[1][i] != null && !syncedArr[1][i].isUsed()) {
                if (syncedArr[1][i].equals(syncedArr[0][startIndex])) {
                    int timingScore = checkAbsoluteTiming(syncedArr[0][startIndex],
                            syncedArr[1][i]);
                    int relativeScore = (int) (RELATIVE_SCORE_FACTOR *
                            checkRelativePositioning(syncedArr, startIndex, i));
                    bestMatch = Math.max(bestMatch, Math.max(timingScore, relativeScore));
                }
            }
        }
        return bestMatch;
    }

    private static int checkRelativePositioning(Note[][] syncedArr,
                                                int startIndex, int checkedIndex) {
        Pair<Note, Note> prevAnsNotes = getNearbyNotes(syncedArr[0],
                startIndex, true);
        Pair<Note, Note> nextAnsNotes = getNearbyNotes(syncedArr[0],
                startIndex, false);
        Pair<Note, Note> prevPlayerNotes = getNearbyNotes(syncedArr[1],
                checkedIndex, true);
        Pair<Note, Note> nextPlayerNotes = getNearbyNotes(syncedArr[1],
                checkedIndex, false);
        int prevScore = 0;
        int prevNotesCounted = 0;
        if (Note.nullableEquals(prevAnsNotes.first, prevPlayerNotes.first)) {
            if (Note.nullableEquals(prevAnsNotes.second, prevPlayerNotes.second)) {
                prevScore = 100;
                prevNotesCounted = 2;
            } else if (prevPlayerNotes.second != null) {
                prevScore = 50;
                prevNotesCounted = 2;
            } else {
                prevScore = 100;
                prevNotesCounted = 1;
            }
        } else if (prevPlayerNotes.first != null) {
            if (Note.nullableEquals(prevPlayerNotes.second, prevAnsNotes.second)) {
                prevScore = 50;
                prevNotesCounted = 2;
            } else if (prevPlayerNotes.second != null) {
                prevNotesCounted = 2;
            } else prevNotesCounted = 1;
        }
        int nextScore = 0;
        int nextNotesCounted = 0;
        if (Note.nullableEquals(nextAnsNotes.first, nextPlayerNotes.first)) {
            if (Note.nullableEquals(nextAnsNotes.second, nextPlayerNotes.second)) {
                nextScore = 100;
                nextNotesCounted = 2;
            } else if (nextPlayerNotes.second != null) {
                nextScore = 50;
                nextNotesCounted = 2;
            } else {
                nextScore = 100;
                nextNotesCounted = 1;
            }
        } else if (nextPlayerNotes.first != null) {
            if (Note.nullableEquals(nextPlayerNotes.second, nextPlayerNotes.second)) {
                nextScore = 50;
                nextNotesCounted = 2;
            } else if (nextPlayerNotes.second != null) {
                nextNotesCounted = 2;
            } else nextNotesCounted = 1;
        }

        if (prevNotesCounted + nextNotesCounted == 0)
            return 0;
        else {
            int score = Math.round((prevScore * prevNotesCounted + nextScore * nextNotesCounted) /
                    (prevNotesCounted + nextNotesCounted));
            if (score >= 75)
                syncedArr[1][checkedIndex].setUsed();
            return score;
        }

    }

    private static Pair<Note, Note> getNearbyNotes(Note[] noteArr, int index, boolean backwardCheck) {
        Note first = null;
        Note second = null;
        if (backwardCheck) {
            for (int i = index - 1; i >= 0; i--) {
                if (noteArr[i] != null) {
                    if (first == null)
                        first = noteArr[i];
                    else {
                        second = noteArr[i];
                        break;
                    }
                }
            }
        } else {
            for (int i = index + 1; i < noteArr.length; i++) {
                if (noteArr[i] != null) {
                    if (first == null)
                        first = noteArr[i];
                    else {
                        second = noteArr[i];
                        break;
                    }
                }
            }
        }
        return new Pair<>(first, second);
    }

    private static int checkAbsoluteTiming(Note ansNote, Note plyNote) {
        double startTime = ansNote.getTimeStamp();
        double startDiff = plyNote.getTimeStamp() - startTime;
        double endDiff = startDiff + (plyNote.getDuration() - ansNote.getDuration());
        int startDiffScore = 0;
        if (Math.abs(startDiff) < ansNote.getDuration()) {
            if (Math.abs(startDiff) < ansNote.getDuration() / 2)
                startDiffScore = 100;
            else
                startDiffScore = 50;
        }
        int endDiffScore = 0;
        if (Math.abs(endDiff) < ansNote.getDuration()) {
            if (Math.abs(endDiff) < ansNote.getDuration() / 2)
                endDiffScore = 100;
            else
                endDiffScore = 50;
        }
        int totalScore = startDiffScore / 2 + endDiffScore / 2;
        if (totalScore >= 75)
            plyNote.setUsed();
        return totalScore;
    }

    public int getDifficulty() {
        return this.difficulty;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public NoteArrayList getCurrentSample() {
        return currentSample;
    }

    public void analyzePlayerAttempt(NoteArrayList playerArray) {

        checkForRecordBadNotes(playerArray);
        if (playerArray.size() > 0) {
            Note[][] syncedArray = playerArray.syncMusicalParts(currentSample, difficulty);
            if (checkInvalidAttemptLength(syncedArray)) {
                currentScore = -1;
                return;
            }

            int tempScore = analyzeOnce(syncedArray);
            currentScore = validateDuplicates(syncedArray, tempScore);
            if (currentScore == -1)
                return;

            totalScore += currentScore;
        } else
            currentScore = -1;
    }

    //Checks to see if the player's notes still getting a good score after the notes being marked "used"
    private int validateDuplicates(Note[][] syncedArray, int chkScore) {

        int secondScore = analyzeOnce(syncedArray);
        //If the score after another check is higher it means something in the array interrupted
        // the first test. getting the higher score
        if (secondScore > chkScore) {
            return validateDuplicates(syncedArray, secondScore);
        }
        //If there is another good score
        if (chkScore > 70 && secondScore > 50) {
            //If there is a third good score it is an invalid record
            if (analyzeOnce(syncedArray) > 50) {
                return -1;
            }
            return secondScore;
        }
        return chkScore;
    }

    private int analyzeOnce(Note[][] syncedArray) {
        int resultSum = 0;
        int notesCounted = 0;
        for (int i = 0; i < syncedArray[0].length; i++) {
            if (syncedArray[0][i] != null) {
                resultSum += scoreSingleNote(syncedArray, i);
                notesCounted++;
            }
        }

        return resultSum / notesCounted;
    }

    private void checkForRecordBadNotes(NoteArrayList playerArray) {

        for (int i = 0; i < playerArray.size(); i++) {
            Note note = playerArray.get(i);
            if (note != null) {
                //Checks to see if the note is in a sequence, if it is - continue to next note.
                if ((i != 0 && playerArray.get(i - 1).equals(note))
                        || i != playerArray.size() - 1 && playerArray.get(i + 1).equals(note)) {
                    continue;
                }
                //Checks if the note is in the current sample, if it is - continue to next note.
                if (currentSample.contains(note) || note.getOctave() < 1 || note.getOctave() > 6)
                    continue;
                playerArray.remove(i);
                i--;

            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return round + "\n" + totalScore + "\n" + difficulty + "\n" + mMusicalParts.toString();
    }

    public int getRound() {
        return round;
    }

    public int nextRound() {
        if (hasNextRound()) {
            currentSample = NoteArrayList.samplePairToNoteArray(mMusicalParts.get(round), difficulty);
            round++;
            playsLeft.setValue(3);
            return round;
        } else {
            return GAME_ENDED;
        }
    }

    public boolean hasNextRound() {
        return round < mMusicalParts.size();
    }

    public int getTotalScore() {
        return totalScore;
    }

    public GameResultModel getGameResult() {
        return new GameResultModel(new Date(), totalScore, difficulty);
    }

    public MutableLiveData<Integer> samplePlayesLeft() {
        return playsLeft;
    }

    public void addSamplePlay() {
        playsLeft.setValue(playsLeft.getValue() - 1);
    }

    public double getSampleLength() {
        return currentSample.getTotalDuration();
    }
}
