package com.example.myapplication;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.dto.pokemonDTO.Pokemon;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private List<Pokemon> pokedex = new ArrayList<>();
    private static Bitmap customPokeImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Spinner spinnerPokemonList = findViewById(R.id.spinner);

        new DownloadPokemonTask(spinnerPokemonList).execute();

        configSpinnerListeners();
        configImageViewListeners();
        setBackgroundGradient(BitmapFactory.decodeResource(getResources(), R.drawable.pokedex));
    }

    private void configSpinnerListeners() {

        Spinner spinnerPokemonList = findViewById(R.id.spinner);
        ImageView imageViewPokemon = findViewById(R.id.imageViewPokemon);
        ImageView backgroundImageView = findViewById(R.id.background);
        ProgressBar baseHappinessProgress = findViewById(R.id.baseHappinessProgress);
        ProgressBar captureRateProgress = findViewById(R.id.captureRateProgress);
        TextView pokeNameTV = findViewById(R.id.pokeName);

        spinnerPokemonList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Pokemon pokemon = pokedex.get(position);
                new DownloadPokemonImageTask(imageViewPokemon, backgroundImageView).execute(pokemon.getImg());
                new DownloadPokemonInfoTask(pokemon, baseHappinessProgress, captureRateProgress, pokeNameTV).execute(pokemon.getPokemonSpeciesUrl());
                updateInfoView(pokemon);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    private void updateInfoView(Pokemon pokemon) {
        TextView pokeNumber = findViewById(R.id.datoNro);;
        if (Objects.nonNull(pokeNumber)){
            pokeNumber.setText(pokemon.getNumber());
        }
    }

    private void configImageViewListeners(){
        ImageView imageViewPokemon = findViewById(R.id.imageViewPokemon);
        imageViewPokemon.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> setBackgroundGradient(customPokeImageBitmap));

        imageViewPokemon.setOnClickListener(view -> setBackgroundGradient(customPokeImageBitmap));
    }

    private void setBackgroundGradient(Bitmap imageBitmap) {
        if (imageBitmap != null) {
            // ** gradient **
            ImageView backgroundImageView = findViewById(R.id.background);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;

            Bitmap backgroundDominantColorBitmap = PaletteUtils.getDominantGradient(imageBitmap, height, width);
            backgroundImageView.setImageBitmap(backgroundDominantColorBitmap);
            // ** gradient **
        }
    }


    private static class DownloadPokemonImageTask extends AsyncTask<String, Void, Bitmap> {
        @SuppressLint("StaticFieldLeak")
        ImageView bmImage;

        @SuppressLint("StaticFieldLeak")
        ImageView backgroundImageView;

        public DownloadPokemonImageTask(ImageView bmImage, ImageView backgroundImageView) {
            this.bmImage = bmImage;
            this.backgroundImageView = backgroundImageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            customPokeImageBitmap = result;
            bmImage.callOnClick();
        }
    }

    private static class DownloadPokemonInfoTask extends AsyncTask<String, Void, JSONObject> {

        private final Pokemon pokemon;

        @SuppressLint("StaticFieldLeak")
        ProgressBar baseHappinessProgress;
        @SuppressLint("StaticFieldLeak")
        ProgressBar captureRateProgress;
        @SuppressLint("StaticFieldLeak")
        TextView pokeName;

        public DownloadPokemonInfoTask(Pokemon pokemon, ProgressBar baseHappinessProgress, ProgressBar captureRateProgress, TextView pokeName) {
            this.pokemon = pokemon;
            this.pokeName = pokeName;
            this.baseHappinessProgress = baseHappinessProgress;
            this.captureRateProgress = captureRateProgress;
        }

        protected JSONObject doInBackground(String... urls) {
            URLConnection urlConn;
            BufferedReader bufferedReader = null;
            try {
                URL url = new URL(pokemon.getPokemonSpeciesUrl());
                urlConn = url.openConnection();
                // Log.d("POKEMON API", "[DownloadPokemonInfoTask] connection opened");
                bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                // Log.d("POKEMON API", "[DownloadPokemonInfoTask] bufferedReader created");

                StringBuilder stringBuffer = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }

                // Log.d("POKEMON API", "[DownloadPokemonInfoTask] stringBuffer appended");

                return new JSONObject(stringBuffer.toString());

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return new JSONObject();
        }

        protected void onPostExecute(JSONObject result) {
            if(result != null) {
                try {
                    pokeName.setText(pokemon.getName().toUpperCase(Locale.ROOT));
                    captureRateProgress.setProgress(nullCheck(result.get("capture_rate")));
                    baseHappinessProgress.setProgress(nullCheck(result.get("base_happiness")));
                } catch (Exception ex) {
                    Log.w(" [DownloadPokemonInfoTask] onPostExecute", "Error", ex);
                }
            }
        }

        private int nullCheck(Object value) {
            try {
                return Objects.nonNull(value) ? (int) value : 0;
            } catch (Exception e) {
                return "null".equals(value)? (int) value : 0;
            }
        }
    }

    class DownloadPokemonTask extends AsyncTask<Void, Void, JSONObject> {

        ArrayList<String> pokemons = new ArrayList<>();
        @SuppressLint("StaticFieldLeak")
        Spinner spinner;

        public DownloadPokemonTask(Spinner spinner) {
            this.spinner = spinner;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            String pokeListURL = "https://pokeapi.co/api/v2/pokemon/?offset=0&limit=905";
            Log.i("POKEMON API", "[POKE API] downloading pokemons list");

            URLConnection urlConn;
            BufferedReader bufferedReader = null;
            try {
                // Log.d("POKEMON API", "[DownloadPokemonTask] getting url");
                URL url = new URL(pokeListURL);
                urlConn = url.openConnection();
                // Log.d("POKEMON API", "[DownloadPokemonTask] connection opened");
                bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                // Log.d("POKEMON API", "[DownloadPokemonTask] bufferedReader created");

                StringBuilder stringBuffer = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
                return new JSONObject(stringBuffer.toString());
            } catch (Exception ex) {
                Log.e("App", "Error downloading pokemons list", ex);
                return null;
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e("App", "Error closing buffered reader", e);
                    }
                }
            }
        }


        @Override
        protected void onPostExecute(JSONObject response) {
            ArrayList<String> pokemons = new ArrayList<>();
            if(response != null) {
                try {
                    JSONArray results = response.getJSONArray("results");
                    Log.i("POKEMON API", "[POKE API] Pokemon list length " + results.length());
                    for (int i = 0 ; i < results.length() ; i ++ ) {
                        Log.d("POKEMON API", "[POKE API] onPostExecute getting pokemon");
                        String pokeName = results.getJSONObject(i).get("name").toString();
                        String baseExperience = results.getJSONObject(i).get("name").toString();
                        Pokemon pokemon = new Pokemon(pokeName, i + 1, baseExperience);
                        pokedex.add(pokemon);
                        pokemons.add(pokeName);
                    }
                } catch (Exception ex) {
                    Log.w("[DownloadPokemonTask] onPostExecute", "Error getting pokemon", ex);
                }
            }
            initSpinnerStatus(pokemons);
        }

        private void initSpinnerStatus(List<String> pokemons) {
            Spinner spinner = findViewById(R.id.spinner);
            Log.i("POKEMON API", "[POKE API] pokemon size: " + pokemons.size());
            String[] pokeArr = new String[pokemons.size()];
            pokeArr = pokemons.toArray(pokeArr);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_list, pokeArr);
            spinner.setAdapter(adapter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}