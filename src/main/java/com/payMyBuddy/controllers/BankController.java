package com.payMyBuddy.controllers;

import com.payMyBuddy.DTO.BankDTO;
import com.payMyBuddy.model.Bank;
import com.payMyBuddy.model.User;
import com.payMyBuddy.services.BankService;
import com.payMyBuddy.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class BankController {

    @Autowired
    private BankService bankTransac;

    @Autowired
    private UserService userService;

    Logger LOGGER = LoggerFactory.getLogger(BankController.class);

    /**
     * Process bank transaction
     * @param bankDTO transaction infos
     * @param userId userID
     * @return Status 200 if transaction OK, status 400 if failed
     */
    @PostMapping(value = "bankTransaction/{userId}")
    public ResponseEntity<BankDTO> processTransaction (@RequestBody BankDTO bankDTO, @PathVariable Long userId){
        if(userService.getById(userId).isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User user = userService.getById(userId).get();
        Bank bank = bankTransac.createBankTransaction(user, bankDTO.getIban(),bankDTO.getAmount());
        if(bankTransac.processTransaction(bank)){
            bankTransac.save(bank);
            LOGGER.info("Process Bank Transaction success");
            return ResponseEntity.ok(bankDTO);
        } else {
            LOGGER.info("Invalid ID, transaction failed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * List of bank transactions by user
     * @param userId the userID
     * @return list of bank transaction, 400 if the user doesn't exist
     */
    @GetMapping(value = "bankTransaction/{userId}")
    public ResponseEntity<List<Bank>> getTransaction(@PathVariable Long userId){
        if(userService.getById(userId).isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User user = userService.getById(userId).get();
        List<Bank> listTransac = bankTransac.findByUser(user);
        return ResponseEntity.ok(listTransac);
    }
}
