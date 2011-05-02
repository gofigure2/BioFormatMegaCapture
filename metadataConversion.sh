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
	

	mv Meg.meg MegaCaptureFormat.meg
		
	echo $1 $2 $3
	

	
	for i in {1..13}
	do

		echo "s/${ZVI[${i}]}:/${MEG[${i}]}/g"
		sed "s/${ZVI[${i}]}:/${MEG[${i}]}/" input.tmp > zvi.tmp

		mv zvi.tmp input.tmp

	done

	for j in {1..13}
	do

		echo "s/$(grep ^${MEG[${j}]} MegaCaptureFormat.meg)/$(grep ^${MEG[${j}]} input.tmp)/g"
		sed "s/$(grep ^${MEG[${j}]} MegaCaptureFormat.meg)/$(grep ^${MEG[${j}]} input.tmp)/" MegaCaptureFormat.meg > mega.tmp
		mv mega.tmp MegaCaptureFormat.meg
		echo $j
	done

	#cp MegaCaptureFormat.meg $2.meg

	for k in {7..9}
	do
		DIM[${k}]=$(grep "${MEG[${k}]}" MegaCaptureFormat.meg  | cut -c 13-) 
        	echo ${DIM[${k}]}
	done

	tail -7	MegaCaptureFormat.meg >	ImageInfo_ToUpdate.meg
	


	# 7 is TM, 8 is ZS, 9 is CH
	NUMIMAGES=$((${DIM[7]}+${DIM[8]}+${DIM[9]}))
	echo $NUMIMAGES
	
	D=$((${DIM[7]}-1))
	E=$((${DIM[8]}-1))
	F=$((${DIM[9]}-1))

	#first is TM
	for t in $(eval echo {0..${D}})
	do

		#second is CH
		for c in $(eval echo {0..${F}})
		do
		
			#third is ZS
			for z in $(eval echo {0..${E}})
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






			
#				echo $z
#				sed "s/zs0000/zs$(printf %4.4u $z)/" ImageInfo_ToUpdate.meg > ImageInfoTmp$z.meg 
#	      	                cat MegaCaptureFormat.meg ImageInfoTmp$z.meg > LongerMegaCaptureFormat.meg

				
				
#	                        mv LongerMegaCaptureFormat.meg MegaCaptureFormat.meg

			done

			

#			echo $c
#			sed "s/ch00/ch$(printf %2.2u $c)/" ImageInfoTmp1.meg > ImageInfoTmp2.meg

#                       cat MegaCaptureFormat.meg ImageInfoTmp2.meg > LongerMegaCaptureFormat.meg

                       # mv ImageInfoTmp.meg ImageInfo_ToUpdate.meg
                       # mv LongerMegaCaptureFormat.meg MegaCaptureFormat.meg


		done


#               echo $t
#               echo "s/TM0000/TM$(printf %4.4u $t)/"
#               sed "s/TM0000/TM$(printf %4.4u $t)/" ImageInfoTmp2.meg > ImageInfoTmp3.meg

#               cat MegaCaptureFormat.meg ImageInfoTmp3.meg > LongerMegaCaptureFormat.meg

                #mv ImageInfoTmp3.meg ImageInfo_ToUpdate.meg
#               mv LongerMegaCaptureFormat.meg MegaCaptureFormat.meg


	done



		# 7 is TM, 8 is ZS, 9 is CH

		#Need to duplicate


#cat MegaCaptureFormat.meg | echo "<Image>\n\
#	Filename image-PL00-CO00-RO00-ZT00-YT00-XT00-TM$(printf %4.4u $t)-ch$(printf %2.2u $c)-zs$(printf %4.4u $z).png\n\
#	DateTime 2009-11-05 09:44:11\n\
#	StageX 1000\n\
#	StageY -1000\n\
#	Pinhole 44.216\n\
#	</Image>\n" > loopedMegaCaptureFormat.meg

#with different TM0000-ch00-zs0000 looped
#		sed 's/
		

	stat -c "%y" log.txt | cut -c 1-19 > DATETIME.txt
	echo "s/^DateTime.*$/DateTime $(cat DATETIME.txt)/g"
	sed "s/^DateTime.*$/DateTime $(cat DATETIME.txt)/g" MegaCaptureFormat.meg > $2.meg

rm loopedMegaCaptureFormat.meg ImageInfo_ToUpdate.meg input.tmp DATETIME.txt MegaCaptureFormat.meg   

fi
