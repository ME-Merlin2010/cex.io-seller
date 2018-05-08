package de.virtualpahl.crypto.trade.cex.seller;
/**
 * This project is licensed under the terms of the MIT license, you can read
 * more in LICENSE.txt; this project utilized the Google GSON library, licensed
 * under the Apache V2 License, which can be found at: gson/LICENSE.txt
 * 
 * CexSeller.java is based on the cex.io-reinvestor of Zack Urben 
 * Version  : 1.0.0
 * Author   : Tobias Pahl
 * Contact  : tobias@virtual-pahl.de
 * Creation : 10.04.2018
 * 
 * Motivation BTC    @ 3JEh8HAT5qntRUtET9xnaWGyrKVExTQnXv (minimum 0.0001 BTC)
 * Cex.io referral   @ https://cex.io/r/0/up114757661/0/
 * PayPal			 @ https://www.paypal.me/TobiasPahl
 * Other donations accepted via email request!
* 
 * Requirements:
 * This program requires a free API Key from Cex.io, which can be
 * obtained here: https://cex.io/trade/profile
 * 
 * This API Key requires the following permissions:
 * Account Balance, Place Order, Cancel Order, Open Order
 * 
 * This program requires the free Cex.io Java library, which can be obtained
 * here: https://github.com/ME-Merlin2010/cex.io-api-java/archive/master.zip
 * 
 * This is the core driver for Selling. This determines if the Seller
 * is run is GUI or Terminal mode.
 * 
 */

public class CexSeller {
    public static void main(String args[]) {
        if (args.length == 0) {
            // run in GUI mode
            new Login();
        } else if (args[0] != "" && args[1] != "" && args[2] != "") {
            // run in terminal/bash/cmd mode
            new Seller(args[0], args[1], args[2]).start();
        } else {
            System.out
                .println("\nTrading requires a Username, API Key, and API Secret.\n"
                    + "Please visit: Cex.io/api, if you do not have an API Key and Secret.\n"
                    + "Proper use is: \"java CexSeller Username API_Key API_Secret\"\n"
                    + "Credentials given:\n"
                    + "Username: "
                    + args[0]
                    + "\n"
                    + "API_Key: "
                    + args[1]
                    + "\n"
                    + "API_Secret: "
                    + args[2]
                    + "\n");
        }
    }
}
