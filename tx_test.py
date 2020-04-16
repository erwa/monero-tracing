#!/usr/bin/env python3

import json
import sys
import urllib.request

#BLOCK_START = 2000000
BLOCK_START = 2037553
BLOCK_END   = 2037653
#BLOCK_START = 2037559
#BLOCK_END   = 2037560
BASE_URL = 'http://127.0.0.1:8081/api/'


def get_block(block):
    url = BASE_URL + 'block/' + str(block)
    return json.loads(urllib.request.urlopen(url).read())


def get_tx(tx_hash):
    url = BASE_URL + 'transaction/' + tx_hash
    return json.loads(urllib.request.urlopen(url).read())


def process_tx(block_id, tx_hash):
    data = get_tx(tx_hash)
    block_key_idx = {} # block -> key -> input_idx
    inputs = data['data']['inputs']
    if inputs:
        for input_idx in range(len(inputs)):
            input_ = inputs[input_idx]
            for mixin in input_['mixins']:
                block_no = mixin['block_no']
                pub_key = mixin['public_key']
                if block_no not in block_key_idx:
                    block_key_idx[block_no] = {}
                block = block_key_idx[block_no]
                if pub_key in block:
                    # same key used in multiple inputs
                    # heuristic won't work. ignore.
                    block[pub_key] = False # False indicating heuristic won't work
                else:
                    block[pub_key] = input_idx

    for block_no, block in block_key_idx.items():
        if len(block) > 1: # more than one pub key from block used
            # find all transactions the pub_keys are from
            tx_idx = {} # map from tx -> input_idx's

            # iter through transactions in block, add any containing a pub_key as output key
            data = get_block(block_no)
            for tx in data['data']['txs']:
                tx_cand = tx['tx_hash']
                tx_data = get_tx(tx_cand)
                for output in tx_data['data']['outputs']:
                    pub_key = output['public_key']
                    if pub_key in block:
                        idx = block[pub_key]
                        if idx is False:
                            continue

                        if tx_cand not in tx_idx:
                            tx_idx[tx_cand] = set()
                        tx_idxs = tx_idx[tx_cand]
                        if tx_idxs is False: # already invalidated
                            continue

                        if idx in tx_idxs:
                            # txn referenced multiple times in same input
                            # heuristic won't work
                            tx_idx[tx_cand] = False  # invalidate tx
                        else:
                            tx_idxs.add(idx)
            for tx, idxs in tx_idx.items():
                if idxs and len(idxs) > 1:
                    # print source txn, destination txn
                    # TODO: print pub_keys
                    print(block_id, tx_hash, block_no, tx, idxs)


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
