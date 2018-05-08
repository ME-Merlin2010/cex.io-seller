package de.virtualpahl.crypto.trade.cex.seller;
/**
 * This project is licensed under the terms of the MIT license, you can read
 * more in LICENSE.txt; this project utilized the Google GSON library, licensed
 * under the Apache V2 License, which can be found at: gson/LICENSE.txt
 * 
 * Seller.java is based on the cex.io-reinvestor of Zack Urben
 * Version  : 1.0.0
 * Author   : Tobias Pahl
 * Contact  : tobias@virtual-pahl.de
 * Creation : 10.04.2018
 * 
 * Motivation BTC    @ 3JEh8HAT5qntRUtET9xnaWGyrKVExTQnXv (minimum 0.0001 BTC)
 * Cex.io referral   @ https://cex.io/r/0/up114757661/0/
 * PayPal			 @ https://www.paypal.me/TobiasPahl
 * Other donations accepted via email request!
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import zackurben.cex.api.CexAPI;
import zackurben.cex.data.*;
import zackurben.cex.data.Balance.Currency;
import com.google.gson.Gson;

public class Seller extends CexAPI {
    protected final int MAX_API_CALLS = 600;
    protected ArrayList<Order> pending;
    protected long startTime, lastTime;
    protected int apiCalls;
    protected Balance balance;
    protected Coin coin;
    protected String currencyPairCrypto = "ETH";
    protected String currencyPairFiat = "USD";
    protected String currencyPair = currencyPairCrypto + "/" + currencyPairFiat;
    protected InputThread input;
    protected SellingThread selling;
    protected Dashboard gui;
    protected boolean done, debug = true;

    
	private void init() {
		this.pending = new ArrayList<Order>();
        this.startTime = System.currentTimeMillis();
        this.lastTime = this.startTime;
        this.apiCalls = 0;
        this.coin = new Coin(true, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, currencyPair);
        this.done = false;
        this.input = new InputThread(this);
	}
    
    /**
     * Constructor for Terminal/bash/cmd mode.
     * 
     * @param user
     *        Cex.io Username.
     * @param key
     *        Cex.io API Key.
     * @param secret
     *        Cex.io API Secret.
     */
    public Seller(String user, String key, String secret) {
        super(user, key, secret);
        init();
    }

    /**
     * Constructor for GUI mode.
     * 
     * @param user
     *        Cex.io Username.
     * @param key
     *        Cex.io API Key.
     * @param secret
     *        Cex.io API Secret.
     */
    public Seller(String user, String key, String secret, Dashboard input) {
        super(user, key, secret);
        init();
        this.gui = input;

        // update gui stuff
        if (gui != null) {
            gui.DISPLAY_API_CALLS.setText("0");
            gui.DISPLAY_CANCELED.setText("0");
            gui.DISPLAY_ORDERS.setText("0");
            gui.DISPLAY_PENDING.setText("0");
            gui.DISPLAY_START_TIME.setText(new Date(this.startTime).toString());
            gui.NUM_START_TIME = this.startTime;
            gui.DISPLAY_STATUS.setText("Idle");
            gui.DISPLAY_USERNAME.setText(user);
        } else {
            // serious error here, should never occur.
            out("Error 0x7.");
            log("error", "Error 0x7.");
        }
    }

    /**
     * Add Seller thread to program, with a reference to the invoked Seller object.
     */
    public void addSellerThread() {
        selling = new SellingThread(this);
        selling.start();

        // update gui display
        if (gui != null) {
            gui.DISPLAY_STATUS.setText("Selling");
            gui.DISPLAY_LOG.append("[" + new Date(System.currentTimeMillis()).toString() + "] Selling.\n");
        }
    }

    /**
     * Start the Input thread.
     */
    public void start() {
        input.start();
    }

    /**
     * Alias of: out(String, String);
     * 
     * @param output
     *        String to output to display to console/terminal.
     */
    public void out(String output) {
        out(output, "");
    }

    /**
     * Output formatted string to console/terminal, with a prepended string.
     * 
     * @param output
     *        String to output to console/terminal.
     * @param prepend
     *        String to prepend to formatted string being displayed to
     *        console/terminal.
     */
    public void out(String output, String prepend) {
        String result = prepend + "[" + new Date(System.currentTimeMillis()) + "] " + output + "\n";

        // determine how to display output, based on which version of the Seller is currently active.
        // (terminal/bash/cmd or GUI)
        if (gui != null) {
            gui.DISPLAY_LOG.append(result);
        } else {
            System.out.print(result);
        }
    }

    /**
     * Write contents to a log, relative to executed script.
     * 
     * @param file
     *        Name of file to write contents to.
     * @param input
     *        Contents to write to file.
     */
    public void log(String file, String input) {
        PrintWriter write = null;
        try {
            write = new PrintWriter(new BufferedWriter(new FileWriter(file + ".txt", true)));
            write.println(input);
            write.println();
        } catch (IOException e) {
            out("Error 0x6.");
            log("error", "Error 0x6:\n" + e.getMessage());
        } finally {
            if (write != null) {
                write.close();
            }
        }
    }

    /**
     * Format the display of floats (removes scientific notation).
     * 
     * @param input
     *        Float to format.
     * @return String representation of the float, up to 8 decimal places, rounded down.
     */
    public String formatAndRoundNumber(BigDecimal input) {
        input = input.setScale(8, RoundingMode.DOWN);
        return input.toPlainString();
    }

    /**
     * Format the account balance.
     * 
     * @param input
     *        JSON data of balance, from API call.
     * @return String representation of the formatted account balance.
     */
    public String formatBalance(String input) {
        String output = "";
        this.balance = new Gson().fromJson(input, Balance.class);

        String label[] = new String[] { "BTC ", "ETH ", "BCH ", "BTG ", "DASH", "XRP ", "XLM ", "ZEC ", "GHS " };
        
        Balance.Currency currency[] = new Balance.Currency[] {
            this.balance.BTC, this.balance.ETH, this.balance.BCH,
            this.balance.BTG, this.balance.DASH, this.balance.XRP,
            this.balance.XLM, this.balance.ZEC, this.balance.GHS };

        int formatted = 0;
        output += "Pair Available   Order";
        for (int a = 0; a < currency.length; a++) {
            output += "\n" + label[a] + " ";

            if (currency[a] != null
                && currency[a].available != null
                && currency[a].available.compareTo(BigDecimal.ZERO.setScale(8)) == 1) {
                output += formatAndRoundNumber(currency[a].available);
                formatted++;
            } else {
                output += "0.00000000";
            }
            output += "  ";
            if (currency[a] != null
                && currency[a].orders != null
                && currency[a].orders.compareTo(BigDecimal.ZERO.setScale(8)) == 1) {
                output += formatAndRoundNumber(currency[a].orders);
                formatted++;
            } else {
                output += "0.00000000";
            }
        }
        
        if (formatted == 0) {
            output = "There was a problem formatting the account balances, please wait until the next API call for an update.";
        }

        return output;
    }

    /**
     * Format the ticker data.
     * 
     * @param input
     *        JSON data of ticker, from API call.
     * @return String representation of formatted ticker data.
     */
    public String formatTicker(String input) {
        Ticker data = new Gson().fromJson(input, Ticker.class);
        return "Last        High        Low         Bid         Ask         Volume\n"
            + formatAndRoundNumber(data.last)
            + "  "
            + formatAndRoundNumber(data.high)
            + "  "
            + formatAndRoundNumber(data.low)
            + "  "
            + formatAndRoundNumber(data.bid)
            + "  "
            + formatAndRoundNumber(data.ask) 
            + "  " 
            + formatAndRoundNumber(data.volume) + "\n";
    }

    /**
     * Format the input time for a duration.
     * 
     * @param time
     *        Time subtract initial calculation.
     * @return String representation of formatted time passed since @param time
     */
    public String formatDuration(long time) {
        time = time - this.startTime;

        int h = (int) ((time / 1000) / 3600);
        int m = (int) (((time / 1000) / 60) % 60);
        int s = (int) ((time / 1000) % 60);

        DecimalFormat df = new DecimalFormat("####00");

        return df.format(h) + ":" + df.format(m) + ":" + df.format(s);
    }

    /**
     * Wrapper function for API calls, to ensure we do not exceed the API limit.
     * 
     * @param function
     *        Name of API function to invoke.
     * @param parameters
     *        String array of parameters required for API function.
     * @return JSON String from Cex.io API call.
     */
    public String execute(String function, String parameters[]) {
        boolean done = false;
        String output = "";
        if (System.currentTimeMillis() > (this.lastTime + 600000)) {
            this.apiCalls = 0;
            this.lastTime = System.currentTimeMillis();
        }

        while (!done) {
            if (this.apiCalls < this.MAX_API_CALLS) {
                if (function == "balance") {
                    output = this.balance();

                    if (this.gui != null) {
                        this.gui.DISPLAY_BALANCE.setText(formatBalance(output));
                    }
                } else if (function == "ticker") {
                    output = this.ticker(parameters[0]);
                } else if (function == "order_book") {
                    output = this.orderBook(parameters[0]);
                } else if (function == "place_order") {
                    output = this.placeOrder(parameters[0], parameters[1], Float.valueOf(parameters[2]), Float.valueOf(parameters[3]));
                } else if (function == "open_orders") {
                    output = this.openOrders(parameters[0]);
                } else if (function == "cancel_order") {
                    output = this.cancelOrder(parameters[0]);
                } else if (function == "trade_history") {
                    output = this.tradeHistory(parameters[0], Integer.valueOf(parameters[1]));
                }

                this.apiCalls++;
                done = true;
            } else {
                if (System.currentTimeMillis() > (this.lastTime + 600000)) {
                    this.apiCalls = 0;
                    this.lastTime = System.currentTimeMillis();
                } else {
                    out("API Limit reached, waiting 30 seconds to try again.\n");

                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        out("Error 0x5." + e.getMessage());
                        log("error", "Error 0x5:\n" + e.getMessage());
                    }
                }
            }
        }

        // update gui display
        if (this.gui != null) {
            this.gui.DISPLAY_API_CALLS.setText(String.valueOf(this.apiCalls));
            long temp = System.currentTimeMillis();
            this.gui.DISPLAY_LAST_ACTIVITY.setText(new Date(temp).toString());
            this.gui.NUM_LAST_ACTIVITY = temp;
            this.gui.DISPLAY_DURATION.setText(this.formatDuration(temp));
        }

        return output;
    }

    /**
     * Load settings from 'settings.txt' file, if it exists.
     * 
     * settings.txt Example:
     * username,apiKey,apiSecret,btcActive,btcReserve,btcMax,btcMin,btcMinAmount
     */
    public void loadSettings() {
        File file = new File("settings.txt");
        if (file.exists() && file.isFile()) {
            try {
                Scanner scanner = new Scanner(file);
                Scanner input = scanner.useDelimiter(",");
                String settings[] = new String[11];

                int a = 0;
                while (input.hasNext()) {
                	settings[a] = input.next();
                	a++;
                }

                scanner.close();
                
                // ignore first 3 inputs (0, 1 and 2)
                // username,apiKey,apiSecret
                coin.active = Boolean.valueOf(settings[3]);
                coin.reserve = BigDecimal.valueOf(Double.valueOf(settings[4]));
                coin.min = BigDecimal.valueOf(Double.valueOf(settings[5]));
                coin.max = BigDecimal.valueOf(Double.valueOf(settings[6]));
                coin.minAmount = BigDecimal.valueOf(Double.valueOf(settings[7]));
                out("Seller-Settings loaded successfully!");
            } catch (FileNotFoundException e) {
                out("Error 0xB.");
                log("error", "Error 0xB:\n" + e.getMessage());
            }
        } else {
        	out("No settings.txt found, no settings loaded.");
        }
    }

    /**
     * Write user settings to a file, 'settings.txt', which will load the last
     * settings upon next program start-up.
     */
    public void saveSettings() {
        PrintWriter write = null;
        try {
            write = new PrintWriter(new BufferedWriter(new FileWriter("settings.txt", false)));
            String settings = 
            	username + "," 
            	+ apiKey + ","
                + apiSecret + "," 
            	+ coin.active + ","
                + coin.reserve.toPlainString() + ","
                + coin.min.toPlainString() + ","
                + coin.max.toPlainString() + "," 
                + coin.minAmount.toPlainString();
            write.write(settings);
            out("Settings saved successfully!");
        } catch (IOException e) {
            out("Error 0xA.");
            log("error", "Error 0xA:\n" + e.getMessage());
        } finally {
            if (write != null) {
                write.close();
            }
        }
    }

	/**
     * Thread wrapper to process user input, in a separate thread.
     */
    class InputThread extends Thread {
        protected Seller user;

        /**
         * InputThread constructor, with reference to parent Seller Object.
         * 
         * @param input
         *        Parent Seller Object.
         */
        public InputThread(Seller input) {
            this.user = input;

            if (this.user.gui != null) {
                this.setPriority(Thread.MIN_PRIORITY);
            } else {
                this.setPriority(Thread.MAX_PRIORITY);
            }
        }

        /**
         * Display input controls for user, if not running in GUI mode.
         */
        public void prompt() {
            if (this.user.gui == null) {
                out("\n[B]alance | [1] Display '"+ currencyPair + "' | [E]xit\n"
                    + "[G]o Selling | [S]top Selling | [L]oad Settings | [W]rite Settings");
            }
        }

        /**
         * Invoke the InputThread to run until program exit is requested.
         */
        public void run() {
            Scanner input = new Scanner(System.in);
            String temp = null;
            while (!this.user.done) {
            	
                if (this.user.debug) {
                    this.user.out("[DBG] InputThread:" + Thread.currentThread().getId());
                }

                this.user.loadSettings();
                this.prompt();
                try {
                    temp = input.nextLine().substring(0, 1);
                    if (temp.compareToIgnoreCase("b") == 0) {
                        out("\n"
                            + this.user.formatBalance(this.user.execute(
                                "balance", new String[] {})), "\n");
                    } else if (temp.compareToIgnoreCase("1") == 0) {
                        out("\n"
                            + this.user.formatTicker(this.user.execute(
                                "ticker", (new String[] { currencyPair }))), "\n");
                    } else if (temp.compareToIgnoreCase("e") == 0) {
                        this.user.done = true;
                    } else if (temp.compareToIgnoreCase("g") == 0) {
                        this.user.addSellerThread();
                    } else if (temp.compareToIgnoreCase("s") == 0) {
                        this.user.selling.interrupt();
                        this.user.selling.stop = true;
                        this.user.selling.join();
                    } else if (temp.compareToIgnoreCase("l") == 0) {
                        this.user.loadSettings();
                    } else if (temp.compareToIgnoreCase("w") == 0) {
                        this.user.saveSettings();
                    }

                    // may need to yield for terminal/bash/cmd mode
                    Thread.sleep(500);
                } catch (IndexOutOfBoundsException e) {
                    // no input was typed before hitting enter ignore the
                    // indexoutofbounds and retry with fresh buffer
                    out("Error 0x9.");
                    log("error", "Error 0x9:\n" + e.getMessage());
                } catch (InterruptedException e) {
                    out("Error 0x3: ");
                    log("error", "Error 0x3:\n" + e.getMessage());
                }
            }
            
            input.close();

            // save last settings
            user.saveSettings();
            // tightguy spam
			out("\nThanks for using my Cex.io Seller tool.\n"
					+ "Motivation BTC    @ 3JEh8HAT5qntRUtET9xnaWGyrKVExTQnXv (minimum 0.0001 BTC)\n"
					+ "Cex.io referral   @ https://cex.io/r/0/up114757661/0/\n"
					+ "PayPal            @ https://www.paypal.me/TobiasPahl\n" 
					+ "Selling ran for " + (Math.round(System.currentTimeMillis() - user.startTime) / 60000) + " minutes!", "\n");
        }
    }

    /**
     * Thread wrapper to run Seller, in a separate thread.
     */
    class SellingThread extends Thread {
        private static final int SECURITY_SELL_BUFFER = 5;
		protected Seller user;
        protected boolean stop;

        /**
         * SellerThread constructor, with reference to parent Seller
         * Object.
         * 
         * @param input
         *        Parent Seller Object.
         */
        public SellingThread(Seller input) {
            this.user = input;
            this.stop = false;

            if (this.user.gui != null) {
                this.setPriority(Thread.MAX_PRIORITY);
            } else {
                this.setPriority(Thread.NORM_PRIORITY);
            }
        }

        /**
         * Invoke the SellerThread to run until the halt of selling is requested.
         */
        public void run() {
            while (!this.user.done && !this.stop) {
                if (this.user.debug) {
                    this.user.out("[DBG] SellingThread:"
                        + Thread.currentThread().getId());
                }

                try {
                    trade();
                } catch (NullPointerException e) {
                    // error with api call
                    if (this.user.debug) {
                        e.printStackTrace();
                    }

                    this.user.nonce = Integer.valueOf((int) (System.currentTimeMillis() / 1000));
                    out("Error 0x1: " + this.user.balance.toString());

                    StringWriter error = new StringWriter();
                    e.printStackTrace(new PrintWriter(error));
                    log("error", "Error 0x1:\n" + error.toString());
                } finally {
                    try {
                        if (this.user.debug) {
                            this.user.out("[DBG] Sleeping Seller Thread.");
                        }

                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        if (this.user.debug) {
                            this.user.out("[DBG] Sleeping Seller Thread was interrupted.");
                        }
                    }
                }
            }
        }

		private void trade() {
			user.balance = new Gson().fromJson(user.execute("balance", new String[] {}), Balance.class);

			if (user.debug) {
			    user.out("[DBG] Determing trades..");
			}

			// additional debug-messages
//			if (user.coin.active) logDebug("Coin is active.", user);
//			if (user.balance != null) logDebug("User.Balance is not NULL", user); 
//			if (user.coin.reserve.compareTo(user.balance.XLM.available) == -1) 
//				logDebug("coin.reserve (" + user.coin.reserve + ") ist kleiner als user.balance (" + user.balance.getCurrency(currencyPairCrypto).available + ")", user);
//			if (!user.pending.isEmpty()) logDebug("user.pending ist nicht leer", user);
			
			// active, balance != null, reserve < available, active, pending
			if (	 (user != null && user.coin.active
				  && user.balance != null && user.balance.getCurrency(currencyPairCrypto).available != null
				  && (user.coin.reserve.compareTo(user.balance.getCurrency(currencyPairCrypto).available) <= -1))
				  || (user.coin.active && !user.pending.isEmpty())) {
					logDebug("[DBG] Pending orders (" + user.pending.size() + ") exist OR\n" + 
							"enought funds (BALANCE: " + user.balance.getCurrency(currencyPairCrypto).available + ") available ... go to analyze", user);
					analyze(user.balance.getCurrency(currencyPairCrypto), user.coin);
			} else {
				if (user.balance != null) {
					logDebug("[DBG] Waiting, insufficient funds to initiate new positions.", user);
				} else {
					log("error", "Error while retrieving balance!");
					out("[ERR] Error while retrieving balance!");
				}
			}
		}

        /**
         * Analyze the trade potential for a coin, with reference to Currency and Coin Objects.
         * 
         * @param currency Parent Currency Object.
         * @param coin Parent Coin Object.
         */
        public void analyze(Currency currency, Coin coin) {
            // Determine if pending orders exist and take appropriate action
            if (!user.pending.isEmpty()) {
                long currentMmillis = System.currentTimeMillis();

                logDebug("[DBG] Analyzing the pending orders..! (Pending: " + user.pending.size() + ", " + currentMmillis + ")", user);

                for (int a = 0; a < user.pending.size(); a++) {
                    if (currentMmillis > (user.pending.get(a).time + 60000)) {
                        Order tempOrder = user.pending.get(a);

                        logDebug("[DBG] Trying to cancel the pending order...!\n (Order: " + tempOrder.toString() + ")", user);

                        boolean canceled = Boolean.valueOf(user.execute("cancel_order", new String[] { (tempOrder.id) }));

                        logDebug("[DBG] Canceled Status: " + canceled, user);

                        if (canceled) {
                            log("sell", "Pending order canceled:\n" + tempOrder);
                            out("Seller: Canceled pending order (ID: "
                                + tempOrder.id + ", Terminated: "
                                + tempOrder.pending.toPlainString() + " " + currencyPairFiat + " "
                                + tempOrder.price.toPlainString() + ")");
                            user.pending.remove(a);

                            // update gui display
                            if (user.gui != null) {
                                user.gui.DISPLAY_CANCELED.setText(String.valueOf(Integer.parseInt(this.user.gui.DISPLAY_CANCELED.getText()) + 1));
                            }
                        } else {
                            // error canceling order, completed already
                        	logDebug("[DBG] Pending order has completed..!", user);

                            log("sell", "Pending order completed:\n" + tempOrder);
                            out("Seller: Pending sell order complete. (ID: "
                                + tempOrder.id
                                + ", Sales: "
                                + formatAndRoundNumber(tempOrder.amount
                                    .multiply(tempOrder.price)) + ")");
                            this.user.pending.remove(a);

                            // update gui display
                            if (user.gui != null) {
                                user.gui.DISPLAY_CANCELED.setText(String.valueOf(Integer.parseInt(user.gui.DISPLAY_ORDERS.getText()) + 1));
                            }
                        }
                    } else {
                    	logDebug("[DBG] Pending orders: " + user.pending, user);
                    }
                }

                // update gui display
                if (user.gui != null) {
                    user.gui.DISPLAY_PENDING.setText(String.valueOf(user.pending.size()));
                }
            }

            user.balance = new Gson().fromJson(user.execute("balance", new String[] {}), Balance.class);

            // Make sales
            if (currency.available.compareTo(coin.reserve) == 1) {
                Ticker price = new Gson().fromJson(user.execute("ticker", new String[] { coin.pairTicker }), Ticker.class);

                // if price range is outside user specified limits: sell
                if (price.bid.compareTo(coin.min) <= 0) logDebug("Bid price (" + price.bid + ") is lesser or equal than minimum price of coin: " + coin.min, user);
                if (price.bid.compareTo(coin.max) >= 0) logDebug("Bid price (" + price.bid + ") is greater or equal than maximum price of coin: " + coin.max, user);
                if (price.bid.compareTo(coin.min) <= 0 || price.bid.compareTo(coin.max) >= 0) {

                	// calculate amount to sell
                	BigDecimal amount = currency.available.subtract(coin.reserve);

	                if (amount.compareTo(coin.minAmount) >= 0) {
	                	logDebug("Place-Order: Pair-Ticker: " + coin.pairTicker + " sell Amount: " + amount 
	                			+ " Price (bid): " + price.bid.toPlainString() 
	                			+ " Price (bid) - SECURITY_SELL_BUFFER: " + SECURITY_SELL_BUFFER + " " + currencyPairFiat + ": " 
	                			+ price.bid.subtract(BigDecimal.valueOf(SECURITY_SELL_BUFFER)).toPlainString() , user);
	                	Order order = null;
	                	order = new Gson().fromJson(user.execute("place_order", new String[] { 
                       			coin.pairTicker, 
                       			"sell", 
                       			amount.toPlainString(), 
                       			price.bid.subtract(BigDecimal.valueOf(SECURITY_SELL_BUFFER)).toPlainString()
                       			}), Order.class);
	                        
                        // check if order contains pending values
                        if (order != null && order.error.equals("")) {
                            if (order.pending.compareTo(BigDecimal.ZERO) == 0) {
                                log("buy", "Order complete:\n" + order);
                                out("Seller: Sold " + formatAndRoundNumber(order.amount) + " " + currencyPairCrypto + " @ " 
                                + formatAndRoundNumber(order.price.add(BigDecimal.valueOf(5))) + " " + currencyPairFiat
                                    + " (Sales: " + formatAndRoundNumber(order.price.multiply(order.amount)) + " "+ currencyPairFiat +")");

                                // update gui display
                                if (user.gui != null) {
                                    user.gui.DISPLAY_ORDERS.setText(String.valueOf((Integer.parseInt(user.gui.DISPLAY_ORDERS.getText()) + 1)));
                                }
                            } else {
                                // add to pending orders array list
                                user.pending.add(order);
                                log("sell", "Pending order:\n" + order.toString());
                                out("Seller: Sold "
                                    + formatAndRoundNumber(order.amount) + " " + currencyPairFiat + " @ "
                                    + formatAndRoundNumber(order.price)
                                    + coin.pairTicker + " (Pending: "
                                    + formatAndRoundNumber(order.pending)
                                    + " " + currencyPairFiat + ", ID: " + order.id + ")");

                                // update gui display
                                if (user.gui != null) {
                                	user.gui.DISPLAY_PENDING.setText(String.valueOf(user.pending.size()));
                                }
                            }
                        } else {
                            // tell sell error @ coin value for x fiatCurrency
                        	if (order == null) {
                        		log("error", "Order is null!");
                        		out("Seller: Order is null!");
                        	}
                        }
                    } else {
                        out("Seller: The amount (" + amount + ") of a " + coin.pairTicker + ", is lesser than the amount limit (" + coin.minAmount + ")"); 
                    }
                } else {
                    out("Seller: The bid price of a " + coin.pairTicker + ", is inside your specified limits --> Waiting ..." 
                    	+ "\n (Price: " + price.bid.toPlainString() 
                    	+ ", Range: " + coin.min.toPlainString() + " to " + coin.max.toPlainString() + ").");
                }
            } else if (currency.available.compareTo(coin.reserve) != 0) {
                out("Seller: The coins available, is less than the allocated reserve limit (Coins: "
                    + user.formatAndRoundNumber(currency.available)
                    + ", Reserve: " + user.formatAndRoundNumber(coin.reserve) + ").");
            }
        }
        
    }
    
    private void logDebug(String logMessageDebug, Seller user) {
		if (user.debug) {
		    user.out(logMessageDebug);
		}
	}
    
}
