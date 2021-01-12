package com.payMyBuddy.controllers;

import com.payMyBuddy.DTO.TransactionDTO;
import com.payMyBuddy.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class TransactionController {

    @Autowired
    private TransactionService transacService;

    /**
     * Payment between users
     * @param transac the transaction infos
     * @return 200 if successful | 400 if failed
     */
    @PostMapping(value = "transaction")
    public ResponseEntity makeTransaction(@RequestBody TransactionDTO transac){
        if(transacService.makePayment(transac)){
            return ResponseEntity.ok(transac);
        } else {
            return ResponseEntity.badRequest().body(transac);
        }
    }
}
