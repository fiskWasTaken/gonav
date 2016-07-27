package fiskie.gonav.scanner;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.GoogleAuthJson;
import com.pokegoapi.auth.GoogleAuthTokenJson;
import com.pokegoapi.auth.GoogleCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import fiskie.gonav.daemon.ICallback;
import okhttp3.OkHttpClient;

/**
 * Created by fiskie on 26/07/2016.
 */
public class PoGoClientManager extends Handler implements GoogleCredentialProvider.OnGoogleLoginOAuthCompleteListener {
    PokemonGo go;
    OkHttpClient httpClient;
    String refreshToken;
    GoogleCredentialProvider.OnGoogleLoginOAuthCompleteListener listener;

    public PoGoClientManager(Looper looper, GoogleCredentialProvider.OnGoogleLoginOAuthCompleteListener listener) {
        super(looper);
        this.httpClient = new OkHttpClient();
        this.listener = listener;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void getClient(ICallback<PokemonGo> callback) {
        if (this.go != null) {
            callback.callback(this.go);
            return;
        }

        try {
            if (this.refreshToken != null) {
                // Reconnect using refresh token
                try {
                    this.go = new PokemonGo(new GoogleCredentialProvider(httpClient, refreshToken), httpClient);
                } catch (LoginFailedException e) {
                    // login failed -- token is probably dead
                    refreshToken = null;
                    Log.e("gonavd", "Looks like the token has expired.");
                    this.getClient(callback);
                }
            } else {
                try {
                    this.go = new PokemonGo(new GoogleCredentialProvider(httpClient, this), httpClient);
                } catch (LoginFailedException e) {
                    // idk todo
                }
            }

            callback.callback(this.go);
        } catch (RemoteServerException e) {
            Log.e("gonavd", "Could not complete Google auth");
            Log.e("gonavd", e.toString());
        }
    }

    @Override
    public void onInitialOAuthComplete(GoogleAuthJson googleAuthJson) {
        this.listener.onInitialOAuthComplete(googleAuthJson);
    }

    @Override
    public void onTokenIdReceived(GoogleAuthTokenJson googleAuthTokenJson) {
        refreshToken = googleAuthTokenJson.getRefreshToken();
        this.listener.onTokenIdReceived(googleAuthTokenJson);
    }
}
