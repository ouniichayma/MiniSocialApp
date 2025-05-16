package com.dev.minisocialapp.utils;

import com.cloudinary.Cloudinary;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryUtils {

    public static Map config;
    static {
        config = new HashMap();
        config.put("cloud_name", "dcxnfv7n4");
        config.put("api_key", "276286436661619");
        config.put("api_secret", "YSsCwN_nRT_IiAAU0QdYeD6jFZ4");
    }

    public static com.cloudinary.Cloudinary getInstance() {
        return new com.cloudinary.Cloudinary(config);
    }

}
