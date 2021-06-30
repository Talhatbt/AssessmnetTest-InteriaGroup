package com.assessmenttest.models.directionapi;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Utils class for parsing Google Directions API response
 */
public class DirectionConverter {
    /**
     * Gets directions points from steps of directions API
     *
     * @param stepList steps list from Direction API response
     * @return LatLng list of direction points
     */
    static ArrayList<LatLng> getDirectionPoint(List<Step> stepList) {
        ArrayList<LatLng> directionPointList = new ArrayList<>();
        if (stepList != null && stepList.size() > 0) {
            for (Step step : stepList) {
                convertStepToPosition(step, directionPointList);
            }
        }
        return directionPointList;
    }

    /**
     * Converts single step of Direction API to LatLng points
     *
     * @param step               Single direction setp
     * @param directionPointList List of direction points
     */
    private static void convertStepToPosition(Step step, ArrayList<LatLng> directionPointList) {
        // Get start location
        directionPointList.add(step.getStartLocation().getCoordination());

        // Get encoded points location
        if (step.getPolyline() != null) {
            List<LatLng> decodedPointList = step.getPolyline().getPointList();
            if (decodedPointList != null && decodedPointList.size() > 0) {
                directionPointList.addAll(step.getPolyline().getPointList());
            }
        }

        // Get end location
        directionPointList.add(step.getEndLocation().getCoordination());
    }

    /**
     * Decodes polyline string to actual LatLng list to display polyline on map
     *
     * @param encoded Encoded polyline string
     * @return LatLng List
     */
    static List<LatLng> decodePoly(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(position);
        }
        return poly;
    }

    /**
     * Gets index of min Route w.r.t Distance, need to set alternatives to true in request param (if required)
     *
     * @param data Route List
     * @return Min Index
     */
    public static int getMinDistIndex(List<Route> data) {
        int minValue = data.get(0).getLegList().get(0).getDistance().getValue();
        int index = 0;
        for (int i = 1; i < data.size(); i++) {
            if (data.get(i).getLegList().get(0).getDistance().getValue() < minValue) {
                minValue = data.get(i).getLegList().get(0).getDistance().getValue();
                index = i;
            }
        }
        return index;
    }
}
