package de.renoth.blposition;

import com.google.gson.annotations.SerializedName;

public class Team {
    @SerializedName("ShortName")
    String shortName;

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
