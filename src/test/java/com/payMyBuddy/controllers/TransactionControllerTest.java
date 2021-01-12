package com.payMyBuddy.controllers;

import com.payMyBuddy.DTO.TransactionDTO;
import com.payMyBuddy.model.Transaction;
import com.payMyBuddy.model.User;
import com.payMyBuddy.services.TransactionService;
import com.payMyBuddy.services.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class TransactionControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    private TestRestTemplate restTemplate = new TestRestTemplate();
    private HttpHeaders httpHeaders = new HttpHeaders();

    private String testURLController(String uri) {
        return "http://localhost:" + port + uri;
    }

    private User createUser(){
        User user = new User();
        user.setPseudo("Nicolas");
        user.setEmail("nicolas@test.com");
        user.setPassword("password");
        user.setAmount(0);
        return user;
    }

    @Before
    public void setup(){
        User user1 = createUser();
        User user2 = createUser();
        user2.setEmail("test@test.com");
        user1 = userService.createUser(user1);
        user2 = userService.createUser(user2);
    }

    @Test
    @DisplayName("Make payment between the users if sold is enough on the bank account")
    public void paymentSuccessBetweenTheUsersWhenSoldIsEnough(){
        User user1 = userService.getById(1L).get();
        User user2 = userService.getById(2L).get();
        user1.setAmount(100);
        userService.addFriend(user1, user2);
        user1 = userService.saveUser(user1);
        assertTrue(transactionService.findAllByUser(user1).isEmpty());
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setSenderId(user1.getId());
        transactionDTO.setReceiverId(user2.getId());
        transactionDTO.setAmount(80d);
        transactionDTO.setDescription("Description of transaction");

        HttpEntity<TransactionDTO> entity = new HttpEntity(transactionDTO, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                testURLController("transaction"), HttpMethod.POST, entity, TransactionDTO.class
        );

        user1 = userService.getById(user1.getId()).get();
        user2 = userService.getById(user2.getId()).get();
        assertFalse(transactionService.findAllByUser(user1).isEmpty());
        Transaction inDB = transactionService.findAllByUser(user1).stream().findFirst().get();
        assertEquals(user1, inDB.getSender());
        assertEquals(user2, inDB.getReceiver());
        assertEquals(16d, user1.getAmount());
        assertEquals(80d, user2.getAmount());
    }

    @Test
    @DisplayName("Payment return bad request if the sender sold is not enough")
    public void paymentReturnBadRequestIfSenderSoldIsNotEnough(){
        User user1 = userService.getById(1L).get();
        User user2 = userService.getById(2L).get();
        assertTrue(transactionService.findAllByUser(user1).isEmpty());
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setSenderId(user1.getId());
        transactionDTO.setReceiverId(user2.getId());
        transactionDTO.setAmount(100d);
        transactionDTO.setDescription("Description of transaction");

        HttpEntity<TransactionDTO> entity = new HttpEntity(transactionDTO, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                testURLController("transaction"), HttpMethod.POST, entity, TransactionDTO.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Payment return bad request if amount is zero or negative")
    public void paymentReturnBadRequestWhenAmountIsZeroOrNegative(){
        User user1 = userService.getById(1L).get();
        User user2 = userService.getById(2L).get();
        assertTrue(transactionService.findAllByUser(user1).isEmpty());
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setSenderId(user1.getId());
        transactionDTO.setReceiverId(user2.getId());
        transactionDTO.setAmount(0);
        transactionDTO.setDescription("Description of transaction");

        HttpEntity<TransactionDTO> entity = new HttpEntity(transactionDTO, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                testURLController("transaction"), HttpMethod.POST, entity, TransactionDTO.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Payment return bad request if no connection between users")
    public void paymentReturnBadRequestIfNoConnectionBetweenUsers(){
        User user1 = userService.getById(1L).get();
        User user2 = userService.getById(2L).get();
        user1.setAmount(100);
        user1 = userService.saveUser(user1);
        assertTrue(transactionService.findAllByUser(user1).isEmpty());
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setSenderId(user1.getId());
        transactionDTO.setReceiverId(user2.getId());
        transactionDTO.setAmount(80d);
        transactionDTO.setDescription("Description of transaction");

        HttpEntity<TransactionDTO> entity = new HttpEntity(transactionDTO, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                testURLController("transaction"), HttpMethod.POST, entity, TransactionDTO.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
