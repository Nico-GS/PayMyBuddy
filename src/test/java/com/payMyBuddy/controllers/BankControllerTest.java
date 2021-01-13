package com.payMyBuddy.controllers;

import com.payMyBuddy.DTO.BankDTO;
import com.payMyBuddy.model.Bank;
import com.payMyBuddy.model.User;
import com.payMyBuddy.repository.BankRepository;
import com.payMyBuddy.services.BankService;
import com.payMyBuddy.services.UserService;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
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
public class BankControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private BankService bankService;

    @Autowired
    private UserService userService;

    @Autowired
    private BankRepository btxRepository;

    private HttpHeaders httpHeaders = new HttpHeaders();
    private TestRestTemplate restTemplate = new TestRestTemplate();

    private String testURLController(String uri) {
        return "http://localhost:" + port + uri;
    }

    private User createUser(){
        User user = new User();
        user.setNickname("Nicolas");
        user.setEmail("nicolas@test.com");
        user.setPassword("password");
        user.setAmount(0);
        return user;
    }

    @Test
    @DisplayName("Process transaction when money is deposited on bank account")
    public void processTransactionWhenMoneyIsDepositedOnBankAccount(){
        User user1 = createUser();
        user1 = userService.saveUser(user1);
        assertTrue(bankService.findByUser(user1).isEmpty());
        BankDTO bankDTO = new BankDTO();
        bankDTO.setAmount(100d);
        bankDTO.setIban("AccountNumber");
        HttpEntity<BankDTO> entity = new HttpEntity<>(bankDTO, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                testURLController("bankTransaction/"+user1.getId()), HttpMethod.POST,entity, Bank.class
        );
        assertFalse(bankService.findByUser(user1).isEmpty());
        assertEquals(userService.getById(user1.getId()).get().getAmount(), 100);
    }

    @Test
    @DisplayName("Process transaction when money (enough) is taken from bank account")
    public void processTransactionWhenMoneyIsTakenOnBankAccountAndAmountEnough(){
        User user1 = createUser();
        user1.setAmount(100);
        user1 = userService.saveUser(user1);
        assertTrue(bankService.findByUser(user1).isEmpty());
        BankDTO bankDTO = new BankDTO();
        bankDTO.setAmount(-90d);
        bankDTO.setIban("IBAN");
        HttpEntity<BankDTO> entity = new HttpEntity<>(bankDTO, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                testURLController("bankTransaction/"+user1.getId()), HttpMethod.POST,entity, Bank.class
        );
        assertFalse(bankService.findByUser(user1).isEmpty());
        assertEquals(userService.getById(user1.getId()).get().getAmount(), 10);
    }

    @Test
    @DisplayName("Failed transaction when not enough money in bank account")
    public void processTransactionFailedWhenNotEnoughMoneyInBankAccount(){
        User user1 = createUser();
        user1.setAmount(10);
        user1 = userService.saveUser(user1);
        assertTrue(bankService.findByUser(user1).isEmpty());
        BankDTO bankDTO = new BankDTO();
        bankDTO.setAmount(-90d);
        bankDTO.setIban("AccountNumber");
        HttpEntity<BankDTO> entity = new HttpEntity<>(bankDTO, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                testURLController("bankTransaction/"+user1.getId()), HttpMethod.POST,entity, Bank.class
        );
        assertTrue(bankService.findByUser(user1).isEmpty());
        assertEquals(userService.getById(user1.getId()).get().getAmount(), 10);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Failed transaction when user ID is invalid")
    public void processTransactionFailedWhenUserIdIsInvalid(){
        User user1 = createUser();
        user1.setAmount(10);
        user1 = userService.saveUser(user1);
        assertTrue(bankService.findByUser(user1).isEmpty());
        BankDTO bankDTO = new BankDTO();
        bankDTO.setAmount(10d);
        bankDTO.setIban("IBAN");
        HttpEntity<BankDTO> entity = new HttpEntity<>(bankDTO, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                testURLController("bankTransaction/"+(user1.getId()+1)), HttpMethod.POST,entity, Bank.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
