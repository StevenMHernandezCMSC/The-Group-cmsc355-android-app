package thegroup.snakego;

import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import thegroup.snakego._entities.BaseEntity;
import thegroup.snakego._entities.GreenApple;
import thegroup.snakego._entities.RedApple;
import thegroup.snakego._models.User;
import thegroup.snakego._services.EntitySpawner;

import org.junit.Assert;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class FoodSpawningTest {

    @Before public void setup() {
        // required for EntitySpawner; ignore for now.
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
    }

    @Test public void randomFoodSpawnsInMyVicinity() {
        // build our map latitude-longitude bounds
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(0, 0), new LatLng(10, 10));

        // build our random food entity spawner
        EntitySpawner spawner = new EntitySpawner(latLngBounds, false);

        // spawn some random entity
        BaseEntity entity = spawner.spawnEntity();

        // assert the entity is in the latitude-longitude bounds
        Assert.assertTrue(latLngBounds.contains(entity.getLatlng()));
    }

    @Test public void foodDissapearsWhenIWalkAway() {
        // build our map latitude-longitude bounds
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(0, 0), new LatLng(10, 10));

        // build our random food entity spawner
        EntitySpawner spawner = new EntitySpawner(latLngBounds, false);

        // spawn some random entity
        BaseEntity entity = spawner.spawnEntity();

        // build our map latitude-longitude bounds
        LatLngBounds newLatLngBounds = new LatLngBounds(new LatLng(20, 20), new LatLng(30, 30));

        spawner.updateLocation(newLatLngBounds);

        // assert that our entity no longer exists
        Assert.assertTrue(!spawner.getCurrentEntities().contains(entity));
    }

    @Test public void entitiesChangeMyScore() {
        // build our map latitude-longitude bounds
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(0, 0), new LatLng(10, 10));
        // build our random food entity spawner
        EntitySpawner spawner = new EntitySpawner(latLngBounds, false);

        LatLng userLocation = new LatLng(1,1);

        // set our user to some location
        User.get().setLatLng(userLocation);

        int initialScore = User.get().getScore();

        // spawn good entity
        spawner.addEntity(new RedApple(userLocation));
        spawner.checkCollisions();

        int secondScore = User.get().getScore();

        assertThat("Initial score should be less than our score after reaching a red apple.",
                initialScore,
                lessThan(secondScore));

        // spawn bad entity
        spawner.addEntity(new GreenApple(userLocation));
        spawner.checkCollisions();

        int thirdScore = User.get().getScore();

        assertThat("Green apple collision should caused our score to go down.",
                secondScore,
                greaterThan(thirdScore));
    }

    @Test public void redApplesGrowMySnake() {
        // build our map latitude-longitude bounds
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(0, 0), new LatLng(10, 10));
        // build our random food entity spawner
        EntitySpawner spawner = new EntitySpawner(latLngBounds, false);

        LatLng userLocation = new LatLng(1,1);

        // set our user to some location
        User.get().setLatLng(userLocation);

        int initialSnakeLength = User.get().getMaxSnakeLength();

        // spawn some good entity
        spawner.addEntity(new RedApple(userLocation));

        spawner.checkCollisions();

        int secondSnakeLength = User.get().getMaxSnakeLength();

        assertThat("Snake length should grow after eating enough red apples",
                initialSnakeLength,
                lessThan(secondSnakeLength));
    }


}
