package com.bankapp.repository;

import com.bankapp.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUserId(Long userId);

    @Query("select a from Account a join fetch a.user where a.user.phoneNumber = :phone")
    Optional<Account> findByUserPhoneNumber(@Param("phone") String phone);

    boolean existsByAccountNumber(String accountNumber);

    /** Row lock (SELECT ... FOR UPDATE) so concurrent balance changes are serialized. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a join fetch a.user where a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);
}
