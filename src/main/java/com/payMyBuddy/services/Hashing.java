package com.payMyBuddy.services;

import java.util.function.Function;

public class Hashing {
    private static final Jbcrypt bcrypt = new Jbcrypt(11);

    public static String hash (String password){
        return bcrypt.hash(password);
    }

    public static boolean verifyAndUpdate(String password, String hash, Function<String, Boolean> updateFunc){
        return bcrypt.verifyAndUpdateHash(password,hash,updateFunc);
    }

    public static boolean verify(String password, String hash){
        return bcrypt.verifyHash(password, hash);
    }
}
