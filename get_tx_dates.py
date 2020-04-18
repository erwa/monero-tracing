#!/usr/bin/env python3
# python3 get_tx_dates.py TxTsAll

import datetime
import sys
import time


def main():
    file = sys.argv[1]
    with open(file) as fh:
        for line in fh:
            t = time.gmtime(int(line))
            print(str(datetime.date(t.tm_year, t.tm_mon, t.tm_mday)))


if __name__ == '__main__':
    main()
