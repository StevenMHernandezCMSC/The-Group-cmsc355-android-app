package thegroup.snakego;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import thegroup.snakego.database.HighScores;
import thegroup.snakego.elements.SnakeTextView;
import thegroup.snakego.interfaces.HttpResultsInterface;

public class HighScoresActivity extends AppCompatActivity implements HttpResultsInterface {

    SnakeTextView returnToOptionsText;
    ListView highscoresList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_high_scores);

        returnToOptionsText = (SnakeTextView) findViewById(R.id.return_to_options_page);
        this.returnToOptionsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressReturnToOptionsButton();
            }
        });

        HighScores highScores = new HighScores(this);
        highScores.load();
    }

    public void pressReturnToOptionsButton() {
        finish(); // clearing the back-stack
    }

    private void renderHighScores(JSONArray highscores) {

        try {
            String[] items = new String[highscores.length()];

            for (int i = 0; i < highscores.length(); i++) {
                JSONObject score = highscores.getJSONObject(i);

                items[i] = score.getString("Username") + " - " + score.getString("Score");
            }

            ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.highscore_list_view, items);

            this.highscoresList = (ListView) findViewById(R.id.high_scores_list);
            highscoresList.setAdapter(adapter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onSuccess(JSONObject response, int method, String endpoint) {
        //
    }

    @Override
    public void onSuccess(JSONArray response, int method, String endpoint) {
        if (method == Request.Method.GET) {
            this.renderHighScores(response);
        }
    }

    @Override
    public void onError(VolleyError error, int method, String endpoint) {
        //
    }
}