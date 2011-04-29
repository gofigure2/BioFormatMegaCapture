#!/bin/bash

echo $1 $2 $3

cp MegaCaptureFormatFile.meg MegaCaptureFormat.meg

INPUTFILE=$1

MEGAFILENAME=$2

EXTENSION=$3



if [ "$EXTENSION" == "zvi" ]
then


	echo $1 $2 $3


	ZVI[1]="TimeInterval"
	#ZVI[1]="Recording #1 Objective"
	ZVI[2]="VoxelSizeX"
	ZVI[3]="VoxelSizeY"
        ZVI[4]="VoxelSizeZ"
	ZVI[5]="DimensionX"
	ZVI[6]="DimensionY"
	ZVI[7]="SizeT"
	ZVI[8]="SizeZ"
	ZVI[9]="SizeC"
	ZVI[10]="DataChannel #1 Color"
	ZVI[11]="DataChannel #2 Color"
	ZVI[12]="DataChannel #3 Color"
	ZVI[13]="Valid bits per pixel"

	 
	MEG[1]="TimeInterval"
        #MEG[1]="Objective"
        MEG[2]="VoxelSizeX"
        MEG[3]="VoxelSizeY"
        MEG[4]="VoxelSizeZ"
        MEG[5]="DimensionX"
        MEG[6]="DimensionY"
        MEG[7]="DimensionTM"
        MEG[8]="DimensionZS"
        MEG[9]="DimensionCH"
        MEG[10]="ChannelColor00"
        MEG[11]="ChannelColor01"
        MEG[12]="ChannelColor02"
        MEG[13]="ChannelDepth"


	echo $1	$2 $3 ${ZVI[2]} $INPUTFILE
	echo "s/${ZVI[1]}:/TimeInterval/g"        
	

	sed -e '/DimensionXT/d' -e '/DimensionYT/d' MegaCaptureFormat.meg > Meg.meg
	sed -e 's/\tSizeT =/SizeT:/g' -e 's/\tSizeZ =/SizeZ:/g' -e 's/\tSizeC =/SizeC:/g' -e 's/\tValid bits per pixel =/Valid bits per pixel:/g' $INPUTFILE > input.tmp


	cp Meg.meg MegaCaptureFormat.meg
		
	echo $1 $2 $3
	

	
	for i in {1..13}
	do

		echo "s/${ZVI[${i}]}:/${MEG[${i}]}/g"
		sed "s/${ZVI[${i}]}:/${MEG[${i}]}/" input.tmp > zvi.tmp

		cp zvi.tmp input.tmp

	done

	for j in {1..13}
	do

		echo "s/$(grep ^${MEG[${j}]} MegaCaptureFormat.meg)/$(grep ^${MEG[${j}]} input.tmp)/g"
		sed "s/$(grep ^${MEG[${j}]} MegaCaptureFormat.meg)/$(grep ^${MEG[${j}]} input.tmp)/" MegaCaptureFormat.meg > mega.tmp
		cp mega.tmp MegaCaptureFormat.meg
		echo $j
	done

	cp MegaCaptureFormat.meg $2.meg


	stat -c "%y" log.txt | sed 

fi


stat -c "%y" log.txt 
