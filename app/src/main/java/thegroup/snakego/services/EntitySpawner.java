package thegroup.snakego.services;

import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import thegroup.snakego.entities.BaseEntity;
import thegroup.snakego.entities.GreenApple;
import thegroup.snakego.entities.RedApple;
import thegroup.snakego.interfaces.Listenable;
import thegroup.snakego.models.User;
import thegroup.snakego.utils.DistanceCalculator;

public class EntitySpawner implements Listenable {

    private static final int SPAWN_FREQUENCY = 20000;

    private static final double COLLISION_DISTANCE = 3;

    private static final int MAX_ENTITIES = 10;

    private int spawnRate = 2;

    private LatLngBounds currentMapBounds;

    private Class[] entityTypes = {GreenApple.class, RedApple.class};

    private ArrayList<BaseEntity> currentEntities = new ArrayList<>();

    private ArrayList<BaseEntity> removeGreenEntities = new ArrayList<>();

    private Handler handler = new Handler();

    private BaseEntity entity;

    private List<PropertyChangeListener> listeners = new ArrayList<>();


    public EntitySpawner(LatLngBounds currentMapBounds) {
        this(currentMapBounds, true);
    }

    public EntitySpawner(LatLngBounds currentMapBounds, boolean automaticSpawning) {
        this.currentMapBounds = currentMapBounds;

        if (automaticSpawning) {
            this.handler.postDelayed(this.spawnEntitiesRunnable, SPAWN_FREQUENCY / spawnRate);
        }
    }

    private LatLng getRandomLocation() {
        double latitude = this.randomInRange(this.currentMapBounds.southwest.latitude, this.currentMapBounds.northeast.latitude);
        double longitude = this.randomInRange(this.currentMapBounds.northeast.longitude, this.currentMapBounds.southwest.longitude);

        return new LatLng(latitude, longitude);
    }

    private double randomInRange(double max, double min) {
        return (Math.random() * (max - min)) + min;
    }

    public BaseEntity spawnEntity() {
        try {
            if (this.currentEntities.size() >= MAX_ENTITIES) {
                this.currentEntities.remove(0);
            }
            int index = new Random().nextInt(this.entityTypes.length);

            entity = (BaseEntity) this.entityTypes[index].getConstructor(LatLng.class).newInstance(this.getRandomLocation());

            this.addEntity(entity);


                moveEntityRunnable.run();


            this.notifyListeners(this, "Entities", null, this.currentEntities);

            return entity;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public void addEntity(BaseEntity entity) {
        this.currentEntities.add(entity);

        this.checkCollisions();
    }

    public boolean checkCollisions() {
        LatLng latlng = User.get().getPosition();

        for (BaseEntity entity : this.currentEntities) {
            if (DistanceCalculator.distance(latlng, entity.getPosition()) < COLLISION_DISTANCE) {
                entity.onCollision();
                if(entity instanceof GreenApple) {
                    this.removeGreenEntities.add(entity);
                }
                else {
                    this.currentEntities.remove(entity);
                }
                notifyListeners(this, "Entities", null, currentEntities);
                return true;
            }

        }
        return false;
    }

    public ArrayList<BaseEntity> getCurrentEntities() {
        return currentEntities;
    }

    public void updateLocation(LatLngBounds newMapBounds) {
        this.currentMapBounds = newMapBounds;

        for (BaseEntity entity : this.currentEntities) {
            if (!this.currentMapBounds.contains(entity.getPosition())) {
                this.currentEntities.remove(entity);
            }
        }
        this.checkCollisions();
    }

    private Runnable spawnEntitiesRunnable = new Runnable() {
        @Override
        public void run() {
            spawnEntity();

            handler.postDelayed(this, (long) randomInRange(SPAWN_FREQUENCY / spawnRate, 0));
        }
    };

    private Runnable moveEntityRunnable =  new Runnable() {
        @Override
        public void run() {


            //Iterator<BaseEntity> iterator = currentEntities.iterator();
            //while (iterator.hasNext()) {
                for (BaseEntity entity : currentEntities) {
                    if (entity instanceof GreenApple) {
                    double oldLong = entity.getPosition().longitude;
                    double oldLat = entity.getPosition().latitude;
                    double newLong = oldLong;
                    double newLat = oldLat;
                    // move the apple
                    if (entity.getPosition() != User.get().getPosition()) {
                        if (oldLong < User.get().getPosition().longitude) {
                            newLong += .000001;
                        } else {
                            newLong -= .000001;
                        }
                        if (oldLat < User.get().getPosition().latitude) {
                            newLat += .000001;
                        } else {
                            newLat -= .000001;
                        }

                    }
                    entity.setPosition(newLat, newLong);

                    if (checkCollisions()) {
                        handler.removeCallbacks(moveEntityRunnable);
                    }
                }
            }
            currentEntities.removeAll(removeGreenEntities);
            notifyListeners(this, "Entities", null, currentEntities);


            handler.postDelayed(this, 1000);


        }
    };


    public void notifyListeners(Object object, String property, Object oldValue, Object newValue) {
        for (PropertyChangeListener name : listeners) {
            name.propertyChange(new PropertyChangeEvent(object, property, oldValue, newValue));
        }
    }

    public void addChangeListener(PropertyChangeListener newListener) {
        listeners.add(newListener);
    }
}