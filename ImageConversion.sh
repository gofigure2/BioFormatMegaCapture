#!/bin/bash

EXT=$(echo ${1#*.})
FILENAME=$(echo ${1%.*})
echo $EXT
echo $FILENAME

./metadataConversion.sh log.txt $FILENAME $EXT


./../BioFormatMegaCaptureConverter/bfconvert $1 image-PL00-CO00-RO00-ZT00-YT00-XT00-TM%t-ch%c-zs%z.png
 



mkdir $FILENAME
mv $FILENAME.meg image*.png $FILENAME

  
