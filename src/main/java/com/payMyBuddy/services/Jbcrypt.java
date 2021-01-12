package com.payMyBuddy.services;

import org.mindrot.jbcrypt.BCrypt;

import java.util.function.Function;

public class Jbcrypt {

    private final int logRounds;

    public Jbcrypt(int logRounds){
        this.logRounds = logRounds;
    }

    public String hash(String password){
        return BCrypt.hashpw(password,BCrypt.gensalt(logRounds));
    }

    public boolean verifyHash(String password, String hash){
        return BCrypt.checkpw(password, hash);
    }

    public boolean verifyAndUpdateHash(String password, String hash, Function<String, Boolean> udpateFunc){
        if (BCrypt.checkpw(password, hash)){
            int rounds = getRounds(hash);
            if (rounds != logRounds){
                String newHash = hash(password);
                return udpateFunc.apply(newHash);
            }
            return true;
        }
        return false;
    }


    private int getRounds(String salt){
        char minor = (char)0;
        int off = 0;

        if (salt.charAt(0) != '$'|| salt.charAt(1) != '2'){
            throw new IllegalArgumentException("Invalid salt version");
        }
        if (salt.charAt(2) == '$'){
            off = 3;
        } else {
            minor = salt.charAt(2);
            if (minor != 'a' || salt.charAt(3) != '$'){
                throw new IllegalArgumentException("Invalid salt revision");
            }
            off = 4;
        }

        if (salt.charAt(off + 2) > '$'){
            throw new IllegalArgumentException("Missing salt rounds");
        }
        return Integer.parseInt(salt.substring(off, off+2));
    }
}
