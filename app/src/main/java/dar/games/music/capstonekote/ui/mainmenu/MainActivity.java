package dar.games.music.capstonekote.ui.mainmenu;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import dar.games.music.capstonekote.R;

/**
 * Main Activity for the main screen of the app, containing the main menu.
 */
public class MainActivity extends AppCompatActivity {

    public static final String DIFFICULTY_EXTRA = "difficulty_extra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                signOut();
                return true;
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Disconnecting from Google Play Games.
     */
    private void signOut() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_SIGN_IN);
        //Replacing the account information with null
        new ViewModelProvider(this).get(MainMenuViewModel.class).setAccount(null);
        signInClient.signOut().addOnCompleteListener(this,
                task -> Toast.makeText(this, getString(R.string.signed_out_message),
                        Toast.LENGTH_LONG).show());
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}