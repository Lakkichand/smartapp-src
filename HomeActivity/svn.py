import os
os.system("svn up src")
os.system("svn up res")
os.system("svn up libs")
os.system("svn up AndroidManifest.xml")
data = os.popen("svn st").readlines()
modify =[]
add =[]
for i in data:
    if ".idea" not in i:
        if "bin" not in i:
            if "out" not in i:
                if "gen" not in i:
                    if "pro" not in i:
                        if "build" not in i:
                            if ".gradle" not in i:
                                if "M" in i:
                                    modify.append(i[1:len(i)-1])
                                if "?" in i:
                                    add.append(i[1:len(i)-1])
                                if "A" in i:
                                    modify.append(i[1:len(i)-1])
modifystring=""
for x in modify:
    modifystring=modifystring+" "+x.strip()
if len(modifystring):
    os.system("svn ci" +modifystring+" -m 'xxxxx'")
addString = ""
for a in add:
    print a 
    addString = addString+" "+a.strip()
    if len(addString):
        print "A  "+addString
        os.system("svn add"+ addString)
        os.system("svn ci" +addString+" -m 'xxxxx'")
