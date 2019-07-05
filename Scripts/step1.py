import subprocess
import re

# The first script of the pipeline

CMD1="/usr/java/jdk1.8.0_121/bin/java"
CMD2="-Dfile.encoding=UTF-8"
CMD3="-classpath"
CMD4="./bin:/home/.gradle/caches/modules-2/files-2.1/commons-cli/commons-cli/1.4/c51c00206bb913cd8612b24abd9fa98ae89719b1/commons-cli-1.4.jar:/home/.gradle/caches/modules-2/files-2.1/org.apache.httpcomponents/httpclient/4.5.2/733db77aa8d9b2d68015189df76ab06304406e50/httpclient-4.5.2.jar:/home/.gradle/caches/modules-2/files-2.1/org.apache.httpcomponents/httpcore/4.4.4/b31526a230871fbe285fbcbe2813f9c0839ae9b0/httpcore-4.4.4.jar:/home/.gradle/caches/modules-2/files-2.1/org.apache.httpcomponents/httpmime/4.5.2/22b4c53dd9b6761024258de8f9240c3dce6ea368/httpmime-4.5.2.jar:/home/.gradle/caches/modules-2/files-2.1/org.apache.commons/commons-io/1.3.2/b6dde38349ba9bb5e6ea6320531eae969985dae5/commons-io-1.3.2.jar:/home/.gradle/caches/modules-2/files-2.1/com.googlecode.json-simple/json-simple/1.1.1/c9ad4a0850ab676c5c64461a05ca524cdfff59f1/json-simple-1.1.1.jar:/home/.gradle/caches/modules-2/files-2.1/commons-logging/commons-logging/1.2/4bfc12adfe4842bf07b657f0369c4cb522955686/commons-logging-1.2.jar"
CMD5="rgnn.ui"

DIR="/media/HD1/SSNs/pkg12"
Pkg="pkg12"
LIST="list_filter"
OUT="out_list"
TEMP="temp"
HOME="/home/"

def read(path):
    f = open(path, 'rb')
    lines = f.read()
    lines = lines.split('\n')
    f.close()
    return lines[0]

def append(path,data,n):
    f = open(path, 'wb')
    f.seek(int(n)*10)
    f.write(data)
    f.close()

def getLines(path):
    output = subprocess.Popen(["wc", "-l", path], stdout=subprocess.PIPE).communicate()[0]
    parts = re.split(" ",output)
    i = parts[0]
    return i

def _main():
    i = getLines(LIST)
    print "Anzahl ", i
    scp = HOME + Pkg + "/scpd"
    subprocess.call(["mkdir", "-p", scp])
    cnt = 0
    while i > 0:
	ipr = read(LIST)
	append(OUT, ipr, cnt)
	cnt += 1
	print ipr
	i = getLines(LIST)
	i = int(i) - 1
	l = subprocess.Popen(["tail", "-n" + str(i), LIST], stdout=subprocess.PIPE).communicate()[0]
	append(TEMP, l, 0)
	subprocess.call(["mv", TEMP, LIST])
	parts = re.split("_",ipr)
	projName = parts[0]
	subprocess.check_call([CMD1,CMD2,CMD3,CMD4,CMD5, "new", DIR + "/" + ipr, projName])
	print "filter..."
	subprocess.call([CMD1,CMD2,CMD3,CMD4,CMD5, "filter"])
	subprocess.call(["cp", HOME + projName + "/" + projName + "FIL.xgmml", HOME + "/" + Pkg + "/scpd"])
	subprocess.call(["mv", HOME + projName + "/", HOME + "/" + Pkg])
	f = None
	try:
	    f = open("exit", 'rb')
	except:
	    print ""
	if f != None:
	    print "Controlled cancel of step1 by exit file"
	    f.close()
	    return

if __name__ == '__main__':
    _main()

