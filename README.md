#Cex.io Java Seller
The Java source files and executable for the Cex.io Seller. This is an opensource project under
the MIT license and utilized the Google Gson project licensed under the Apache V2 License, which can be found
at: gson/LICENSE.txt. This project is based on the cex.io-reinvestor of Zack Urben.  

## Contact
* Author    : Tobias Pahl
* Contact   : Tobias@virtual-pahl.de

### Support
If you would like to support the development of this project, please spread the word and donate!

* Motivation BTC    @ 3JEh8HAT5qntRUtET9xnaWGyrKVExTQnXv (minimum 0.0001 BTC)
* Cex.io referral   @ https://cex.io/r/0/up114757661/0/
* PayPal			@ https://www.paypal.me/TobiasPahl
* Other donations accepted via email request!

### Features
1. GUI/CLI version.
2. COIN Selection.
3. Reserve amounts.
4. Max/Min sell amounts.
5. Load/Save settings from/to file.
6. sell/error logging.

##How to use (GUI):
1. Download the [latest release](https://github.com/ME-Merlin2010/cex.io-seller/releases/).
2. Generate a Cex.io API key and API secret (https://cex.io/trade/profile).
     This key needs the following permissions, to enable full functionality:
  * Account Balance
  * Open Order
  * Place Order
  * Cancel Order 
3. Run the executable and login.
4. Set your desired settings, and Toggle Selling.

##How to use (CLI):
1. Download the executable (https://github.com/ME-Merlin2010/cex.io-seller/releases/).
2. Generate a Cex.io API key and API secret (https://cex.io/trade/profile).
    This key needs the following permissions, to enable full functionality:
  * Account Balance
  * Open Order
  * Place Order
  * Cancel Order 
3. Run the executable and give it your API information, then follow the prompt.

```
java -jar Seller_v1.0.jar username api_key api_secret
``` 

## Available Tabs
The following are the 'Tabs' available in the GUI, and their settings/definitions.

### Settings
This is the settings tab, you can configure your desired options currently supported by
this program.

```
Note, all numbers are limited to 6 places left of the decimal point, and 8 places right of the
decimal, as specified in the Cex.io Terms of Service (https://cex.io/tos). These values are not
validated, incorrect values may lead to program failure.
```

#### Coin

```
This checkbox, enables or disables each respective coin for use in the program.
```

#### Reserve

```
The Reserve sets the limit for the untradeable amount of each respective coin. Seller
will utilize all available funds that exceed the reserve, but leave the reserve.
```

#### Maximum

```
The Maximum sets the upper limit for what you are willing to sell 1 COIN. If the
current price for 1 COIN is greater than your specified Maximum, action will be taken.
```

#### Minimum

```
The Minimum sets the lower limit for what you are willing to sell 1 COIN. If the
current price for 1 COIN is lower than your specified Minimum, action will be taken.
```

#### Save Settings

```
Write your settings to a text file named 'settings.txt'. If this file exists upon program
start, it will load your credentials and setting from it. Note, when the Seller is
started, the settings will be automatically saved.
```

### Information
This tab is mostly statistics and debug information, but its interesting to see.

#### Balance

```
Your account balance for every available currency.
```

```
Note, the balance will not refresh until Seller restarts.
```

#### Start

```
The timestamp from when the program was started.
```

#### Orders

```
The total number of orders placed by the program.
```

#### Canceled

```
The total number of orders canceled by the program, due to time expirations.
```

```
Note, an order will be canceled, if not completed within ~60 seconds. This keeps funds
available for selling, if an order is placed during a price spike, and a new order will
be placed at the updated price.
```

#### Pending

```
The total number of current pending orders. An order will be removed from pending if
completed, or canceled due to time.
```

### Log
This tab is the Seller program output. Current actions are displayed in
this temporary log.

### About
This tab is information about the program. It includes the methods in which to
support the continuation of this project.
