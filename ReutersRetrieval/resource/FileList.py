#-*- coding: utf-8 -*-

import os

base = r"/Users/Rex/Desktop/Reuters/"
fileHandle = open(r"/Users/Rex/Desktop/Reuters/FileList.txt", "a")
for i in range(0, 30000):
	diretory = base + str(i) + ".html"
	if os.path.exists(diretory):
		fileHandle.write(str(i) + "\n")
fileHandle.close()

