
package com.sas.wallet.controller;

import com.sas.wallet.dto.*;
import com.sas.wallet.entity.Wallet;
import com.sas.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class WalletController {

    private final WalletService service;

    public WalletController(WalletService service) {
        this.service = service;
    }

    @PostMapping("/wallet")
    public WalletResponse operate(@Valid @RequestBody WalletOperationRequest req) {
        Wallet wallet = service.operate(req);
        return new WalletResponse(wallet.getId(), wallet.getBalance());
    }

    @GetMapping("/wallets/{id}")
    public WalletResponse get(@PathVariable UUID id) {
        Wallet wallet = service.get(id);
        return new WalletResponse(wallet.getId(), wallet.getBalance());
    }
}
