package edu.uw.bn22.sunspotter;

/**
 * Created by bruceng on 1/19/16.
 */
public class WeatherData {
    private String weather;
    private int temperature;
    private String hour;
    private String day;
    private boolean foundSunnyDay;

    //Initializes a WeatherData with no parameters
    public WeatherData() {
        temperature = 0;
        weather = "";
        hour = "";
        day = "";
        foundSunnyDay = false;
    }

    //Returns the stored data as a string
    public String toString() {
        String sun = "";
        if (weather.equals("Clear")) {
            sun = "Sun";
        } else {
            sun = "No Sun";
        }
        return sun + " ( " + day + " " + hour + " ) - " + temperature + "\u2109";
    }

    //Returns if the given weather data indicates a sunny day
    public boolean getFoundSunnyDay() {
        return this.foundSunnyDay;
    }

    //Returns the temperature of the current weather report
    public int getTemperature() {
        return this.temperature;
    }

    //Returns the weather of the current weather report
    public String getWeather() {
        return this.weather;
    }

    //Returns the current hour of the given forecast
    public String getHour() {
        return this.hour;
    }

    //Returns the day of the week of the given forecast
    public String getDay() {
        return this.day;
    }

    //Changes to indicate that it will be a sunny (clear) day
    public void setFoundSunnyDay(boolean sunny) {
        this.foundSunnyDay = sunny;
    }

    //Rounds the given temperature into an int
    public void setTemperature(String curTemperature) {
        Double tempFah = Double.parseDouble(curTemperature);
        if (tempFah >= 0) {
            this.temperature = (int) (tempFah + 0.5);
        } else {
            this.temperature = (int) (tempFah - 0.5);
        }
    }

    //Returns the current hour of the forecast report
    public void setHour(String curHour) {
        this.hour = curHour;
    }

    //Returns the current day of the week for the forecast report
    public void setDay(String curDay) {
        this.day = curDay;
    }

    //Returns the weather description (i.e. clear)
    public void setWeather(String curWeather) {
        this.weather = curWeather;
    }
}
