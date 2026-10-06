package com.sachin.bloodbank.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Approximate city-centre coordinates for major Indian cities. Used to place
 * markers on the map for blood banks (which only store a city name, not
 * exact coordinates) and as a fallback lookup elsewhere. Not exact addresses
 * — good enough for a city-level map pin, not for turn-by-turn navigation.
 */
public final class CityCoordinates {
    private static final Map<String, double[]> COORDS = new HashMap<>();

    static {
        put("new delhi", 28.6139, 77.2090);
        put("delhi", 28.6139, 77.2090);
        put("mumbai", 19.0760, 72.8777);
        put("bangalore", 12.9716, 77.5946);
        put("bengaluru", 12.9716, 77.5946);
        put("chennai", 13.0827, 80.2707);
        put("kolkata", 22.5726, 88.3639);
        put("hyderabad", 17.3850, 78.4867);
        put("pune", 18.5204, 73.8567);
        put("ahmedabad", 23.0225, 72.5714);
        put("vadodara", 22.3072, 73.1812);
        put("baroda", 22.3072, 73.1812);
        put("jaipur", 26.9124, 75.7873);
        put("lucknow", 26.8467, 80.9462);
        put("surat", 21.1702, 72.8311);
        put("nagpur", 21.1458, 79.0882);
        put("indore", 22.7196, 75.8577);
        put("bhopal", 23.2599, 77.4126);
        put("patna", 25.5941, 85.1376);
        put("chandigarh", 30.7333, 76.7794);
        put("kanpur", 26.4499, 80.3319);
        put("coimbatore", 11.0168, 76.9558);
        put("kochi", 9.9312, 76.2673);
        put("cochin", 9.9312, 76.2673);
        put("guwahati", 26.1445, 91.7362);
        put("bhubaneswar", 20.2961, 85.8245);
        put("visakhapatnam", 17.6868, 83.2185);
        put("vizag", 17.6868, 83.2185);
        put("ludhiana", 30.9010, 75.8573);
        put("nashik", 19.9975, 73.7898);
        put("vellore", 12.9165, 79.1325);
        put("varanasi", 25.3176, 82.9739);
        put("amritsar", 31.6340, 74.8723);
        put("rajkot", 22.3039, 70.8022);
        put("agra", 27.1767, 78.0081);
    }

    private static void put(String city, double lat, double lng) {
        COORDS.put(city, new double[]{lat, lng});
    }

    /** Returns {lat, lng} for a known city, or null if not recognised. */
    public static double[] lookup(String city) {
        if (city == null) return null;
        return COORDS.get(city.trim().toLowerCase(Locale.ROOT));
    }

    private CityCoordinates() { }
}
