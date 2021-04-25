package com.example.largraph;

import android.widget.LinearLayout;
import android.widget.TextView;

public class OldEntity {
    public LinearLayout linearLayout;
    public int HP;
    // Singleton Pattern
//    private static Entity instance;

    public OldEntity(LinearLayout linearLayout, int healthPoints) {
        this.linearLayout = linearLayout;
        this.HP = healthPoints;

        ((TextView)(this.linearLayout.findViewById(R.id.HP))).setText(healthPoints); // https://stackoverflow.com/questions/28465315/retrieving-ids-from-nested-layouts (call nested view)
    }

    public void setHP(int healthPoints) {
        this.HP = healthPoints;
        ((TextView)(this.linearLayout.findViewById(R.id.HP))).setText(healthPoints);
    }

    // TODO: display in view
    public void show() {

    }

    // TODO: remove from view
    public void hide() {

    }
}
