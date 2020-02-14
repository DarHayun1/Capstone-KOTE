package dar.games.music.capstonekote.ui.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import dar.games.music.capstonekote.repository.GamesHistoryRepository;

public class SignInViewModel extends AndroidViewModel {

    private GamesHistoryRepository mRepo;

    public SignInViewModel(@NonNull Application application) {
        super(application);
        mRepo = GamesHistoryRepository.getInstance(application);
    }

    void setAccount(GoogleSignInAccount account) {
        mRepo.setAccount(account);
    }

    boolean appStarted() {
        return mRepo.appStarted();
    }
}
