// ******************************************************************************
// Copyright (c) 2011 Synchrotron Soleil.
// The CDMA library is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version.
// Contributors :
// See AUTHORS file
// ******************************************************************************

#include <cdma/array/SliceIterator.h>
#include <cdma/array/View.h>
#include <cdma/array/ArrayIterator.h>
#include <cdma/array/Array.h>

namespace cdma
{
//---------------------------------------------------------------------------
// SliceIterator::SliceIterator
//---------------------------------------------------------------------------
SliceIterator::SliceIterator(const SliceIterator& iterator)
{
  CDMA_FUNCTION_TRACE("SliceIterator::SliceIterator");
  m_dimension = iterator.m_dimension;
  m_iterator  = iterator.m_iterator;
  m_array     = iterator.m_array;
  m_slice     = m_slice;
}

SliceIterator::SliceIterator(const ArrayPtr& array, int dim)
{
  CDMA_FUNCTION_TRACE("SliceIterator::SliceIterator");
  
  // If ranks are equal, make sure at least one iteration is performed.
  // We cannot use 'reshape' (which would be ideal) as that creates a
  // new copy of the array storage and so is unusable
  m_array = array;
  m_slice = array;
  ViewPtr index = new View(m_array->getView());
  int rank = index->getRank();
  std::vector<int> position( rank );
  std::vector<int> shape  = index->getShape();
  std::vector<int> origin = index->getOrigin();
  std::vector<int> stride = index->getStride();


  // shape to make a multiple-dim array
  m_dimension = dim;
  for( int i = 0; i < dim; i++ ) {
      shape[rank - i - 1] = 1;
  }

  // Create an iterator over the higher dimensions. We leave in the
  // final dimensions so that we can use the getPosition method
  // to create an origin.

  // As we get a reference on array's View we directly modify it
  index->setOrigin(origin);
  index->setStride(stride);
  index->setShape(shape);

  m_iterator = new ArrayIterator(m_array, index, position);
	
}

//---------------------------------------------------------------------------
// SliceIterator::~SliceIterator
//---------------------------------------------------------------------------
SliceIterator::~SliceIterator()
{
  CDMA_FUNCTION_TRACE("SliceIterator::~SliceIterator");
}

//---------------------------------------------------------------------------
// SliceIterator::next
//---------------------------------------------------------------------------
void SliceIterator::next()
{
  ++(*m_iterator);
}
/*
//---------------------------------------------------------------------------
// SliceIterator::getArrayNext
//---------------------------------------------------------------------------
ArrayPtr SliceIterator::getArrayNext() throw ( cdma::Exception )
{
  ++(*m_iterator);
  return get();
}

//---------------------------------------------------------------------------
// SliceIterator::getArrayCurrent
//---------------------------------------------------------------------------
ArrayPtr SliceIterator::getArrayCurrent() throw ( cdma::Exception )
{
  return get();
}
*/
//---------------------------------------------------------------------------
// SliceIterator::getSliceShape
//---------------------------------------------------------------------------
std::vector<int> SliceIterator::getSliceShape() throw ( cdma::Exception )
{
  // Get the iterator position
  std::vector<int> shape = m_array->getShape();
  std::vector<int> result( m_dimension );
  int firsDim = m_array->getRank() - m_dimension;
  
  // Remove all dimension corresponding to the slice's shape
  for( int i = 0; i < m_dimension; i++ )
  {
    result[i] = shape[firsDim - i];
  }
  return result;
}

//---------------------------------------------------------------------------
// SliceIterator::getPosition
//---------------------------------------------------------------------------
std::vector<int> SliceIterator::getPosition()
{
  // Get the iterator position
  std::vector<int> position = m_iterator->getPosition();
  std::vector<int> result( m_array->getRank() - m_dimension );
  
  // Remove all dimension corresponding to the slice's shape
  for( int i = 0; i < (int)(m_array->getRank()) - m_dimension; i++ )
  {
    result[i] = position[i];
  }
  return result;
}

//---------------------------------------------------------------------------
// SliceIterator::operator++
//---------------------------------------------------------------------------
SliceIterator& SliceIterator::operator++(void)
{
  ++(*m_iterator);
  return *this;
}

//---------------------------------------------------------------------------
// SliceIterator::operator++(int)
//---------------------------------------------------------------------------
SliceIterator SliceIterator::operator++(int)
{
  SliceIterator iterator (*this);
  operator++();
  return iterator;
}

//---------------------------------------------------------------------------
// SliceIterator::operator*
//---------------------------------------------------------------------------
ArrayPtr& SliceIterator::operator*(void)
{
  CDMA_FUNCTION_TRACE("SliceIterator::operator*");
  this->get();
  return m_slice;
}

//---------------------------------------------------------------------------
// SliceIterator::operator==
//---------------------------------------------------------------------------
bool SliceIterator::operator==(const SliceIteratorPtr& iter)
{
  std::vector<int> source = this->getPosition();
  std::vector<int> destin = iter->getPosition();
  bool result = ( ( *(*iter) )->getStorage() == (*(*this))->getStorage() );
 
  if( result )
  {
    for( unsigned int i = 0; i < source.size(); i++ )
    {
      if( result )
      {
        result = ( source[i] == destin[i] );
      }
      else
      {
        break;
      }
    }
  }
  
  return result;
}

//---------------------------------------------------------------------------
// SliceIterator::operator!=
//---------------------------------------------------------------------------
bool SliceIterator::operator!=(const SliceIteratorPtr& iter)
{
  return !( (*this) == iter );
}

//---------------------------------------------------------------------------
// SliceIterator::get
//---------------------------------------------------------------------------
void SliceIterator::get()
{
  CDMA_FUNCTION_TRACE("SliceIterator::get");

  // Get array current informations
  ViewPtr index = new View( m_array->getView() );
  std::vector<int> current = m_iterator->getPosition();
  std::vector<int> shape   = index->getShape();
  int rank = m_array->getRank();

  // Reshape useless dimensions
  for( int i = 0; i < rank - m_dimension; i++ )
  {
    shape[i] = 1;
  }
  // Defines the viewable part of the array
  index->setShape(shape);
  index->setOrigin(current);
  index->reduce();

  // Create a new array with the right view
  m_slice = new Array( m_array, index );
}

}
