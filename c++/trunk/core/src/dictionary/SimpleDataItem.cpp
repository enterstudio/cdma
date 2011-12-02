// ****************************************************************************
// Synchrotron SOLEIL
//
// Creation : 08/12/2011
// Author   : Rodriguez Clément
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; version 2 of the License.
//
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// ****************************************************************************
#include <cdma/dictionary/SimpleDataItem.h>

#define TEMP_EXCEPTION(a,b) throw cdma::Exception("TARTAMPION", a, b)

namespace cdma
{
//=============================================================================
//
// SimpleDataItem
//
//=============================================================================
//---------------------------------------------------------------------------
// c-tor
//---------------------------------------------------------------------------
SimpleDataItem::SimpleDataItem(IDataset* dataset, IArrayPtr ptrArray, const std::string &name):
m_dataset_ptr(dataset), m_name(name), m_array_ptr(ptrArray)
{
  CDMA_SCOPE_DBG("SimpleDataItem::SimpleDataItem");
}

//---------------------------------------------------------------------------
// SimpleDataItem::findAttributeIgnoreCase
//---------------------------------------------------------------------------
cdma::IAttributePtr SimpleDataItem::findAttributeIgnoreCase(const std::string& name)
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::findAttributeIgnoreCase");
}

//---------------------------------------------------------------------------
// SimpleDataItem::findDimensionIndex
//---------------------------------------------------------------------------
int SimpleDataItem::findDimensionIndex(const std::string& name)
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::findDimensionIndex");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getASlice
//---------------------------------------------------------------------------
cdma::IDataItemPtr SimpleDataItem::getASlice(int dimension, int value) throw ( cdma::Exception )
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::getASlice");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getParent
//---------------------------------------------------------------------------
cdma::IGroupPtr SimpleDataItem::getParent()
{
  return NULL;
}

//---------------------------------------------------------------------------
// SimpleDataItem::getRoot
//---------------------------------------------------------------------------
cdma::IGroupPtr SimpleDataItem::getRoot()
{
  return NULL;
}

//---------------------------------------------------------------------------
// SimpleDataItem::getData
//---------------------------------------------------------------------------
cdma::IArrayPtr SimpleDataItem::getData(std::vector<int> position) throw ( cdma::Exception )
{
  CDMA_SCOPE_DBG("SimpleDataItem::getData(vector<int> position)");
  int node_rank = m_array_ptr->getRank();
  int slab_rank = node_rank - position.size();
  
  std::vector<int> origin;
  std::vector<int> shape;

  for( int dim = 0; dim < node_rank; dim++ )
  {
    if( dim < position.size() )
    {
      origin.push_back( position[dim] );
      shape.push_back( m_array_ptr->getShape()[dim] - position[dim] );
    }
    else
    {
      origin.push_back( 0 );
      shape.push_back( m_array_ptr->getShape()[dim] );
    }
  }
  cdma::IArrayPtr array = getData(origin, shape);
  return array;
}

//---------------------------------------------------------------------------
// SimpleDataItem::getData
//---------------------------------------------------------------------------
cdma::IArrayPtr SimpleDataItem::getData(std::vector<int> origin, std::vector<int> shape) throw ( cdma::Exception )
{
  CDMA_SCOPE_DBG("SimpleDataItem::getData(vector<int> origin, vector<int> shape)");

  int rank = m_array_ptr->getRank();
  int* iShape = new int[rank];
  int* iStart = new int[rank];
  for( int i = 0; i < rank; i++ )
  {
    iStart[i] = origin[i];
    iShape[i]  = shape[i];
  }
  cdma::IIndexPtr index = new cdma::Index( "### POINTER_TO_THE_PLUGIN_FACTORY", rank, iShape, iStart );
  //## Should pass the shared pointer rather than a pointer to the referenced object
  cdma::IArrayPtr array = new cdma::Array( *(static_cast<Array*>(m_array_ptr.get())), index );
  return array;
}

//---------------------------------------------------------------------------
// SimpleDataItem::getDescription
//---------------------------------------------------------------------------
std::string SimpleDataItem::getDescription()
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::getDescription");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getDimensions
//---------------------------------------------------------------------------
std::list<cdma::IDimensionPtr> SimpleDataItem::getDimensions(int i)
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::getDimensions");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getDimensionList
//---------------------------------------------------------------------------
std::list<cdma::IDimensionPtr> SimpleDataItem::getDimensionList()
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::getDimensionList");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getDimensionsString
//---------------------------------------------------------------------------
std::string SimpleDataItem::getDimensionsString()
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::getDimensionsString");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getElementSize
//---------------------------------------------------------------------------
int SimpleDataItem::getElementSize()
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::getNameAndDimensions");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getNameAndDimensions
//---------------------------------------------------------------------------
std::string SimpleDataItem::getNameAndDimensions()
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::getNameAndDimensions");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getNameAndDimensions
//---------------------------------------------------------------------------
std::string SimpleDataItem::getNameAndDimensions(bool useFullName, bool showDimLength)
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::getNameAndDimensions");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getRangeList
//---------------------------------------------------------------------------
std::list<cdma::IRangePtr> SimpleDataItem::getRangeList()
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::getRangeList");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getRank
//---------------------------------------------------------------------------
int SimpleDataItem::getRank()
{
  return m_array_ptr->getRank();
}

//---------------------------------------------------------------------------
// SimpleDataItem::getSection
//---------------------------------------------------------------------------
cdma::IDataItemPtr SimpleDataItem::getSection(std::list<cdma::IRangePtr> section) throw ( cdma::Exception )
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::getSection");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getSectionRanges
//---------------------------------------------------------------------------
std::list<cdma::IRangePtr> SimpleDataItem::getSectionRanges()
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::getSectionRanges");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getShape
//---------------------------------------------------------------------------
std::vector<int> SimpleDataItem::getShape()
{
  return m_array_ptr->getShape();
}

//---------------------------------------------------------------------------
// SimpleDataItem::getSize
//---------------------------------------------------------------------------
long SimpleDataItem::getSize()
{
  return m_array_ptr->getSize();
}

//---------------------------------------------------------------------------
// SimpleDataItem::getSizeToCache
//---------------------------------------------------------------------------
int SimpleDataItem::getSizeToCache()
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::getSizeToCache");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getSlice
//---------------------------------------------------------------------------
cdma::IDataItemPtr SimpleDataItem::getSlice(int dim, int value) throw ( cdma::Exception )
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::getSlice");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getType
//---------------------------------------------------------------------------
const std::type_info& SimpleDataItem::getType()
{
  return m_array_ptr->getElementType();
}

//---------------------------------------------------------------------------
// SimpleDataItem::getUnitsString
//---------------------------------------------------------------------------
std::string SimpleDataItem::getUnitsString()
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::getUnitsString");
}

//---------------------------------------------------------------------------
// SimpleDataItem::hasCachedData
//---------------------------------------------------------------------------
bool SimpleDataItem::hasCachedData()
{
  return false;
}

//---------------------------------------------------------------------------
// SimpleDataItem::findAttributeIgnoreCase
//---------------------------------------------------------------------------
//##int SimpleDataItem::hashCode()

//---------------------------------------------------------------------------
// SimpleDataItem::invalidateCache
//---------------------------------------------------------------------------
void SimpleDataItem::invalidateCache()
{
  // Do nothing
}

//---------------------------------------------------------------------------
// SimpleDataItem::isCaching
//---------------------------------------------------------------------------
bool SimpleDataItem::isCaching()
{
  return false;
}

//---------------------------------------------------------------------------
// SimpleDataItem::isMemberOfStructure
//---------------------------------------------------------------------------
bool SimpleDataItem::isMemberOfStructure()
{
  return false;
}

//---------------------------------------------------------------------------
// SimpleDataItem::isMetadata
//---------------------------------------------------------------------------
bool SimpleDataItem::isMetadata()
{
  return false;
}

//---------------------------------------------------------------------------
// SimpleDataItem::isScalar
//---------------------------------------------------------------------------
bool SimpleDataItem::isScalar()
{
  return m_array_ptr->getRank() == 0;
}

//---------------------------------------------------------------------------
// SimpleDataItem::isUnlimited
//---------------------------------------------------------------------------
bool SimpleDataItem::isUnlimited()
{
  return false;
}

//---------------------------------------------------------------------------
// SimpleDataItem::isUnsigned
//---------------------------------------------------------------------------
bool SimpleDataItem::isUnsigned()
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::isUnsigned");
}

//---------------------------------------------------------------------------
// SimpleDataItem::readScalarByte
//---------------------------------------------------------------------------
unsigned char SimpleDataItem::readScalarByte() throw ( cdma::Exception )
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::readScalarByte");
}

//---------------------------------------------------------------------------
// SimpleDataItem::readScalarDouble
//---------------------------------------------------------------------------
double SimpleDataItem::readScalarDouble() throw ( cdma::Exception )
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::readScalarDouble");
}

//---------------------------------------------------------------------------
// SimpleDataItem::readScalarFloat
//---------------------------------------------------------------------------
float SimpleDataItem::readScalarFloat() throw ( cdma::Exception )
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::readScalarFloat");
}

//---------------------------------------------------------------------------
// SimpleDataItem::readScalarInt
//---------------------------------------------------------------------------
int SimpleDataItem::readScalarInt() throw ( cdma::Exception )
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::readScalarInt");
}

//---------------------------------------------------------------------------
// SimpleDataItem::readScalarLong
//---------------------------------------------------------------------------
long SimpleDataItem::readScalarLong() throw ( cdma::Exception )
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::readScalarLong");
}

//---------------------------------------------------------------------------
// SimpleDataItem::readScalarShort
//---------------------------------------------------------------------------
short SimpleDataItem::readScalarShort() throw ( cdma::Exception )
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::readScalarShort");
}

//---------------------------------------------------------------------------
// SimpleDataItem::readScalarString
//---------------------------------------------------------------------------
std::string SimpleDataItem::readScalarString() throw ( cdma::Exception )
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::readScalarString");
}

//---------------------------------------------------------------------------
// SimpleDataItem::removeAttribute
//---------------------------------------------------------------------------
bool SimpleDataItem::removeAttribute(const cdma::IAttributePtr& a)
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::removeAttribute");
}

//---------------------------------------------------------------------------
// SimpleDataItem::setCachedData
//---------------------------------------------------------------------------
//##void SimpleDataItem::setCachedData(IArray& cacheData, bool isMetadata) throw ( cdma::Exception )

//---------------------------------------------------------------------------
// SimpleDataItem::setCaching
//---------------------------------------------------------------------------
void SimpleDataItem::setCaching(bool caching)
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::setCaching");
}

//---------------------------------------------------------------------------
// SimpleDataItem::setDataType
//---------------------------------------------------------------------------
void SimpleDataItem::setDataType(const std::type_info& dataType)
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::setDataType");
}

//---------------------------------------------------------------------------
// SimpleDataItem::setDimensions
//---------------------------------------------------------------------------
void SimpleDataItem::setDimensions(const std::string& dimString)
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::setDimensions");
}

//---------------------------------------------------------------------------
// SimpleDataItem::setDimension
//---------------------------------------------------------------------------
void SimpleDataItem::setDimension(const cdma::IDimensionPtr& dim, int ind) throw ( cdma::Exception )
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::setDimension");
}

//---------------------------------------------------------------------------
// SimpleDataItem::setElementSize
//---------------------------------------------------------------------------
void SimpleDataItem::setElementSize(int elementSize)
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::setElementSize");
}

//---------------------------------------------------------------------------
// SimpleDataItem::setSizeToCache
//---------------------------------------------------------------------------
void SimpleDataItem::setSizeToCache(int sizeToCache)
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::setSizeToCache");
}

//---------------------------------------------------------------------------
// SimpleDataItem::setUnitsString
//---------------------------------------------------------------------------
void SimpleDataItem::setUnitsString(const std::string& units)
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::setUnitsString");
}

//---------------------------------------------------------------------------
// SimpleDataItem::clone
//---------------------------------------------------------------------------
IDataItemPtr SimpleDataItem::clone()
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::clone");
}

//---------------------------------------------------------------------------
// SimpleDataItem::addOneAttribute
//---------------------------------------------------------------------------
void SimpleDataItem::addOneAttribute(const cdma::IAttributePtr&)
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::addOneAttribute");
}

//---------------------------------------------------------------------------
// SimpleDataItem::addStringAttribute
//---------------------------------------------------------------------------
void SimpleDataItem::addStringAttribute(const std::string&, const std::string&)
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::addStringAttribute");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getAttribute
//---------------------------------------------------------------------------
IAttributePtr SimpleDataItem::getAttribute(const std::string&)
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::getAttribute");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getAttributeList
//---------------------------------------------------------------------------
std::list<yat::SharedPtr<cdma::IAttribute, yat::Mutex> > SimpleDataItem::getAttributeList()
{
  return m_attr_list;
}

//---------------------------------------------------------------------------
// SimpleDataItem::getLocation
//---------------------------------------------------------------------------
std::string SimpleDataItem::getLocation()
{
  return m_name;
}

//---------------------------------------------------------------------------
// SimpleDataItem::getName
//---------------------------------------------------------------------------
std::string SimpleDataItem::getName()
{
  return m_name;
}

//---------------------------------------------------------------------------
// SimpleDataItem::getShortName
//---------------------------------------------------------------------------
std::string SimpleDataItem::getShortName()
{
  return m_name;
}

//---------------------------------------------------------------------------
// SimpleDataItem::hasAttribute
//---------------------------------------------------------------------------
bool SimpleDataItem::hasAttribute(const std::string&, const std::string&)
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::hasAttribute");
}

//---------------------------------------------------------------------------
// SimpleDataItem::setName
//---------------------------------------------------------------------------
void SimpleDataItem::setName(const std::string& name)
{
  m_name = name;
}

//---------------------------------------------------------------------------
// SimpleDataItem::setShortName
//---------------------------------------------------------------------------
void SimpleDataItem::setShortName(const std::string& /*name*/)
{
}

//---------------------------------------------------------------------------
// SimpleDataItem::setParent
//---------------------------------------------------------------------------
void SimpleDataItem::setParent(const cdma::IGroupPtr&)
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::setParent");
}

//---------------------------------------------------------------------------
// SimpleDataItem::getDataset
//---------------------------------------------------------------------------
cdma::IDatasetPtr SimpleDataItem::getDataset()
{
  THROW_NOT_IMPLEMENTED("SimpleDataItem::getDataset");
}

} // namespace