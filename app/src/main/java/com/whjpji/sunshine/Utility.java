/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.whjpji.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.format.Time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {
    public static int WIND_DIRECTION_UNKNOWN = -1;
    public static int WIND_DIRECTION_NORTH = 0;
    public static int WIND_DIRECTION_NORTHEAST = 1;
    public static int WIND_DIRECTION_EAST = 2;
    public static int WIND_DIRECTION_SOUTHEAST = 3;
    public static int WIND_DIRECTION_SOUTH = 4;
    public static int WIND_DIRECTION_SOUTHWEST = 5;
    public static int WIND_DIRECTION_WEST = 6;
    public static int WIND_DIRECTION_NORTHWEST = 7;

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    static String formatTemperature(Context context, double temperature, boolean isMetric) {
        double temp;
        if ( !isMetric ) {
            temp = 9*temperature/5+32;
        } else {
            temp = temperature;
        }
        String formatString = context.getString(R.string.format_temperature);
        return String.format(formatString, temp);
    }

    static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }
    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (julianDay == currentJulianDay) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, dateInMillis)));
        } else if ( julianDay < currentJulianDay + 7 ) {
            // If the input date is less than a week in the future, just return the day name.
            return getDayName(context, dateInMillis);
        } else {
            // Otherwise, use the form "Mon Jun 3"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return
     */
    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(R.string.tomorrow);
        } else {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param dateInMillis The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, long dateInMillis) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    /**
     * Get the wind level of the given wind speed, measured by Beaufort Wind Force Scale.
     * @param windSpeed Wind speed in km/h.
     * @return The wind level measured by Beaufort Wind Force Scale.
     */
    private static int getWindLevel(float windSpeed) {
        int windLevel = 0;

        if (windSpeed >= 0 && windSpeed < 2) {
            windLevel = 0;
        } else if (windSpeed >= 2 && windSpeed < 7) {
            windLevel = 1;
        } else if (windSpeed >= 7 && windSpeed < 13) {
            windLevel = 2;
        } else if (windSpeed >= 13 && windSpeed < 20) {
            windLevel = 3;
        } else if (windSpeed >= 20 && windSpeed < 31) {
            windLevel = 4;
        } else if (windSpeed >= 31 && windSpeed < 41) {
            windLevel = 5;
        } else if (windSpeed >= 41 && windSpeed < 52) {
            windLevel = 6;
        } else if (windSpeed >= 52 && windSpeed < 63) {
            windLevel = 7;
        } else if (windSpeed >= 63 && windSpeed < 76) {
            windLevel = 8;
        } else if (windSpeed >= 76 && windSpeed < 88) {
            windLevel = 9;
        } else if (windSpeed >= 88 && windSpeed < 104) {
            windLevel = 10;
        } else if (windSpeed >= 104 && windSpeed < 118) {
            windLevel = 11;
        } else if (windSpeed >= 118) {
            windLevel = 12;
        }
        return windLevel;
    }

    /**
     * Get the direction code of wind by the degrees.
     * @param degrees Degrees in [0, 360).
     * @return the direction code. -1 for unknown, 0 for north, 1, for northeast, etc.
     */
    public static int getWindDirectionIndex(float degrees) {
        int direction = WIND_DIRECTION_UNKNOWN;

        if (degrees >= 337.5 || degrees < 22.5) {
            direction = WIND_DIRECTION_NORTH;
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = WIND_DIRECTION_NORTHEAST;
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = WIND_DIRECTION_EAST;
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = WIND_DIRECTION_SOUTHEAST;
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = WIND_DIRECTION_SOUTH;
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = WIND_DIRECTION_SOUTHWEST;
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = WIND_DIRECTION_WEST;
        } else if (degrees >= 292.5 && degrees < 337.5) {
            direction = WIND_DIRECTION_NORTHWEST;
        }

        return direction;
    }

    public static String getWindDirection(Context context, float degrees) {
        String direction = "Unknown";
        final String[] WIND_DIRECTIONS = context.getResources()
                .getStringArray(R.array.wind_directions);
        int directionIndex = getWindDirectionIndex(degrees);
        if (directionIndex != WIND_DIRECTION_UNKNOWN) {
            direction = WIND_DIRECTIONS[directionIndex];
        }
        return direction;
    }

    public static int getWindFlagDrawableId(float windSpeed) {
        switch (getWindLevel(windSpeed)) {
            case 0:
                return 0;
            case 1:
                return R.drawable.wind_flag_level_1;
            case 2:
                return R.drawable.wind_flag_level_2;
            case 3:
                return R.drawable.wind_flag_level_3;
            case 4:
                return R.drawable.wind_flag_level_4;
            case 5:
                return R.drawable.wind_flag_level_5;
            case 6:
                return R.drawable.wind_flag_level_6;
            case 7:
                return R.drawable.wind_flag_level_7;
            case 8:
                return R.drawable.wind_flag_level_8;
            case 9:
                return R.drawable.wind_flag_level_9;
            case 10:
                return R.drawable.wind_flag_level_10;
            case 11:
                return R.drawable.wind_flag_level_11;
            case 12:
                return R.drawable.wind_flag_level_12;
            default:
                return 0;
        }
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        final String[] WIND_LEVELS = context.getResources()
                .getStringArray(R.array.wind_levels);

        int windLevel = getWindLevel(windSpeed);
        String windDescription = WIND_LEVELS[windLevel];

        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }
        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
        String directionString = getWindDirection(context, degrees);

        return String.format(context.getString(windFormat),
                windDescription, windSpeed, directionString
        );
    }

    public static String getFormattedHumidity(Context context, float humidity) {
        return String.format(context.getString(R.string.format_humidity), humidity);
    }

    public static String getFormattedPressure(Context context, float pressure) {
        return String.format(context.getString(R.string.format_pressure), pressure);
    }

    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding image. -1 if no relation is found.
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_rain;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }
}