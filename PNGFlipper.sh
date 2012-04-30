#!/bin/bash
#Set Path1 and Path2 variables correctly
Path1="/home/krm15/data/Kishore/KishoreRamil/2011_12_28/nmegacapture"
Path2="/home/krm15/data/Kishore/KishoreRamil/2011_12_28/nnmegacapture"


for ch in {0..0}
do
  for t in {0..9}
  do
    Files="${Path1}/*-TM000${t}*-ch0${ch}*.png"
    for a in ${Files}
    do
      filename=${a##*/}

      cmd="convert -flip ${Path1}/${filename} ${Path2}/${filename}";

      echo $cmd
      eval $cmd
    done
  done
done

for ch in {0,0}
do
  for t in {10..52}
  do
    Files="${Path1}/*-TM00${t}*-ch0${ch}*.png"
    for a in ${Files}
    do
      filename=${a##*/}

      cmd="convert -flip ${Path1}/${filename} ${Path2}/${filename}";

      echo $cmd
      eval $cmd
    done
  done
done

