#!/usr/bin/env python3

import json
import sys
import urllib.request

#BLOCK_START = 2000000
# BLOCK_START = 2037553
# BLOCK_END   = 2037653
BLOCK_START = 2076095
BLOCK_END   = 2077095
BASE_URL = 'http://127.0.0.1:8081/api/'


def get_block(block):
    url = BASE_URL + 'block/' + str(block)
    return json.loads(urllib.request.urlopen(url).read())


def get_tx(tx_hash):
    url = BASE_URL + 'detailedtransaction/' + tx_hash
    return json.loads(urllib.request.urlopen(url).read())


def process_tx(block_id, tx_hash):
    data = get_tx(tx_hash)
    tx_key_idx = {} # tx -> key -> input_idx
    inputs = data['data']['inputs']
    if inputs:
        for input_idx in range(len(inputs)):
            input_ = inputs[input_idx]
            seen_txns = set()
            for mixin in input_['mixins']:
                mix_tx = mixin['mix_tx_hash'][0]
                pub_key = mixin['mix_pub_key'][0]
                if mix_tx in seen_txns:
                    tx_key_idx[mix_tx] = False
                    continue
                else:
                    seen_txns.add(mix_tx)

                if mix_tx not in tx_key_idx:
                    tx_key_idx[mix_tx] = {}
                keys = tx_key_idx[mix_tx]
                if keys is False:  # tx marked as indeterminate
                    continue

                if pub_key in keys:
                    # same key used in multiple inputs
                    # heuristic won't work. ignore txn.
                    tx_key_idx[mix_tx] = False # False indicating heuristic won't work
                else:
                    keys[pub_key] = input_idx

    for src_tx_hash, keys in tx_key_idx.items():
        if keys is not False and len(keys) > 1: # more than one pub key from txn used
            idxs = set(keys.values())
            # print dest block, dest txn, src txn, dest input idxs
            # TODO: print pub_keys
            print(block_id, tx_hash, src_tx_hash, idxs)


def process_block(block):
    data = get_block(block)
    for tx in data['data']['txs']:
        process_tx(block, tx['tx_hash'])


def main():
    for block in range(BLOCK_START, BLOCK_END):
        sys.stderr.write('Processing block ' + str(block) + '\n')
        process_block(block)

if __name__ == '__main__':
    main()
