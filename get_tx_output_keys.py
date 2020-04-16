#!/usr/bin/env python3

import json
# import requests
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
    url = BASE_URL + 'transaction/' + tx_hash
    return json.loads(urllib.request.urlopen(url).read())


def process_tx(block_id, tx_hash):
    data = get_tx(tx_hash)
    tx_key_idx = {} # tx -> key -> input_idx
    outputs = data['data']['outputs']
    if len(outputs) > 1:
        keys = []
        for output in outputs:
            keys.append(output['public_key'])
        print(tx_hash, ','.join(keys))


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
