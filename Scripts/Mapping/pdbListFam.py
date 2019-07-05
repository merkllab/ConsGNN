import sys
import re
import urllib2, urlparse, gzip
from StringIO import StringIO
from HTMLParser import HTMLParser

# The script generates a list of the PDB-IDs together with the Pfam- and InterPro-IDs 
# Execution: python pdbListFamGO.py pdblist_AB

class SmartRedirectHandler(urllib2.HTTPRedirectHandler):
    def http_error_301(self, req, fp, code, msg, headers):
        result = urllib2.HTTPRedirectHandler.http_error_301(
            self, req, fp, code, msg, headers)
        result.status = code
        return result

    def http_error_302(self, req, fp, code, msg, headers):
        result = urllib2.HTTPRedirectHandler.http_error_302(
            self, req, fp, code, msg, headers)
        result.status = code
        return result

class DefaultErrorHandler(urllib2.HTTPDefaultErrorHandler):
    def http_error_default(self, req, fp, code, msg, headers):
        result = urllib2.HTTPError(
            req.get_full_url(), code, msg, headers, fp)
        result.status = code
        return result

class FilterHTMLParser(HTMLParser):
    def __init__(self):
        HTMLParser.__init__(self)
        self.data = []
    def handle_starttag(self, tag, attrs):
        if tag == "a": 
            for name, value in attrs:  
               	if name == "href":  
		   self.data.append(value)
	if tag == "div":
	    for name, value in attrs:
		if name == "div" and value == "Family":
		   self.data.append(value)
    def handle_data(self, data):
      	self.data.append(data)

def fetch(source,zipped):
    result = {}
    if hasattr(source, 'read'):
        return source
    request = urllib2.Request(source)
    request.add_header('Accept-encoding', 'gzip')
    opener = urllib2.build_opener(SmartRedirectHandler(), DefaultErrorHandler())
    f = opener.open(request)
    data = f.read()
    if zipped:
    	data = gzip.GzipFile(fileobj=StringIO(data)).read()
    f.close()
    return data

def readGO(pdbN):		
    pdbN = "".join(pdbN)
    str = "http://www.ebi.ac.uk/pdbe/entry/pdb/" + pdbN + "/biology" 
    html = fetch(str,True)	
    parser = FilterHTMLParser()
    result = parser.feed(html)
    result = parser.data
    parser.close()
    cnt = 0
    state = 0
    goterm = ""
    for line in result:
	cnt = cnt + 1
	if "GO terms" in line:
	    state = 1
	if "Biochemical function:" in line and state == 1:
	    state = 2
	if "Biological process:" in line:
	    state = 0
	if state == 2 and "GO:" in line:
	    part = re.split(':',line)
	    goterm = part[2]
	    print "GO: ", goterm

def finalizeOutput(iprA,numIpr, famDone, output):
    if iprA[0] != "":
	if famDone == 0:
	    for i in range(0,numIpr):
	    	output = output + " " + iprA[i]  
    print output

def readChains(pdbN):
    pdbN = "".join(pdbN)
    str = "http://www.ebi.ac.uk/pdbe/entry/pdb/" + pdbN + "/analysis"
    print ">>> %s:" % pdbN
    html = fetch(str,True)
    parser = FilterHTMLParser()
    result = parser.feed(html)
    result = parser.data
    parser.close()
    cnt = 0
    mask = 0
    output = ""
    ipr = ""
    iprA = ["", "", "", "", "", "", "", "", "", ""]	
    numIpr = 0
    family = False
    famDone = 0
    for line in result:
	cnt = cnt + 1
	if "Chains:" in line or "Chain:" in line and len(line) < 10:
	    if len(output) > 0:
		finalizeOutput(iprA,numIpr,famDone,output)
		numIpr = 0
		iprA = ["", "", "", "", "", "", "", "", "", ""]
		famDone = 0
	    output = result[cnt]
	    output += " = "
	if "pfam.xfam.org" in line:
	    part = re.split('/',line) 
	    for p1 in part:
		if "PF" in p1:
		    output+=" "+p1
        if "interpro" in line:
	    if "superfamily" in line:
		mask = 1
		continue
	    if mask == 1:
		mask = 0
		continue
	    if "www.ebi.ac.uk" in line:
		part = re.split('/|"',line)
		for p1 in part:
		    if "IPR" in p1:
			ipr = p1
			iprA[numIpr] = p1
			if numIpr < 9:	
			    numIpr = numIpr + 1
		part2 = re.split('"',line)
		for p1 in part2:
		    if "http:" in p1:
			iprHtml = fetch(p1,False)
    			parser1 = FilterHTMLParser()
    			res = parser1.feed(iprHtml)
    			res = parser1.data
    			parser1.close()
			for line in res:
			    if "Family" in line and len(line) == 6:
				family = True
			    if "Homologous Superfamily" in line:
				numIpr = numIpr - 1
		if family and ipr != "":
		    output = output + " " + ipr
		    family = False
		    ipr = ""
		    famDone = 1
    finalizeOutput(iprA,numIpr,famDone,output)

def _main(argv):
    file = "".join(argv)
    f = open(file, 'rb')
    lines = f.read();
    lines = re.split('\n',lines)
    for pdbN in lines:
	pdbN = ''.join(pdbN.split())
	if len(pdbN) == 4:
	    readChains(pdbN)
	    readGO(pdbN)

if __name__ == '__main__':
    _main(sys.argv[1:]) 

