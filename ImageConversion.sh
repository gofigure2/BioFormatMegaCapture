#!/bin/bash

EXT=$(echo ${1#*.})
FILENAME=$(echo ${1%.*}) #filename path
echo $EXT
echo $FILENAME



./BioFormatsTools/bfconvert $1 image-PL00-CO00-RO00-ZT00-YT00-XT00-TM%t-ch%c-zs%z.png

 
./BioFormatsTools/showinf -nopix $1 > $FILENAME_metadata.txt


./metadataConversion.sh $FILENAME_metadata.txt $FILENAME $EXT



mkdir $FILENAME
mv $FILENAME.meg image*.png $FILENAME


