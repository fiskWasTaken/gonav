package fiskie.gonav.auth;

import android.util.Log;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import okhttp3.OkHttpClient;

public class CredentialValidator {
    private CredentialProvider provider;

    public CredentialValidator(CredentialProvider provider) {
        this.provider = provider;
    }

    public void test() throws LoginFailedException, RemoteServerException {
        Log.d("gonav", "Token ID for credentials is " + provider.getTokenId());

        OkHttpClient httpClient = new OkHttpClient();
        PokemonGo pokemonGo = new PokemonGo(provider, httpClient);

        Log.d("gonav", "PoGo username: " + pokemonGo.getPlayerProfile().getUsername());
        Log.d("gonav", "PoGo creation time: " + pokemonGo.getPlayerProfile().getCreationTime());
    }
}
