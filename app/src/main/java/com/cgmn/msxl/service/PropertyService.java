package com.cgmn.msxl.service;

import java.io.InputStream;
import java.util.Properties;

public class PropertyService {

    private Properties props;
    private static PropertyService entity = null;

    private PropertyService(){}

    public static PropertyService getInstance(){
        if(entity == null){
            entity = new PropertyService();
        }
        return entity;
    }

    public String getKey(String key){
        if(props == null){
            props = new Properties();
            try {
                InputStream in = PropertyService.class.getResourceAsStream(
                        "/assets/application.properties");
                props.load(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return props.getProperty(key);
    }
}
