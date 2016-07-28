package fiskie.gonav.service;

import android.os.AsyncTask;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import fiskie.gonav.auth.PoGoClientContext;
import okhttp3.OkHttpClient;

public class ClientManager {
    PokemonGo pokemonGo;
    PoGoClientContext context;

    public ClientManager(PoGoClientContext context) {
        this.context = context;
    }

    public void prepare(final ClientManagerCallback callback) {
        new AsyncTask<PoGoClientContext, Void, PokemonGo>() {
            @Override
            protected PokemonGo doInBackground(PoGoClientContext... poGoClientBundles) {
                OkHttpClient httpClient = new OkHttpClient();

                PokemonGo pokemonGo = null;

                int maxAttempts = 10;

                for (int i = 1; i <= maxAttempts; i++) {
                    callback.onStatusChange(EServiceState.CONNECTING, String.format("Authenticating (attempt %d of %d).", i, maxAttempts));

                    try {
                        CredentialProvider provider = poGoClientBundles[0].getCredentialProvider();

                        if (provider == null) {
                            callback.onStatusChange(EServiceState.NO_PROVIDER, null);
                        } else {
                            pokemonGo = new PokemonGo(provider, httpClient);
                            callback.onStatusChange(EServiceState.AVAILABLE, null);
                        }
                        break;
                    } catch (LoginFailedException e) {
                        callback.onStatusChange(EServiceState.AUTH_FAILURE_LOGIN_FAILED, "Login failed: " + e.getMessage());
                        // break out, because this is probably an authentication issue and retrying isn't going to solve it
                        break;
                    } catch (RemoteServerException e) {
                        callback.onStatusChange(EServiceState.REMOTE_NETWORK_FAILURE, "Network failure: " + e.getMessage());

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    } catch (RuntimeException e) {
                        callback.onStatusChange(EServiceState.AUTH_FAILURE_UNKNOWN, "Runtime exception: " + e.getMessage());

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }

                return pokemonGo;
            }

            @Override
            protected void onPostExecute(PokemonGo pokemonGo) {
                if (pokemonGo == null) {
                    callback.onFailure();
                } else {
                    callback.onSuccess(pokemonGo);
                }
            }
        }.execute(context);
    }

    public interface ClientManagerCallback {
        void onStatusChange(EServiceState serviceState, String overrideMessage);

        void onSuccess(PokemonGo pokemonGo);

        void onFailure();
    }
}
