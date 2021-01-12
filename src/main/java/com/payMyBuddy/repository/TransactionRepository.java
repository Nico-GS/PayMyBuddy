package com.payMyBuddy.repository;

import com.payMyBuddy.model.Transaction;
import com.payMyBuddy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllBySenderOrReceiver(User first, User second);
}
