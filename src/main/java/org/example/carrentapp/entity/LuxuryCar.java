package org.example.carrentapp.entity;

import org.example.carrentapp.entity.Car;

public class LuxuryCar extends Car {
    @Override
    public boolean getAvailable() {

        return super.getAvailable() && this.getYear() > 2018;
    }
}
