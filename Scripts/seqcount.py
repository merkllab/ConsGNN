#!/usr/bin/env python
# -*- coding: utf-8 -*-

import argparse
import networkxgmml
import sys

# This script is automatically called by step2_3.py.

def setmax(scnt,max,i1,node,pn):
    for i in range(i1,10):
	if scnt > max[i]:
	    tmp = max[i]
	    tNode = node[i]
	    max[i] = scnt
	    node[i] = pn
	    scnt = 0
	    if i < 10:
		setmax(tmp,max,i+1,node,tNode)

def _main(argv):
    str = "".join(argv)
    parser = argparse.ArgumentParser(description="get seqcount")
    parser.add_argument('XGMML', default=file(str), help='XGMML file default: %(default)s', nargs='?', type=argparse.FileType('r'))
    options = parser.parse_args()

    g = networkxgmml.XGMMLReader(options.XGMML)

    print '# of edges', len(g.edges())
    print '# of nodes', len(g.nodes())

    max = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    node = ["", "", "", "", "", "", "", "", "", ""]
    sum = 0
    for onenode in g.nodes():
        scnt = 0
	if onenode[0] == 'P':
	    scnt = g.node[onenode]["SeqCount"]
	    setmax(scnt,max,0,node,onenode)
	sum += scnt

    for i in range(0,10):
	print node[i],max[i]
    print "sum =",sum

if __name__ == '__main__':
    _main(sys.argv[1:])

