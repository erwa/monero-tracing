#!/usr/bin/env python3

from collections import OrderedDict

import pprint
import sys


def process_src(src, parent, parent_depth, src_dest_map, txn_map):
    if src not in txn_map:
        txn_map[src] = {'depth': parent_depth + 1, 'parent': parent}
    else:
        entry = txn_map[src]
        if entry['depth'] >= parent_depth + 1:
            # already found chain of same or longer length
            return
        entry['depth'] = parent_depth + 1
        entry['parent'] = parent

    if src in src_dest_map:
        entry = src_dest_map[src]
        for dst in entry:
            process_src(dst, src, parent_depth + 1, src_dest_map, txn_map)
    else: # end of chain
        txn_map[src]['end'] = True


def find_src_dst_chains(src_dest_map):
    txn_map = {}
    for src in src_dest_map:
        if src in txn_map:
            # already processed through chain
            continue

        # otherwise, start of chain
        process_src(src, None, 0, src_dest_map, txn_map)

    return txn_map


def gen_depth_count_map(txn_map):
    depth_count_map = {}
    for txn, entry in txn_map.items():
        if 'end' in entry:
            depth = entry['depth']
            if depth not in depth_count_map:
                depth_count_map[depth] = 0
            depth_count_map[depth] += 1

    return depth_count_map


def print_chain(txn, txn_map):
    print(txn)
    while True:
        txn = txn_map[txn]['parent']
        if not txn:
            break
        print(txn)


def print_longest_chain(txn_map, depth_count_map):
    deepest = sorted(depth_count_map)[-1]
    for txn, entry in txn_map.items():
        if entry['depth'] == deepest:
            print_chain(txn, txn_map)


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

    sys.stderr.write('Num unique srcs: ' + str(len(src_dest_map)) + '\n')
    sys.stderr.write('Num unique dests: ' + str(len(dests)) + '\n')
    txn_map = find_src_dst_chains(src_dest_map)
    depth_count_map = gen_depth_count_map(txn_map)
    print_longest_chain(txn_map, depth_count_map)


if __name__ == '__main__':
    main()
