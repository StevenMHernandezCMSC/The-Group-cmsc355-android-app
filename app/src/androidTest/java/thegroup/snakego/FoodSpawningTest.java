package thegroup.snakego;

import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import thegroup.snakego.entities.BaseEntity;
import thegroup.snakego.entities.GreenApple;
import thegroup.snakego.entities.Ouroboros;
import thegroup.snakego.entities.RedApple;
import thegroup.snakego.models.User;
import thegroup.snakego.services.EntitySpawner;

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

    @After public void tearDown(){
        Looper.myLooper().quit();
    }

    @Test public void randomFoodSpawnsInMyVicinity() {
        // build our map latitude-longitude bounds
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(0, 0), new LatLng(10, 10));

        // build our random food entity spawner
        EntitySpawner spawner = new EntitySpawner(latLngBounds, false);

        // spawn some random entity
        BaseEntity entity = spawner.spawnEntity();

        // assert the entity is in the latitude-longitude bounds
        Assert.assertTrue(latLngBounds.contains(entity.getPosition()));
    }

    @Test public void foodDisappearsWhenIWalkAway() {
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


    @Test public void spawnedEntityIsEitherRedOrGreenAppleorOuroborosToken() {
        // Given the the user starts playing the game, which starts automatically, when the apples begin
        // to populate the screen, the graphics look like images of apples that are green and red

        // build our map latitude-longitude bounds
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(0, 0), new LatLng(10, 10));

        // build our random food entity spawner
        EntitySpawner spawner = new EntitySpawner(latLngBounds, false);

        // spawn some random entity
        BaseEntity entity = spawner.spawnEntity();

        Assert.assertTrue(entity.getImage() == R.mipmap.green_apple ||
                entity.getImage() == R.mipmap.ic_ouroboros || entity.getImage() == R.mipmap.red_apple);
    }

    @Test public void ouroborosGrowsMySnake() {
        // build our map latitude-longitude bounds
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(0, 0), new LatLng(10, 10));
        // build our random food entity spawner
        EntitySpawner spawner = new EntitySpawner(latLngBounds, false);

        LatLng userLocation = new LatLng(1,1);

        // set our user to some location
        User.get().setLatLng(userLocation);

        int initialSnakeLength = User.get().getMaxSnakeLength();

        // spawn some good entity
        spawner.addEntity(new Ouroboros(userLocation));

        spawner.checkCollisions();

        int secondSnakeLength = User.get().getMaxSnakeLength();

        assertThat("Snake length should grow after getting ouroboros token",
                initialSnakeLength,
                lessThan(secondSnakeLength));
    }


    @Test public void ouroborosTokenIncreasesScore() {
        // build our map latitude-longitude bounds
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(0, 0), new LatLng(10, 10));
        // build our random food entity spawner
        EntitySpawner spawner = new EntitySpawner(latLngBounds, false);

        LatLng userLocation = new LatLng(1,1);

        // set our user to some location
        User.get().setLatLng(userLocation);

        int initialScore = User.get().getScore();

        // spawn good entity
        spawner.addEntity(new Ouroboros(userLocation));
        spawner.checkCollisions();

        int secondScore = User.get().getScore();

        Assert.assertTrue(secondScore - initialScore == 215);
    }

    @Test public void test_EntitySpawner_checkCollisions() {
        // build our map latitude-longitude bounds
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(0, 0), new LatLng(10, 10));
        // build our random food entity spawner
        EntitySpawner spawner = new EntitySpawner(latLngBounds, false);

        LatLng userLocation = new LatLng(1,1);

        // set our user to some location
        User.get().setLatLng(userLocation);

        int initialScore = User.get().getScore();

        // spawn good entity
        spawner.addEntity(new Ouroboros(userLocation));
        spawner.checkCollisions();

        int secondScore = User.get().getScore();

        Assert.assertTrue(secondScore != initialScore);
    }

    @Test public void test_EntitySpawner_addEntity() {
        // build our map latitude-longitude bounds
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(0, 0), new LatLng(10, 10));
        // build our random food entity spawner
        EntitySpawner spawner = new EntitySpawner(latLngBounds, false);

        LatLng userLocation = new LatLng(1,1);
        LatLng userLocation2 = new LatLng(2,2);
        LatLng userLocation3 = new LatLng(3,3);

        // set our user to some location
        User.get().setLatLng(userLocation);

        int initialSize = spawner.getCurrentEntities().size();
        Assert.assertEquals(3, initialSize);

        // spawn good entity
        spawner.addEntity(new Ouroboros(userLocation));
        spawner.addEntity(new Ouroboros(userLocation2));
        int subsequentSize = spawner.getCurrentEntities().size();

        Assert.assertEquals(4, subsequentSize);

        spawner.addEntity(new Ouroboros(userLocation3));
        subsequentSize = spawner.getCurrentEntities().size();

        Assert.assertEquals(5, subsequentSize);
    }
}
