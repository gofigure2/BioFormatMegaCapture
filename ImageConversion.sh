#!/bin/bash

LEN=$(echo ${#1})
LAST=$(($LEN-3))
EXT=$(echo $1 | cut -c $(($LEN-2))-) 
FILENAME=$(echo ${1%.*}) #filename path
echo $EXT
echo $FILENAME



./../BioFormatsTools/bfconvert $1 image-PL00-CO00-RO00-ZT00-YT00-XT00-TM%t-ch%c-zs%z.png

 
./../BioFormatsTools/showinf -nopix $1 > $FILENAME_metadata.txt


./metadataConversion.sh $FILENAME_metadata.txt $FILENAME $EXT



mkdir $FILENAME
mv $FILENAME.meg $FILENAME*.png $FILENAME


