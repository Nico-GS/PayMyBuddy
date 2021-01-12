package com.payMyBuddy.repository;

import com.payMyBuddy.model.Bank;
import com.payMyBuddy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface BankRepository extends JpaRepository<Bank, Long> {
    List<Bank> findAllByUser(User user);
}
