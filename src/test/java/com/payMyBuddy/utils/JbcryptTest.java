package com.payMyBuddy.utils;

import com.payMyBuddy.services.Hashing;
import org.junit.Test;
import org.junit.jupiter.api.Tag;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JbcryptTest {

    @Test
    @Tag("Test hash password")
    public void testHashPassword(){
        String[] hashPassword = new String[1];
        Function<String, Boolean> update = hash -> {hashPassword[0] = hash; return true;};

        String hashPw1 = Hashing.hash("password");
        assertTrue(Hashing.verifyAndUpdate("password",hashPw1,update));
        assertFalse(Hashing.verifyAndUpdate("password1",hashPw1,update));

        String hashPw2 = Hashing.hash("password");
        assertTrue(Hashing.verifyAndUpdate("password",hashPw2,update));
        assertFalse(Hashing.verifyAndUpdate("password2",hashPw2,update));

        // Verify if password are the same and not the same hash
        assertNotEquals(hashPw1, hashPw2);
    }
}
