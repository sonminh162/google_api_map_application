package com.lifetime.map.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectionsParser {

    public List<List<HashMap<String, String>>> parse(JSONObject jObject){
        List<List<HashMap<String,String>>> routes = new ArrayList<>();
        JSONArray jRoutes;
        JSONArray jLeg;
        JSONArray jManeuver;

        try{
            jRoutes = jObject.getJSONObject("response").getJSONArray("route");

            //Loop for all route
            for(int i = 0; i < jRoutes.length();i++){
                List path = new ArrayList<HashMap<String,String>>();
                jLeg = ((JSONObject) jRoutes.get(i)).getJSONArray("leg");

                //Loop for all waypoint
                for(int j =0; j<jLeg.length();j++){
                    jManeuver = ((JSONObject) jLeg.get(j)).getJSONArray("maneuver");

                    //Loop for all maneuver
                    for(int k = 0; k<jManeuver.length();k++){
                        JSONObject jPosition = ((JSONObject)jManeuver.get(k)).getJSONObject("position");
                        HashMap<String,String> hm = new HashMap<>();
                        hm.put("lat", jPosition.getString("latitude"));
                        hm.put("long", jPosition.getString("longitude"));
                        path.add(hm);
                    }
                    routes.add(path);
                }
            }

        }catch (JSONException e){
            e.printStackTrace();
        }

        return routes;
    }
}
