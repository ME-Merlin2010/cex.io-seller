/**
 * This project is licensed under the terms of the MIT license, you can read
 * more in LICENSE.txt; this project utilized the Google GSON library, licensed
 * under the Apache V2 License, which can be found at: gson/LICENSE.txt
 * 
 * Coin.java
 * Version  : 1.0.4
 * Author   : Zack Urben
 * Contact  : zackurben@gmail.com
 * Creation : 12/31/13
 * 
 * This is the Coin data type, used to parse JSON to a Java Object.
 * 
 * Support:
 * Motivation BTC       @ 1HvXfXRP9gZqHPkQUCPKmt5wKyXDMADhvQ
 * Cex.io Referral      @ https://cex.io/r/0/kannibal3/0/
 * Scrypt Referral      @ http://scrypt.cc?ref=baaah
 * Cryptsy Trade Key    @ e5447842f0b6605ad45ced133b4cdd5135a4838c
 * Other donations accepted via email request.
 */

package zackurben.cex.data;

import java.math.BigDecimal;

public class Coin {
    public boolean active;
    public BigDecimal reserve = new BigDecimal("0.00000000");
    public BigDecimal min = new BigDecimal("0.00000000");
    public BigDecimal max = new BigDecimal("0.00000000");
    public BigDecimal minAmount = new BigDecimal("0.00000000");
    public String pairTicker;

    /**
     * Default Coin constructor, used to edit reinvestment options.
     * 
     * @param active
     *        Determine if the coin is enabled for reinvestment.
     * @param reserve
     *        The amount to with-hold from reinvestment.
     * @param min
     *        The minimum amount allowed to sell for 1 XXX/COIN
     * @param max
     *        The maximum amount allowed to sell for 1 XXX/COIN
     * @param pairTicker
     *        The pair ticker for Cex.io
     */
    public Coin(Boolean active, BigDecimal reserve, BigDecimal min, BigDecimal max, BigDecimal minAmount, String pairTicker) {
        this.active = active;
        this.reserve = reserve;
        this.min = min;
        this.max = max;
        this.minAmount = minAmount;
        this.pairTicker = pairTicker;
    }

    /**
     * Overide the default toString method to give basic object data dump.
     */
    public String toString() {
        return "{" + active + ":" + reserve.toPlainString() + ":" + min.toPlainString() + ":" + max.toPlainString() + ":" + pairTicker + "}";
    }
}
