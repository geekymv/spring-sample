package com.geekymv.spring;

import com.geekymv.spring.domain.Car;
import org.springframework.beans.factory.FactoryBean;

public class CarFactoryBean implements FactoryBean<Car> {

    private String carInfo;

    public String getCarInfo() {
        return carInfo;
    }

    public void setCarInfo(String carInfo) {
        this.carInfo = carInfo;
    }

    @Override
    public Car getObject() throws Exception {
        Car car = new Car();
        String[] carInfoArr = carInfo.split(",");
        car.setColor(carInfoArr[0]);
        car.setMaxSpeed(Integer.valueOf(carInfoArr[1]));
        car.setPrice(Double.parseDouble(carInfoArr[2]));
        return car;
    }

    @Override
    public Class<?> getObjectType() {
        return Car.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
