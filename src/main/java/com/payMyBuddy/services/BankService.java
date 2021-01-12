package com.payMyBuddy.services;

import com.payMyBuddy.model.BankTransaction;
import com.payMyBuddy.model.User;
import com.payMyBuddy.repository.BankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class BankService {

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private UserService userService;

    /**
     * Create an object BankTransaction
     * @param user user for the transaction
     * @param accountNumber account number of bank account
     * @param amount amount of money transferred, positive or negative
     * @return the object BankTransaction
     */
    public BankTransaction createBankTransaction(User user, String accountNumber, double amount){
        BankTransaction transac = new BankTransaction();
        transac.setUser(user);
        transac.setAccountNumber(accountNumber);
        transac.setAmount(amount);
        transac.setDate(new Date());
        return transac;
    }

    /**
     * Save the transaction in dB
     * @param transac the transaction
     * @return the transaction in dB
     */
    public BankTransaction save(BankTransaction transac){
        return bankRepository.save(transac);
    }

    /**
     * Process the bankTransaction
     * @param bankTransac the bankTransaction
     * @return true success | false failed
     */
    public boolean processTransaction(BankTransaction bankTransac){
        // If amount 0 return false
        if(bankTransac.getAmount() == 0){
            return false;
        }

        User user = bankTransac.getUser();
        double absAmount = Math.abs(bankTransac.getAmount());

        // If amount > 0 add money to account
        // If < 0 withdraw money
        // If user don't have enough money return false
        if(bankTransac.getAmount() > 0){
            user.setAmount(user.getAmount() + absAmount);
        } else if (bankTransac.getAmount() < 0 && absAmount <= user.getAmount()) {
            user.setAmount(user.getAmount() - absAmount);
        } else {
            return false;
        }

        userService.saveUser(user);
        return true;
    }

    /**
     * Find all transactions by user
     * @param user the user
     * @return a list of transactions
     */
    public List<BankTransaction> findByUser(User user){
        return bankRepository.findAllByUser(user);
    }
}
