package com.bankapp.repository;

import com.bankapp.entity.BankTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

    @Query("""
           select t from BankTransaction t
           left join fetch t.sender
           left join fetch t.receiver
           where t.sender.id = :accountId or t.receiver.id = :accountId
           order by t.createdAt desc, t.id desc
           """)
    List<BankTransaction> findForAccount(@Param("accountId") Long accountId, Pageable pageable);
}
