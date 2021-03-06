# monero-tracing

Please see our write-up, [Alt-Coin Traceability](https://eprint.iacr.org/2020/593) for more details. This project was done as part of the final project for CMU's 17703/19733 Cryptocurrencies, Blockchains, and Applications class.


### Prerequisites for running code in this repo

* Java 9+
* Python 3.6+
* Git LFS
* [Monero Blockchain Explorer](https://github.com/moneroexamples/onion-monero-blockchain-explorer) running locally at http://127.0.0.1:8081 pointing to locally synced Monero blockchain


### Setting up Monero Blockchain Explorer

Use the Monero client to sync the Monero blockchain on your machine. Then follow the instructions at [onion-monero-blockchain-explorer](https://github.com/moneroexamples/onion-monero-blockchain-explorer) to build and run it.

When starting the blockchain explorer, make sure to enable the JSON API and mixin details:

```
./xmrblocks --enable-json-api --enable-mixin-details=1 -b /path/to/monero/blockchain/data/lmdb/
```


### Data and scripts overview

Most of the large generated data files (except InputKeysAll, which exceeds GitHub's 2 GB Git LFS file size limit) are checked in to this repo using Git LFS. To skip downloading the data files when cloning the repo, use

```
GIT_LFS_SKIP_SMUDGE=1 git clone git@github.com:erwa/monero-tracing.git
```

To generate InputKeysAll, run

```
java GetAllInputKeys TxHashesAll > InputKeysAll
```

The generated data looks at blocks 0 through 2077094.

A brief description of the main files is given below.


#### Common Files

* TxHashesAll - a list of all the transaction hashes for the blocks analyzed. Generated using [GetAllTxs.java](GetAllTxs.java). In the following descriptions, `txId` is an index that maps to line number (txId - 1) of this file.

* InputKeysAll - a list of all the transaction output (TXO) public keys used as inputs in the above transactions. Generated using [GetAllInputKeys.java](GetAllInputKeys.java). In the following descriptions, `keyId` is an index that maps to line number (keyId - 1) of this file.

* TxInputsAll - each line is "txId inputIdx keyId1 keyId2 ...", where `inputIdx` is the `inputIdx`th input (0-indexed) of the transaction. We use `inputIdx` to mean the same thing in the following descriptions. Generated using [GetTxInputs.java](GetTxInputs.java).

* [Utils.java](Utils.java) - utilities for loading and getting info about transactions and inputs and converting between hashes and integer ids.


#### Zero-Mixin Chain Analysis

* TracedInputsAllWithKeys - This file lists fully-deduced inputs (anonymity set reduced to 1). Each line has the format `txId inputIdx keyId`, where `keyId` is the deduced real input. Generated using [TraceTxs.java](TraceTxs.java).

* InputsReduced - same format as TxInputsAll, after running zero-mixin chain analysis and removing spent outputs. Generated using [TraceTxsReduceAnonSet.java](TraceTxsReduceAnonSet.java).

* InputsReducedX - same as InputsReduced, after simulating a breach of fraction X of remaining non-fully-deduced inputs (those inputs for which the anonymity set is still larger than 1). Generated using [TraceTxsSimBreach.java](TraceTxsSimBreach.java).

* AnonSetSizes - for inputs with 0 to 10 mixins, the number of such inputs with anonymity set sizes of 1 through (numMixins + 1). Generated using [ParseAnonSetData.java](ParseAnonSetData.java).

* AnonSetSizesX - same as AnonSetSizes, except after simulating a breach of a fraction X of non-fully-deduced inputs. Generated using [ParseAnonSetDataWith0.java](ParseAnonSetDataWith0.java).

* TxTsAll - each line is the timestamp in seconds since epoch when the transaction on the corresponding line of TxHashesAll occurred. Generated using [GetTxTimestamps.java](GetTxTimestamps.java).

* TxDatesAll - each line is the date (yyyy-mm-dd) when the transaction on the corresponding line of TxHashesAll occurred. Generated using [get_tx_dates.py](get_tx_dates.py).

* FullyTracedTxs - list of `txId`s of fully-traced transactions (all inputs deducible). Generated using [GetFullyTracedTxs.java](GetFullyTracedTxs.java).

* DayFullyTraced.tsv - each line is `yyyy-mm-dd <fraction_of_txns_fully_traced>`. Generated using [GetFullyTracedByDay.java](GetFullyTracedByDay.java).

* PartiallyTracedTxs - list of `txId`s of partially-traced transactions (at least one input was deducible). Generated using [GetPartiallyTracedTxs.java](GetPartiallyTracedTxs.java).

* DayPartiallyTraced.tsv - each line is `yyyy-mm-dd <fraction_of_txns_partially_traced>` (partially traced means at least one of the inputs was deducible). Generated using [GetPartiallyTracedByDay.java](GetPartiallyTracedByDay.java).

* ZeroMixinTxns - list of `txId`s of transactions with 0 mixins. Generated using [GetZeroMixinTxns.java](GetZeroMixinTxns.java).

* DayZeroMixins.tsv - each line is `yyyy-mm-dd <fraction_of_txns_with_zero_mixins>`. Generated using [GetZeroMixinsByDay.java](GetZeroMixinsByDay.java).


#### Guess-Newest Heuristic

* NewestGroupedBefore201701 - counts how many inputs with X mixins prior to 201701 were traceable (anonymity set reduced to 1) and for which the guess-newest heuristic is correct. Note that inputs with 10+ mixins were all lumped together in the "10 mixins" bucket. Generated using [CalculateNewestSuccessByNumMixinsBefore.java](CalculateNewestSuccessByNumMixinsBefore.java).

* NewestGroupedAfter201701 - same as NewestGroupedBefore201701, except for inputs from 201701 or later. Generated using [CalculateNewestSuccessByNumMixinsStartingFrom.java](CalculateNewestSuccessByNumMixinsStartingFrom.java).

* [CalculateNewestSuccessByNumMixinsStartingFromTxType.java](CalculateNewestSuccessByNumMixinsStartingFromTxType.java) calculates stats on the accuracy of the guess-newest heuristic after some tx id, broken down by tx type, where 1 = known inputs and outputs, 2 = some known inputs but hidden outputs, and 3 = hidden inputs and ouputs.

* [CountTracedTypesAfter.java](CountTracedTypesAfter.java) counts the number of transactions and inputs for each transaction type, where 1 means known inputs and ouputs, 2 means some known inputs but hidden outputs, and 3 means hidden inputs and outputs. Data for traced inputs in the RingCT era is in [TracedTypesAfter890806](TracedTypesAfter890806).

* To analyze the transactions between [AHwallet.csv](AHwallet.csv) and [RHWallet.csv](RHWallet.csv), please see [ANALYZE_INTERNAL_TRANSACTION.md](ANALYZE_INTERNAL_TRANSACTION.md).


#### Merging Outputs Heuristic

* TxOutputsRingCTAll - each line is `txHash txo1,txo2,...` where `txHash` is the transaction hash and each `txo` is a transaction output public key hash. This file only includes transactions after the start of RingCT (from 201701 onward) that have at least two outputs. Generated using [GetTxOutputs.java](GetTxOutputs.java).

* [FindSrcDestTxs.java](FindSrcDestTxs.java) reads `TxOutputsRingCTAll` and is used to output candidate source-destination transaction pairs, after filtering out S1, S3, and S4 from section 5.2 of the paper "A Traceability Analysis of Monero's Blockchain" by Kumar et al. It also filters out an additional "S5", which is the case where different source-destination pairs overlap in the destination inputs. The output format is `srcTxHash dstTxHash txo1,txo2,...` where `srcTxHash` is the source transaction hash, `dstTxHash` is the destination transaction hash, and each `txo` is a transaction output public key hash.

* [filter_src_dst_txs.py](filter_src_dst_txs.py) is then applied on the output of [FindSrcDestTxs.java](FindSrcDestTxs.java) and filters out S2 from the Kumar et al. paper. The output format is the same as that of [FindSrcDestTxs.java](FindSrcDestTxs.java).

* [GetSrcDstTxTypes.java](GetSrcDstTxTypes.java) consumes the output of [filter_src_dst_txs.py](filter_src_dst_txs.py) and outputs the source and destination transaction types. The output format is `srcType dstType`, where `1` means a non-confidential transaction (all inputs and outputs known), `2` means a semi-confidential transaction (where some inputs are known but outputs are hidden), and `3` is a confidential transaction (where both inputs and outputs are hidden).

* [ParseDestIdxKey.java](ParseDestIdxKey.java) is used to parse the output of [filter_src_dst_txs.py](filter_src_dst_txs.py), converting hashes to ids and adding indices. The output format is `dstId idx keyId`. The output file is [DestKeysRingCTFiltered3](DestKeysRingCTFiltered3).

* [FilterTraced.java](FilterTraced.java) is used to filter [DestKeysRingCTFiltered3](DestKeysRingCTFiltered3) down to those inputs we have ground truth for, resulting in [DestKeysRingCTTraced](DestKeysRingCTTraced).

* [CountStatsTracedDstKeys.java](CountStatsTracedDstKeys.java) then reads [DestKeysRingCTTraced](DestKeysRingCTTraced) and generates stats on the number of correct and incorrect merging outputs guesses by number of mixins. Output file is [MergingOutputStats](MergingOutputStats).

* [GetTracedDstTxTypes.java](GetTracedDstTxTypes.java) reads [DestKeysRingCTTraced](DestKeysRingCTTraced) and outputs `srcType dstType` (types are 1, 2, or 3, as explained above).
