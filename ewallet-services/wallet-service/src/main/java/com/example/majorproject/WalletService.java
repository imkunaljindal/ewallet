package com.example.majorproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    @Autowired
    WalletRepository walletRepository;
    public void createWallet(String userName){
        Wallet wallet = Wallet.builder().
                userName(userName).
                amount(0).
                build();

        walletRepository.save(wallet);
    }

    Wallet incrementWallet(String userName, int amount){

        Wallet oldWallet = walletRepository.findByUserName(userName);
        int newAmount = oldWallet.getAmount() + amount;

        Wallet newWallet = Wallet.builder().userName(userName).
                amount(newAmount).
                id(oldWallet.getId()).
                build();

        walletRepository.save(newWallet);
        return newWallet;

        //Method 2 : write the query
//            int success = walletRepository.updateWallet(userName,amount);
    }

    Wallet decrementWallet(String userName, int amount){
        Wallet oldWallet = walletRepository.findByUserName(userName);
        int newAmount = oldWallet.getAmount() - amount;

        Wallet newWallet = Wallet.builder().userName(userName).
                amount(newAmount).
                id(oldWallet.getId()).
                build();

        walletRepository.save(newWallet);
        return newWallet;
    }
}
