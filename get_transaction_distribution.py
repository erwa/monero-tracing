#!/usr/bin/env python3

import requests 
import json
import csv
import sys
import matplotlib.pyplot as plt
from config import viewkey_in, viewkey_out, address_in, address_out

try:
    url, wallet = sys.argv[0], sys.argv[1]
except:
    print("Usage: python get_transaction_distribution.py <url> <wallet csv>")

mixins = dict()
mixin_blocks = dict()
tx_outputs = []

with open(wallet, newline='') as csvfile:
    reader = csv.DictReader(csvfile)
    for row in reader:
        res = requests.get(url = url + 'transaction/' + row['tx_id'])
        data = res.json()
        for tx_in in data['data']['inputs']:
            rank = 0
            for mixin in tx_in['mixins']:
                mixins[mixin['public_key']] = rank
                rank += 1
        if float(row['amount']) > 0:
            out_url = url + 'outputs?txhash=' + row['tx_id'] + '&address=' + address_in + '&viewkey=' + viewkey_in
            res_out = requests.get(url=out_url)
            data_out = res_out.json()
            for output in data_out['data']['outputs']:
                if output['match']:
                    tx_outputs.append(output['output_pubkey'])
        else:
            res_out = requests.get(url=url + 'outputs?txhash=' + row['tx_id'] + '&address=' + address_out + '&viewkey=' + viewkey_out)
            data_out = res_out.json()
            for output in data_out['data']['outputs']:
                if output['match']:
                    tx_outputs.append(output['output_pubkey'])

tx_output_rank = dict()
for out in tx_outputs:
    if out in mixins:
        print(mixins[out])
        tx_output_rank[mixins[out]] = tx_output_rank.get(mixins[out], 0) + 1

ranks = list(tx_output_rank.keys())
print(sum(tx_output_rank))
tx_output_freq = {k: v / total for total in (sum(tx_output_rank.values()),) for k, v in tx_output_rank.items()}
freqs = list(tx_output_freq.values())

fig, axs = plt.subplots()
axs.bar(ranks, freqs)
fig.suptitle('Distribution of real input')
plt.savefig('distribution.png', dpi=300)

