package dar.games.music.capstonekote.gamelogic;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dar.games.music.capstonekote.repository.GameResultModel;


public class KoteGame {

    // Game constants
    public static final int EASY_DIFFICULTY = 0;
    public static final int HARD_DIFFICULTY = 1;
    public static final int EXTREME_DIFFICULTY = 2;

    public static final int GAME_ENDED = 101;

    private static final double RELATIVE_SCORE_FACTOR = 0.85;

    private static final int MAX_UNUSED_SCORE = 75;
    public static final int INVALID_SCORE = 102;
    private static final int ROUND_MAX_LENGTH = 5;

    private int difficulty;
    private ArrayList<NoteArrayList> mMusicalParts;
    //Current melody of the round
    private int round;
    private MutableLiveData<Integer> playsLeft;
    //The current round score
    private int currentScore;
    private int totalScore;


    /**
     * A simple constructor creating a new game with no more than 6 musical parts.
     *
     * @param difficulty        - The game difficulty.
     * @param melodiesTextArray - Strings representing the notes and timing of the melodies - for
     *                          the selected difficulty
     */
    public KoteGame(int difficulty, String[] melodiesTextArray) {
        round = 1;
        playsLeft = new MutableLiveData<>(3);
        totalScore = 0;
        this.difficulty = difficulty;
        generateMusicalParts(melodiesTextArray);

    }

    public static String getDifficultyName(int diff) {
        switch (diff) {
            case EASY_DIFFICULTY:
                return "easy";
            case HARD_DIFFICULTY:
                return "hard";
            default:
                return "extreme";
        }
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
            startDiffScore = (Math.abs(startDiff) < ansNote.getDuration() / 2) ? 100 : 50;
        }
        int endDiffScore = 0;
        if (Math.abs(endDiff) < ansNote.getDuration()) {
            endDiffScore = (Math.abs(endDiff) < ansNote.getDuration() / 2) ? 100 : 50;
        }
        int totalScore = startDiffScore / 2 + endDiffScore / 2;
        if (totalScore > MAX_UNUSED_SCORE)
            plyNote.setUsed();
        return totalScore;
    }

    // ***************
    // Public Methods
    // ***************

    /**
     * Check if there is a note in the player's array that has the same notes before and after
     * like the original note.
     * <p>
     * A test made to handle un-synchronized arrays.
     *
     * @param syncedArray  - The 2D melodies array.
     * @param startIndex   - The original note start index in the array.
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
            } else
                prevNotesCounted = (prevPlayerNotes.second != null) ? 2 : 1;
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
     * Check if the player's recording has too many notes (trying to manipulate
     * the scoring algorithm).
     *
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
     *
     * @param syncedArr - The 2D melodies array.
     * @param noteIndex - the inspected note's index
     * @return the score for the best match from the player's attempt.
     */
    private static int scoreSingleNote(Note[][] syncedArr, int noteIndex) {

        int bestMatch = 0;
        for (int i = 0; i < syncedArr[1].length; i++) {
            if (syncedArr[1][i] != null && !syncedArr[1][i].isUsed()) {
                if (syncedArr[1][i].equals(syncedArr[0][noteIndex])) {
                    int timingScore = checkAbsoluteTiming(syncedArr[0][noteIndex],
                            syncedArr[1][i]);
                    if (timingScore > MAX_UNUSED_SCORE) {
                        return timingScore;
                    } else {
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
     * Moving the game to the next round if available.
     *
     * @return The New round or GAME_ENDED if there are not more rounds available.
     */
    public int nextRound() {
        if (hasNextRound()) {
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

    private void generateMusicalParts(String[] textArray) {
        ArrayList<Integer> ints = new ArrayList<>(
                IntStream.rangeClosed(1, textArray.length)
                        .boxed().collect(Collectors.toList()));
        Random rand = new Random();
        mMusicalParts = new ArrayList<>(ROUND_MAX_LENGTH);
        //Generating ROUND_MAX_LENGTH random numbers representing the melodies' numbers.
        IntStream.range(0, ROUND_MAX_LENGTH)
                .forEach(i -> {
                    int randomMelodyNum = ints.remove(rand.nextInt(ints.size()));
                    mMusicalParts.add(NoteArrayList
                            .textToNoteArrayList(textArray[randomMelodyNum - 1],
                                    randomMelodyNum, difficulty));
                });
    }

    public int getRound() {
        return round;
    }

    /**
     * The main method for the scoring process.
     * Cleaning and validating the array.
     *
     * @param playerArray - The player's record NoteArrayList.
     */
    public void analyzePlayerAttempt(NoteArrayList playerArray) {

        this.checkForRecordBadNotes(playerArray);
        if (playerArray.size() > 0) {
            Note[][] syncedArray = playerArray.syncMusicalParts(mMusicalParts.get(round - 1), difficulty);

            if (checkInvalidAttemptLength(syncedArray)) {
                currentScore = INVALID_SCORE;
                return;
            }

            int tempScore = analyzeOnce(syncedArray);

            currentScore = validateDuplicates(syncedArray, tempScore);
            if (currentScore == INVALID_SCORE)
                return;

            totalScore += currentScore;
        } else
            currentScore = INVALID_SCORE;
    }

    public int getTotalScore() {
        return totalScore;
    }

    // ***************
    // Private methods
    // ***************

    public NoteArrayList getCurrentSample() {
        return mMusicalParts.get(round - 1);
    }

    @NonNull
    @Override
    public String toString() {
        return round + "\n" + totalScore + "\n" + difficulty + "\n" + mMusicalParts.toString();
    }

    public double getSampleLength() {
        return mMusicalParts.get(round - 1).getTotalDuration();
    }

    /**
     * @return A newly created GameResultModel Ready for DB insertion.
     */
    public GameResultModel getGameResult() {
        return new GameResultModel(LocalDateTime.now(), totalScore, difficulty);
    }

    /**
     * @param noteArr       - The Melody Array.
     * @param index         - The relevant note index.
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
     *
     * @param syncedArray - The 2D melodies array
     * @param chkScore    - The original generated score.
     * @return INVALID_SCORE if the record is invalid, otherwise, the calculated score.
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
            return (analyzeOnce(syncedArray) > 50)
                    ? INVALID_SCORE
                    : secondScore;
        }
        return chkScore;
    }

    /**
     * Iterate through the Original notes and score them.
     *
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
     *
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
                if (mMusicalParts.get(round - 1).contains(note) || note.getOctave() < 1 || note.getOctave() > 6)
                    continue;
                playerArray.remove(i);
                i--;

            }
        }
    }


    public boolean hasAudioFile() {
        return mMusicalParts.get(round - 1).getFileName() != null;
    }

    public String getFileName() {
        return mMusicalParts.get(round - 1).getFileName();
    }
}
