import subprocess
import re
import os

# This script contains steps 2 and 3 of the SW pipeline

CMD1="/usr/java/jdk1.8.0_121/bin/java"
CMD2="-Dfile.encoding=UTF-8"
CMD3="-classpath"
CMD4="./bin:/home/.gradle/caches/modules-2/files-2.1/commons-cli/commons-cli/1.4/c51c00206bb913cd8612b24abd9fa98ae89719b1/commons-cli-1.4.jar:/home/.gradle/caches/modules-2/files-2.1/org.apache.httpcomponents/httpclient/4.5.2/733db77aa8d9b2d68015189df76ab06304406e50/httpclient-4.5.2.jar:/home/.gradle/caches/modules-2/files-2.1/org.apache.httpcomponents/httpcore/4.4.4/b31526a230871fbe285fbcbe2813f9c0839ae9b0/httpcore-4.4.4.jar:/home/.gradle/caches/modules-2/files-2.1/org.apache.httpcomponents/httpmime/4.5.2/22b4c53dd9b6761024258de8f9240c3dce6ea368/httpmime-4.5.2.jar:/home/.gradle/caches/modules-2/files-2.1/org.apache.commons/commons-io/1.3.2/b6dde38349ba9bb5e6ea6320531eae969985dae5/commons-io-1.3.2.jar:/home/.gradle/caches/modules-2/files-2.1/com.googlecode.json-simple/json-simple/1.1.1/c9ad4a0850ab676c5c64461a05ca524cdfff59f1/json-simple-1.1.1.jar:/home/.gradle/caches/modules-2/files-2.1/commons-logging/commons-logging/1.2/4bfc12adfe4842bf07b657f0369c4cb522955686/commons-logging-1.2.jar"
CMD5="rgnn.ui"

INDIR="/media/HD1/GNNs/pkg12"
DIR="/home/"
Pkg="pkg12"

def setstr(path,str,num):
    f = open(path, 'wb')
    f.seek(num)
    f.write(str)

def subst(path,str):
    f = open(path, 'r+b')
    line = "initialString"
    while line != None:
       	line = f.readline()
    	if "IPR" in line:
	    ix = line.find("IPR")
	    f.seek(ix - len(line),1)
	    f.write(str)
	    f.close
	    return

def _main():
    output = subprocess.Popen(["ls", "-l", INDIR], stdout=subprocess.PIPE).communicate()[0]
    lines = output.split('\n')
    print lines
    for line in lines:
	if ".xgmml" in line:
	    if "coloredssn" in line:
		lx = line.split();
		File = lx[8]
		ix = File.find("IPR")
		project = File[ix:9]
		print "Project: " + project
	    elif "pfam_family_gnn" in line:
		lx = line.split();
		FilePfam = lx[8]
		projFP = File[ix:9]	
		if projFP != project:
		    print "colored file doesn't fit to pfam file"
		    return
		Dir = ''
		try:
		    Dir = subprocess.Popen(["ls", "-l", DIR + Pkg + "/" + project], stdout=subprocess.PIPE).communicate()[0]
		except:
		    print ""
		if Dir == '':
		    print "not found in " + Pkg
		    return
		try:
		    Dir = subprocess.Popen(["ls", "-l", DIR + project], stdout=subprocess.PIPE).communicate()[0]
		except:
		    print ""
		if Dir != '':
		    print "already available " + project
		    return
		subprocess.call(["mv", DIR + Pkg + "/" + project, DIR])
		subst(DIR + "current", project)
		print "creating rgnn for files: " + File + " " + FilePfam
		subprocess.call([CMD1,CMD2,CMD3,CMD4,CMD5, "gnn", INDIR + "/" + File,  INDIR + "/" + FilePfam])
		output = subprocess.Popen(["ls", DIR + project + "/" + project], stdout=subprocess.PIPE).communicate()[0]
		parts = output.split()
		rgnn=""
		for p in parts:
		    if "x.xgmml" in p:
			rgnn = p
			break
		print "file " + rgnn
		output = subprocess.Popen(["./seqcount.py", DIR + project + "/" + project + "/" + rgnn], stdout=subprocess.PIPE).communicate()[0]
		f = open(DIR + project +"/seqcount",'wb')
		f.write(output)
		f.close()
 		print DIR + project + "/seqcount ready"
		output = subprocess.Popen(["./seqcount3.py", DIR + project + "/" + project + "/" + rgnn], stdout=subprocess.PIPE).communicate()[0]
		f = open(DIR + project +"/seqcount3",'wb')
		f.write(output)
		f.close()		
		subprocess.call(["mv", DIR + project, DIR + Pkg + "/" + project])

if __name__ == '__main__':
    _main()

