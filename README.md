# monero-tracing

For CMU 17703 Cryptocurrencies, Blockchains, and Applications final project on alt coin traceability.


### Prerequisites for running code in this repo

* Java 9+
* Python 3.6+
* Git LFS
* [Monero Blockchain Explorer](https://github.com/moneroexamples/onion-monero-blockchain-explorer) running locally at http://127.0.0.1:8081 pointing to locally synced Monero blockchain


### Data overview

All the generated data files (except InputKeysAll, which exceeds GitHub's 2 GB Git LFS file size limit) are checked in to this repo using Git LFS. To skip downloading the data files when cloning the repo, use

```
GIT_LFS_SKIP_SMUDGE=1 git clone git@github.com:erwa/monero-tracing.git
```

To generate InputKeysAll, run

```
java GetAllInputKeys TxHashesAll > InputKeysAll
```

The generated data looks at blocks 0 through 2077094.

A brief description of the main data files and how they were generated is given below:

* TxHashesAll - a list of all the transaction hashes for the blocks analyzed. Generated using [GetAllTxs.java](GetAllTxs.java). In the following descriptions, `txId` is an index that maps to line number (txId - 1) of this file.
* InputKeysAll - a list of all the transaction output (TXO) public keys used as inputs in the above transactions
* TxInputsAll - each line is "txId inputIdx keyId1 keyId2 ...", where `txId` maps to line number (txId - 1) of TxHashesAll, `inputIdx` is the `inputIdx`th input (0-indexed) of the transaction, and each `keyId` maps to line number (keyId - 1) of InputKeysAll.
* InputsReduced - same format as TxInputsAll, after running zero-mixin chain analysis and removing spent outputs
* InputsReducedX - same as InputsReduced, after simulating a breach of fraction X of remaining non-fully-deduced inputs (those inputs for which the anonymity set is still larger than 1).
* NewestGroupedBefore201701 - counts how many inputs with X mixins prior to 201701 were traceable (anonymity set reduced to 1) and for which the guess-newest heuristic is correct. Note that inputs with 10+ mixins were all lumped together in the "10 mixins" bucket.
* NewestGroupedAfter201701 - same as NewestGroupedBefore201701, except for inputs from 201701 or later
* AnonSetSizes - for inputs with 0 to 10 mixins, the number of such inputs with anonymity set sizes of 1 through (numMixins + 1)
* AnonSetSizesX - same as AnonSetSizes, except after simulating a breach of a fraction X of non-fully-deduced inputs
* DestKeysRingCT - each line has the format `txId inputIdx keyId` where txId maps to line (txId - 1) of TxHashesAll, `inputIdx` is the `inputIdx`th input (0-indexed) of the transaction, and `keyId` maps to line number (keyId - 1) of InputKeysAll.
* TracedInputsAllWithKeys - each line has the format `txId inputIdx keyId`