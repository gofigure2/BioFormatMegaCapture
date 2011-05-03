#!/bin/bash

#This function take input metadata and converts it to MegaCapture metadata formatted file.

cp MegaCaptureFormatFile.meg MegaCaptureFormat.meg

INPUTFILE=$1

MEGAFILENAME=$2

EXTENSION=$3

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


if [ "$EXTENSION" == "lsm" ]
then


	LSM[1]="TimeInterval"
	#LSM[1]="Recording #1 Objective"
	LSM[2]="VoxelSizeX"
	LSM[3]="VoxelSizeY"
        LSM[4]="VoxelSizeZ"
	LSM[5]="DimensionX"
	LSM[6]="DimensionY"
	LSM[7]="SizeT"
	LSM[8]="SizeZ"
	LSM[9]="SizeC"
	LSM[10]="DataChannel #1 Color"
	LSM[11]="DataChannel #2 Color"
	LSM[12]="DataChannel #3 Color"
	LSM[13]="Valid bits per pixel"  
	

	# Delete parameters that get confused with DimensionX and DimensionY

	sed -e 's/DimensionXT/DXT/g' -e 's/DimensionYT/DYT/g' -e 's/DimensionZT/DZT/g' MegaCaptureFormat.meg > Meg.meg

	# Rearrange SizeT, SizeZ, SizeC and Valid bits per pixel to fit format of rest of parameters
	sed -e 's/\tSizeT =/SizeT:/g' -e 's/\tSizeZ =/SizeZ:/g' -e 's/\tSizeC =/SizeC:/g' -e 's/\tValid bits per pixel =/Valid bits per pixel:/g' $INPUTFILE > input.tmp


 

#elif [ "$EXTENSION" == "zvi" ]
#then

    	EXT[1]="TimeInterval"
        #EXT[1]="Recording #1 Objective"
        EXT[2]="VoxelSizeX"
        EXT[3]="VoxelSizeY"
        EXT[4]="VoxelSizeZ"
        EXT[5]="DimensionX"
        EXT[6]="DimensionY"
        EXT[7]="SizeT"
        EXT[8]="SizeZ"
        EXT[9]="SizeC"
        EXT[10]="DataChannel #1 Color"
        EXT[11]="DataChannel #2 Color"
        EXT[12]="DataChannel #3 Color"
        EXT[13]="Valid bits per pixel"



	mv Meg.meg MegaCaptureFormat.meg
		
	
	for i in {1..13}
	do

		# Replaces elements in vector ending in colon with elements in vector MEG in tmp file input.tmp which is a tmp metadata format 	

		sed "s/${LSM[${i}]}:/${MEG[${i}]}/" input.tmp > ext.tmp

		mv ext.tmp input.tmp

	done

	for j in {1..13}
	do

		# Replaces lines in MegaCaptureFormat.meg with the converted lines in the tmp metadata format file input.tmp

		sed "s/$(grep ^${MEG[${j}]} MegaCaptureFormat.meg)/$(grep ^${MEG[${j}]} input.tmp)/" MegaCaptureFormat.meg > mega.meg
		mv mega.meg MegaCaptureFormat.meg
		
	done
	
	
	sed -e 's/DXT/DimensionXT/g' -e 's/DYT/DimensionYT/g' -e 's/DZT/DimensionZT/g' MegaCaptureFormat.meg > tmp.meg
	mv tmp.meg MegaCaptureFormat.meg


	for k in {7..9}
	do
	
		# Locates the parameters of Time points, z slice, and channel and keeps their parameter values in DIM

		DIM[${k}]=$(grep "${MEG[${k}]}" MegaCaptureFormat.meg  | cut -c 13-) 
        	
	done

	
	# 7 is TM, 8 is ZS, 9 is CH
	
	TM=$((${DIM[7]}-1))
	ZS=$((${DIM[8]}-1))
	CH=$((${DIM[9]}-1))

else
	echo "This is an unsupported file extension!!!"
fi

	#first is TM
	for t in $(eval echo {0..${TM}})
	do

		#second is CH
		for c in $(eval echo {0..${CH}})
		do
		
			#third is ZS
			for z in $(eval echo {0..${ZS}})
			do


				# prints these image lines with looped number for TM, ch, zs in 0001, 01, 0001 number format
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
				
				mv image-PL00-CO00-RO00-ZT00-YT00-XT00-TM$t-ch$c-zs$z.png $2-PL00-CO00-RO00-ZT00-YT00-XT00-TM$(printf %4.4u $t)-ch$(printf %2.2u $c)-zs$(printf %4.4u $z).png

			done
                    
		done

	done
	
	
	sed '/^$/d' MegaCaptureFormat.meg > tmp.tmp
	mv tmp.tmp MegaCaptureFormat.meg


	
	# aquires date and time of input metadata file and replaces old date and time everywhere in MegaCaptureFormat.meg 

	stat -c "%y" $1 | cut -c 1-19 > DATETIME.txt
	sed "s/^DateTime.*$/DateTime $(cat DATETIME.txt)/g" MegaCaptureFormat.meg > $2.meg

	rm loopedMegaCaptureFormat.meg input.tmp DATETIME.txt MegaCaptureFormat.meg   

		

