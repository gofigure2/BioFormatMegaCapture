#!/bin/bash
#Set Path1 and Path2 variables correctly
Path1="/home/krm15/data/Kishore/KishoreFengzhu/110911_forKishoreSomiteTimelapseGC/110911_session_2011_11_09__23_52_45/megacapture"
Path2="/home/krm15/data/Kishore/KishoreFengzhu/110911_forKishoreSomiteTimelapseGC/110911_session_2011_11_09__23_52_45/newmegacapture"


for ch in {0,1}
do
  for t in {0,1}
  do
    Files="${Path1}/*-TM0${t}*-ch0${ch}*.png"
    for a in ${Files}
    do
      filename=${a##*/}

      cmd="convert -flip ${Path1}/${filename} ${Path2}/${filename}";

      echo $cmd
      eval $cmd
    done
  done
done

