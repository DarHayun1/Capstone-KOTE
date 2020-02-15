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

    // Game constants
    public static final int EASY_DIFFICULTY = 0;
    public static final int HARD_DIFFICULTY = 1;
    public static final int EXTREME_DIFFICULTY = 2;

    public static final int GAME_ENDED = -1;

    private static final double RELATIVE_SCORE_FACTOR = 0.9;

    private static final int MAX_UNUSED_SCORE = 75;

    private int difficulty;
    private List<Pair<String, Integer>> mMusicalParts;
    //Current melody of the round
    private NoteArrayList currentSample;
    private int round;
    private MutableLiveData<Integer> playsLeft;
    //The current round score
    private int currentScore;
    private int totalScore;


    /**
     * A simple constructor creating a new game with no more than 6 musical parts.
     * @param difficulty - The game difficulty.
     * @param musicalPartsPairs - The musical parts available for the game.
     */
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

    /**
     * Moving the game to the next round if available.
     * @return The New round or GAME_ENDED if there are not more rounds available.
     */
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

    public void addSamplePlay() {
        playsLeft.setValue(playsLeft.getValue() - 1);
    }

    /**
     * The main method for the scoring process.
     * Cleaning and validating the array.
     * @param playerArray - The player's record NoteArrayList.
     */
    public void analyzePlayerAttempt(NoteArrayList playerArray) {

        this.checkForRecordBadNotes(playerArray);
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

    /**
     * Check if the player's recording has too many notes (trying to manipulate
     * the scoring algorithm).
     * @param syncedArray - The 2d melodies array.
     * @return true if the attempt length is invalid
     */
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

    /**
     * evaluate a score for a single note in the original melody,
     * @param syncedArr - The 2D melodies array.
     * @param noteIndex - the inspected note's index
     * @return the score for the best match from the player's attempt.
     */
    private static int scoreSingleNote(Note[][] syncedArr, int noteIndex) {

        int bestMatch = 0;
        for (int i = 0; i < syncedArr[1].length; i++)
        {
            if (syncedArr[1][i] != null && !syncedArr[1][i].isUsed())
            {
                if (syncedArr[1][i].equals(syncedArr[0][noteIndex]))
                {
                    int timingScore = checkAbsoluteTiming(syncedArr[0][noteIndex],
                            syncedArr[1][i]);
                    if (timingScore > MAX_UNUSED_SCORE)
                    {
                        return timingScore;
                    }
                    else
                    {
                        int relativeScore = (int) (RELATIVE_SCORE_FACTOR *
                                checkRelativePositioning(syncedArr, noteIndex, i));
                        bestMatch = Math.max(bestMatch, Math.max(timingScore, relativeScore));
                    }
                }
            }
        }
        return bestMatch;
    }

    /**
     * @param ansNote - The original melody note.
     * @param plyNote - The player's note that being tested.
     * @return Score based on how close the player's note to the original note in the meaning
     * of timing.
     */
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
        if (totalScore > MAX_UNUSED_SCORE)
            plyNote.setUsed();
        return totalScore;
    }


    /**
     * Check if there is a note in the player's array that has the same notes before and after
     * like the original note.
     *
     * A test made to handle un-synchronized arrays.
     * @param syncedArray - The 2D melodies array.
     * @param startIndex - The original note start index in the array.
     * @param checkedIndex - The player's current checked note.
     * @return estimated score.
     */
    private static int checkRelativePositioning(Note[][] syncedArray,
                                                int startIndex, int checkedIndex) {
        Pair<Note, Note> prevAnsNotes = getNearbyNotes(syncedArray[0],
                startIndex, true);
        Pair<Note, Note> nextAnsNotes = getNearbyNotes(syncedArray[0],
                startIndex, false);
        Pair<Note, Note> prevPlayerNotes = getNearbyNotes(syncedArray[1],
                checkedIndex, true);
        Pair<Note, Note> nextPlayerNotes = getNearbyNotes(syncedArray[1],
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
        Integer nextScore = 0;
        Integer nextNotesCounted = 0;
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
                syncedArray[1][checkedIndex].setUsed();
            return score;
        }

    }

    /**
     * @param noteArr - The Melody Array.
     * @param index - The relevant note index.
     * @param backwardCheck - if true, looking for the previous notes.
     * @return A Pair containing the two nearby notes from the selected direction
     */
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

    //Checks to see if the player's notes still getting a good score after the notes being marked "used"

    /**
     * Checks to see if the player's notes still getting a good score after the notes being
     * marked "used".
     * @param syncedArray - The 2D melodies array
     * @param chkScore - The original generated score.
     * @return -1 if the record is invalid, otherwise, the calculated score.
     */
    private int validateDuplicates(Note[][] syncedArray, int chkScore) {

        int secondScore = analyzeOnce(syncedArray);
        //If the score after another check is higher it means something in the array interrupted
        // the first test. getting the higher score
        if (secondScore > chkScore) {
            return validateDuplicates(syncedArray, secondScore);
        }
        //If there is another good score, take the lower score.
        if (chkScore > 70 && secondScore > 50) {
            //If there is a third good score it is an invalid record
            if (analyzeOnce(syncedArray) > 50) {
                return -1;
            }
            return secondScore;
        }
        return chkScore;
    }

    /**
     * Iterate through the Original notes and score them.
     * @param syncedArray - The 2D melodies array
     * @return The attempt score.
     */
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

    /**
     * Looking for suspicious short notes that are not in the original array.
     * @param playerArray - the player's attempt.
     */
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

    public MutableLiveData<Integer> samplePlayesLeft() {
        return playsLeft;
    }

    /*** Getters ***/

    public int getDifficulty() {
        return difficulty;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public NoteArrayList getCurrentSample() {
        return currentSample;
    }

    public int getRound() {
        return round;
    }

    public double getSampleLength() {
        return currentSample.getTotalDuration();
    }

    public int getTotalScore() {
        return totalScore;
    }

    /**
     * @return A newly created GameResultModel Ready for DB insertion.
     */
    public GameResultModel getGameResult() {
        return new GameResultModel(new Date(), totalScore, difficulty);
    }

    @NonNull
    @Override
    public String toString() {
        return round + "\n" + totalScore + "\n" + difficulty + "\n" + mMusicalParts.toString();
    }



}
