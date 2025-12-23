
package com.sas.wallet.service;

import com.sas.wallet.dto.*;
import com.sas.wallet.entity.Wallet;
import com.sas.wallet.exception.InsufficientFundsException;
import com.sas.wallet.exception.WalletNotFoundException;
import com.sas.wallet.repository.WalletRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository repo;

    public WalletService(WalletRepository repo) {
        this.repo = repo;
    }

    @Retryable(
      retryFor = OptimisticLockException.class,
      maxAttempts = 5,
      backoff = @Backoff(delay = 10)
    )
    @Transactional
    public Wallet operate(WalletOperationRequest req) {
        Wallet wallet = repo.findById(req.walletId())
                .orElseGet(() -> repo.save(new Wallet(req.walletId())));

        if (req.operationType() == OperationType.WITHDRAW &&
                wallet.getBalance().compareTo(req.amount()) < 0) {
            throw new InsufficientFundsException();
        }

        BigDecimal updated = req.operationType() == OperationType.DEPOSIT
                ? wallet.getBalance().add(req.amount())
                : wallet.getBalance().subtract(req.amount());

        wallet.setBalance(updated);
        return repo.save(wallet);
    }

    @Transactional(readOnly = true)
    public Wallet get(UUID id) {
        return repo.findById(id).orElseThrow(WalletNotFoundException::new);
    }
}
