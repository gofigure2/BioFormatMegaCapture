#if defined(_MSC_VER)
#pragma warning ( disable : 4786 )
#endif

#include <iostream>
#include <fstream>
#include "itkSCIFIOImageIO.h"
#include "itkImageFileReader.h"
#include "itkImageFileWriter.h"
#include "itkImage.h"
#include "itkRGBPixel.h"
#include "itkMetaDataObject.h"
#include "itkExtractImageFilter.h"
#include <itkImageIORegion.h>
#include "itkNumericSeriesFileNames.h"
#include "itkImageSeriesWriter.h"
#include "itkFlipImageFilter.h"
#include "itkFileTools.h"
#include <sys/types.h>
#include <sys/stat.h>

#if defined(ITK_USE_MODULAR_BUILD)
  #define SPECIFIC_IMAGEIO_MODULE_TEST
#endif

template < class PixelType >
int Process3DImage ( char* filename )
{
  const unsigned int Dimension = 3;
  const unsigned int RDimension = 3;

  typedef itk::Image< PixelType, Dimension > ImageType;
  typedef itk::Image< PixelType, RDimension > RImageType;
  typedef itk::ImageFileReader<ImageType> ReaderType;
  typedef itk::ImageIORegion ImageIORegionType;
  typedef itk::ImageFileWriter<RImageType> WriterType;
  typedef itk::NumericSeriesFileNames NameGeneratorType;
  typedef itk::Image< PixelType, 2 > WriterImageType;
  typedef itk::ImageSeriesWriter< RImageType, WriterImageType> SeriesWriterType;
  typedef itk::FlipImageFilter< RImageType >  FlipFilterType;
  typedef typename FlipFilterType::FlipAxesArrayType  FlipAxesArrayType;

  FlipAxesArrayType flipArray;
  flipArray[0] = 0;
  flipArray[1] = 1;
  flipArray[2] = 0;

  std::string filename2 = filename;
  std::string folderName = filename2.substr( 0, filename2.length() - 4 );
  unsigned found = folderName.find_last_of("/\\");
  itk::FileTools::CreateDirectory( folderName.c_str() );
  std::string megFilename = folderName + "/" + folderName.substr(found+1) + ".meg";

  itk::SCIFIOImageIO::Pointer io = itk::SCIFIOImageIO::New();
  io->DebugOn();
  io->SetFileName( filename );
  io->ReadImageInformation();

  ImageIORegionType region(3);
  for( unsigned long i = 0; i < Dimension; i++ )
  {
    std::cout << "Setting index: " << i << " to: " << io->GetDimensions(i) << std::endl;
    region.SetIndex( i, 0 );
    region.SetSize( i, io->GetDimensions(i) );
  }

  unsigned int m_NumberOfTimePoints = 1;
  unsigned int m_NumberOfChannels   = 1;
  unsigned int m_NumberOfZSlices    = io->GetDimensions(2);

  double m_DimensionX = io->GetDimensions(0);
  double m_DimensionY = io->GetDimensions(1);
  double m_SpacingX   = 1000*io->GetSpacing(0);// LSM records in meters
  double m_SpacingY   = 1000*io->GetSpacing(1);
  double m_SpacingZ   = 1000*io->GetSpacing(2);
  double m_SpacingT   = 1;
  std::string m_FileType =  "png";

  std::ofstream file( megFilename.c_str() );
  file << "MegaCapture" << std::endl;
  file << "<ImageSessionData>" << std::endl;
  file << "Version 3.0" << std::endl;
  file << "ExperimentTitle " << std::endl;
  file << "ExperimentDescription " << std::endl;
  file << "TimeInterval " << m_SpacingT << std::endl;
  file << "Objective " << std::endl;
  file << "VoxelSizeX " << m_SpacingX << std::endl;
  file << "VoxelSizeY " << m_SpacingY << std::endl;
  file << "VoxelSizeZ " << m_SpacingZ << std::endl;
  file << "DimensionX " << m_DimensionX << std::endl;
  file << "DimensionY " << m_DimensionY << std::endl;
  file << "DimensionPL 1" << std::endl;
  file << "DimensionCO 1" << std::endl;
  file << "DimensionRO 1" << std::endl;
  file << "DimensionZT 1" << std::endl;
  file << "DimensionYT 1" << std::endl;
  file << "DimensionXT 1" << std::endl;
  file << "DimensionTM " << m_NumberOfTimePoints << std::endl;
  file << "DimensionZS " << m_NumberOfZSlices << std::endl;

  if ( m_NumberOfChannels == 1 )
  {
    file << "DimensionCH 1" << std::endl;
    int green = 65280;
    file << "ChannelColor00 " << green << std::endl;
  }
  else if( m_NumberOfChannels == 2 )
  {
    file << "DimensionCH 2" << std::endl;
    int green = 255;
    int blue = 0;
    file << "ChannelColor00 " << green * 256 + blue << std::endl;
    green = 0;
    blue = 255;
    file << "ChannelColor01 " << green * 256 + blue << std::endl;
  }
  else
  {
    file << "DimensionCH 3" << std::endl;
    int red = 255;
    int green = 0;
    int blue = 0;
    file << "ChannelColor00 " << red * 256 * 256 + green * 256 + blue << std::endl;
    red = 0;
    green = 255;
    blue = 0;
    file << "ChannelColor01 " << red * 256 * 256 + green * 256 + blue << std::endl;
    red = 0;
    green = 0;
    blue = 255;
    file << "ChannelColor02 " << red * 256 * 256 + green * 256 + blue << std::endl;
  }
  file << "ChannelDepth 8" << std::endl;
  file << "FileType PNG" << std::endl;
  file << "</ImageSessionData>" << std::endl;

  // XYZ 012
  itk::SCIFIOImageIO::Pointer imageIO = itk::SCIFIOImageIO::New();
  imageIO->DebugOn();
  imageIO->SetFileName( filename );
  imageIO->SetUseStreamedReading(true);
  imageIO->SetUseStreamedWriting(true);
  imageIO->SetIORegion( region );
  imageIO->ReadImageInformation();

  typename ReaderType::Pointer reader = ReaderType::New();
  reader->SetFileName( filename );
  reader->UseStreamingOn();
  reader->SetImageIO(imageIO);

  std::string m_SeriesFormat = megFilename.substr(0, megFilename.length()-4 ) + "-PL00-CO00-RO00-ZT00-YT00-XT00-TM0000-ch00" +
          "-zs%04d." + m_FileType;
  typename NameGeneratorType::Pointer nameGenerator3 = NameGeneratorType::New();
  nameGenerator3->SetSeriesFormat( m_SeriesFormat );
  nameGenerator3->SetStartIndex( 0 );
  nameGenerator3->SetEndIndex( m_NumberOfZSlices-1 );
  nameGenerator3->SetIncrementIndex( 1 );

  typename FlipFilterType::Pointer flipFilter = FlipFilterType::New();
  flipFilter->SetFlipAxes( flipArray );
  flipFilter->SetInput( reader->GetOutput() );
  flipFilter->Update();

  typename SeriesWriterType::Pointer series_writer = SeriesWriterType::New();
  series_writer->SetInput( flipFilter->GetOutput() );
  series_writer->SetFileNames( nameGenerator3->GetFileNames() );

  try
  {
    series_writer->Update();
  }
  catch (itk::ExceptionObject &e)
  {
    std::cerr << e << std::endl;
    return EXIT_FAILURE;
  }

  char timeStr[100] = "";
  struct stat buf;

  if ( !stat(filename, &buf) )
    {
    strftime( timeStr, 100, "%Y-%m-%d %H:%M:%S", localtime(&buf.st_mtime) );
    }

  for ( int j = 0; j < m_NumberOfZSlices; j++ )
  {
    std::string fname = nameGenerator3->GetFileNames()[j];
    unsigned found = fname.find_last_of("/\\");

    file << "<Image>" << std::endl;
    file << "Filename " << fname.substr(found+1) << std::endl;
    file << "DateTime " << timeStr << std::endl;
    file << "StageX 1000" << std::endl;
    file << "StageY -1000" << std::endl;
    file << "Pinhole 44.216" << std::endl;
    file << "</Image>" << std::endl;
  }
  file.close();

  return EXIT_SUCCESS;
}

template < class PixelType >
int Process4DImage ( char* filename )
{
  const unsigned int Dimension = 4;
  const unsigned int RDimension = 3;

  typedef itk::Image< PixelType, Dimension > ImageType;
  typedef itk::Image< PixelType, RDimension > RImageType;
  typedef itk::ImageFileReader<ImageType> ReaderType;
  typedef itk::ImageIORegion ImageIORegionType;
  typedef itk::ImageFileWriter<RImageType> WriterType;
  typedef itk::ExtractImageFilter<ImageType, RImageType> ExtractImageFilter;
  typedef itk::NumericSeriesFileNames NameGeneratorType;
  typedef itk::Image< PixelType, 2 > WriterImageType;
  typedef itk::ImageSeriesWriter< RImageType, WriterImageType> SeriesWriterType;
  typedef itk::FlipImageFilter< RImageType >  FlipFilterType;
  typedef typename FlipFilterType::FlipAxesArrayType  FlipAxesArrayType;

  FlipAxesArrayType flipArray;
  flipArray[0] = 0;
  flipArray[1] = 1;
  flipArray[2] = 0;

  std::string filename2 = filename;
  std::string folderName = filename2.substr( 0, filename2.length() - 4 );
  unsigned found = folderName.find_last_of("/\\");
  itk::FileTools::CreateDirectory( folderName.c_str() );
  std::string megFilename = folderName + "/" + folderName.substr(found+1) + ".meg";

  itk::SCIFIOImageIO::Pointer io = itk::SCIFIOImageIO::New();
  io->DebugOn();
  io->SetFileName(filename);
  io->ReadImageInformation();

  ImageIORegionType region(4);
  for( unsigned long i = 0; i < Dimension; i++ )
  {
    std::cout << "Setting index: " << i << " to: " << io->GetDimensions(i) << std::endl;
    region.SetIndex( i, 0 );
    region.SetSize( i, io->GetDimensions(i) );
  }

  unsigned int m_NumberOfTimePoints = io->GetDimensions(3);;
  unsigned int m_NumberOfChannels   = 1;
  unsigned int m_NumberOfZSlices    = io->GetDimensions(2);

  double m_DimensionX = io->GetDimensions(0);
  double m_DimensionY = io->GetDimensions(1);
  double m_SpacingX   = 1000*io->GetSpacing(0);// LSM records in meters
  double m_SpacingY   = 1000*io->GetSpacing(1);
  double m_SpacingZ   = 1000*io->GetSpacing(2);
  double m_SpacingT   = io->GetSpacing(3);
  std::string m_FileType =  "png";

  std::ofstream file( megFilename.c_str() );
  file << "MegaCapture" << std::endl;
  file << "<ImageSessionData>" << std::endl;
  file << "Version 3.0" << std::endl;
  file << "ExperimentTitle " << std::endl;
  file << "ExperimentDescription " << std::endl;
  file << "TimeInterval " << m_SpacingT << std::endl;
  file << "Objective " << std::endl;
  file << "VoxelSizeX " << m_SpacingX << std::endl;
  file << "VoxelSizeY " << m_SpacingY << std::endl;
  file << "VoxelSizeZ " << m_SpacingZ << std::endl;
  file << "DimensionX " << m_DimensionX << std::endl;
  file << "DimensionY " << m_DimensionY << std::endl;
  file << "DimensionPL 1" << std::endl;
  file << "DimensionCO 1" << std::endl;
  file << "DimensionRO 1" << std::endl;
  file << "DimensionZT 1" << std::endl;
  file << "DimensionYT 1" << std::endl;
  file << "DimensionXT 1" << std::endl;
  file << "DimensionTM " << m_NumberOfTimePoints << std::endl;
  file << "DimensionZS " << m_NumberOfZSlices << std::endl;

  if ( m_NumberOfChannels == 1 )
  {
    file << "DimensionCH 1" << std::endl;
    int green = 65280;
    file << "ChannelColor00 " << green << std::endl;
  }
  else if( m_NumberOfChannels == 2 )
  {
    file << "DimensionCH 2" << std::endl;
    int green = 255;
    int blue = 0;
    file << "ChannelColor00 " << green * 256 + blue << std::endl;
    green = 0;
    blue = 255;
    file << "ChannelColor01 " << green * 256 + blue << std::endl;
  }
  else
  {
    file << "DimensionCH 3" << std::endl;
    int red = 255;
    int green = 0;
    int blue = 0;
    file << "ChannelColor00 " << red * 256 * 256 + green * 256 + blue << std::endl;
    red = 0;
    green = 255;
    blue = 0;
    file << "ChannelColor01 " << red * 256 * 256 + green * 256 + blue << std::endl;
    red = 0;
    green = 0;
    blue = 255;
    file << "ChannelColor02 " << red * 256 * 256 + green * 256 + blue << std::endl;
  }

  file << "ChannelDepth 8" << std::endl;
  file << "FileType PNG" << std::endl;
  file << "</ImageSessionData>" << std::endl;

  // Track text file format
  typename NameGeneratorType::Pointer nameGenerator1 = NameGeneratorType::New();
  nameGenerator1->SetSeriesFormat( "-PL00-CO00-RO00-ZT00-YT00-XT00-TM%04d-ch00" );
  nameGenerator1->SetStartIndex( 0 );
  nameGenerator1->SetEndIndex( m_NumberOfTimePoints-1 );
  nameGenerator1->SetIncrementIndex( 1 );

  typename ReaderType::Pointer reader = ReaderType::New();
  reader->SetFileName(filename);
  reader->UseStreamingOn();

  typename WriterType::Pointer writer = WriterType::New();

  // XYZT 0123
  for( unsigned int i = 0; i < region.GetSize( 3 ); i++ )
  {
    std::cout << i << std::endl;

    itk::SCIFIOImageIO::Pointer imageIO = itk::SCIFIOImageIO::New();
    typename ExtractImageFilter::Pointer extractor = ExtractImageFilter::New();

    imageIO->DebugOn();
    imageIO->SetFileName(filename);
    imageIO->SetUseStreamedReading(true);
    imageIO->SetUseStreamedWriting(true);

    // reset to the largest possible region, so that the extraction region is contained
    imageIO->SetIORegion( region );

    imageIO->ReadImageInformation();

    reader->SetImageIO(imageIO);

    typename ImageType::IndexType start;
    typename ImageType::SizeType size;

    for( unsigned int j = 0; j < 4; j++ )
    {
      start[j] = 0;
      size[j] = io->GetDimensions(j);
    }

    start[ 3 ] = i;
    size[ 3 ] = 0;

    typename ImageType::RegionType desiredRegion;
    desiredRegion.SetSize(size);
    desiredRegion.SetIndex(start);

    extractor->SetExtractionRegion(desiredRegion);
    extractor->SetDirectionCollapseToIdentity();

    std::cout << "Region of extraction: " << extractor->GetExtractionRegion() << std::endl;

    extractor->SetInput(reader->GetOutput());
    extractor->Update();

    std::string m_SeriesFormat = megFilename.substr(0, megFilename.length()-4 ) + nameGenerator1->GetFileNames()[i] + "-zs%04d." + m_FileType;
    typename NameGeneratorType::Pointer nameGenerator3 = NameGeneratorType::New();
    nameGenerator3->SetSeriesFormat( m_SeriesFormat );
    nameGenerator3->SetStartIndex( 0 );
    nameGenerator3->SetEndIndex( m_NumberOfZSlices-1 );
    nameGenerator3->SetIncrementIndex( 1 );

    typename FlipFilterType::Pointer flipFilter = FlipFilterType::New();
    flipFilter->SetFlipAxes( flipArray );
    flipFilter->SetInput( extractor->GetOutput() );
    flipFilter->Update();

    typename SeriesWriterType::Pointer series_writer = SeriesWriterType::New();
    series_writer->SetInput( flipFilter->GetOutput() );
    series_writer->SetFileNames( nameGenerator3->GetFileNames() );
    series_writer->Update();

    char timeStr[100] = "";
    struct stat buf;

    if ( !stat(filename, &buf) )
      {
      strftime( timeStr, 100, "%Y-%m-%d %H:%M:%S", localtime(&buf.st_mtime) );
      }

    for ( int j = 0; j < m_NumberOfZSlices; j++ )
    {
      std::string fname = nameGenerator3->GetFileNames()[j];
      unsigned found = fname.find_last_of("/\\");

      file << "<Image>" << std::endl;
      file << "Filename " << fname.substr(found+1) << std::endl;
      file << "DateTime " << timeStr << std::endl;
      file << "StageX 1000" << std::endl;
      file << "StageY -1000" << std::endl;
      file << "Pinhole 44.216" << std::endl;
      file << "</Image>" << std::endl;
    }
  }
  file.close();

  return EXIT_SUCCESS;
}

template < class PixelType >
int Process5DImage ( char* filename )
{
  const unsigned int Dimension = 5;
  const unsigned int RDimension = 3;

  typedef itk::Image< PixelType, Dimension > ImageType;
  typedef itk::ImageFileReader<ImageType> ReaderType;
  typedef itk::ImageIORegion ImageIORegionType;
  typedef itk::NumericSeriesFileNames NameGeneratorType;

  typedef itk::Image< PixelType, RDimension > RImageType;
  typedef itk::ExtractImageFilter<ImageType, RImageType> ExtractImageFilter;
  typedef itk::ImageFileWriter<RImageType> WriterType;
  typedef itk::Image< PixelType, 2 > WriterImageType;
  typedef itk::ImageSeriesWriter< RImageType, WriterImageType> SeriesWriterType;
  typedef itk::FlipImageFilter< RImageType >  FlipFilterType;
  typedef typename FlipFilterType::FlipAxesArrayType  FlipAxesArrayType;

  FlipAxesArrayType flipArray;
  flipArray[0] = 0;
  flipArray[1] = 1;
  flipArray[2] = 0;

  std::string filename2 = filename;
  std::string folderName = filename2.substr( 0, filename2.length() - 4 );
  unsigned found = folderName.find_last_of("/\\");
  itk::FileTools::CreateDirectory( folderName.c_str() );
  std::string megFilename = folderName + "/" + folderName.substr(found+1) + ".meg";

  itk::SCIFIOImageIO::Pointer io = itk::SCIFIOImageIO::New();
  io->DebugOn();
  io->SetFileName(filename);
  io->ReadImageInformation();

  ImageIORegionType region( 5 );
  for( unsigned long i = 0; i < 5; i++ )
  {
    std::cout << "Setting index: " << i << " to: " << io->GetDimensions(i) << std::endl;
    region.SetIndex( i, 0 );
    region.SetSize( i, io->GetDimensions(i) );
  }

  unsigned int m_NumberOfTimePoints = io->GetDimensions(3);
  unsigned     m_NumberOfChannels   = io->GetDimensions(4);;
  unsigned int m_NumberOfZSlices    = io->GetDimensions(2);

  double m_DimensionX = io->GetDimensions(0);
  double m_DimensionY = io->GetDimensions(1);
  double m_SpacingX   = 1000*io->GetSpacing(0);// LSM records in meters
  double m_SpacingY   = 1000*io->GetSpacing(1);
  double m_SpacingZ   = 1000*io->GetSpacing(2);
  double m_SpacingT   = io->GetSpacing(3);;
  std::string m_FileType =  "png";

  std::ofstream file( megFilename.c_str() );
  file << "MegaCapture" << std::endl;
  file << "<ImageSessionData>" << std::endl;
  file << "Version 3.0" << std::endl;
  file << "ExperimentTitle " << std::endl;
  file << "ExperimentDescription " << std::endl;
  file << "TimeInterval " << m_SpacingT << std::endl;
  file << "Objective " << std::endl;
  file << "VoxelSizeX " << m_SpacingX << std::endl;
  file << "VoxelSizeY " << m_SpacingY << std::endl;
  file << "VoxelSizeZ " << m_SpacingZ << std::endl;
  file << "DimensionX " << m_DimensionX << std::endl;
  file << "DimensionY " << m_DimensionY << std::endl;
  file << "DimensionPL 1" << std::endl;
  file << "DimensionCO 1" << std::endl;
  file << "DimensionRO 1" << std::endl;
  file << "DimensionZT 1" << std::endl;
  file << "DimensionYT 1" << std::endl;
  file << "DimensionXT 1" << std::endl;
  file << "DimensionTM " << m_NumberOfTimePoints << std::endl;
  file << "DimensionZS " << m_NumberOfZSlices << std::endl;

  if ( m_NumberOfChannels == 1 )
  {
    file << "DimensionCH 1" << std::endl;
    int green = 65280;
    file << "ChannelColor00 " << green << std::endl;
  }
  else if( m_NumberOfChannels == 2 )
  {
    file << "DimensionCH 2" << std::endl;
    int green = 255;
    int blue = 0;
    file << "ChannelColor00 " << green * 256 + blue << std::endl;
    green = 0;
    blue = 255;
    file << "ChannelColor01 " << green * 256 + blue << std::endl;
  }
  else
  {
    file << "DimensionCH 3" << std::endl;
    int red = 255;
    int green = 0;
    int blue = 0;
    file << "ChannelColor00 " << red * 256 * 256 + green * 256 + blue << std::endl;
    red = 0;
    green = 255;
    blue = 0;
    file << "ChannelColor01 " << red * 256 * 256 + green * 256 + blue << std::endl;
    red = 0;
    green = 0;
    blue = 255;
    file << "ChannelColor02 " << red * 256 * 256 + green * 256 + blue << std::endl;
  }

  file << "ChannelDepth 8" << std::endl;
  file << "FileType PNG" << std::endl;
  file << "</ImageSessionData>" << std::endl;


  typename ReaderType::Pointer reader = ReaderType::New();
  reader->SetFileName( filename );
  reader->UseStreamingOn();

  typename WriterType::Pointer writer = WriterType::New();

  typename ImageType::RegionType desiredRegion;
  typename ImageType::IndexType start;
  typename ImageType::SizeType size;

  for( unsigned int j = 0; j < 5; j++ )
  {
    start[j] = 0;
    size[j] = io->GetDimensions(j);
  }

  // Write out the megacapture file here in the outputDir
  typename NameGeneratorType::Pointer nameGenerator1 = NameGeneratorType::New();
  nameGenerator1->SetSeriesFormat( "-PL00-CO00-RO00-ZT00-YT00-XT00-TM%04d" );
  nameGenerator1->SetStartIndex( 0 );
  nameGenerator1->SetEndIndex( m_NumberOfTimePoints-1 );
  nameGenerator1->SetIncrementIndex( 1 );

  typename NameGeneratorType::Pointer nameGenerator2 = NameGeneratorType::New();
  nameGenerator2->SetSeriesFormat( "-ch%02d" );
  nameGenerator2->SetStartIndex( 0 );
  nameGenerator2->SetEndIndex( m_NumberOfChannels-1 );
  nameGenerator2->SetIncrementIndex( 1 );

  // XYZTC 01234
  for( unsigned int ch = 0; ch < region.GetSize( 4 ); ch++ )
  {
    for( unsigned int time = 0; time < region.GetSize( 3 ); time++ )
    {
      std::cout << time << std::endl;
      itk::SCIFIOImageIO::Pointer imageIO = itk::SCIFIOImageIO::New();
      typename ExtractImageFilter::Pointer extractor = ExtractImageFilter::New();

      imageIO->DebugOn();
      imageIO->SetFileName(filename);
      imageIO->SetUseStreamedReading(true);
      imageIO->SetUseStreamedWriting(true);

      // reset to the largest possible region, so that the extraction region is contained
      imageIO->SetIORegion( region );
      imageIO->ReadImageInformation();

      reader->SetImageIO(imageIO);

      start[ 4 ] = ch;
      size[ 4 ] = 0;

      start[ 3 ] = time;
      size[ 3 ] = 0;

      desiredRegion.SetSize(size);
      desiredRegion.SetIndex(start);

      extractor->SetExtractionRegion(desiredRegion);
      extractor->SetDirectionCollapseToIdentity();
      extractor->SetInput(reader->GetOutput());
      extractor->Update();
      std::cout << "Region of extraction: " << desiredRegion << std::endl;

      typename FlipFilterType::Pointer flipFilter = FlipFilterType::New();
      flipFilter->SetFlipAxes( flipArray );
      flipFilter->SetInput( extractor->GetOutput() );
      flipFilter->Update();

      std::string m_SeriesFormat = megFilename.substr(0, megFilename.length()-4 ) + nameGenerator1->GetFileNames()[time] +
        nameGenerator2->GetFileNames()[ch] + "-zs%04d." + m_FileType;
      typename NameGeneratorType::Pointer nameGenerator3 = NameGeneratorType::New();
      nameGenerator3->SetSeriesFormat( m_SeriesFormat );
      nameGenerator3->SetStartIndex( 0 );
      nameGenerator3->SetEndIndex( m_NumberOfZSlices-1 );
      nameGenerator3->SetIncrementIndex( 1 );

      typename SeriesWriterType::Pointer series_writer = SeriesWriterType::New();
      series_writer->SetInput( flipFilter->GetOutput() );
      series_writer->SetFileNames( nameGenerator3->GetFileNames() );
      series_writer->Update();

      // TODO: Write out the megacapture file here in the outputDir
      char timeStr[100] = "";
      struct stat buf;

      if ( !stat(filename, &buf) )
        {
        strftime( timeStr, 100, "%Y-%m-%d %H:%M:%S", localtime(&buf.st_mtime) );
        }

      for ( int j = 0; j < m_NumberOfZSlices; j++ )
      {
        std::string filename = nameGenerator3->GetFileNames()[j];
        unsigned int found = filename.find_last_of("/\\");

        file << "<Image>" << std::endl;
        file << "Filename " << filename.substr(found+1) << std::endl;
        file << "DateTime " << timeStr << std::endl;
        file << "StageX 1000" << std::endl;
        file << "StageY -1000" << std::endl;
        file << "Pinhole 44.216" << std::endl;
        file << "</Image>" << std::endl;
      }

    }
  }
  file.close();
  return EXIT_SUCCESS;
}

template < class PixelType >
void ProcessDimension ( unsigned int numberOfDim, char* filename )
{
  if (numberOfDim == 3)
  {
    Process3DImage< PixelType >( filename );
  }
  else if( numberOfDim == 4 )
  {
    Process4DImage< PixelType >( filename );
  }
  else if( numberOfDim == 5 )
  {
    Process5DImage< PixelType >( filename );
  }
}

int main( int argc, char * argv [] )
{
  if( argc < 2)
    {
    std::cerr << "Usage: " << argv[0] << " inputLSM" << std::endl;
    return EXIT_FAILURE;
    }

  typedef unsigned char PixelType;

  // This ImageIO won't actually be used, it's just a reference to remember how many
  // T-slices there are, as the extracted ImageIO will be truncated.
  itk::SCIFIOImageIO::Pointer io = itk::SCIFIOImageIO::New();
  io->DebugOn();

  io->SetFileName(argv[1]);
  io->ReadImageInformation();

  unsigned int numberOfDim = io->GetNumberOfDimensions();// this is typically 5
  std::cout << "Number of dimensions: " << numberOfDim << std::endl;

  ProcessDimension< unsigned char >( numberOfDim, argv[1] );

  return EXIT_SUCCESS;
}

