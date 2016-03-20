package com.juniorcarvalho.tempoagora;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


public class WeatherFragment extends Fragment {
    Typeface weatherFont;

    TextView cityField;
    TextView updatedField;
    TextView detailsField;
  // TextView detalhesVendoNuvens;
    TextView currentTemperatureField;
    TextView weatherIcon;
    TextView tempmin;
    TextView tempmax;
    TextView vento;
    TextView nuvem;


    Handler handler;

    public WeatherFragment() {
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        cityField = (TextView) rootView.findViewById(R.id.city_field);
        updatedField = (TextView) rootView.findViewById(R.id.updated_field);
        detailsField = (TextView) rootView.findViewById(R.id.details_field);

     //  detalhesVendoNuvens = (TextView) rootView.findViewById(R.id.detalhesVentoNuvens);

        currentTemperatureField = (TextView) rootView.findViewById(R.id.current_temperature_field);
        weatherIcon = (TextView) rootView.findViewById(R.id.weather_icon);

        tempmin = (TextView) rootView.findViewById(R.id.tempmin);
        tempmax = (TextView) rootView.findViewById(R.id.tempmax);
        vento = (TextView) rootView.findViewById(R.id.vento);
        nuvem = (TextView) rootView.findViewById(R.id.nuvens);


        weatherIcon.setTypeface(weatherFont);
        return rootView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "weather.ttf");
        updateWeatherData(new CityPreference(getActivity()).getCity());
    }


    private void updateWeatherData(final String city) {
        new Thread() {
            public void run() {
                final JSONObject json = RemoteFetch.getJSON(getActivity(), city);
                if (json == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }

    private void renderWeather(JSONObject json) {
        try {
            //cityField.setText(json.getString("name").toUpperCase(Locale.US) +
            cityField.setText(json.getString("name").toUpperCase(Locale.getDefault()) +
                              ", " +
                              json.getJSONObject("sys").getString("country"));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");

            JSONObject vento = json.getJSONObject("wind");
            JSONObject nuvens = json.getJSONObject("clouds");
            //
      //          detalhesVendoNuvens.setText(json.getJSONObject("rain").getString("3h"));

            detailsField.setText(
                    String.format("%s\nVento: %s Km/H %sº\nNuven: %s ", details.getString("description").toUpperCase(Locale.US), vento.getString("speed"), vento.getString("deg"), nuvens.getString("all"))
                                     );

            currentTemperatureField.setText(String.format("%.2f", main.getDouble("temp")) + " ℃");
         /*   detailsField.setText(
                    details.getString("description").toUpperCase(Locale.US) +
                            "\n" + "Umidade : " + main.getString("humidity") + "%" +
                            "\n" + "Pressão  : " + main.getString("pressure") + " hPa" +
                            "\n" + "Mínima   : " + main.getString("temp_min") + " ℃" +
                            "\n" + "Máxima  : " + main.getString("temp_max") + " ℃" +
                            "\n" + "Vento      : " + vento.getString("speed") + " Km/H " + vento.getDouble("deg") + "º" +
                            "\n" + "Nuven     : " + nuvens.getString("all") + " %"
            );*/


 /*              detailsField.setText(
                       String.format("%s\nUmidade : %s%%\nPressão  : %s hPa\nMínima   : %s ℃\nMáxima  : %s ℃\nVento      : %s Km/H %sº\nNuven     : %s %%", details.getString("description").toUpperCase(Locale.US), main.getString("humidity"), main.getString("pressure"), main.getString("temp_min"), main.getString("temp_max"), vento.getString("speed"), vento.getDouble("deg"), nuvens.getString("all"))
               );
            */
            detailsField.setText(
                    new StringBuilder().append(details.getString("description").toUpperCase(Locale.US)).
                            append("\n").
                            append("Umidade : ").append(main.getString("humidity")).append("%").
                            append("\n").
                            append("Pressão  : ").append(main.getString("pressure")).append(" hPa").
                            append("\n").
                            append("Mínima   : ").append(main.getString("temp_min")).append(" ℃").
                            append("\n").
                            append("Máxima  : ").append(main.getString("temp_max")).append(" ℃").
                            append("\n").
                            append("Vento      : ").append(vento.getString("speed")).append(" Km/H ").
                            append(vento.getDouble("deg")).append("º").
                            append("\n").
                            append("Nuven     : ").
                            append(nuvens.getString("all")).append(" %").toString()
            );





            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt") * 1000));
            updatedField.setText("Última atualização: " + updatedOn); //Last Update

            setWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);

        } catch (Exception e) {
            Log.e("Tempo Agora", "Um ou mais campos não encontrados nos dados JSON!");
        }
    }

    private void setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                icon = getActivity().getString(R.string.weather_sunny);
            } else {
                icon = getActivity().getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2:
                    icon = getActivity().getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = getActivity().getString(R.string.weather_drizzle);
                    break;
                case 7:
                    icon = getActivity().getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = getActivity().getString(R.string.weather_cloudy);
                    break;
                case 6:
                    icon = getActivity().getString(R.string.weather_snowy);
                    break;
                case 5:
                    icon = getActivity().getString(R.string.weather_rainy);
                    break;
            }
        }
        weatherIcon.setText(icon);
    }

    public void changeCity(String city) {
        updateWeatherData(city);
    }

}