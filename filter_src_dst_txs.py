#!/usr/bin/env python3

from collections import OrderedDict

import pprint
import sys

# Applies S2.
# ./filter_src_dst_txs.py SrcDestTxsRingCTAll
# SrcDestTxsRingCTAll has format
# srcHash dstHash key1,key2,...
def main():
    file = sys.argv[1]

    # src -> {
    #   dst1 -> key1 key2 ...
    #   dst2 -> key3 key4 ...
    #   used_keys -> set()
    #   multi_used_keys -> set()
    # }
    src_dest_map = {}
    with open(file) as fh:
        for line in fh:
            parts = line.strip().split(" ")
            src = parts[0]
            dest = parts[1]
            keys = parts[2].split(',')
            if src not in src_dest_map:
                src_dest_map[src] = {
                    'used_keys': set(),
                    'multi_used_keys': set(),
                    # dest1: set()
                    # dest2: set()
                    # ... added below
                }
            entry = src_dest_map[src]
            entry[dest] = set(keys)
            for key in keys:
                if key in entry['used_keys']:
                    entry['multi_used_keys'].add(key)
                entry['used_keys'].add(key)

    dests = set()
    # remove dests that use multi_used keys (S2)
    for src in list(src_dest_map.keys()):
        entry = src_dest_map[src]
        # also removes 'multi_used_keys' from the dict
        multi_used_keys = entry.pop('multi_used_keys')
        del entry['used_keys']
        for dst in list(entry.keys()):
            keys = entry[dst]
            for key in keys:
                if key in multi_used_keys:
                    # dst contains a multi-used key, ignore dst
                    del entry[dst]
                    break

        # add remaining dests as unique dests
        for dst in entry:
            dests.add(dst)

        # remove srcs that then no longer have any dests
        if len(entry) == 0:
            del src_dest_map[src]

    # get all unique involved txns
    all_txs = set()
    for src, dest_map in src_dest_map.items():
        all_txs.add(src)
        for dest in dest_map:
            all_txs.add(dest)

    sys.stderr.write('Num unique srcs: ' + str(len(src_dest_map)) + '\n')
    sys.stderr.write('Num unique dests: ' + str(len(dests)) + '\n')
    sys.stderr.write('Num unique txs (src or dest): ' + str(len(all_txs)) + '\n')

    for src, dests in src_dest_map.items():
        for dest, keys in dests.items():
            sys.stdout.write(src + ' ' + dest + ' ')
            sys.stdout.write(','.join(keys))
            sys.stdout.write('\n')


if __name__ == '__main__':
    main()
