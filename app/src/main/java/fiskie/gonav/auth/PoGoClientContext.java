package fiskie.gonav.auth;

import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.auth.GoogleCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import fiskie.gonav.AppSettings;
import okhttp3.OkHttpClient;

public class PoGoClientContext {
    private AppSettings settings;

    public PoGoClientContext(AppSettings settings) {
        this.settings = settings;
    }

    public CredentialProvider getCredentialProvider() throws LoginFailedException, RemoteServerException {
        if (this.settings.getGoogleRefreshToken() != null) {
            return new GoogleCredentialProvider(new OkHttpClient(), this.settings.getGoogleRefreshToken());
        } else if (this.settings.getPTCCredentialsPair() != null) {
            PTCCredentialsPair pair = this.settings.getPTCCredentialsPair();
            return new PtcCredentialProvider(new OkHttpClient(), pair.getUsername(), pair.getPassword());
        }

        return null;
    }
}
