package de.virtualpahl.crypto.trade.cex.seller;
/**
 * This project is licensed under the terms of the MIT license, you can read
 * more in LICENSE.txt; this project utilized the Google GSON library, licensed
 * under the Apache V2 License, which can be found at: gson/LICENSE.txt
 * 
 * DashboardSeller.java is based on the cex.io-reinvestor of Zack Urben
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
 * This is Dashboard GUI for the CexSeller, when run in GUI mode.
 */

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.google.gson.Gson;

import zackurben.cex.data.Balance;

public class Dashboard {
    protected JFrame FRAME_DASHBOARD;
    protected JTextField DISPLAY_USERNAME, DISPLAY_STATUS, INPUT_RESERVE_COIN,
        INPUT_MAX_COIN, INPUT_MIN_COIN, DISPLAY_API_CALLS, DISPLAY_ORDERS, 
        DISPLAY_CANCELED, DISPLAY_PENDING, DISPLAY_START_TIME, 
        DISPLAY_LAST_ACTIVITY, DISPLAY_DURATION;
    protected JPanel PANEL, TAB_SETTINGS, TAB_INFO, TAB_LOG, TAB_ABOUT;
    protected JTabbedPane PANEL_TAB;
    protected JLabel LABEL_USERNAME, LABEL_STATUS, LABEL_COINS, LABEL_MIN,
        LABEL_MAX, LABEL_RESERVE, LABEL_BALANCE, LABEL_API_CALLS, LABEL_ORDERS,
        LABEL_CANCELED, LABEL_PENDING, LABEL_START_TIME, LABEL_LAST_ACTIVITY,
        LABEL_DURATION;
    protected JLabel LABEL_MIN_AMOUNT;
    protected JTextField INPUT_MIN_AMOUNT_COIN;
    protected JToggleButton BUTTON_TOGGLE_SELLER;
    protected JButton BUTTON_SAVE;
    protected JCheckBox CHECKBOX_COIN;
    protected JTextArea DISPLAY_LOG;
    protected JTextPane DISPLAY_BALANCE, TEXTPANE_ABOUT, TEXTPANE_BTC,
        TEXTPANE_CEX, TEXTPANE_CRYPTSY, TEXTPANE_SCRYPT;
    protected JScrollPane SCROLLPANE;
    protected long NUM_START_TIME;
    protected long NUM_LAST_ACTIVITY;
    protected Seller user;

    /**
     * Create the application.
     */
    public Dashboard(String user, String apiKey, String apiSecret) {
        initialize();
        this.FRAME_DASHBOARD.setVisible(true);
        this.user = new Seller(user, apiKey, apiSecret, this);
        this.user.start();
        loadSettings();

        // query user balance
        this.user.balance = new Gson().fromJson(this.user.execute("balance", new String[] {}), Balance.class);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        FRAME_DASHBOARD = new JFrame();
        FRAME_DASHBOARD.setTitle("Cex.io Seller v1.0.0 - By Merlin2010");
        FRAME_DASHBOARD.setBounds(100, 100, 1030, 800);
        FRAME_DASHBOARD.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FRAME_DASHBOARD.getContentPane().setLayout(null);

        PANEL = new JPanel();
        PANEL.setBounds(6, 6, 999, 777);
        PANEL.setLayout(null);
        FRAME_DASHBOARD.getContentPane().add(PANEL);

        LABEL_USERNAME = new JLabel("Username");
        LABEL_USERNAME.setHorizontalAlignment(SwingConstants.RIGHT);
        LABEL_USERNAME.setBounds(6, 12, 75, 16);
        PANEL.add(LABEL_USERNAME);

        DISPLAY_USERNAME = new JTextField();
        DISPLAY_USERNAME.setEditable(false);
        DISPLAY_USERNAME.setBounds(93, 6, 115, 28);
        DISPLAY_USERNAME.setColumns(10);
        PANEL.add(DISPLAY_USERNAME);

        BUTTON_TOGGLE_SELLER = new JToggleButton("Toggle Seller");
        BUTTON_TOGGLE_SELLER.setBounds(377, 7, 200, 29);
        BUTTON_TOGGLE_SELLER.addActionListener(new ActionListener() {
        	
            public void actionPerformed(ActionEvent evt) {
                if (BUTTON_TOGGLE_SELLER.isSelected()) {
                    // update all values in obj from gui
                    updateSettings();
                    user.saveSettings();
                    user.addSellerThread();
                } else {
                    // button is deselected
                    // stop selling thread
                    user.selling.interrupt();
                    user.selling.stop = true;

                    try {
                        user.selling.join();
                    } catch (InterruptedException e) {
                        // Thread.currentThread().interrupt();
                        user.out("Error 0x3.");
                        user.log("error", "Error 0x3:\n" + e.getMessage());
                    }

                    DISPLAY_STATUS.setText("Idle");
                    DISPLAY_LOG.append("[" + new Date(System.currentTimeMillis()) + "] Idle.\n");
                }
            }
        });
        PANEL.add(BUTTON_TOGGLE_SELLER);

        LABEL_STATUS = new JLabel("Status:");
        LABEL_STATUS.setBounds(220, 12, 48, 16);
        PANEL.add(LABEL_STATUS);

        DISPLAY_STATUS = new JTextField();
        DISPLAY_STATUS.setEditable(false);
        DISPLAY_STATUS.setBounds(265, 6, 100, 28);
        DISPLAY_STATUS.setColumns(10);
        PANEL.add(DISPLAY_STATUS);

        PANEL_TAB = new JTabbedPane(JTabbedPane.TOP);
        PANEL_TAB.setBounds(6, 40, 876, 620);
        PANEL.add(PANEL_TAB);

        TAB_SETTINGS = new JPanel();
        TAB_SETTINGS.setLayout(null);
        PANEL_TAB.addTab("Settings", null, TAB_SETTINGS, null);

        LABEL_COINS = new JLabel("Coins");
        LABEL_COINS.setToolTipText("Select which coins to enable selling with.");
        LABEL_COINS.setBounds(6, 6, 70, 16);
        TAB_SETTINGS.add(LABEL_COINS);

        CHECKBOX_COIN = new JCheckBox("ETH");
        CHECKBOX_COIN.setSelected(true);
        CHECKBOX_COIN.setBounds(6, 34, 70, 23);
        TAB_SETTINGS.add(CHECKBOX_COIN);

        LABEL_RESERVE = new JLabel("Reserve");
        LABEL_RESERVE.setToolTipText("Set an amount to reserve from trades.");
        LABEL_RESERVE.setBounds(88, 6, 61, 16);
        TAB_SETTINGS.add(LABEL_RESERVE);

        INPUT_RESERVE_COIN = new JTextField();
        INPUT_RESERVE_COIN.setText("0.00000000");
        INPUT_RESERVE_COIN.setBounds(88, 32, 134, 28);
        INPUT_RESERVE_COIN.setColumns(10);
        TAB_SETTINGS.add(INPUT_RESERVE_COIN);

        LABEL_MIN = new JLabel("Minimum");
        LABEL_MIN.setToolTipText("Set the minimum value you would like to sell 1 XXX/COIN.");
        LABEL_MIN.setBounds(244, 6, 100, 16);
        TAB_SETTINGS.add(LABEL_MIN);

        INPUT_MIN_COIN = new JTextField();
        INPUT_MIN_COIN.setText("0.00000000");
        INPUT_MIN_COIN.setBounds(244, 32, 134, 28);
        INPUT_MIN_COIN.setColumns(10);
        TAB_SETTINGS.add(INPUT_MIN_COIN);

        LABEL_MAX = new JLabel("Maximum");
        LABEL_MAX.setToolTipText("Set the maximum value you would like to sell 1 XXX/COIN.");
        LABEL_MAX.setBounds(400, 6, 61, 16);
        TAB_SETTINGS.add(LABEL_MAX);

        INPUT_MAX_COIN = new JTextField();
        INPUT_MAX_COIN.setText("0.00000000");
        INPUT_MAX_COIN.setBounds(400, 32, 134, 28);
        INPUT_MAX_COIN.setColumns(10);
        TAB_SETTINGS.add(INPUT_MAX_COIN);

        LABEL_MIN_AMOUNT = new JLabel("Amount-Limit");
        LABEL_MIN_AMOUNT.setToolTipText("Set the amount sell limit of this coin.");
        LABEL_MIN_AMOUNT.setBounds(556, 6, 100, 16);
        TAB_SETTINGS.add(LABEL_MIN_AMOUNT);

        INPUT_MIN_AMOUNT_COIN = new JTextField();
        INPUT_MIN_AMOUNT_COIN.setText("0.00000000");
        INPUT_MIN_AMOUNT_COIN.setBounds(556, 32, 134, 28);
        INPUT_MIN_AMOUNT_COIN.setColumns(10);
        TAB_SETTINGS.add(INPUT_MIN_AMOUNT_COIN);

        BUTTON_SAVE = new JButton("Save Settings");
        BUTTON_SAVE.setBounds(6, 560, 134, 29);
        BUTTON_SAVE.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateSettings();
                user.saveSettings();
            }
        });
        TAB_SETTINGS.add(BUTTON_SAVE);

        TAB_INFO = new JPanel();
        TAB_INFO.setLayout(null);
        PANEL_TAB.addTab("Information", null, TAB_INFO, null);

        DISPLAY_BALANCE = new JTextPane();
        DISPLAY_BALANCE.setBackground(UIManager.getColor("Panel.background"));
        DISPLAY_BALANCE.setFont(new Font("Monospaced", Font.PLAIN, 13));
        DISPLAY_BALANCE.setEditable(false);
        DISPLAY_BALANCE.setBounds(6, 34, 256, 218);
        TAB_INFO.add(DISPLAY_BALANCE);

        LABEL_BALANCE = new JLabel("Balance");
        LABEL_BALANCE.setBounds(6, 6, 61, 16);
        TAB_INFO.add(LABEL_BALANCE);

        LABEL_API_CALLS = new JLabel("API Calls");
        LABEL_API_CALLS.setHorizontalAlignment(SwingConstants.LEFT);
        LABEL_API_CALLS.setBounds(274, 10, 58, 16);
        TAB_INFO.add(LABEL_API_CALLS);

        DISPLAY_API_CALLS = new JTextField();
        DISPLAY_API_CALLS.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
        DISPLAY_API_CALLS.setHorizontalAlignment(SwingConstants.LEFT);
        DISPLAY_API_CALLS.setEditable(false);
        DISPLAY_API_CALLS.setBounds(344, 6, 211, 24);
        DISPLAY_API_CALLS.setColumns(9);
        TAB_INFO.add(DISPLAY_API_CALLS);

        LABEL_ORDERS = new JLabel("Orders");
        LABEL_ORDERS.setHorizontalAlignment(SwingConstants.LEFT);
        LABEL_ORDERS.setBounds(274, 154, 58, 16);
        TAB_INFO.add(LABEL_ORDERS);

        DISPLAY_ORDERS = new JTextField();
        DISPLAY_ORDERS.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
        DISPLAY_ORDERS.setHorizontalAlignment(SwingConstants.LEFT);
        DISPLAY_ORDERS.setEditable(false);
        DISPLAY_ORDERS.setBounds(344, 150, 211, 24);
        DISPLAY_ORDERS.setColumns(9);
        TAB_INFO.add(DISPLAY_ORDERS);

        DISPLAY_CANCELED = new JTextField();
        DISPLAY_CANCELED.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
        DISPLAY_CANCELED.setHorizontalAlignment(SwingConstants.LEFT);
        DISPLAY_CANCELED.setEditable(false);
        DISPLAY_CANCELED.setBounds(344, 188, 211, 24);
        DISPLAY_CANCELED.setColumns(9);
        TAB_INFO.add(DISPLAY_CANCELED);

        LABEL_CANCELED = new JLabel("Canceled");
        LABEL_CANCELED.setHorizontalAlignment(SwingConstants.LEFT);
        LABEL_CANCELED.setBounds(274, 192, 58, 16);
        TAB_INFO.add(LABEL_CANCELED);

        DISPLAY_PENDING = new JTextField();
        DISPLAY_PENDING.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
        DISPLAY_PENDING.setHorizontalAlignment(SwingConstants.LEFT);
        DISPLAY_PENDING.setEditable(false);
        DISPLAY_PENDING.setBounds(344, 228, 211, 24);
        DISPLAY_PENDING.setColumns(9);
        TAB_INFO.add(DISPLAY_PENDING);

        LABEL_PENDING = new JLabel("Pending");
        LABEL_PENDING.setHorizontalAlignment(SwingConstants.LEFT);
        LABEL_PENDING.setBounds(274, 232, 58, 16);
        TAB_INFO.add(LABEL_PENDING);

        DISPLAY_START_TIME = new JTextField();
        DISPLAY_START_TIME.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
        DISPLAY_START_TIME.setHorizontalAlignment(SwingConstants.LEFT);
        DISPLAY_START_TIME.setEditable(false);
        DISPLAY_START_TIME.setBounds(344, 78, 211, 24);
        DISPLAY_START_TIME.setColumns(9);
        TAB_INFO.add(DISPLAY_START_TIME);

        LABEL_START_TIME = new JLabel("Start");
        LABEL_START_TIME.setHorizontalAlignment(SwingConstants.LEFT);
        LABEL_START_TIME.setBounds(274, 82, 58, 16);
        TAB_INFO.add(LABEL_START_TIME);

        DISPLAY_LAST_ACTIVITY = new JTextField();
        DISPLAY_LAST_ACTIVITY
            .setFont(new Font("Lucida Grande", Font.PLAIN, 12));
        DISPLAY_LAST_ACTIVITY.setEditable(false);
        DISPLAY_LAST_ACTIVITY.setBounds(344, 42, 211, 24);
        TAB_INFO.add(DISPLAY_LAST_ACTIVITY);
        DISPLAY_LAST_ACTIVITY.setColumns(9);

        LABEL_LAST_ACTIVITY = new JLabel("Last");
        LABEL_LAST_ACTIVITY.setHorizontalAlignment(SwingConstants.LEFT);
        LABEL_LAST_ACTIVITY.setBounds(274, 46, 58, 16);
        TAB_INFO.add(LABEL_LAST_ACTIVITY);

        DISPLAY_DURATION = new JTextField();
        DISPLAY_DURATION.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
        DISPLAY_DURATION.setEditable(false);
        DISPLAY_DURATION.setBounds(344, 114, 211, 24);
        TAB_INFO.add(DISPLAY_DURATION);
        DISPLAY_DURATION.setColumns(9);

        LABEL_DURATION = new JLabel("Duration");
        LABEL_DURATION.setHorizontalAlignment(SwingConstants.LEFT);
        LABEL_DURATION.setBounds(274, 118, 58, 16);
        TAB_INFO.add(LABEL_DURATION);

        TAB_LOG = new JPanel();
        TAB_LOG.setLayout(null);
        PANEL_TAB.addTab("Log", null, TAB_LOG, null);

        SCROLLPANE = new JScrollPane();
        SCROLLPANE.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//        SCROLLPANE.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        SCROLLPANE.setBounds(6, 6, 855, 570);
        TAB_LOG.add(SCROLLPANE);

        DISPLAY_LOG = new JTextArea();
        DISPLAY_LOG.setFont(new Font("Monospaced", Font.PLAIN, 12));
        DISPLAY_LOG.setWrapStyleWord(true);
        DISPLAY_LOG.setLineWrap(true);
        DISPLAY_LOG.setEditable(false);
        DISPLAY_LOG.setAutoscrolls(true);
        SCROLLPANE.setViewportView(DISPLAY_LOG);

        TAB_ABOUT = new JPanel();
        TAB_ABOUT.setLayout(null);
        PANEL_TAB.addTab("About", null, TAB_ABOUT, null);

        TEXTPANE_ABOUT = new JTextPane();
        TEXTPANE_ABOUT.setEditable(false);
        TEXTPANE_ABOUT.setBackground(UIManager.getColor("Panel.background"));
        TEXTPANE_ABOUT.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
        TEXTPANE_ABOUT.setText("This program was created to automate"
            + " selling from the Cex.io platform. "
            + "This is freeware distributed under the MIT open-source"
            + " license. If this program has helped you at all, please"
            + " donate to motivate me to continue with development. All"
            + " feature/update suggestions are welcome on the projects "
            + "Github page (https://github.com/ME-Merlin2010/cex.io-seller).");
        TEXTPANE_ABOUT.setBounds(6, 6, 543, 112);
        TAB_ABOUT.add(TEXTPANE_ABOUT);

        TEXTPANE_BTC = new JTextPane();
        TEXTPANE_BTC.setEditable(false);
        TEXTPANE_BTC.setBackground(UIManager.getColor("Panel.background"));
        TEXTPANE_BTC.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
        TEXTPANE_BTC.setText("Motivation BTC @ 3JEh8HAT5qntRUtET9xnaWGyrKVExTQnXv (min. 0.0001 BTC)");
        TEXTPANE_BTC.setBounds(6, 130, 543, 22);
        TAB_ABOUT.add(TEXTPANE_BTC);

        TEXTPANE_CEX = new JTextPane();
        TEXTPANE_CEX.setEditable(false);
        TEXTPANE_CEX.setBackground(UIManager.getColor("Panel.background"));
        TEXTPANE_CEX.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
        TEXTPANE_CEX.setText("Cex.io referral @ https://cex.io/r/0/up114757661/0/");
        TEXTPANE_CEX.setBounds(6, 164, 543, 22);
        TAB_ABOUT.add(TEXTPANE_CEX);

        TEXTPANE_CRYPTSY = new JTextPane();
        TEXTPANE_CRYPTSY.setEditable(false);
        TEXTPANE_CRYPTSY.setBackground(UIManager.getColor("Panel.background"));
        TEXTPANE_CRYPTSY.setText("PayPal @ https://www.paypal.me/TobiasPahl");
        TEXTPANE_CRYPTSY.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
        TEXTPANE_CRYPTSY.setBounds(6, 232, 543, 22);
        TAB_ABOUT.add(TEXTPANE_CRYPTSY);

        TEXTPANE_SCRYPT = new JTextPane();
        TEXTPANE_SCRYPT.setText("Other donations accepted via email request!");
        TEXTPANE_SCRYPT.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
        TEXTPANE_SCRYPT.setEditable(false);
        TEXTPANE_SCRYPT.setBackground(UIManager.getColor("Panel.background"));
        TEXTPANE_SCRYPT.setBounds(6, 198, 543, 22);
        TAB_ABOUT.add(TEXTPANE_SCRYPT);
    }

    /**
     * Load settings from 'settings.txt' file, if it exists.
     * 
     * settings.txt contents:
     * username, apiKey, apiSecret, btcActive, btcReserve, btcMax, btcMin, btcMinAmount
     */
    private void loadSettings() {
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

                // ignore first 3 inputs
                // username,apiKey,apiSecret
                CHECKBOX_COIN.setSelected(Boolean.valueOf(settings[3]));
                INPUT_RESERVE_COIN.setText(settings[4]);
                INPUT_MIN_COIN.setText(settings[5]);
                INPUT_MAX_COIN.setText(settings[6]);
                INPUT_MIN_AMOUNT_COIN.setText(settings[7]);
                user.out("Dashboard-Settings loaded successfully!");
            } catch (FileNotFoundException e) {
                user.out("Error while loading settings: ", e.getMessage());
                e.printStackTrace();
            }
        } else {
        	user.out("No settings.txt found, no settings loaded.");
        }
        
    } 

    /**
     * Write settings from GUI to user Object.
     */
    private void updateSettings() {
        this.user.coin.active = CHECKBOX_COIN.isSelected();
        this.user.coin.reserve = new BigDecimal(INPUT_RESERVE_COIN.getText()).setScale(8, RoundingMode.DOWN);
        this.user.coin.min = new BigDecimal(INPUT_MIN_COIN.getText()).setScale(8, RoundingMode.DOWN);
        this.user.coin.max = new BigDecimal(INPUT_MAX_COIN.getText()).setScale(8,RoundingMode.DOWN);
        this.user.coin.minAmount = new BigDecimal(INPUT_MIN_AMOUNT_COIN.getText()).setScale(8,RoundingMode.DOWN);
    }
}
