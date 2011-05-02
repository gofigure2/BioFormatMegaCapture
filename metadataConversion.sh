#!/bin/bash



cp MegaCaptureFormatFile.meg MegaCaptureFormat.meg

INPUTFILE=$1

MEGAFILENAME=$2

EXTENSION=$3



if [ "$EXTENSION" == "zvi" ]
then


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
	

	sed -e '/DimensionXT/d' -e '/DimensionYT/d' MegaCaptureFormat.meg > Meg.meg
	sed -e 's/\tSizeT =/SizeT:/g' -e 's/\tSizeZ =/SizeZ:/g' -e 's/\tSizeC =/SizeC:/g' -e 's/\tValid bits per pixel =/Valid bits per pixel:/g' $INPUTFILE > input.tmp
	

	mv Meg.meg MegaCaptureFormat.meg
		
	
	for i in {1..13}
	do

		sed "s/${ZVI[${i}]}:/${MEG[${i}]}/" input.tmp > zvi.tmp

		mv zvi.tmp input.tmp

	done

	for j in {1..13}
	do

		sed "s/$(grep ^${MEG[${j}]} MegaCaptureFormat.meg)/$(grep ^${MEG[${j}]} input.tmp)/" MegaCaptureFormat.meg > mega.tmp
		mv mega.tmp MegaCaptureFormat.meg
		
	done


	for k in {7..9}
	do
		DIM[${k}]=$(grep "${MEG[${k}]}" MegaCaptureFormat.meg  | cut -c 13-) 
        	
	done

	tail -7	MegaCaptureFormat.meg >	ImageInfo_ToUpdate.meg
	


	# 7 is TM, 8 is ZS, 9 is CH
	
	TM=$((${DIM[7]}-1))
	ZS=$((${DIM[8]}-1))
	CH=$((${DIM[9]}-1))

	#first is TM
	for t in $(eval echo {0..${TM}})
	do

		#second is CH
		for c in $(eval echo {0..${CH}})
		do
		
			#third is ZS
			for z in $(eval echo {0..${ZS}})
			do


				echo -e "\
<Image>\n\
Filename image-PL00-CO00-RO00-ZT00-YT00-XT00-TM$(printf %4.4u $t)-ch$(printf %2.2u $c)-zs$(printf %4.4u $z).png\n\
DateTime 2009-11-05 09:44:11\n\
StageX 1000\n\
StageY -1000\n\
Pinhole 44.216\n\
</Image>" > loopedMegaCaptureFormat.meg
				
				cat MegaCaptureFormat.meg loopedMegaCaptureFormat.meg > new.meg

				mv new.meg MegaCaptureFormat.meg
				
			done
                    
		done

	done


	stat -c "%y" log.txt | cut -c 1-19 > DATETIME.txt
	sed "s/^DateTime.*$/DateTime $(cat DATETIME.txt)/g" MegaCaptureFormat.meg > $2.meg

	rm loopedMegaCaptureFormat.meg ImageInfo_ToUpdate.meg input.tmp DATETIME.txt MegaCaptureFormat.meg   

fi
