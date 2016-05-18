**Howdy!**

This is Proto! The aim of this project is to provide a tool that can analyse jar files which are obfuscated, (e.g Minecraft!).

This project is very much in development, hopefully one day it will be the most magical tool around.


Usage (For generating data):
    `java -jar proto.jar <jarfile> <outputfile> <filter>`
    
  
Usage (For generating mappings):
    `java -jar proto.jar <data1json> <data2json>`
    
By default the filter is *, if you only want net.minecraft, use net.minecraft.


You can use the filter . to only include the root directory classes.

Where jarfile is the file you want to extract data from, for now it saves to output.json.

In the future we hope to have it so you can pass in a mapped jar and have it generate some sort of mapping so you can identify files 