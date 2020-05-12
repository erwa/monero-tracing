# monero-tracing
## Analysis of internal transaction
This file is to clarify the transactions between our group members, i.e. with known transaction id, known view key and wallet address. 

### Prerequisites
* Python 3.6+
* Matplotlib

## Script execution
To execute the script, in command line run ```python get_transaction_distribution.py <url> <wallet csv>```, where ```<url>``` is the url of Monero Blockchain Explorer, ```http://127.0.0.1:8081``` if the explorer is set up locally. The ```<wallet csv>``` file is exported with [MyMonero](https://mymonero.com/), and is included in the repo: ```AHWallet.csv``` and ```RHWallet.csv```. You will also need a ```config.py``` file to store the wallet addresses and private view keys of the recipient and sender, with the variable name to be ```viewkey_in, viewkey_out, address_in, address_out```.

The output will be a figure named ```distribution.png``` including the time distribution of the real input, ranked 1-10, with the larger rank to be the newer input.