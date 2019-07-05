import sys
import re
import urllib2, urlparse, gzip
from StringIO import StringIO
from HTMLParser import HTMLParser

# The script generates a list of the PDB-IDs together with the Pfam- and InterPro-IDs 
# Executio: python pdbListFamGO.py pdblist_AB

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

class MyHTMLParser(HTMLParser):
    def __init__(self):
        HTMLParser.__init__(self)
        self.data = []
    def handle_starttag(self, tag, attrs):
        if tag == "a": 
           for name, value in attrs: 
               if name == "href": 
		   self.data.append(value)
    def handle_data(self, data):
	self.data.append(data)

def fetch(source):
    result = {}
    if hasattr(source, 'read'):
        return source
    request = urllib2.Request(source)
    request.add_header('Accept-encoding', 'gzip')
    opener = urllib2.build_opener(SmartRedirectHandler(), DefaultErrorHandler())
    f = opener.open(request)
    data = f.read()
    data = gzip.GzipFile(fileobj=StringIO(data)).read()
    f.close()
    return data

def readPDB(pdbN):
    pdbN = "".join(pdbN)
    str = "http://www.ebi.ac.uk/pdbe/entry/pdb/" + pdbN + "/analysis"
    print ">>> %s:" % pdbN
    html = fetch(str)
    parser = MyHTMLParser()
    result = parser.feed(html)
    result = parser.data
    parser.close()
    cnt = 0
    mask = 0
    output = ""
    for line in result:
	cnt = cnt + 1
	if "Chains:" in line or "Chain:" in line and len(line) < 10:
	    if len(output) > 0:
	    	print output
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
			 output = output + " " + p1
    print output
	

def _main(argv):
    file = "".join(argv)
    f = open(file, 'rb')
    lines = f.read();
    lines = re.split('\n',lines)
    cnt = 0
    for pdbN in lines:
	pdbN = ''.join(pdbN.split())
	if len(pdbN) == 4:
	    readPDB(pdbN)

if __name__ == '__main__':
    _main(sys.argv[1:]) 
