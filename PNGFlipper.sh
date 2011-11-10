#!/bin/bash
Path="/home/krm15/data/Nik/2011-05-20_EarData"

count=0;
i=1;

for b in {28,17,17,14,31,52,79,102,240,72}
do
  for (( a=0; a<$b; a++ ))
  do
    cmd="cp ${Path}/${i}/membrane/$a.mha ${Path}/membrane/${count}.mha";

    echo $cmd
    eval $cmd
    count=$((count+1));
done
  i=$((i+1));
done

