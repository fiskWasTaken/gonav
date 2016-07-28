package fiskie.gonav.pokedex;

import android.util.Log;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Builder {
    private InputStream stream;

    public Builder(InputStream stream) {
        this.stream = stream;
    }

    public Pokedex getPokedex() {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Pokedex> jsonAdapter = moshi.adapter(Pokedex.class);

        try {
            this.stream.reset();
            BufferedReader r = new BufferedReader(new InputStreamReader(this.stream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }

            return jsonAdapter.fromJson(total.toString());
        } catch (IOException e) {
            Log.e("gonav", "JSON interpreter failed!");
            return new Pokedex();
        }
    }
}
