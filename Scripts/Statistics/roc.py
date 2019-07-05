import sys
import re
import pdb as p
import string

import matplotlib.pyplot as plt

# This script determines the values nneded for a ROC-curve.
# Execution: python roc.py outpdbAB pathList

def calcPair(iprPf,path,threshold):
    f = open(path, 'rb')
    lines = f.read();
    lines = lines.split('\n')
    cnt = 0
    absN = 0
    pos = 0
    negN = []
    TP = 0
    FN = 0
    FP = 0
    TN = 0
    PFamFound = 0
    for line in lines:
	if "PF" in line:
	    parts = re.split(" ",line)
	    absN = parts[len(parts)-1]
	    if iprPf[1] in line:
		PFamFound += 1
	    	if int(absN) > int(pos):
		    pos = absN
	    else:
	    	negN.append(absN)
	if "sum" in line:
	    parts = re.split(" ",line)
	    sum = 0
	    sum = parts[len(parts)-1]
	    pro = (string.atof(pos) / string.atof(sum)) * 100
	    if pro > threshold:
		TP += 1
	    else:
		FN += 1
	    for i1 in range(0,len(negN)-1):
		pro = (string.atof(negN[i1]) / string.atof(sum)) * 100
		if pro > threshold:
		    FP += 1
		else:
		    TN += 1
    if PFamFound == 0:
	TP = 0
	FN = 0
	FP = 0
	TN = 0
    return (TP,FN,FP,TN,PFamFound)



def getPairs(iprLine,pfamLine,pairs):
    partsI = re.split(" ",iprLine);
    partsP = re.split(" ",pfamLine);
    cnt = 0
    for partI in partsI:
	if "IPR" in partI:
	    for partP in partsP:
		if "PF" in partP:
		    pairs.append((partI,partP))
		    cnt += 1
    return pairs
		

def readPositives(file):
    f = open(file, 'rb')
    lines = f.read();
    lines = lines.split('\n')
    pdbs = {}
    cnt = 0
    for i in range(0,len(lines)-1):
	pairs = []
	cnt = cnt + 1
	if ">>>" in lines[i]:
	    pdb = re.split(" |:",lines[i])
	    if "IPR" in lines[i+1]:
		pairs = getPairs(lines[i+1],lines[i+2],pairs)
	    if "IPR" in lines[i+2]:
		pairs = getPairs(lines[i+2],lines[i+1],pairs)
	    pdbs[pdb[1]] = pairs
    f.close()
    return pdbs

def findIPR(ipr,file):
    f = open(file, 'rb')
    lines = f.read();
    lines = lines.split('\n')
    f.close()
    parts = re.split("/",file)
    path = "/"
    for i1 in range(0,len(parts)-1):
	if parts[i1] != "":
	    path += parts[i1]+"/"
    for line in lines:
	if ipr[0] in line:
	    return path + line 
    return ""

def calcTPR_FPR(pdbs,threshold,fileL):
    (TP,FN,FP,TN) = (0,0,0,0)
    (TPs,FNs,FPs,TNs) = (0,0,0,0)
    (TPR,FPR) = (0,0)
    PFs,PFamFound = 0,0
    cnt = 0
    for p1 in pdbs:
	for ipr in pdbs.get(p1):
	    path = findIPR(ipr,fileL)
	    if path != "":
		(TP,FN,FP,TN,PFamFound) = calcPair(ipr,path,threshold)
 		cnt += 1
		TPs += TP
		FNs += FN
		FPs += FP
		TNs += TN
		PFs += PFamFound
	    else:
		continue
    TPR = float(TPs) / (float(TPs) + float(FNs))
    FPR = float(FPs) / (float(FPs) + float(TNs))
    print threshold, " TPR=",TPR, " (TP=",TPs, " FN=",FNs, ") FPR=",FPR, " (FP=",FPs, " TN=",TNs, ")"
    return (TPR,FPR)

def _main(argv):
    fileP = argv[0]
    fileL = argv[1]
    notPrinted = True
    pdbs = readPositives(fileP)
    threshold = 30.0 
    rocx = []
    rocy = []
    for i1 in range(0,100):
	for i2 in range(1,5):
	    (TPR,FPR) = calcTPR_FPR(pdbs,float(i1*800+i2)/800,fileL)
	    rocx.append(FPR)
	    rocy.append(TPR) 
    print "x: ",rocx
    print "y: ",rocy
    plt.plot(rocx, rocy)
    plt.show()


if __name__ == '__main__':
    _main(sys.argv[1:])

