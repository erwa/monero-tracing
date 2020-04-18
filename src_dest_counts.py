#!/usr/bin/env python3

from collections import OrderedDict

import pprint
import sys

def main():
    file = sys.argv[1]
    src_dest_map = {}
    dests = set()
    with open(file) as fh:
        for line in fh:
            parts = line.split(" ")
            src = parts[0]
            dest = parts[1]
            if src not in src_dest_map:
                src_dest_map[src] = set()
            src_dest_map[src].add(dest)
            dests.add(dest)

    sys.stderr.write('Num unique srcs: ' + str(len(src_dest_map)) + '\n')
    sys.stderr.write('Num unique dests: ' + str(len(dests)) + '\n')

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
