import os
data = os.popen("svn st src res lib").readlines()
modify =["AndroidManifest.xml"]
add =[]
delet=[]
for i in data:
    if "M" in i[0]:
        modify.append(i[1:len(i)-1])
    if "?" in i[0]:
        add.append(i[1:len(i)-1])
    if "A" in i[0]:
        modify.append(i[1:len(i)-1])
    if "D" in i[0]:
        delet.append(i[1:len(i)-1])
deString = ""
for x in delet:
    deString = deString+" "+x.strip()
if len(deString):
    print deString
    os.system("svn del"+deString)
os.system("svn up src")
os.system("svn up res")
os.system("svn up libs")
os.system("svn up AndroidManifest.xml")
modifystring=""
for x in modify:
    modifystring=modifystring+" "+x.strip()
if len(modifystring):
    os.system("svn ci" +modifystring+" -m 'xxxxx'")
addString = ""
for a in add:
    addString = addString+" "+a.strip()
    if len(addString):
        os.system("svn add"+ addString)
commitstring = ""
for a in add :
    commitstring = commitstring+" "+a.strip()
    os.system("svn ci" +commitstring+" -m 'xxxxx'")
