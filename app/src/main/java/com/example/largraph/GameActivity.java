package com.example.largraph;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    public static int currentLevel = 0;

    private ArrayList<Entity> attackQueue;
    private ArrayList<Entity> enemies;
    public int enemyCount;
    public Entity hero;

    private ArrayList<PointF> enemyPositions; // This is made to not place enemy in the same place
    public static final float MINIMUM_DISTANCE = 90;

    public RelativeLayout gameLayout;
    public ImageView smoke;

    public static int width, height;

    public PointF lastDrawPos;

    public static final long ANIMATION_DURATION = 1000; // 1000 ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameLayout = findViewById(R.id.gameLayout);

        // https://stackoverflow.com/questions/4743116/get-screen-width-and-height-in-android
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels; // Will be use in setup later

        setup();
    }

    /**
     * Initial activity creation tasks
     */
    public void setup() {
        currentLevel++;

        enemyPositions = new ArrayList<>();

        // Action bar
        String level = getString(R.string.lvl); // https://stackoverflow.com/questions/13388493/how-can-i-convert-the-android-resources-int-to-a-string-eg-android-r-string-c
        // https://stackoverflow.com/questions/3438276/how-to-change-the-text-on-the-action-bar
        Objects.requireNonNull(getSupportActionBar()).setTitle(level + " " + currentLevel); // using "R.string.lvl" directly will return an int instead of string and will cause "unintended behavior(tm)"

        // Random Background
        RelativeLayout gameLayout = findViewById(R.id.gameLayout); // https://stackoverflow.com/questions/3307090/how-to-add-background-image-to-activity
        Random rnd = new Random();
        switch (rnd.nextInt(8)) { // 0 inclusive to n exclusive
            case 0:
                gameLayout.setBackgroundResource(R.mipmap.map1);
                break;
            case 1:
                gameLayout.setBackgroundResource(R.mipmap.map2);
                break;
            case 2:
                gameLayout.setBackgroundResource(R.mipmap.map3);
                break;
            case 3:
                gameLayout.setBackgroundResource(R.mipmap.map4);
                break;
            case 4:
                gameLayout.setBackgroundResource(R.mipmap.map5);
                break;
            case 5:
                gameLayout.setBackgroundResource(R.mipmap.map6);
                break;
            case 6:
                gameLayout.setBackgroundResource(R.mipmap.map7);
                break;
            case 7:
                gameLayout.setBackgroundResource(R.mipmap.map8);
                break;
            default:
                break;
        }

        // Smoke
        smoke = new ImageView(this);
        smoke.setLayoutParams(
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                )
        );
        smoke.setImageResource(R.mipmap.smoke_reduced);

        // Hero
        hero = new Entity(20, Entity.HERO, this, gameLayout);

        // Enemies
        enemyCount = 5 + (int) (Math.log(currentLevel)) + 1;
        attackQueue = new ArrayList<>();
        enemies = new ArrayList<>();
        for (int i = 0; i < enemyCount; i++) {
            // Create new view objects (aka an enemy)
            // Then add the newly created layout to the GameActivity layout (to display) [random positions]
            int randomHP = (rnd.nextInt(enemyCount - 2) + 1) * 5;
            enemies.add(new Entity(randomHP, Entity.ENEMY, this, gameLayout));
        }

        // Initiate initial line position at the Hero's location
        lastDrawPos = hero.pos;
    }

    /**
     * After clicking on an enemy sprite, this method will be called
     *
     * @param that an Entity
     */
    public void addToQueue(Entity that) {
        attackQueue.add(that);    // Add to ArrayList
        that.defUnClickable();    // Enemy become unclickable

        // Draw line (from hero to 1st enemy OR from previous enemy to current enemy)
        new DrawLine(this, gameLayout, lastDrawPos, that.pos);
        lastDrawPos = that.pos;

        // If all enemies have been clicked
        if (attackQueue.size() >= enemyCount) {
            // TODO: Thread sleep 500ms? (https://www.youtube.com/watch?v=aTT4GfojkHA)
            battle();
        }
    }

    public void battle() {
        boolean victory = true;
        for (Entity enemy : attackQueue) {
            hero.animate(enemy);
            // TODO: Thread sleep 200ms?
            if (hero.HP <= 0) {
                victory = false;
                lose();
                break;
            }
        }
        if (victory) win();
    }

    // https://www.youtube.com/watch?v=lvoKGGErwhw (creating dialog box)
    // Will check after each animation
    public void win() {
        AlertDialog.Builder winDialogBuilder = new AlertDialog.Builder(GameActivity.this);
        winDialogBuilder.setTitle(R.string.win);
        winDialogBuilder.setMessage(R.string.win_detail);
        winDialogBuilder.setCancelable(false);
        winDialogBuilder.setPositiveButton(R.string.next_lvl, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                recreate(); // https://stackoverflow.com/questions/1397361/how-to-restart-activity-in-android
            }
        });

        AlertDialog winDialog = winDialogBuilder.create();
        winDialog.show();
    }

    public void lose() {
        AlertDialog.Builder loseDialogBuilder = new AlertDialog.Builder(GameActivity.this);
        loseDialogBuilder.setTitle(R.string.lose);
        loseDialogBuilder.setMessage(R.string.lose_detail);
        loseDialogBuilder.setCancelable(false);
        loseDialogBuilder.setPositiveButton(R.string.go_back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentLevel = 0;
                GameActivity.this.finish();
            }
        });

        AlertDialog loseDialog = loseDialogBuilder.create();
        loseDialog.show();
    }

    /**
     * Creates a line with every instantiation
     */
    public class DrawLine extends View {
        PointF from, to;

        public DrawLine(Context context, RelativeLayout layout, PointF from, PointF to) {
            super(context);

            this.from = from;
            this.to = to;

            layout.addView(this); // We need something to display on
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10);

            canvas.drawLine(from.x, from.y, to.x, to.y, paint);
            super.onDraw(canvas);
        }
    }

    /**
     * A class that manages entities
     */
    public class Entity {
        // This will decide the behavior for each entity (clickable, sprite, HP color)
        public static final int HERO = 1;
        public static final int ENEMY = -1;

        public ImageView sprite;
        public TextView HPText;
        public RelativeLayout layout;
        public int HP;
        public PointF pos;

        // Entity enemy = new Entity(20, ENEMY, this, mainLayout); // Usage in GameActivity
        public Entity(int HP, int alliance, Context context, RelativeLayout layout) {
            this.layout = layout;

            // https://stackoverflow.com/questions/2395769/how-to-programmatically-add-views-to-views
            // Sprite creation
            sprite = new ImageView(context);
            sprite.setLayoutParams(
                    new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
            );
            switch (alliance) {
                case HERO:
                    sprite.setImageResource(R.mipmap.player_reduced);
                    break;
                case ENEMY:
                    sprite.setImageResource(R.mipmap.enemy_reduced);
                    break;
            }

            // Health text creation
            HPText = new TextView(context);
            HPText.setLayoutParams(
                    new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
            );
            setHP(HP);
            switch (alliance) {
                case HERO:
                    HPText.setTextColor(Color.BLUE);
                    break;
                case ENEMY:
                    HPText.setTextColor(Color.RED);
                    break;
            }

            // https://stackoverflow.com/questions/363681/how-do-i-generate-random-integers-within-a-specific-range-in-java
            // Random coordinates
//            int width = layout.getMeasuredWidth();
//            int height = layout.getMeasuredHeight(); // https://stackoverflow.com/questions/14592930/getwidth-returns-0-if-set-by-androidlayout-width-match-parent (too much of a hassle, will use DisplayMetric Instead)
            // The point of this do-while block is to not place enemy in the same place
            boolean farEnough = true;
            PointF randomPoint;
            do {
                float randomX = (float) (0.1 + Math.random() * (0.8 - 0.1)) * GameActivity.width; // Random within the screen 20% border
                float randomY = (float) (0.1 + Math.random() * (0.8 - 0.1)) * GameActivity.height;
                randomPoint = new PointF(randomX, randomY);

                for (PointF occupiedPoint : GameActivity.this.enemyPositions) {
                    if (Math.abs(randomPoint.x - occupiedPoint.x) >= MINIMUM_DISTANCE ||
                            Math.abs(randomPoint.y - occupiedPoint.y) >= MINIMUM_DISTANCE) {
                        farEnough = true;
                    } else {
                        farEnough = false;
                        break;
                    }
                }
            } while (!farEnough);
            GameActivity.this.enemyPositions.add(randomPoint);
            setCoordinates(context, randomPoint);

            // Display what was being made
            layout.addView(sprite);
            layout.addView(HPText);

            // Clickable
            if (alliance == ENEMY) {
                defClickable();
            }
        }

        // https://stackoverflow.com/questions/33963861/adding-onclick-to-buttons-that-are-created-programmatically
        public void defClickable() {
            this.sprite.setClickable(true);
            this.sprite.setOnClickListener(new View.OnClickListener() { // This is basically just implementing an event listener interface (anonymously, aka without declaring the class name)
                @Override
                public void onClick(View v) {
                    GameActivity.this.addToQueue(Entity.this); // This why Entity is an inner class
                }
            });
        }

        // https://stackoverflow.com/questions/5195321/remove-an-onclick-listener
        public void defUnClickable() {
            this.sprite.setClickable(false);
            this.sprite.setOnClickListener(null);
        }

        public void setCoordinates(Context context, PointF pos) { // Use context in order to position things properly (display dpi)
            this.pos = pos;

            // https://stackoverflow.com/questions/12351695/programmatically-set-imageview-position-in-dp
            float factor = context.getResources().getDisplayMetrics().density; // display independent

            int xOffsetDP = 18; // 18dp
            float xOffsetPX = xOffsetDP * factor;
            int yOffsetDP = 20; // 20dp
            float yOffsetPX = yOffsetDP * factor;

            sprite.setX(pos.x);
            sprite.setY(pos.y);
            HPText.setX(pos.x + xOffsetPX); // Adding will move it to the right
            HPText.setY(pos.y - yOffsetPX); // Reducing will make the object go higher
//            System.out.println(sprite.getWidth() + sprite.getHeight()); // Useless, it will only return 0

            // This is mostly for drawing correctly
            this.pos.x = pos.x + xOffsetPX;
            this.pos.y = pos.y + yOffsetPX;
        }

        public void setHP(int HP) {
            this.HP = HP;
            HPText.setText(String.valueOf(HP)); // Make sure that you aren't really just trying to set the text to a number and expecting it to automatically convert to a string.
            // https://stackoverflow.com/questions/7727808/android-resource-not-found-exception
        }

        // TODO: Hero teleports to enemy in queue, then smoke covering the fight for some time (maybe 1 sec?) (not frozen, unlike sleep).
        public void animate(Entity that) { // In GameActivity will define entity[0] (Hero) to be the only fighter (implicit param) && just animate, will determine lose/win in GameActivity
            // Translate hero to enemy
            // Sprite
            ObjectAnimator spriteAnimatorX = ObjectAnimator.ofFloat(hero.sprite, "x", that.pos.x);
            spriteAnimatorX.setDuration(ANIMATION_DURATION);
            ObjectAnimator spriteAnimatorY = ObjectAnimator.ofFloat(hero.sprite, "y", that.pos.y);
            spriteAnimatorY.setDuration(ANIMATION_DURATION);
            // HPText
            ObjectAnimator textAnimatorX = ObjectAnimator.ofFloat(hero.HPText, "x", that.pos.x);
            textAnimatorX.setDuration(ANIMATION_DURATION);
            ObjectAnimator textAnimatorY = ObjectAnimator.ofFloat(hero.HPText, "y", that.pos.y);
            textAnimatorY.setDuration(ANIMATION_DURATION);
            // Actually translating
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(spriteAnimatorX, spriteAnimatorY, textAnimatorX, textAnimatorY);
            animatorSet.start();
            // Add smoke to RelativeLayout where the hero and enemy are, then animate it (somehow)

            // Health logic
            if (hero.HP > that.HP) { // Hero wins
                hero.setHP(hero.HP + that.HP);
                that.setHP(0);
                that.hide();
            } else { // Enemy wins
                that.setHP(hero.HP + that.HP);
                hero.setHP(0);
            }

            // Display hero aftermath (always hero)
            that.hide(); // Hide the enemy
        }

        public void hide() {
            sprite.setVisibility(View.INVISIBLE);
            HPText.setVisibility(View.INVISIBLE);
        }
    }
}