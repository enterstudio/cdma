// *****************************************************************************
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
// *****************************************************************************

#include <typeinfo>

#include <cdma/Common.h>
#include <cdma/exception/Exception.h>
#include <cdma/array/Array.h>
#include <cdma/math/ArrayMath.h>
#include <cdma/utils/ArrayUtils.h>
#include <cdma/array/ArrayIterator.h>
#include <cdma/array/SliceIterator.h>
#include <cdma/array/Slicer.h>

namespace cdma
{
//---------------------------------------------------------------------------
// Array::~Array
//---------------------------------------------------------------------------
Array::~Array()
{
  CDMA_FUNCTION_TRACE("Array::~Array");
}

//---------------------------------------------------------------------------
// Array::Array
//---------------------------------------------------------------------------
Array::Array( const Array& array ) : m_view_ptr ( array.m_view_ptr )
{
  CDMA_FUNCTION_TRACE("Array::Array( const Array& array )");
  m_data_impl = array.m_data_impl;
  m_shape = array.m_view_ptr->getShape();
}

//---------------------------------------------------------------------------
// Array::Array
//---------------------------------------------------------------------------
Array::Array( const Array& array, const ViewPtr& view ) : m_view_ptr (view)
{
  CDMA_FUNCTION_TRACE("Array::Array( const Array& array, const ViewPtr& view )");
  m_data_impl = array.m_data_impl;
  m_shape = view->getShape();
}

//---------------------------------------------------------------------------
// Array::Array
//---------------------------------------------------------------------------
Array::Array( const ArrayPtr& array, const ViewPtr& view ) : m_view_ptr (view)
{
  CDMA_FUNCTION_TRACE("Array::Array( const ArrayPtr& array, const ViewPtr& view )");
  m_data_impl = array->getStorage();
  m_shape = view->getShape();
}

//---------------------------------------------------------------------------
// Array::Array
//---------------------------------------------------------------------------
Array::Array( const IArrayStoragePtr& data_ptr, const ViewPtr& view ) : m_view_ptr (view)
{
  CDMA_FUNCTION_TRACE("Array::Array( const std::string& factory, const IArrayStoragePtr& data_ptr, const ViewPtr& view )");
  m_data_impl = data_ptr;
  m_shape = view->getShape();
}

//---------------------------------------------------------------------------
// Array::Array
//---------------------------------------------------------------------------
Array::Array( const std::type_info& type, std::vector<int> shape, void* pData )
{
  CDMA_FUNCTION_TRACE("Array::Array");
  int rank = shape.size();
  unsigned int size = 1;
  if( pData == NULL )
  {
    for( int i = 0; i < rank; i++ )
    {
      size *= shape[i];
    }
  }

  if( type == typeid( short ) )
  {
    if( pData == NULL )
    {
      pData = new short[size];
    }
    m_data_impl = new DefaultArrayStorage<short>((short*) pData, shape);
  }
  else if( type == typeid( unsigned short ) )
  {
    if( pData == NULL )
    {
      pData = new unsigned short[size];
    }
    m_data_impl = new DefaultArrayStorage<unsigned short>((unsigned short*) pData, shape);
  }
  else if( type == typeid( long ) )
  {
    if( pData == NULL )
    {
      pData = new long[size];
    }
    m_data_impl = new DefaultArrayStorage<long>((long*)pData, shape);
  }
  else if( type == typeid( unsigned long ) )
  {
    if( pData == NULL )
    {
      pData = new unsigned long[size];
    }
    m_data_impl = new DefaultArrayStorage<unsigned long>((unsigned long*)pData, shape);
  }
  else if( type == typeid( float ) )
  {
    if( pData == NULL )
    {
      pData = new float[size];
    }
    m_data_impl = new DefaultArrayStorage<float>((float*)pData, shape);
  }
  else if( type == typeid( yat::uint64 ) )
  {
    if( pData == NULL )
    {
      pData = new yat::uint64[size];
    }
    m_data_impl = new DefaultArrayStorage<yat::uint64>((yat::uint64*) pData, shape);
  }
  else if( type == typeid( yat::int64 ) )
  {
    if( pData == NULL )
    {
      pData = new yat::int64[size];
    }
    m_data_impl = new DefaultArrayStorage<yat::int64>((yat::int64*) pData, shape);
  }
  else if( type == typeid( double ) )
  {
    if( pData == NULL )
    {
      pData = new double[size];
    }
    m_data_impl = new DefaultArrayStorage<double>((double*) pData, shape);
  }
  else
  {
    if( pData == NULL )
    {
      pData = new char[size];
    }
    m_data_impl = new DefaultArrayStorage<char>((char*) pData, shape);
  }
  
  m_shape = shape;
  int *shape_ptr = new int[rank];
  int *start_ptr = new int[rank];
  for( yat::uint16 i = 0; i < shape.size(); i++ )
  {
    shape_ptr[i] = shape[i];
    start_ptr[i] = 0;
  }
  m_view_ptr = new View( rank, shape_ptr, start_ptr );
}

//---------------------------------------------------------------------------
// Array::deepCopy
//---------------------------------------------------------------------------
ArrayPtr Array::deepCopy()
{
  // Copy memory storage
  std::vector<int> origin( m_view_ptr->getRank() );
  std::vector<int> shape  = m_view_ptr->getShape();
  std::vector<int> stride = m_view_ptr->getStride();

  int newStride = 1;
  for( int i = m_view_ptr->getRank() - 1; i >= 0; i-- )
  {
    stride[i] = newStride;
    newStride *= shape[i];
  }
  
  return new Array( m_data_impl->deepCopy(m_view_ptr), new View( shape, origin, stride ) );
}

//---------------------------------------------------------------------------
// Array::getValueType
//---------------------------------------------------------------------------
const std::type_info& Array::getValueType()
{
  return m_data_impl->getType();
}

//---------------------------------------------------------------------------
// Array::getView
//---------------------------------------------------------------------------
ViewPtr Array::getView()
{
  CDMA_FUNCTION_TRACE("Array::getView");
  return cdma::ViewPtr(m_view_ptr);
}

//---------------------------------------------------------------------------
// Array::begin
//---------------------------------------------------------------------------
ArrayIterator Array::begin()
{
  CDMA_FUNCTION_TRACE("Array::begin");
  ViewPtr view = new View(m_view_ptr);
  std::vector<int> position;
  for( int i = 0; i < view->getRank(); i++ )
  {
    position.push_back(0);
  }

  // We use "*this" to copy the array so the SharePtr on memory storage is incremented
  // that permits to not destroy memory storage when the iterator is released
  // There is no memory copy doing so only references are shared
  ArrayIterator iterator ( new Array(*this), view, position );

  return iterator;
}

//---------------------------------------------------------------------------
// Array::end
//---------------------------------------------------------------------------
ArrayIterator Array::end()
{
  CDMA_FUNCTION_TRACE("Array::end");
  // Copy view
  ViewPtr view = new View(m_view_ptr);
  
  // Construct a vector of position having 0 in each low dimension
  // the first is having shape[0]. Means positioned at the cell that just
  // follows the last one.
  std::vector<int> position = view->getShape();
  for( int i = 1; i < view->getRank(); i++ )
  {
    position[i] = 0;
  }

  // We use "*this" to copy the array so the SharePtr on memory storage is incremented
  // that permits to not destroy memory storage when the iterator is released
  // There is no memory copy doing so only references are shared
  ArrayIterator iterator ( new Array(*this), view, position );

  return iterator;
}

//---------------------------------------------------------------------------
// Array::getRank
//---------------------------------------------------------------------------
int Array::getRank()
{
  return m_view_ptr->getRank();
}

//---------------------------------------------------------------------------
// Array::getRegion
//---------------------------------------------------------------------------
ArrayPtr Array::getRegion(std::vector<int> start, std::vector<int> shape) throw ( cdma::Exception )
{
  CDMA_FUNCTION_TRACE("Array::getRegion");
  ViewPtr view = new View(m_view_ptr);
  view->setOrigin(start);
  view->setShape(shape);
  return new Array(*this, view);
}

//---------------------------------------------------------------------------
// Array::getShape
//---------------------------------------------------------------------------
std::vector<int> Array::getShape()
{
  return m_view_ptr->getShape();
}

//---------------------------------------------------------------------------
// Array::getSize
//---------------------------------------------------------------------------
long Array::getSize()
{
  return m_view_ptr->getSize();
}

//---------------------------------------------------------------------------
// Array::setView
//---------------------------------------------------------------------------
void Array::setView(const cdma::ViewPtr& view)
{
  m_view_ptr = view;
}

//---------------------------------------------------------------------------
// Array::getSlicer
//---------------------------------------------------------------------------
cdma::SlicerPtr Array::getSlicer(int rank) throw ( cdma::Exception )
{
  return new Slicer(new Array(*this, m_view_ptr), rank);
}

//---------------------------------------------------------------------------
// Array::isDirty
//---------------------------------------------------------------------------
bool Array::dirty()
{
  return m_data_impl->dirty();
}

}

