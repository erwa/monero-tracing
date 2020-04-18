#!/usr/bin/env python3

from collections import OrderedDict

import pprint
import sys

def main():
    file = sys.argv[1]
    src_dest_map = {}
    with open(file) as fh:
        for line in fh:
            parts = line.split(" ")
            src = parts[0]
            dest = parts[1]
            keys = parts[2].split(',')
            if src not in src_dest_map:
                src_dest_map[src] = {
                    'used_keys': set(),
                    'multi_used_keys': set(),
                }
            entry = src_dest_map[src]
            entry[dest] = set(keys)
            for key in keys:
                if key in entry['used_keys']:
                    entry['multi_used_keys'].add(key)
                entry['used_keys'].add(key)

    dests = set()
    # remove dests that use multi_used keys
    for src in list(src_dest_map.keys()):
        entry = src_dest_map[src]
        multi_used_keys = entry.pop('multi_used_keys')
        del entry['used_keys']
        for dst in list(entry.keys()):
            keys = entry[dst]
            for key in keys:
                if key in multi_used_keys:
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

    src_dest_counts = {}
    for src, dests in src_dest_map.items():
        num_dests = len(dests)
        if num_dests not in src_dest_counts:
            src_dest_counts[num_dests] = 0
        src_dest_counts[num_dests] += 1

    # print('Source destination counts:')
    # pp = pprint.PrettyPrinter(indent=2)
    # pp.pprint(src_dest_counts)
    sys.stderr.write('Writing out src dest counts\n')
    for num in sorted(src_dest_counts):
        print(str(num) + '\t' + str(src_dest_counts[num]))


if __name__ == '__main__':
    main()
